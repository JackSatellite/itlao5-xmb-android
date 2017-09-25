package com.e7yoo.e7;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;

import com.e7yoo.e7.adapter.MsgRefreshRecyclerAdapter;
import com.e7yoo.e7.adapter.PushMsgRefreshRecyclerAdapter;
import com.e7yoo.e7.adapter.RecyclerAdapter;
import com.e7yoo.e7.app.news.NewsWebviewActivity;
import com.e7yoo.e7.model.PushMsg;
import com.e7yoo.e7.net.NetHelper;
import com.e7yoo.e7.sql.DbThreadPool;
import com.e7yoo.e7.sql.MessageDbHelper;
import com.e7yoo.e7.util.ActivityUtil;
import com.e7yoo.e7.util.RandomUtil;

import java.util.ArrayList;

public class PushMsgActivity extends BaseActivity implements OnClickListener {
    private RecyclerView mRecyclerView;
    private PushMsgRefreshRecyclerAdapter mRvAdapter;
    private ArrayList<PushMsg> mPushMsgs;

    @Override
    protected String initTitle() {
        return getString(R.string.mine_msg);
    }

    @Override
    protected int initLayoutResId() {
        return R.layout.activity_push_msg;
    }

    @Override
    protected void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.push_msg_rv);
    }

    @Override
    protected void initSettings() {
        mPushMsgs = MessageDbHelper.getInstance(this).getPushMsgs(0, 15);
        mRvAdapter = new PushMsgRefreshRecyclerAdapter(this);
        // mRvAdapter.refreshData();
        mRvAdapter.setOnItemClickListener(mOnItemClickListener);
        mRvAdapter.setOnItemLongClickListener(mOnItemLongClickListener);
        mRvAdapter.refreshData(mPushMsgs);
        mRecyclerView.setAdapter(mRvAdapter);
    }

    @Override
    protected void initViewListener() {
        initLoadMoreListener();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.actionbar_back:
                break;
        }
    }

    RecyclerAdapter.OnItemClickListener mOnItemClickListener = new RecyclerAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            PushMsg msg = mRvAdapter.getItem(position);
            if(msg != null) {
                switch (msg.getAction()) {
                    case 0:
                        break;
                    case 1:
                        Intent
                        ActivityUtil.toActivity(PushMsgActivity.this, NewsWebviewActivity.class);
                        break;
                }
            }
        }
    };

    RecyclerAdapter.OnItemLongClickListener mOnItemLongClickListener = new RecyclerAdapter.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(View view, int position) {
            return false;
        }
    };


    private void initLoadMoreListener() {
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastVisibleItem;
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //判断RecyclerView的状态 是空闲时，同时，是最后一个可见的ITEM时才加载
                if (isNeedLoadMore(newState)) {
                    mRvAdapter.setFooter(PushMsgRefreshRecyclerAdapter.FooterType.LOADING, R.string.loading, true);
                    DbThreadPool.getInstance().execute(new Runnable() {
                        @Override
                        public void run() {
                            mPushMsgs = MessageDbHelper.getInstance(PushMsgActivity.this).getPushMsgs(0, 15);
                        }
                    });
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                //最后一个可见的ITEM
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
            }

            private boolean isNeedLoadMore(int newState) {
                return !isFinishing() && newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == mRvAdapter.getItemCount()
                        && mRvAdapter.getFooter() != PushMsgRefreshRecyclerAdapter.FooterType.LOADING
                        && mRvAdapter.getFooter() != PushMsgRefreshRecyclerAdapter.FooterType.NO_MORE;
            }
        });

    }
}
