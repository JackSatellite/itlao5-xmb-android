package com.e7yoo.e7.community;

import android.content.Intent;
import android.view.View;

import com.e7yoo.e7.R;
import com.e7yoo.e7.adapter.RecyclerAdapter;
import com.e7yoo.e7.util.ActivityUtil;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.listeners.Listeners;
import com.umeng.comm.core.nets.responses.FeedsResponse;

import java.util.List;

public abstract class FeedListFragment extends ListFragment {

    @Override
    protected ListRefreshRecyclerAdapter initAdapter() {
        return new FeedItemRefreshRecyclerAdapter(getActivity());
    }

    protected void addListener() {
        mRvAdapter.setOnItemClickListener(new RecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(mRvAdapter.getItem(position) != null && mRvAdapter.getItem(position) instanceof FeedItem) {
                    Intent intent = new Intent(getActivity(), FeedDetailActivity.class);
                    intent.putExtra("FeedItem", (FeedItem) mRvAdapter.getItem(position));
                    ActivityUtil.toActivity(getActivity(), intent);
                }
            }
        });
    }

    protected abstract void saveDataToDb(List<FeedItem> feedItems);

    protected void refreshData(List<FeedItem> feedItems) {
        if(mDatas == null) {
            mDatas = feedItems;
            mRvAdapter.refreshData(mDatas);
        }
    }

    protected Listeners.FetchListener mRefreshFetchListener = new Listeners.FetchListener<FeedsResponse>() {
        @Override
        public void onStart() {
        }
        @Override
        public void onComplete(FeedsResponse feedsResponse) {
            mSRLayout.setRefreshing(false);
            mRvAdapter.setFooter(FeedItemRefreshRecyclerAdapter.FooterType.END, R.string.loading_up_load_more, false);
            if(feedsResponse != null && feedsResponse.result != null) {
                mNextPageUrl = feedsResponse.nextPageUrl;
                mDatas = feedsResponse.result;
                mRvAdapter.refreshData(mDatas);
                saveDataToDb(mDatas);
            }
        }
    };

    protected Listeners.FetchListener mFetchListener = new Listeners.FetchListener<FeedsResponse>() {
        @Override
        public void onStart() {
        }
        @Override
        public void onComplete(FeedsResponse feedsResponse) {
            mSRLayout.setRefreshing(false);
            if(feedsResponse != null && feedsResponse.result != null && feedsResponse.result.size() != 0) {
                mNextPageUrl = feedsResponse.nextPageUrl;
                mDatas = feedsResponse.result;
                mRvAdapter.addItemBottom(mDatas);
                mRvAdapter.setFooter(FeedItemRefreshRecyclerAdapter.FooterType.END, R.string.loading_up_load_more, false);
            } else {
                mRvAdapter.setFooter(FeedItemRefreshRecyclerAdapter.FooterType.NO_MORE, R.string.loading_no_more, false);
            }
        }
    };

}