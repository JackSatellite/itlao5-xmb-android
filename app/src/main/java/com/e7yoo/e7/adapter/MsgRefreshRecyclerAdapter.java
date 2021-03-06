package com.e7yoo.e7.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.e7yoo.e7.R;
import com.e7yoo.e7.model.PrivateMsg;
import com.e7yoo.e7.model.Robot;
import com.e7yoo.e7.model.User;
import com.e7yoo.e7.model.UserUtil;
import com.e7yoo.e7.sql.DbThreadPool;
import com.e7yoo.e7.util.ChatPopUtil;
import com.e7yoo.e7.util.Constant;
import com.e7yoo.e7.util.DebugUtil;
import com.e7yoo.e7.util.MyIconUtil;
import com.e7yoo.e7.util.PreferenceUtil;
import com.e7yoo.e7.util.RobotUtil;
import com.e7yoo.e7.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/8/28.
 */
public class MsgRefreshRecyclerAdapter extends RecyclerAdapter {
    private LayoutInflater mInflater;
    private List<PrivateMsg> mMsgs = new ArrayList<>();
    private static final int VIEW_TYPE_SEND = 0;
    private static final int VIEW_TYPE_REV = 1;
    private static final int VIEW_TYPE_HINT = 2;
    private static final int VIEW_TYPE_FOOTER = 10;
    /** 用于Footer的类型 */
    private FooterType mFooterType = FooterType.DEFAULT;
    /** 用于Footer的类型 */
    private boolean mFooterShowProgress = false;
    /** 用于Footer的文字显示，<= 0 时不显示GONE */
    private int mFooterStringId = 0;
    private static final int FOOTER_COUNT = 1;
    private Robot mRobot;
    private Context mContext;
    private User mUser;
    private String mMyIcon;
    private boolean mShowCheckBox = false;

    public void setCheckAll(boolean checkAll) {
        checkTimes.clear();
        if(checkAll) {
            for (PrivateMsg msg : mMsgs) {
                checkTimes.add(msg.getTime());
            }
        }
        notifyDataSetChanged();
    }

    public void showCheckBox(boolean showCheckBox) {
        this.mShowCheckBox = showCheckBox;
        checkTimes.clear();
        notifyDataSetChanged();
    }

    public boolean isShowCheckBox() {
        return mShowCheckBox;
    }

