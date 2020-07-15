package com.qbao.newim.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseViewHolder;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.manager.ChatMsgBuildManager;
import com.qbao.newim.manager.NIMComplexManager;
import com.qbao.newim.manager.NIMGroupMsgManager;
import com.qbao.newim.manager.NIMMsgCountManager;
import com.qbao.newim.model.SessionModel;
import com.qbao.newim.model.message.GcMessageModel;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.DateUtil;

import java.util.HashMap;
import java.util.List;

/**
 * Created by chenjian on 2017/7/10.
 */

public class AssistAdapter extends BaseQuickAdapter<SessionModel, BaseViewHolder> {

    public HashMap<Long, SessionModel> adapter_session = new HashMap();
    Context m_context;
    public AssistAdapter(Context context, List<SessionModel> data)
    {
        super(R.layout.nim_fragment_session, data);
        m_context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, SessionModel item) {
        String session_content = "";
        GcMessageModel gc_m_model = NIMGroupMsgManager.getInstance().getLastMessageInfoByGroupID(item.session_id);
        if (null != gc_m_model)
        {
            session_content = ChatMsgBuildManager.HandleRichText(gc_m_model.m_type, gc_m_model.msg_content, gc_m_model.s_type);
        }


        String session_name = NIMComplexManager.getInstance().GetSessionName(item.session_id, item.chat_type);
        helper.setText(R.id.msg_center_last_msg, session_content);
        helper.setText(R.id.msg_center_friend_name, session_name);

        if (item.is_top) {
            helper.getView(R.id.session_item_layout).setBackgroundResource(R.drawable.nim_session_top_selector);
        } else {
            helper.getView(R.id.session_item_layout).setBackgroundResource(R.drawable.nim_layout_selector);
        }

        Glide.with(helper.getConvertView().getContext()).load(AppUtil.getGroupUrl(item.session_id)).crossFade()
                .placeholder(R.mipmap.nim_head)
                .centerCrop().into((ImageView) helper.getView(R.id.iv_head));

        helper.setText(R.id.msg_center_msg_time, DateUtil.formatSnsDate(item.msg_time));

        int unread_count = NIMMsgCountManager.getInstance().GetUnreadCount(item.session_id, item.chat_type);
        if (unread_count > 0) {
            helper.getView(R.id.msg_center_unread_count).setVisibility(View.VISIBLE);
            if (unread_count > 99) {
                helper.getView(R.id.msg_center_unread_count).setBackgroundResource(R.drawable.nim_blue_max_circle);
                helper.setText(R.id.msg_center_unread_count, m_context.getString(R.string.nim_99_plus));
            } else {
                helper.getView(R.id.msg_center_unread_count).setBackgroundResource(R.drawable.nim_blue_circle);
                helper.setText(R.id.msg_center_unread_count, String.valueOf(unread_count));
            }
        } else {
            helper.getView(R.id.msg_center_unread_count).setVisibility(View.GONE);
        }

        GcMessageModel gc_msg_model = NIMGroupMsgManager.getInstance().getLastMessageInfoByGroupID(item.session_id);
        int msg_status = gc_msg_model == null ? -1 : gc_msg_model.msg_status;
        // 显示发送状态
        switch (msg_status) {
            case MsgConstDef.MSG_STATUS.SENDING:
                helper.getView(R.id.msg_center_send_status).setVisibility(View.VISIBLE);
                helper.setBackgroundRes(R.id.msg_center_send_status, R.mipmap.nim_sending_arrow);
                helper.setText(R.id.msg_center_msg_time, R.string.nim_sending);
                break;
            case MsgConstDef.MSG_STATUS.SEND_SUCCESS:
                helper.getView(R.id.msg_center_send_status).setVisibility(View.GONE);
                break;
            case MsgConstDef.MSG_STATUS.SEND_FAILED:
                helper.getView(R.id.msg_center_send_status).setVisibility(View.VISIBLE);
                helper.setBackgroundRes(R.id.msg_center_send_status, R.mipmap.nim_chat_send_status);
                break;
        }

        if (!adapter_session.containsKey(item.session_id)) {
            adapter_session.put(item.session_id, item);
        }
    }

    public boolean isExist(long session_id) {
        return adapter_session.containsKey(session_id);
    }

    public void removeSession(long session_id) {
        adapter_session.remove(session_id);
    }
}
