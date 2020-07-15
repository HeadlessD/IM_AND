package com.qbao.newim.model.message;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.qbao.newim.constdef.MsgConstDef;

import scpack.T_CHAT_SERVER_SEND_MESSAGE_RQ;
import scpack.T_OFFLINE_MSG;

/**
 * Created by shiyunjie on 2017/9/26.
 */

public class ScMessageModel extends BaseMessageModel
{
    public long opt_user_id = 0;                                       // 聊天对方的id
    public String send_user_name = "";                                 // 聊天对方名字
    @Override
    public void SerializeRQ(FlatBufferBuilder builder)
    {
        int send_user_name_offset = builder.createString(this.send_user_name);
        int msg_content_offset = builder.createString(this.msg_content);
        commonpack.S_MSG.startS_MSG(builder);
        commonpack.S_MSG.addSendUserName(builder, send_user_name_offset);
        commonpack.S_MSG.addAppId(builder, this.app_id);
        commonpack.S_MSG.addChatType(builder, this.chat_type);
        commonpack.S_MSG.addMType(builder, this.m_type);
        commonpack.S_MSG.addSType(builder, this.s_type);
        commonpack.S_MSG.addExtType(builder, this.ext_type);
        commonpack.S_MSG.addMsgContent(builder, msg_content_offset);
        commonpack.S_MSG.addMsgTime(builder, this.msg_time);
        int s_msg_offset = commonpack.S_MSG.endS_MSG(builder);
        scpack.T_CHAT_CLIENT_SEND_MESSAGE_RQ.startT_CHAT_CLIENT_SEND_MESSAGE_RQ(builder);
        scpack.T_CHAT_CLIENT_SEND_MESSAGE_RQ.addOpUserId(builder, this.opt_user_id);
        scpack.T_CHAT_CLIENT_SEND_MESSAGE_RQ.addMessageId(builder, this.message_id);
        scpack.T_CHAT_CLIENT_SEND_MESSAGE_RQ.addSMsg(builder, s_msg_offset);
        scpack.T_CHAT_CLIENT_SEND_MESSAGE_RQ.addBId(builder, this.b_id);
        scpack.T_CHAT_CLIENT_SEND_MESSAGE_RQ.addWId(builder, this.w_id);
        scpack.T_CHAT_CLIENT_SEND_MESSAGE_RQ.addCId(builder, this.c_id);
    }

    @Override
    public void SerializeRS(FlatBufferBuilder builder)
    {
        int send_user_name_offset = builder.createString(this.send_user_name);
        int msg_content_offset = builder.createString(this.msg_content);
        commonpack.S_MSG.startS_MSG(builder);
        commonpack.S_MSG.addSendUserName(builder, send_user_name_offset);
        commonpack.S_MSG.addAppId(builder, this.app_id);
        commonpack.S_MSG.addChatType(builder, this.chat_type);
        commonpack.S_MSG.addMType(builder, this.m_type);
        commonpack.S_MSG.addSType(builder, this.s_type);
        commonpack.S_MSG.addExtType(builder, this.ext_type);
        commonpack.S_MSG.addMsgContent(builder, msg_content_offset);
        commonpack.S_MSG.addMsgTime(builder, this.msg_time);
        int s_msg_offset = commonpack.S_MSG.endS_MSG(builder);
        scpack.T_CHAT_SERVER_SEND_MESSAGE_RS.startT_CHAT_SERVER_SEND_MESSAGE_RS(builder);
        scpack.T_CHAT_SERVER_SEND_MESSAGE_RS.addOpUserId(builder, this.opt_user_id);
        scpack.T_CHAT_SERVER_SEND_MESSAGE_RS.addMessageId(builder, this.message_id);
        scpack.T_CHAT_SERVER_SEND_MESSAGE_RS.addSMsg(builder, s_msg_offset);
        scpack.T_CHAT_SERVER_SEND_MESSAGE_RS.addBId(builder, this.b_id);
        scpack.T_CHAT_SERVER_SEND_MESSAGE_RS.addWId(builder, this.w_id);
        scpack.T_CHAT_SERVER_SEND_MESSAGE_RS.addCId(builder, this.c_id);
    }


    @Override
    public boolean UnSerializeRQ(Table data)
    {
        T_CHAT_SERVER_SEND_MESSAGE_RQ server_data = (T_CHAT_SERVER_SEND_MESSAGE_RQ)data;
        if (server_data.sMsg() == null) {
            return false;
        }

        this.message_id = server_data.messageId();
        this.send_user_name = server_data.sMsg().sendUserName();
        this.opt_user_id = server_data.opUserId();
        this.is_self = false;
        this.b_id = server_data.bId();
        this.w_id = server_data.wId();
        this.c_id = server_data.cId();
        this.app_id = server_data.sMsg().appId();
        this.session_id = server_data.sMsg().sessionId();
        this.chat_type = server_data.sMsg().chatType();
        this.m_type = server_data.sMsg().mType();
        this.s_type = server_data.sMsg().sType();
        this.ext_type = server_data.sMsg().extType();
        this.msg_content = server_data.sMsg().msgContent();
        this.msg_time = server_data.sMsg().msgTime();
        this.msg_status = MsgConstDef.MSG_STATUS.UNREAD;

        return true;
    }

    @Override
    public boolean UnSerializeRS(Table data)
    {
        T_OFFLINE_MSG server_data = (T_OFFLINE_MSG)data;
        if (server_data.sMsg() == null) {
            return false;
        }

        this.message_id = server_data.messageId();
        this.send_user_name = server_data.sMsg().sendUserName();
        this.opt_user_id = server_data.opUserId();
        this.is_self = false;
        this.app_id = server_data.sMsg().appId();
        this.session_id = server_data.sMsg().sessionId();
        this.chat_type = server_data.sMsg().chatType();
        this.m_type = server_data.sMsg().mType();
        this.s_type = server_data.sMsg().sType();
        this.ext_type = server_data.sMsg().extType();
        this.msg_content = server_data.sMsg().msgContent();
        this.msg_time = server_data.sMsg().msgTime();
        this.msg_status = MsgConstDef.MSG_STATUS.UNREAD;

        return true;
    }

    public void CopyFrom(ScMessageModel src)
    {
        super.CopyFrom(src);
        this.opt_user_id = src.opt_user_id;
    }
}
