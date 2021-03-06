package com.e7yoo.e7;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.e7yoo.e7.adapter.PushMsgRefreshRecyclerAdapter;
import com.e7yoo.e7.adapter.RecyclerAdapter;
import com.e7yoo.e7.app.news.NewsWebviewActivity;
import com.e7yoo.e7.model.PushMsg;
import com.e7yoo.e7.sql.DbThreadPool;
import com.e7yoo.e7.sql.MessageDbHelper;
import com.e7yoo.e7.util.ActivityUtil;

import java.util.ArrayList;

public class PushMsgActivity extends BaseActivity {
    private RecyclerView mRecyclerView;
    private PushMsgRefreshRecyclerAdapter mRvAdapter;
    private ArrayList<PushMsg> mPushMsgs;
    public final static int PAGE_NUM = 15;

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
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mPushMsgs = MessageDbHelper.getInstance(this).getPushMsgs(0, PAGE_NUM);
        mRvAdapter = new PushMsgRefreshRecyclerAdapter(this);
        mRvAdapter.setOnItemClickListener(mOnItemClickListener);
        mRvAdapter.setOnItemLongClickListener(mOnItemLongClickListener);
        mRvAdapter.refreshData(mPushMsgs);
        mRecyclerView.setAdapter(mRvAdapter);
        if(mPushMsgs.size() <= 0) {
            mRvAdapter.setFooter(PushMsgRefreshRecyclerAdapter.FooterType.HINT, R.string.loading_no_push_msg, false);
        } else if(mPushMsgs.size() < PAGE_NUM) {
            mRvAdapter.setFooter(PushMsgRefreshRecyclerAdapter.FooterType.HINT, R.string.loading_no_more, false);
        } else {
            mRvAdapter.setFooter(PushMsgRefreshRecyclerAdapter.FooterType.HINT, R.string.loading_up_load_more, false);
        }
    }

    @Override
    protected void initViewListener() {
        initLoadMoreListener();
    }

    RecyclerAdapter.OnItemClickListener mOnItemClickListener = new RecyclerAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            final PushMsg msg = mRvAdapter.getItem(position);
            if(msg == null) {
                return;
            }
            switch (msg.getAction()) {
                case 0:
                    ActivityUtil.toPushMsgDetailsActivity(PushMsgActivity.this, msg);
                    break;
                case 1:
                    ActivityUtil.toNewsWebviewActivity(PushMsgActivity.this, msg.getUrl(), NewsWebviewActivity.INTENT_FROM_PUSH_MSG);
                    break;
            }
            mRvAdapter.setRead(position);
            DbThreadPool.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    MessageDbHelper.getInstance(E7App.mApp).updatePushMSg(msg.get_id(), 0);
                }
            });
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
                            if(mRvAdapter != null) {
                                mPushMsgs = MessageDbHelper.getInstance(PushMsgActivity.this).getPushMsgs(mRvAdapter.getLastId(), PAGE_NUM);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(!isFinishing() && mRvAdapter != null) {
                                            mRvAdapter.addItemBottom(mPushMsgs);
                                            if(mPushMsgs.size() < PAGE_NUM) {
                                                mRvAdapter.setFooter(PushMsgRefreshRecyclerAdapter.FooterType.NO_MORE, R.string.loading_no_more, false);
                                            } else {
                                                mRvAdapter.setFooter(PushMsgRefreshRecyclerAdapter.FooterType.HINT, R.string.loading_up_load_more, false);
                                            }
                                        }
                                    }
                                });
                            }
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
                        && mRvAdapter.getFooter() != PushMsgRefreshRecyclerAdapter.FooterType.NO_MORE && mPushMsgs.size() >= PAGE_NUM;
            }
        });

    }
}
