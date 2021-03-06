package com.e7yoo.e7.fragment;

import android.os.Message;

import com.e7yoo.e7.E7App;
import com.e7yoo.e7.R;
import com.e7yoo.e7.adapter.JokeListRefreshRecyclerAdapter;
import com.e7yoo.e7.adapter.ListRefreshRecyclerAdapter;
import com.e7yoo.e7.model.Joke;
import com.e7yoo.e7.sql.MessageDbHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andy on 2018/4/6.
 */

public class CollectListFragment extends ListFragment {

    public static CollectListFragment newInstance() {
        CollectListFragment fragment = new CollectListFragment();
        return fragment;
    }

    @Override
    public void onEventMainThread(Message msg) {
    }

    protected void refreshData(List<Joke> jokes, boolean refresh) {
        if(mDatas == null || refresh) {
            mDatas = jokes;
            if(mRvAdapter != null) {
                mRvAdapter.refreshData(mDatas);
            }
        }
    }

    @Override
    protected ListRefreshRecyclerAdapter initAdapter() {
        JokeListRefreshRecyclerAdapter jokeListRefreshRecyclerAdapter = new JokeListRefreshRecyclerAdapter(getContext());
        jokeListRefreshRecyclerAdapter.setShowCollect(false);
        return jokeListRefreshRecyclerAdapter;
    }

    @Override
    protected void addListener() {

    }

    boolean isRefresh;
    @Override
    protected void loadDataFromNet(boolean isRefresh) {

    }

    @Override
    protected void loadData(boolean isRefresh) {
        boolean noMore;
        if(isRefresh || mRvAdapter == null) {
            ArrayList<Joke> jokes = MessageDbHelper.getInstance(E7App.mApp).getCollect(-1, 0);
            refreshData(jokes, true);
            noMore = false;
        } else {
            Joke joke = ((JokeListRefreshRecyclerAdapter) mRvAdapter).getLastJoke();
            int lastId = -1;
            if(joke != null) {
                lastId = joke.get_id();
            }
            if(lastId == 0) {
                noMore = true;
            } else {
                ArrayList<Joke> jokes = MessageDbHelper.getInstance(E7App.mApp).getCollect(lastId, 0);
                mRvAdapter.addItemBottom(jokes);
                if (jokes != null && jokes.size() > 0) {
                    noMore = false;
                } else {
                    noMore = true;
                }
            }
        }
        if(mSRLayout != null) {
            mSRLayout.setRefreshing(false);
        }
        if(mRvAdapter != null) {
            if(noMore) {
                mRvAdapter.setFooter(ListRefreshRecyclerAdapter.FooterType.NO_MORE, R.string.loading_no_more, false);
            } else {
                mRvAdapter.setFooter(ListRefreshRecyclerAdapter.FooterType.END, R.string.loading_up_load_more, false);
            }
        }
    }

    @Override
    protected void loadDataFromDb() {
        if(mDatas == null) {
            ArrayList<Joke> jokes = MessageDbHelper.getInstance(E7App.mApp).getCollect(-1, 0);
            refreshData(jokes, false);
        }
    }
}
