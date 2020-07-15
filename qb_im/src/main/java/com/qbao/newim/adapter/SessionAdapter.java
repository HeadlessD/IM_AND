package com.qbao.newim.adapter;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseViewHolder;
import com.qbao.newim.configure.GlobalVariable;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.manager.ChatMsgBuildManager;
import com.qbao.newim.manager.NIMComplexManager;
import com.qbao.newim.manager.NIMFriendInfoManager;
import com.qbao.newim.manager.NIMGroupInfoManager;
import com.qbao.newim.manager.NIMGroupMsgManager;
import com.qbao.newim.manager.NIMMsgCountManager;
import com.qbao.newim.manager.NIMMsgManager;
import com.qbao.newim.manager.NIMSessionManager;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.model.SessionModel;
import com.qbao.newim.model.message.GcMessageModel;
import com.qbao.newim.model.message.ScMessageModel;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.DateUtil;
import com.qbao.newim.util.FaceUtil;
import com.qbao.newim.util.Logger;
import com.qbao.newim.util.Utils;
import com.qbao.newim.views.FaceTextView;

import java.util.List;

/**
 * Created by luoxw on 2016/6/20.
 */
public class SessionAdapter extends BaseQuickAdapter<SessionModel, BaseViewHolder> {

    protected static final String TAG = SessionAdapter.class.getSimpleName();
    Context m_context;
    public SessionAdapter(Context context, List<SessionModel> data) {
        super(R.layout.nim_fragment_session, data);
        m_context = context;
    }

    //更新消息状态空间
    private void UpdateMsgStatusView(BaseViewHolder helper, int msg_status)
    {
        // 显示发送状态
        switch (msg_status)
        {
            case MsgConstDef.MSG_STATUS.UPLOADING:
            case MsgConstDef.MSG_STATUS.SENDING:
                helper.getView(R.id.msg_center_send_status).setVisibility(View.VISIBLE);
                helper.setBackgroundRes(R.id.msg_center_send_status, R.mipmap.nim_sending_arrow);
                break;
            case MsgConstDef.MSG_STATUS.INVALID:
            case MsgConstDef.MSG_STATUS.DRAFT:
            case MsgConstDef.MSG_STATUS.UNREAD:
            case MsgConstDef.MSG_STATUS.READED:
            case MsgConstDef.MSG_STATUS.UPLOAD_SUCCESS:
            case MsgConstDef.MSG_STATUS.SEND_SUCCESS:
            case MsgConstDef.MSG_STATUS.DOWNLOADING:
            case MsgConstDef.MSG_STATUS.DOWNLOAD_SUCCESS:
            case MsgConstDef.MSG_STATUS.PLAYED:
                helper.getView(R.id.msg_center_send_status).setVisibility(View.GONE);
                break;
            case MsgConstDef.MSG_STATUS.UPLOAD_FAILED:
            case MsgConstDef.MSG_STATUS.SEND_FAILED:
            case MsgConstDef.MSG_STATUS.DOWNLOAD_FAILED:
                helper.getView(R.id.msg_center_send_status).setVisibility(View.VISIBLE);
                helper.setBackgroundRes(R.id.msg_center_send_status, R.mipmap.nim_chat_send_status);
                break;

        }
    }

    //更新未读数控件
    private void UpdateUnreadView(BaseViewHolder helper, SessionModel item, boolean is_notify)
    {
        int unread_count = NIMMsgCountManager.getInstance().GetUnreadCount(item.session_id, item.chat_type);
        if (unread_count > 0)
        {
            if (item.chat_type == MsgConstDef.MSG_CHAT_TYPE.PUBLIC)
            {
                helper.getView(R.id.msg_center_unread_count).setVisibility(View.GONE);
                helper.getView(R.id.msg_center_unread_count2).setVisibility(View.VISIBLE);
            }
            else
            {
                if (is_notify)
                {
                    helper.getView(R.id.msg_center_unread_count).setVisibility(View.VISIBLE);
                    helper.getView(R.id.msg_center_unread_count2).setVisibility(View.GONE);
                    if (unread_count > 99)
                    {
                        helper.getView(R.id.msg_center_unread_count).setBackgroundResource(R.drawable.nim_red_max_circle);
                        helper.setText(R.id.msg_center_unread_count, m_context.getString(R.string.nim_99_plus));
                    }
                    else
                        {
                        helper.getView(R.id.msg_center_unread_count).setBackgroundResource(R.drawable.nim_red_circle);
                        helper.setText(R.id.msg_center_unread_count, String.valueOf(unread_count));
                    }
                }
                else
                {
                    helper.getView(R.id.msg_center_unread_count).setVisibility(View.GONE);
                    helper.getView(R.id.msg_center_unread_count2).setVisibility(View.VISIBLE);
                }
            }
        }
        else
        {
            helper.getView(R.id.msg_center_unread_count).setVisibility(View.GONE);
            helper.getView(R.id.msg_center_unread_count2).setVisibility(View.GONE);
        }
    }