    public MsgRefreshRecyclerAdapter(Context context, Robot robot, User user) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mRobot = robot;
        this.mUser = user;
        DebugUtil.setDatas(mMsgs, 1, true);
        if(mUser != null && mUser.getIcon() != null) {
            mMyIcon = mUser.getIcon();
        } else {
            mMyIcon = MyIconUtil.getMyIcon();
        }
        ChatPopUtil.getInstance().init();
    }

    public void refreshRobot(Robot robot) {
        mRobot = robot;
        notifyDataSetChanged();
    }

    public void refreshMe(User user) {
        this.mUser= user;
        if(mUser != null && mUser.getIcon() != null) {
            mMyIcon = mUser.getIcon();
        } else {
            mMyIcon = MyIconUtil.getMyIcon();
        }
        notifyDataSetChanged();
    }

    public void addItemTop(PrivateMsg newData) {
        mMsgs.add(0, newData);
        notifyDataSetChanged();
    }

    public void addItemTop(List<PrivateMsg> newDatas) {
        if(newDatas != null && newDatas.size() > 0) {
            mMsgs.addAll(0, newDatas);
            //notifyItemRangeChanged(0, newDatas.size());
            notifyDataSetChanged();
        }
    }

    public void addItemBottom(List<PrivateMsg> newDatas) {
        if(newDatas != null && newDatas.size() > 0) {
            mMsgs.addAll(newDatas);
            notifyDataSetChanged();
        }
    }

    public void addItemBottom(PrivateMsg newData) {
        if(mRobot != null && mRobot.getName() != null) {
            newData.setUser(mRobot.getName());
        }
        mMsgs.add(newData);
        notifyItemRangeChanged(mMsgs.size() - 2, 2);
        DbThreadPool.getInstance().insert(mContext, newData);
    }

    public void refreshData(List<PrivateMsg> newDatas) {
        mMsgs.clear();
        if(newDatas != null) {
            mMsgs.addAll(newDatas);
        }
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        mMsgs.remove(position);
        notifyItemRemoved(position);
        if(position < getItemCount()) {
            notifyItemRangeChanged(position, getItemCount() - position);
        }
    }

    public FooterType getFooter() {
        return mFooterType;
    }

    public void setFooter(FooterType type, int footerStringId, boolean footerShowProgress) {
        mFooterType = type;
        mFooterStringId = footerStringId;
        mFooterShowProgress = footerShowProgress;
        notifyDataSetChanged();
        // notifyItemChanged(getItemCount() - 1);
    }

    public int getLastId() {
        return (mMsgs == null || mMsgs.size() == 0) ? -1 : mMsgs.get(0).get_id();
    }

    public boolean isEndWithNetError() {
        return (mMsgs == null || mMsgs.size() == 0) ? false : mMsgs.get(mMsgs.size() - 1).getType() == PrivateMsg.Type.HINT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        View view;
        switch (viewType) {
            case VIEW_TYPE_FOOTER:
                view = mInflater.inflate(R.layout.item_msg_footer, parent, false);
                viewHolder = new ViewHolderFooter(view);
                break;
            case VIEW_TYPE_SEND:
                view = mInflater.inflate(R.layout.item_msg_send, parent, false);
                viewHolder = new ViewHolderSend(view);
                break;
            case VIEW_TYPE_REV:
                view = mInflater.inflate(R.layout.item_msg_rev, parent, false);
                viewHolder = new ViewHolderRev(view);
                break;
            case VIEW_TYPE_HINT:
            default:
                view = mInflater.inflate(R.layout.item_msg_hint, parent, false);
                viewHolder = new ViewHolderHint(view);
                break;
        }
        return viewHolder;
    }

    private ArrayList<Long> checkTimes = new ArrayList<>();

    public ArrayList<Long> getCheckIdsAndRemove() {
        for(int i = 0; i < mMsgs.size(); i++) {
            PrivateMsg msg = mMsgs.get(i);
            if(msg != null && checkTimes.contains(msg.getTime())) {
                mMsgs.remove(i);
                i--;
            }
        }
        return new ArrayList<>(checkTimes);
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        boolean showTime = showTime(position);
        if(holder instanceof ViewHolderSend) {
            final ViewHolderSend viewHolderSend = (ViewHolderSend) holder;
            initCheck(viewHolderSend.itemCheck, mMsgs.get(position));
            if(showTime) {
                viewHolderSend.itemMsgTime.setText(TimeUtil.formatMsgTime(mMsgs.get(position).getTime()));
                viewHolderSend.itemMsgTime.setBackgroundResource(R.drawable.rounded_corners_tag_gray_trans);
                viewHolderSend.itemMsgTime.setVisibility(View.VISIBLE);
            } else {
                viewHolderSend.itemMsgTime.setText("");
                viewHolderSend.itemMsgTime.setBackgroundResource(0);
                viewHolderSend.itemMsgTime.setVisibility(View.GONE);
            }
            viewHolderSend.itemMsgContent.setText(mMsgs.get(position).getContent());
//            if(mUser != null && mUser.getIcon() != null) {
//                RequestOptions options = new RequestOptions();
//                options.placeholder(R.mipmap.icon_me).error(R.mipmap.icon_me);
//                Glide.with(mContext).load(mUser.getIcon()).apply(options).into(viewHolderSend.itemMsgIcon);
//            } else if(mMyIcon != null) {
//                RequestOptions options = new RequestOptions();
//                options.placeholder(R.mipmap.icon_me).error(R.mipmap.icon_me);
//                Glide.with(mContext).load(mMyIcon).apply(options).into(viewHolderSend.itemMsgIcon);
//            } else {
//                viewHolderSend.itemMsgIcon.setImageResource(R.mipmap.icon_me);
//            }
            UserUtil.setIcon(mContext, viewHolderSend.itemMsgIcon, mMyIcon);
            viewHolderSend.itemMsgVoice.setVisibility(View.GONE);
            if(ttsMsgTime == mMsgs.get(position).getTime()) {
                viewHolderSend.itemMsgVoice.setSelected(true);
            } else {
                viewHolderSend.itemMsgVoice.setSelected(false);
            }
            viewHolderSend.itemMsgContent.setTextColor(ChatPopUtil.getInstance().getChatPopTextColor()[0]);
            viewHolderSend.itemMsgTime.setTextColor(ChatPopUtil.getInstance().getChatPopTextColor()[2]);
            viewHolderSend.contentLayout.setBackgroundResource(ChatPopUtil.getInstance().getChatPop()[0]);
            viewHolderSend.itemMsgVoice.setImageResource(ChatPopUtil.getInstance().getChatPop()[2]);
            // viewHolderSend.itemMsgVoice.setImageResource(mMsgs.get(position).getContent());
            addClickListener(viewHolderSend.contentLayout, viewHolderSend.itemMsgVoice, null, position);
        } else if(holder instanceof ViewHolderRev) {
            ViewHolderRev viewHolderRev = (ViewHolderRev) holder;
            initCheck(viewHolderRev.itemCheck, mMsgs.get(position));
            if(showTime) {
                viewHolderRev.itemMsgTime.setText(TimeUtil.formatMsgTime(mMsgs.get(position).getTime()));
                viewHolderRev.itemMsgTime.setBackgroundResource(R.drawable.rounded_corners_tag_gray_trans);
                viewHolderRev.itemMsgTime.setVisibility(View.VISIBLE);
            } else {
                viewHolderRev.itemMsgTime.setText("");
                viewHolderRev.itemMsgTime.setBackgroundResource(0);
                viewHolderRev.itemMsgTime.setVisibility(View.GONE);
            }
            viewHolderRev.itemMsgContent.setText(mMsgs.get(position).getContent());
            int resIcon = RobotUtil.getDefaultIconResId(mRobot);
            if(mRobot != null && mRobot.getIcon() != null) {
                RequestOptions options = new RequestOptions();
                options.placeholder(resIcon).error(resIcon);
                Glide.with(mContext).load(mRobot.getIcon()).apply(options).into(viewHolderRev.itemMsgIcon);
            } else {
                viewHolderRev.itemMsgIcon.setImageResource(resIcon);
            }
            viewHolderRev.itemMsgVoice.setVisibility(View.VISIBLE);
            if(ttsMsgTime == mMsgs.get(position).getTime()) {
                viewHolderRev.itemMsgVoice.setSelected(true);
            } else {
                viewHolderRev.itemMsgVoice.setSelected(false);
            }
            View urlView;
            String url = mMsgs.get(position).getUrl();
            if(TextUtils.isEmpty(url)) {
                viewHolderRev.itemMsgPic.setVisibility(View.GONE);
                viewHolderRev.itemMsgUrl.setVisibility(View.GONE);
                urlView = null;
            } else {
                if(mMsgs.get(position).getCode() == -2) { // 图片
                    RequestOptions options = new RequestOptions();
                    options.placeholder(R.mipmap.log_e7yoo_transport).error(R.mipmap.log_e7yoo_transport);
                    if(url.endsWith(".gif")) {
                        Glide.with(mContext).asGif().load(url).apply(options).into(viewHolderRev.itemMsgPic);
                    } else {
                        Glide.with(mContext).asBitmap().load(url).apply(options).into(viewHolderRev.itemMsgPic);
                    }
                    viewHolderRev.itemMsgPic.setVisibility(View.VISIBLE);
                    viewHolderRev.itemMsgUrl.setVisibility(View.GONE);
                    urlView = viewHolderRev.itemMsgPic;
                } else {
                    viewHolderRev.itemMsgPic.setVisibility(View.GONE);
                    viewHolderRev.itemMsgUrl.setVisibility(View.VISIBLE);
                    urlView = viewHolderRev.itemMsgUrl;
                }
            }
            viewHolderRev.itemMsgContent.setTextColor(ChatPopUtil.getInstance().getChatPopTextColor()[1]);
            viewHolderRev.itemMsgTime.setTextColor(ChatPopUtil.getInstance().getChatPopTextColor()[2]);
            viewHolderRev.contentLayout.setBackgroundResource(ChatPopUtil.getInstance().getChatPop()[1]);
            viewHolderRev.itemMsgVoice.setImageResource(ChatPopUtil.getInstance().getChatPop()[2]);
            // viewHolderRev.itemMsgIcon.setImageResource();
            // viewHolderRev.itemMsgVoice.setImageResource(mMsgs.get(position).getContent());
            addClickListener(viewHolderRev.contentLayout, viewHolderRev.itemMsgVoice, urlView, position);
        } else if(holder instanceof ViewHolderHint) {
            ViewHolderHint viewHolderHint = (ViewHolderHint) holder;
            initCheck(viewHolderHint.itemCheck, mMsgs.get(position));
            if(showTime) {
                viewHolderHint.itemMsgTime.setText(TimeUtil.formatMsgTime(mMsgs.get(position).getTime()));
                viewHolderHint.itemMsgTime.setBackgroundResource(R.drawable.rounded_corners_tag_gray_trans);
                viewHolderHint.itemMsgTime.setVisibility(View.VISIBLE);
            } else {
                viewHolderHint.itemMsgTime.setText("");
                viewHolderHint.itemMsgTime.setBackgroundResource(0);
                viewHolderHint.itemMsgTime.setVisibility(View.GONE);
            }
            viewHolderHint.itemMsgHint.setText(mMsgs.get(position).getContent());
            viewHolderHint.itemMsgTime.setTextColor(ChatPopUtil.getInstance().getChatPopTextColor()[2]);
            viewHolderHint.itemMsgHint.setTextColor(ChatPopUtil.getInstance().getChatPopTextColor()[2]);
            addClickListener(viewHolderHint.itemMsgHint, null, null, position);
        } else if(holder instanceof ViewHolderFooter) {
            ViewHolderFooter viewHolderFooter = (ViewHolderFooter) holder;
            int footerStringId = getFooterStringId();
            if(footerStringId > 0) {
                viewHolderFooter.loadingTv.setText(footerStringId);
                viewHolderFooter.layout.setBackgroundResource(R.drawable.rounded_corners_tag_gray_trans);
            } else {
                viewHolderFooter.loadingTv.setText("　");
                viewHolderFooter.layout.setBackgroundResource(0);
            }
            viewHolderFooter.loadingPb.setVisibility(mFooterShowProgress ? View.VISIBLE : View.GONE);
            viewHolderFooter.loadingTv.setTextColor(ChatPopUtil.getInstance().getChatPopTextColor()[2]);
        }
        holder.itemView.setTag(position);
    }

    private void initCheck(final TextView check, final PrivateMsg msg) {
        if(mShowCheckBox) {
            check.setSelected(checkTimes.contains(msg.getTime()));
            check.setVisibility(View.VISIBLE);
            check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = checkTimes.indexOf(msg.getTime());
                    if(index >= 0) {
                        checkTimes.remove(index);
                        check.setSelected(false);
                    } else {
                        checkTimes.add(msg.getTime());
                        check.setSelected(true);
                    }
                }
            });
        } else {
            check.setVisibility(View.GONE);
        }
    }

    private int voicePosition;
    private void addClickListener(View view, View voice, View url, final int position) {
        if(isShowCheckBox()) {
            return;
        }
        if(view != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, position);
                    }
                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(mOnItemLongClickListener != null) {
                        return mOnItemLongClickListener.onItemLongClick(view, position);
                    }
                    return false;
                }
            });
        }
        if(url != null) {
            url.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mOnUrlClickListener != null) {
                        mOnUrlClickListener.onUrlClick(view, position);
                    }
                }
            });
        }
        if(voice != null) {
            voice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mOnVoiceClickListener != null) {
                        mOnVoiceClickListener.onVoiceClick(view, position);
                    }
                }
            });
        }
    }

    private boolean showTime(int position) {
        if(position == 0) {
            return true;
        } else if(position >= mMsgs.size()) {
            return false;
        } else {
            return (mMsgs.get(position).getTime() - mMsgs.get(position - 1).getTime()) / (1000 * 60) >= 1;
        }
    }

    private int getFooterStringId() {
        int footerStringId = mFooterStringId;
        switch (mFooterType) {
            case DEFAULT:
                break;
            case LOADING:
                if(footerStringId <= 0) {
                    footerStringId = R.string.loading;
                }
                break;
            case NO_MORE:
                if(footerStringId <= 0) {
                    footerStringId = R.string.loading_no_more;
                }
                break;
            case END:
                break;
            case HINT:
                if(footerStringId <= 0) {
                    footerStringId = R.string.loading_up_load_more;
                }
                break;
            default:
                break;
        }
        return footerStringId;
    }

    public int getLastPosition() {
        return mMsgs == null || mMsgs.size() == 0 ? 0 : mMsgs.size() - 1;
    }

    @Override
    public int getItemCount() {
        return mMsgs.size() + FOOTER_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        int itemViewType;
        if (position == getItemCount() - FOOTER_COUNT) {
            itemViewType = VIEW_TYPE_FOOTER;
        } else {
            switch (mMsgs.get(position).getType()) {
                case SEND:
                    itemViewType = VIEW_TYPE_SEND;
                    break;
                case REPLY:
                    itemViewType = VIEW_TYPE_REV;
                    break;
                case HINT:
                default:
                    itemViewType = VIEW_TYPE_HINT;
                    break;
            }
        }
        return itemViewType;
    }

    /**
     * 消息基类
     */
    public static class BaseMsgViewHolder extends RecyclerView.ViewHolder {
        public TextView itemMsgTime;
        public TextView itemCheck;
        public ImageView itemMsgIcon;
        public TextView itemMsgContent;
        public View contentLayout;
        public ImageView itemMsgVoice;

        public BaseMsgViewHolder(View view) {
            super(view);
            itemMsgTime = view.findViewById(R.id.item_msg_time);
            itemCheck = view.findViewById(R.id.item_msg_check);
            itemMsgIcon = view.findViewById(R.id.item_msg_icon);
            itemMsgContent = view.findViewById(R.id.item_msg_content);
            contentLayout = view.findViewById(R.id.item_msg_content_layout);
            itemMsgVoice = view.findViewById(R.id.item_msg_voice);
        }
    }

    /**
     * 发送消息item
     */
    public static class ViewHolderSend extends BaseMsgViewHolder {

        public ViewHolderSend(View view) {
            super(view);
        }
    }

    /**
     * 接收消息item
     */
    public static class ViewHolderRev extends BaseMsgViewHolder {

        public ImageView itemMsgPic;
        public TextView itemMsgUrl;
        public ViewHolderRev(View view) {
            super(view);
            itemMsgPic = view.findViewById(R.id.item_msg_pic);
            itemMsgUrl = view.findViewById(R.id.item_msg_url);
        }
    }

    /**
     * 提示消息item
     */
    public static class ViewHolderHint extends RecyclerView.ViewHolder {
        public TextView itemMsgTime;
        public TextView itemCheck;
        public TextView itemMsgHint;

        public ViewHolderHint(View view) {
            super(view);
            itemMsgTime = view.findViewById(R.id.item_msg_time);
            itemCheck = view.findViewById(R.id.item_msg_check);
            itemMsgHint = view.findViewById(R.id.item_msg_hint);
        }
    }

    /**
     * 上拉刷新提示item
     */
    public static class ViewHolderFooter extends RecyclerView.ViewHolder {
        public View layout;
        public ProgressBar loadingPb;
        public TextView loadingTv;
        public ViewHolderFooter(View view) {
            super(view);
            layout = view.findViewById(R.id.item_msg_footer_layout);
            loadingPb = view.findViewById(R.id.item_msg_loading_progressbar);
            loadingTv = view.findViewById(R.id.item_msg_loading);
        }
    }

    public enum FooterType {
        DEFAULT,
        END,
        LOADING,
        NO_MORE,
        HINT;
    }

    public PrivateMsg getItem(int position) {
        return mMsgs != null && mMsgs.size() > position && position >= 0 ? mMsgs.get(position) : null;
    }

    private OnUrlClickListener mOnUrlClickListener;
    public void setOnUrlClickListener(OnUrlClickListener onUrlClickListener) {
        mOnUrlClickListener = onUrlClickListener;
    }
    public interface OnUrlClickListener{
        void onUrlClick(View view, int position);
    }

    private OnVoiceClickListener mOnVoiceClickListener;
    public void setOnVoiceClickListener(OnVoiceClickListener onVoiceClickListener) {
        mOnVoiceClickListener = onVoiceClickListener;
    }
    public interface OnVoiceClickListener{
        void onVoiceClick(View view, int position);
    }

    private long ttsMsgTime = -1;
    public void setTtsMsgTime(long ttsMsgTime) {
        this.ttsMsgTime = ttsMsgTime;
    }
    public long getTtsMsgTime() {
        return ttsMsgTime;
    }
}
