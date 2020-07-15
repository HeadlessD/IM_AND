package com.qbao.newim.views;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qbao.newim.activity.NIMChatActivity;
import com.qbao.newim.adapter.ChatAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.FriendTypeDef;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.helper.AudioPlayManager;
import com.qbao.newim.manager.GcChatSendManager;
import com.qbao.newim.manager.NIMFriendInfoManager;
import com.qbao.newim.manager.NIMGroupMsgManager;
import com.qbao.newim.manager.NIMGroupUserManager;
import com.qbao.newim.manager.NIMMsgManager;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.manager.ScChatSendManager;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.model.IMGroupUserInfo;
import com.qbao.newim.model.InitViews;
import com.qbao.newim.model.NIM_Chat_ID;
import com.qbao.newim.model.message.BaseMessageModel;
import com.qbao.newim.model.message.GcMessageModel;
import com.qbao.newim.model.message.ScMessageModel;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.util.NIMStartActivityUtil;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.BaseUtil;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.DateUtil;

import java.util.ArrayList;

/**
 * Created by chenjian on 2017/3/25.
 */

public abstract class ChatView extends LinearLayout implements InitViews,
        View.OnClickListener, View.OnLongClickListener {

    public static final boolean SHOW_AVATAR = true;
    protected NIMChatActivity mActivity = null;

    /**
     * 在列表中的位置
     */
    protected int position;
    protected ChatAdapter adapter;
    protected BaseMessageModel entry;

    protected LinearLayout viewContent;
    protected ImageView imgAvatar;
    protected ProgressBar barSending;
    protected TextView txtName;
    protected View statusLayout;
    protected ImageView imgStatus;
    protected TextView txtStatus;
    protected TextView txtTimeLine;
    protected View timeLineView;
    private View avatarLayout;

    private static int avatarSize = 0;

    public ChatView(Context context) {
        super(context);
        initViews(context, null);
        bindListener();
        initData();
    }

    public ChatView(Context context, ChatAdapter adapter) {
        super(context);

        this.adapter = adapter;

        initViews(context, null);
        bindListener();
        initData();
    }

    public ChatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context, null);
        bindListener();
        initData();
    }

    @Override
    public void initViews(Context context, View root) {
        mActivity = (NIMChatActivity) context;

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        setOrientation(LinearLayout.VERTICAL);

        final View rootView = layoutInflater.inflate(getTemplateLayoutId(),
                this);
        viewContent = (LinearLayout) rootView
                .findViewById(R.id.chat_layout_content_container3);
        layoutInflater.inflate(getLayoutId(), viewContent);
        timeLineView = rootView.findViewById(R.id.chat_time_line_layout);
        txtTimeLine = (TextView) rootView.findViewById(R.id.chat_tv_timeline);
        imgAvatar = (ImageView) rootView.findViewById(R.id.chat_img_avatar);
        txtName = (TextView) rootView.findViewById(R.id.chat_name);
        statusLayout = rootView.findViewById(R.id.chat_layout_status);
        txtStatus = (TextView) rootView.findViewById(R.id.chat_tv_status);
        barSending = (ProgressBar) rootView.findViewById(R.id.chat_progressbar);
        imgStatus = (ImageView) rootView.findViewById(R.id.chat_img_status);

        avatarLayout = rootView.findViewById(R.id.chat_avatar_layout);
    }

    @Override
    public void bindListener() {
        if (viewContent != null && avatarLayout != null) {
            viewContent.setOnClickListener(this);
            viewContent.setOnLongClickListener(this);

            avatarLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    long user_id = NIMUserInfoManager.getInstance().GetSelfUserId();
                    if(!entry.is_self)
                    {
                        // TODO: 2017/9/26 史云杰，需要添加群和公众号
                        switch (entry.chat_type)
                        {
                            case MsgConstDef.MSG_CHAT_TYPE.PRIVATE:
                            {
                                ScMessageModel sc_message_model = (ScMessageModel)entry;
                                user_id = sc_message_model.opt_user_id;
                                break;
                            }
                            case MsgConstDef.MSG_CHAT_TYPE.GROUP:
                            {
                                GcMessageModel gc_message_model = (GcMessageModel)entry;
                                user_id = gc_message_model.user_id;
                                break;
                            }
                            case MsgConstDef.MSG_CHAT_TYPE.PUBLIC:
                            {
                                break;
                            }
                        }
                    }

                    NIMStartActivityUtil.startToUserForResult(mActivity, user_id, FriendTypeDef.FRIEND_SOURCE_TYPE.CHATTING,
                            mActivity.REQUEST_CODE_DELETE_FRIEND, -1);
                }
            });
        }

        if (statusLayout != null) {
            statusLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    reSend();
                }
            });
        }
    }

    @Override
    public void initData() {
        if (avatarSize == 0) {
            avatarSize = (int) getResources().getDimension(R.dimen.chat_avatar_size);
        }
    }

    @Override
    public int getLayoutId() {
        return 0;
    }

    @Override
    public boolean onLongClick(View v) {
        doOnLongClick();
        return true;
    }

    /**
     * 获取模板布局id
     */
    protected abstract int getTemplateLayoutId();

    /**
     * 设置消息数据到控件
     *
     * @param position
     * @param chatMsg
     */
    public void setMessage(final int position, final BaseMessageModel chatMsg) {
        this.position = position;
        this.entry = chatMsg;
        addTimeLine(position);
        setName();
        setAvatar();
        setStatus();
    }

    private void setName()
    {
        if (txtName == null)
            return;
        if (!entry.is_self && adapter.is_show_nick() && entry.chat_type == MsgConstDef.MSG_CHAT_TYPE.GROUP)
        {
            // TODO: 2017/9/26 史云杰 根据群MODEL添加群信息
            txtName.setVisibility(View.VISIBLE);
            GcMessageModel gcMessageModel = (GcMessageModel)entry;
            IMGroupUserInfo userInfo = NIMGroupUserManager.getInstance().getGroupUserInfo(gcMessageModel.group_id, gcMessageModel.user_id);
            if (userInfo == null)
            {
                txtName.setText(gcMessageModel.send_user_name);
            }
            else
            {
                IMFriendInfo friendInfo = NIMFriendInfoManager.getInstance().getFriendUser(gcMessageModel.user_id);
                if (friendInfo != null && !TextUtils.isEmpty(friendInfo.remark_name))
                {
                    txtName.setText(friendInfo.remark_name);
                } 
                else
                {
                    txtName.setText(userInfo.user_nick_name);
                }
            }
        }
    }

    public void setAvatar() {
        if (imgAvatar == null) {
            return;
        }
        imgAvatar.setImageResource(R.mipmap.nim_head);

        String url = AppUtil.getHeadUrl(NIMUserInfoManager.getInstance().GetSelfUserId());

        if (entry.is_self) {
            Glide.with(getContext()).load(url).
                    placeholder(R.mipmap.nim_head).centerCrop().crossFade().into(imgAvatar);

        } else {
            if (SHOW_AVATAR) {
                if (avatarLayout.getVisibility() == View.GONE) {
                    avatarLayout.setVisibility(View.VISIBLE);
                }

                // TODO: 2017/9/26 史云杰，需要添加群和公众号
                long user_id = 0;
                switch (entry.chat_type)
                {
                    case MsgConstDef.MSG_CHAT_TYPE.PRIVATE:
                    {
                        ScMessageModel sc_message_model = (ScMessageModel)entry;
                        user_id = sc_message_model.opt_user_id;
                        break;
                    }
                    case MsgConstDef.MSG_CHAT_TYPE.GROUP:
                    {
                        break;
                    }
                    case MsgConstDef.MSG_CHAT_TYPE.PUBLIC:
                    {
                        break;
                    }
                }
                String opt_url = AppUtil.getHeadUrl(user_id);
                if (!TextUtils.isEmpty(opt_url)) {
                    Glide.with(getContext()).load(opt_url).placeholder(R.mipmap.nim_head).
                            centerCrop().crossFade().into(imgAvatar);
                }
            } else {
                if (avatarLayout.getVisibility() == View.VISIBLE) {
                    avatarLayout.setVisibility(View.GONE);
                }
            }
        }
    }

    protected void setStatus() {
        if (barSending != null && imgStatus != null) {
            if (entry.msg_status == MsgConstDef.MSG_STATUS.SENDING) {
                imgStatus.setVisibility(View.GONE);
                txtStatus.setVisibility(View.GONE);
                barSending.setVisibility(View.VISIBLE);
                statusLayout.setVisibility(View.VISIBLE);
            } else if (entry.msg_status == MsgConstDef.MSG_STATUS.SEND_FAILED) {
                barSending.setVisibility(View.GONE);
                txtStatus.setVisibility(View.GONE);
                imgStatus.setVisibility(View.VISIBLE);
                statusLayout.setVisibility(View.VISIBLE);
            } else {
                statusLayout.setVisibility(View.INVISIBLE);
            }
        } else if (statusLayout != null) {
            statusLayout.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 判断是否添加时间分割线
     *
     * @return boolean
     */
    protected void addTimeLine(int position) {
        // 如果当前是第一条消息，或者两条消息间隔五分钟，或者本地数据记录需要显示的时间线
        if (position == 0 || (entry.msg_time - adapter.getItem(position - 1).msg_time) > 5 * 60 * 1000) {
            timeLineView.setVisibility(View.VISIBLE);
            String timeStr = DateUtil.formatSnsDate(entry.msg_time);
            txtTimeLine.setText(timeStr);
        } else {
            timeLineView.setVisibility(View.GONE);
        }
    }

    private void doOnLongClick() {
        final ArrayList<String> op_aar = new ArrayList<>();
        if (entry.m_type == MsgConstDef.MSG_M_TYPE.TEXT) {
            op_aar.add("拷贝");
        }
        if (entry.m_type == MsgConstDef.MSG_M_TYPE.VOICE) {
            op_aar.add(AudioPlayManager.getManager().isCustomEarMode() ? "使用扬声器模式" : "使用听筒模式");
        }
        op_aar.add("转发");
        op_aar.add("删除");
        op_aar.add("取消");
        ProgressDialog.showCustomDialog(mActivity, "操作", op_aar, new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                String click_str = op_aar.get(position);
                if (click_str.equals("转发")) {
                    repeat(entry);
                } else if (click_str.equals("拷贝")) {
                    copy();
                } else if (click_str.equals("删除")) {
                    delete();
                } else if (click_str.contains("模式")) {
                    switchMode();
                }
            }
        });
    }

    protected void reSend() {
        if (entry.msg_status == MsgConstDef.MSG_STATUS.SEND_FAILED) {
            String title = getResources().getString(R.string.nim_chat_resend);
            String message = getResources().getString(R.string.chat_resend_hint);
            ProgressDialog.showCustomDialog(mActivity, title, message
            , new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            doReSend();
                        }
                    });
        }
    }

    private void switchMode() {
        boolean earMode = AudioPlayManager.getManager().isCustomEarMode();
        int nextMode = earMode ? AudioPlayManager.MODE_SPEAKER : AudioPlayManager.MODE_EARPIECE;
        AudioPlayManager.getManager().setCustomMode(nextMode);
        DataObserver.Notify(DataConstDef.EVENT_VOICE_MODE, !earMode, null);
    }

    protected void repeat(BaseMessageModel chatMsg) {
        String data = "";
		// TODO: 2017/9/28 史云杰 加上对应的群，私聊，公众号逻辑
        switch (chatMsg.chat_type) {
            case MsgConstDef.MSG_CHAT_TYPE.PRIVATE:
                ScMessageModel sc_message_model = (ScMessageModel)entry;
                data = sc_message_model.chat_type + "_" + sc_message_model.message_id + "_" + sc_message_model.opt_user_id;
                break;
            case MsgConstDef.MSG_CHAT_TYPE.GROUP:
                GcMessageModel gcMessageModel = (GcMessageModel)entry;
                data = gcMessageModel.chat_type + "_" + gcMessageModel.message_id + "_" + gcMessageModel.group_id;
                break;
            case MsgConstDef.MSG_CHAT_TYPE.PUBLIC:
                break;
        }

        NIMStartActivityUtil.startToChooseSessionActivity(mActivity, data);
    }

    protected void doReSend() {
		// TODO: 2017/9/28 史云杰 加上对应的群，私聊，公众号逻辑
        switch (entry.chat_type) {
            case MsgConstDef.MSG_CHAT_TYPE.PRIVATE:
                ScMessageModel sc_message_model = (ScMessageModel)entry;
                NIMMsgManager.getInstance().RemoveMessage(new NIM_Chat_ID(sc_message_model.opt_user_id, position));
                sc_message_model.message_id = NetCenter.getInstance().CreateMsgID();
                sc_message_model.msg_time = BaseUtil.GetServerTime();
                sc_message_model.msg_status = MsgConstDef.MSG_STATUS.SENDING;
                ScChatSendManager.getInstance().send(sc_message_model);
                break;
            case MsgConstDef.MSG_CHAT_TYPE.GROUP:
                GcMessageModel gcMessageModel = (GcMessageModel)entry;
                NIMGroupMsgManager.getInstance().delGroupMessageInfoByMsgID(gcMessageModel.group_id, gcMessageModel.message_id);
                gcMessageModel.message_id = NetCenter.getInstance().CreateGroupMsgId();
                gcMessageModel.msg_time = BaseUtil.GetServerTime();
                gcMessageModel.msg_status = MsgConstDef.MSG_STATUS.SENDING;
                GcChatSendManager.getInstance().send(gcMessageModel);
                break;
        }
    }

    protected void copy() {
        if(entry.m_type == MsgConstDef.MSG_M_TYPE.TEXT) {
            String content = entry.msg_content;
            ClipData clipData = ClipData.newPlainText("text", content);
            ((ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(clipData);
        }
    }

    protected void delete() {
		// TODO: 2017/9/28 史云杰 加上对应的群，私聊，公众号逻辑
        switch (entry.chat_type) {
            case MsgConstDef.MSG_CHAT_TYPE.PRIVATE:
                ScMessageModel sc_message_model = (ScMessageModel)entry;
                NIMMsgManager.getInstance().RemoveMessage(new NIM_Chat_ID(sc_message_model.opt_user_id, position));
                break;
            case MsgConstDef.MSG_CHAT_TYPE.GROUP:
                GcMessageModel gcMessageModel = (GcMessageModel)entry;
                NIMGroupMsgManager.getInstance().delGroupMessageInfoByMsgID(gcMessageModel.group_id, gcMessageModel.message_id);
                break;
        }
    }
}