    //更新会话相关控件[会话名, 会话内容]
    private void UpdateSessionView(BaseViewHolder helper, String session_name, String msg_content, boolean is_notify)
    {
        FaceTextView tv_content = helper.getView(R.id.msg_center_last_msg);
        CharSequence body = FaceUtil.getInstance().formatTextToFace(msg_content, FaceUtil.FACE_TYPE.CHAT_TEXTVIEW);
        if (body instanceof Spannable)
        {
            Spannable spannable = (Spannable) body;
            tv_content.setText(spannable);
        }
        else
        {
            SpannableString spannable = new SpannableString(body);
            tv_content.setText(spannable);
        }

        helper.setText(R.id.msg_center_friend_name, session_name);
        if (is_notify)
        {
            helper.getView(R.id.message_notify_img).setVisibility(View.GONE);
        }
        else
        {
            helper.getView(R.id.message_notify_img).setVisibility(View.VISIBLE);
        }
    }

    //更新私聊相关控件
    private void UpdatePrivateView(BaseViewHolder helper, SessionModel item)
    {
        String session_name = NIMComplexManager.getInstance().GetSessionName(item.session_id, item.chat_type);
        session_name = session_name != null ? session_name :String.valueOf(item.session_id);
        String session_content = "";
        boolean is_notify;
        int msg_status = MsgConstDef.MSG_STATUS.INVALID;
        IMFriendInfo friend_info = NIMFriendInfoManager.getInstance().getFriendUser(item.session_id);
        if (friend_info != null)
        {
            is_notify = friend_info.notify;
        }
        else
        {
            is_notify = false;
        }

        // 获取最新的1条消息
        ScMessageModel sc_model = NIMMsgManager.getInstance().GetLastMessage(item.session_id);
        if (null != sc_model)
        {
            session_content = ChatMsgBuildManager.HandleRichText(sc_model.m_type, sc_model.msg_content, sc_model.s_type);
            msg_status = sc_model.msg_status;
        }


        Glide.with(helper.getConvertView().getContext()).load(AppUtil.getHeadUrl(item.session_id)).crossFade()
                .placeholder(R.mipmap.nim_head)
                .centerCrop().into((ImageView) helper.getView(R.id.iv_head));

        UpdateSessionView(helper, session_name, session_content, is_notify);
        UpdateMsgStatusView(helper, msg_status);
        UpdateUnreadView(helper, item, is_notify);
    }


    //更新群聊相关控件
    private void UpdateGroupView(BaseViewHolder helper, SessionModel item)
    {
        String session_name = NIMComplexManager.getInstance().GetSessionName(item.session_id, item.chat_type);
        session_name = session_name != null ? session_name :String.valueOf(item.session_id);
        String session_content = "";
        boolean is_notify;
        int msg_status = MsgConstDef.MSG_STATUS.INVALID;
        IMGroupInfo group_info = NIMGroupInfoManager.getInstance().getGroupInfo(item.session_id);
        if (group_info == null)
        {
            is_notify = false;
            Logger.error(TAG, "group is not exsist: " + item.session_id);
        }
        else
        {
            if (group_info.notify_type != MsgConstDef.GROUP_MESSAGE_STATUS.GROUP_MESSAGE_STATUS_NORMAL)
                is_notify = false;
            else
                is_notify = true;
        }

        GcMessageModel gc_m_model = NIMGroupMsgManager.getInstance().getLastMessageInfoByGroupID(item.session_id);
        if (null != gc_m_model)
        {
            session_content = ChatMsgBuildManager.HandleRichText(gc_m_model.m_type, gc_m_model.msg_content, gc_m_model.s_type);
            msg_status = gc_m_model.msg_status;
        }

        Glide.with(helper.getConvertView().getContext()).load(AppUtil.getGroupUrl(item.session_id)).crossFade()
                .placeholder(R.mipmap.nim_head)
                .centerCrop().into((ImageView) helper.getView(R.id.iv_head));
        UpdateSessionView(helper, session_name, session_content, is_notify);
        UpdateMsgStatusView(helper, msg_status);
        UpdateUnreadView(helper, item, is_notify);
    }

    //更新公众号相关控件
    private void UpdatePublicView(BaseViewHolder helper, SessionModel item)
    {
        Glide.with(helper.getConvertView().getContext()).load(R.mipmap.nim_icon_qb_service)
                .into((ImageView) helper.getView(R.id.iv_head));

        UpdateSessionView(helper, "", "", true);
        UpdateMsgStatusView(helper, MsgConstDef.MSG_STATUS.INVALID);
        UpdateUnreadView(helper, item, true);
    }

    //更新系统消息相关控件
    private void UpdateSysView(BaseViewHolder helper, SessionModel item)
    {
        String session_name = m_context.getString(R.string.nim_sys_message);
        Glide.with(helper.getConvertView().getContext()).load(R.mipmap.nim_icon_task)
                .into((ImageView) helper.getView(R.id.iv_head));
        UpdateSessionView(helper, session_name, "", true);
        UpdateMsgStatusView(helper, MsgConstDef.MSG_STATUS.INVALID);
        UpdateUnreadView(helper, item, true);
    }

