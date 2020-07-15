package com.qbao.newim.model.message;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.model.IMGroupUserInfo;
import com.qbao.newim.util.Logger;

import java.util.ArrayList;
import java.util.List;

import commonpack.USER_BASE_INFO;
import grouppack.T_OFFLINE_GROUP_MSG;

/**
 * Created by qlguoze on 17/9/26.
 */

public class GcMessageModel extends BaseMessageModel
{
    public String primary_key;
    public long group_id = 0;                                          // 群id
    public long user_id = 0;                                           // 发消息的id
    public String send_user_name = "";                                 // 发消息的人名
    public int big_msg_type = 0;                                       // 操作类型
    public long message_old_id = 0;                                    // 在群操作是会有值

    public String operate_user_name = "";
    public String group_modify_content = "";

    public List<IMGroupUserInfo> user_info_list;
    public String str_user_list = "";

    public void GenPrimaryKey()
    {
        this.primary_key = String.format("%d_%d", group_id, message_id);
    }

    public IMGroupInfo group_info;                                      // 群信息不需要存db

    @Override
    public void SerializeRQ(FlatBufferBuilder builder) {
        int send_user_name_offset = builder.createString(this.send_user_name);
        int msg_content_offset = builder.createString(this.msg_content);
        int group_name_offset = 0;

        if (!TextUtils.isEmpty(group_info.group_name)) {
            group_name_offset = builder.createString(this.group_info.group_name);
        }
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
        grouppack.T_GROUP_CLIENT_SEND_MESSAGE_RQ.startT_GROUP_CLIENT_SEND_MESSAGE_RQ(builder);
        grouppack.T_GROUP_CLIENT_SEND_MESSAGE_RQ.addGroupId(builder, this.group_id);
        grouppack.T_GROUP_CLIENT_SEND_MESSAGE_RQ.addMessageId(builder, this.message_id);
        grouppack.T_GROUP_CLIENT_SEND_MESSAGE_RQ.addSMsg(builder, s_msg_offset);
        grouppack.T_GROUP_CLIENT_SEND_MESSAGE_RQ.addGroupName(builder, group_name_offset);

        this.GenPrimaryKey();
    }

    @Override
    public void SerializeRS(FlatBufferBuilder builder) {}

    @Override
    public boolean UnSerializeRQ(Table data) {return false;}

    @Override
    public int compareTo(@NonNull BaseMessageModel o)
    {
        return super.compareTo(o);
    }

    public boolean UnSerializeRS(Table data)
    {
        T_OFFLINE_GROUP_MSG server_data = (T_OFFLINE_GROUP_MSG)data;
        if (server_data == null)
        {
            return false;
        }

        this.message_id = server_data.messageId();                                             // 消息id
        this.user_id = server_data.userId();                                                   // 发送消息用户
        this.big_msg_type = server_data.bigMsgType();                                          // 操作类型
        this.message_old_id = server_data.messageOldId();                                      // 消息id

        if (server_data.groupInfo() != null)
        {
            this.group_id = server_data.groupInfo().groupId();
            this.group_info = new IMGroupInfo();
            this.group_info.UnSerialize(server_data.groupInfo());
        }

        if (server_data.operateGroupMsg() != null)
        {
            this.msg_time = server_data.operateGroupMsg().msgTime();
            this.operate_user_name = server_data.operateGroupMsg().operateUserName();
            this.group_modify_content = server_data.operateGroupMsg().groupModifyContent();
            int length = server_data.operateGroupMsg().userInfoListLength();
            this.user_info_list = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                USER_BASE_INFO t_group_user = server_data.operateGroupMsg().userInfoList(i);
                IMGroupUserInfo group_user = new IMGroupUserInfo();
                group_user.UnSerialize(t_group_user);
                this.user_info_list.add(group_user);
            }

            this.str_user_list = user_info_list.toString();
        }
        else if (server_data.sMsg() != null)
        {
            this.app_id = server_data.sMsg().appId();
            this.session_id = server_data.sMsg().sessionId();
            this.chat_type = server_data.sMsg().chatType();
            this.m_type = server_data.sMsg().mType();
            this.s_type = server_data.sMsg().sType();
            this.ext_type = server_data.sMsg().extType();
            this.msg_content = server_data.sMsg().msgContent();
            this.msg_time = server_data.sMsg().msgTime();
            this.send_user_name = server_data.sMsg().sendUserName();
        }
        else
        {
            Logger.error("GcMessageModel", "UnSerializeRS failed");
            return false;
        }

        this.GenPrimaryKey();

        return true;
    }

    public void setUserIsSelf(long user_id)
    {
        if(this.user_id == user_id)
        {
            this.is_self = true;
            return ;
        }

        this.is_self = false;
    }
}
