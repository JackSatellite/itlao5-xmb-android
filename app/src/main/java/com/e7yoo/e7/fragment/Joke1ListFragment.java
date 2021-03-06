package com.e7yoo.e7.fragment;

import android.os.Message;
import android.view.View;

import com.e7yoo.e7.E7App;
import com.e7yoo.e7.R;
import com.e7yoo.e7.adapter.JokeListRefreshRecyclerAdapter;
import com.e7yoo.e7.adapter.ListRefreshRecyclerAdapter;
import com.e7yoo.e7.model.Joke;
import com.e7yoo.e7.model.JokeType;
import com.e7yoo.e7.net.NetHelper;
import com.e7yoo.e7.sql.DbThreadPool;
import com.e7yoo.e7.util.Constant;
import com.e7yoo.e7.util.IOUtils;
import com.e7yoo.e7.util.JokeUtil;
import com.e7yoo.e7.util.PreferenceUtil;
import com.e7yoo.e7.util.TastyToastUtil;
import com.e7yoo.e7.util.UmengUtil;
import com.tencent.bugly.crashreport.CrashReport;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andy on 2018/4/6.
 */

public class Joke1ListFragment extends ListFragment {

    public static Joke1ListFragment newInstance() {
        Joke1ListFragment fragment = new Joke1ListFragment();
        return fragment;
    }

    private JokeType jokeType = JokeType.JOKE;

    public Joke1ListFragment setJokeType(JokeType jokeType) {
        this.jokeType = jokeType;
        return this;
    }

    @Override
    public void onEventMainThread(Message msg) {
        if(isDetached()) {
            return;
        }
        switch (msg.what) {
            case Constant.EVENT_BUS_NET_jokeRand:
                if(JokeType.JOKE == jokeType) {
                    doMsg(msg);
                    if(isRefresh) {
                        UmengUtil.onEvent(UmengUtil.JOKE_LIST_JOKE_REFRESH);
                    } else {
                        UmengUtil.onEvent(UmengUtil.JOKE_LIST_JOKE_MORE);
                    }
                }
                break;
            case Constant.EVENT_BUS_NET_jokeRand_pic:
                if(JokeType.PIC == jokeType) {
                    doMsg(msg);
                    if(isRefresh) {
                        UmengUtil.onEvent(UmengUtil.JOKE_LIST_PIC_REFRESH);
                    } else {
                        UmengUtil.onEvent(UmengUtil.JOKE_LIST_PIC_MORE);
                    }
                }
                break;
        }
    }

    private void doMsg(Message msg) {
        if(mSRLayout == null) {
            return;
        }
        mSRLayout.setRefreshing(false);
        ArrayList<Joke> joke = JokeUtil.parseJokeRand((JSONObject) msg.obj);
        if(isRefresh) {
            if(joke != null && joke.size() > 0) {
                saveDataToDb(joke);
                refreshData(joke, true);
            }
            mRvAdapter.setFooter(ListRefreshRecyclerAdapter.FooterType.END, R.string.loading_up_load_more, false);
        } else {
            mRvAdapter.addItemBottom(joke);
            if(joke != null && joke.size() > 0) {
                mRvAdapter.setFooter(ListRefreshRecyclerAdapter.FooterType.END, R.string.loading_up_load_more, false);
            } else {
                mRvAdapter.setFooter(ListRefreshRecyclerAdapter.FooterType.NO_MORE, R.string.loading_no_more, false);
            }
        }
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
        jokeListRefreshRecyclerAdapter.setShowCollect(true);
        return jokeListRefreshRecyclerAdapter;
    }

    @Override
    protected void addListener() {
        ((JokeListRefreshRecyclerAdapter) mRvAdapter).setOnCollectListener(new JokeListRefreshRecyclerAdapter.OnCollectListener() {
            @Override
            public void onCollect(View view, Joke joke, int position) {
                DbThreadPool.getInstance().insertCollect(E7App.mApp, joke);
                if(mRvAdapter != null) {
                    ((JokeListRefreshRecyclerAdapter) mRvAdapter).remove(position);
                }
                TastyToastUtil.toast(getActivity(), R.string.collect_suc);
            }
        });
    }

    boolean isRefresh;
    @Override
    protected void loadDataFromNet(boolean isRefresh) {
        this.isRefresh = isRefresh;
        if(jokeType == null) {
            jokeType = JokeType.JOKE;
        }
        switch (jokeType) {
            case PIC:
                NetHelper.newInstance().jokeRand(true);
                break;
            case JOKE:
                NetHelper.newInstance().jokeRand(false);
                break;
            case ALL:
            default:
                break;
        }
    }

    @Override
    protected void loadDataFromDb() {
        String jokeList = PreferenceUtil.getString(getKey(jokeType), null);
        try {
            if(jokeList == null) {
                return;
            }
            Object obj = IOUtils.UnserializeStringToObject(jokeList);
            if(obj != null) {
                ArrayList<Joke> jokes = (ArrayList<Joke>) obj;
                refreshData(jokes, false);
            }
        } catch (Throwable e) {
            CrashReport.postCatchedException(e);
        }
    }

    private void saveDataToDb(ArrayList<Joke> jokes) {
        PreferenceUtil.commitString(getKey(jokeType), IOUtils.SerializeObjectToString(jokes));
    }

    private String getKey(JokeType jokeType) {
        String preferenceKey;
        if(jokeType == null) {
            jokeType = JokeType.JOKE;
        }
        switch (jokeType) {
            case JOKE:
                preferenceKey = Constant.PREFERENCE_CIRCLE_JOKE_JOKE;
                break;
            case PIC:
                preferenceKey = Constant.PREFERENCE_CIRCLE_JOKE_PIC;
                break;
            case ALL:
            default:
                preferenceKey = Constant.PREFERENCE_CIRCLE_JOKE_ALL;
                break;
        }
        return preferenceKey;
    }
}