    //更新订阅消息相关控件
    private void UpdateSubscribeView(BaseViewHolder helper, SessionModel item)
    {
        String session_name = m_context.getString(R.string.nim_subscribe_session_name);
        String msg_content = m_context.getString(R.string.nim_subscribe_msg_content);
        Glide.with(helper.getConvertView().getContext()).load(R.mipmap.nim_icon_subscribe)
                .into((ImageView) helper.getView(R.id.iv_head));
        UpdateSessionView(helper, session_name, msg_content, true);
        UpdateMsgStatusView(helper, MsgConstDef.MSG_STATUS.INVALID);
        UpdateUnreadView(helper, item, true);
    }

    //更新任务助手相关控件
    private void UpdateTaskView(BaseViewHolder helper, SessionModel item)
    {
        String session_name = m_context.getString(R.string.nim_task_session_name);
        String msg_content = m_context.getString(R.string.nim_task_msg_content);
        Glide.with(helper.getConvertView().getContext()).load(R.mipmap.nim_icon_task)
                .into((ImageView) helper.getView(R.id.iv_head));
        UpdateSessionView(helper, session_name, msg_content, true);
        UpdateMsgStatusView(helper, MsgConstDef.MSG_STATUS.INVALID);
        UpdateUnreadView(helper, item, true);
    }

    //更新群助手相关控件
    private void UpdateAssistView(BaseViewHolder helper, SessionModel item)
    {
        String session_name = m_context.getString(R.string.group_assist);
        String session_content;
        int assist_group_count = NIMSessionManager.getInstance().AssistSessionCount();
        if(assist_group_count > 0)
        {
            int unread_count = NIMMsgCountManager.getInstance().
                    GetUnreadCount(GlobalVariable.GROUP_ASSIST_SESSION_ID, MsgConstDef.MSG_CHAT_TYPE.ASSIST);
            if(unread_count > 0)
            {
                session_content = m_context.getString(R.string.nim_assist_msg_content1, unread_count);
            }
            else
            {
                SessionModel assist_model = NIMSessionManager.getInstance().GetASessionByIndex(0);
                GcMessageModel assist_gc_model = NIMGroupMsgManager.getInstance().getLastMessageInfoByGroupID(assist_model.session_id);
                IMGroupInfo a_group_info = NIMGroupInfoManager.getInstance().getGroupInfo(assist_model.session_id);
                if(null != assist_gc_model && a_group_info != null)
                {
                    String gc_msg_content = ChatMsgBuildManager.HandleRichText(assist_gc_model.m_type, assist_gc_model.msg_content, assist_gc_model.s_type);
                    session_content = a_group_info.group_name + ":" + gc_msg_content;
                }
                else
                {
                    session_content = m_context.getString(R.string.nim_assist_msg_content3);
                }
            }
        }
        else
        {
            session_content = m_context.getString(R.string.nim_assist_msg_content3);
        }

        Glide.with(helper.getConvertView().getContext()).load(R.mipmap.nim_icon_group_assist)
                .into((ImageView) helper.getView(R.id.iv_head));

        UpdateSessionView(helper, session_name, session_content, false);
        UpdateMsgStatusView(helper, MsgConstDef.MSG_STATUS.INVALID);
        UpdateUnreadView(helper, item, true);
    }
    @Override
    protected void convert(BaseViewHolder helper, SessionModel item)
    {
        // 当前聊天类型
        switch (item.chat_type)
        {
            // 私聊
            case MsgConstDef.MSG_CHAT_TYPE.PRIVATE:
                {
                    UpdatePrivateView(helper, item);
                }
                break;
            case MsgConstDef.MSG_CHAT_TYPE.GROUP:
                {
                    UpdateGroupView(helper, item);
                }
                break;
            // 当前公众号类型
            case MsgConstDef.MSG_CHAT_TYPE.PUBLIC:
                {
                    UpdatePublicView(helper, item);
                }
                break;
                // 当前系统消息
            case MsgConstDef.MSG_CHAT_TYPE.SYS:
                {
                    UpdateSysView(helper, item);
                }
                break;
                // 当前任务助手
            case MsgConstDef.MSG_CHAT_TYPE.TASK:
                {
                    UpdateTaskView(helper, item);
                }
                break;
            // 当前订阅助手
            case MsgConstDef.MSG_CHAT_TYPE.SUBSCRIBE:
                {
                    UpdateSubscribeView(helper, item);
                }
                break;
            case MsgConstDef.MSG_CHAT_TYPE.ASSIST:
                {
                    UpdateAssistView(helper, item);
                }
                break;
        }

        //公共部分
        if (item.msg_time >  0)
            helper.setText(R.id.msg_center_msg_time, DateUtil.formatSnsDate(item.msg_time));

        if (item.is_top)
        {
            helper.getView(R.id.session_item_layout).setBackgroundResource(R.drawable.nim_session_top_selector);
        }
        else
        {
            helper.getView(R.id.session_item_layout).setBackgroundResource(R.drawable.nim_layout_selector);
        }
    }
}
