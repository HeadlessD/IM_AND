package com.qbao.newim.model;

import com.google.flatbuffers.FlatBufferBuilder;

import java.util.ArrayList;
import java.util.List;

import commonpack.USER_BASE_INFO;
import grouppack.T_GROUP_CREATE_RQ;
import grouppack.T_GROUP_CREATE_RS;
import grouppack.T_GROUP_LEADER_CHANGE_RQ;
import grouppack.T_GROUP_LEADER_CHANGE_RS;
import grouppack.T_GROUP_MODIFY_ChANGE_RQ;
import grouppack.T_GROUP_MODIFY_ChANGE_RS;
import grouppack.T_OFFLINE_GROUP_MSG;
import grouppack.T_OPERATE_GROUP_MSG;

/**
 * Created by chenjian on 2017/6/21.
 */

public class GroupOperateMode {
    public long group_id;
    public String group_name = "";
    public String group_img_url = "";
    public String group_remark = "";
    public long group_ct;
    public long user_id;
    public long message_id;
    public int big_msg_type;
    public long message_old_id;
    public long msg_time;
    public String operate_user_name = "";
    public String group_modify_content = "";
    public short app_id;
    public long session_id;
    public short chat_type;
    public int m_type;
    public int s_type;
    public int ext_type;
    public String msg_content = "";
    public String send_user_name = "";
    public List<IMGroupUserInfo> user_info_list;
    public int group_count;
    public long group_manager_user_id;
    public byte group_add_is_agree;
    public int group_max_count;
    public int group_type;
    public byte message_status;
    public int group_add_max_count;

    public void SerializeCreateRQ(FlatBufferBuilder builder) {
        int group_name_offset = builder.createString(this.group_name);
        int group_img_offset = builder.createString(this.group_img_url);
        int group_remark_offset = builder.createString(this.group_remark);
        int operate_name_offset = builder.createString(this.operate_user_name);
        int group_modify_offset = builder.createString(this.group_modify_content);
        int msg_content_offset = builder.createString(this.msg_content);
        int send_name_offset = builder.createString(this.send_user_name);

        // 里层消息结构体
        commonpack.S_MSG.startS_MSG(builder);
        commonpack.S_MSG.addAppId(builder, this.app_id);
        commonpack.S_MSG.addSendUserName(builder, send_name_offset);
        commonpack.S_MSG.addChatType(builder, this.chat_type);
        commonpack.S_MSG.addMType(builder, this.m_type);
        commonpack.S_MSG.addSType(builder, this.s_type);
        commonpack.S_MSG.addExtType(builder, this.ext_type);
        commonpack.S_MSG.addMsgContent(builder, msg_content_offset);
        commonpack.S_MSG.addMsgTime(builder, this.msg_time);
        int s_msg_offset = commonpack.S_MSG.endS_MSG(builder);

        // 里层群用户结构体
        int user_list_offset = 0;
        if (user_info_list != null) {
            int nSize = user_info_list.size();
            int[] user_offset = new int[nSize];
            for (int i = 0; i < nSize; i++) {
                // 传群用户
                IMGroupUserInfo userInfo = user_info_list.get(i);
                int user_nick_offset = builder.createString(userInfo.user_nick_name);
                USER_BASE_INFO.startUSER_BASE_INFO(builder);
                USER_BASE_INFO.addUserNickName(builder, user_nick_offset);
                USER_BASE_INFO.addUserGroupIndex(builder, userInfo.user_group_index);
                USER_BASE_INFO.addUserId(builder, userInfo.user_id);
                user_offset[i] = USER_BASE_INFO.endUSER_BASE_INFO(builder);
            }
            user_list_offset = T_OPERATE_GROUP_MSG.createUserInfoListVector(builder, user_offset);
        }

        T_OPERATE_GROUP_MSG.startT_OPERATE_GROUP_MSG(builder);
        T_OPERATE_GROUP_MSG.addMsgTime(builder, this.msg_time);
        T_OPERATE_GROUP_MSG.addGroupModifyContent(builder, group_modify_offset);
        T_OPERATE_GROUP_MSG.addOperateUserName(builder, operate_name_offset);
        T_OPERATE_GROUP_MSG.addUserInfoList(builder, user_list_offset);
        int operate_offset = T_OPERATE_GROUP_MSG.endT_OPERATE_GROUP_MSG(builder);

        // 中间层操作结构体
        T_OFFLINE_GROUP_MSG.startT_OFFLINE_GROUP_MSG(builder);
        T_OFFLINE_GROUP_MSG.addSMsg(builder, s_msg_offset);
        T_OFFLINE_GROUP_MSG.addUserId(builder, this.user_id);
        T_OFFLINE_GROUP_MSG.addMessageId(builder, this.message_id);
        T_OFFLINE_GROUP_MSG.addMessageOldId(builder, this.message_old_id);
        T_OFFLINE_GROUP_MSG.addBigMsgType(builder, this.big_msg_type);
        T_OFFLINE_GROUP_MSG.addOperateGroupMsg(builder, operate_offset);
        int group_offset = T_OFFLINE_GROUP_MSG.endT_OFFLINE_GROUP_MSG(builder);

        // 最外层群操作结构体
        T_GROUP_CREATE_RQ.startT_GROUP_CREATE_RQ(builder);
        T_GROUP_CREATE_RQ.addGroupCt(builder, this.group_ct);
        T_GROUP_CREATE_RQ.addGroupImgUrl(builder, group_img_offset);
        T_GROUP_CREATE_RQ.addGroupName(builder, group_name_offset);
        T_GROUP_CREATE_RQ.addGroupRemark(builder, group_remark_offset);
        T_GROUP_CREATE_RQ.addGroupType(builder, this.group_type);
        T_GROUP_CREATE_RQ.addOfflineGroupMsg(builder, group_offset);
    }

    public void SerializeLeaderChangeRQ(FlatBufferBuilder builder) {
        int operate_name_offset = builder.createString(this.operate_user_name);
        int group_modify_offset = builder.createString(this.group_modify_content);
        int msg_content_offset = builder.createString(this.msg_content);
        int send_name_offset = builder.createString(this.send_user_name);

        // 里层消息结构体
        commonpack.S_MSG.startS_MSG(builder);
        commonpack.S_MSG.addAppId(builder, this.app_id);
        commonpack.S_MSG.addSendUserName(builder, send_name_offset);
        commonpack.S_MSG.addChatType(builder, this.chat_type);
        commonpack.S_MSG.addMType(builder, this.m_type);
        commonpack.S_MSG.addSType(builder, this.s_type);
        commonpack.S_MSG.addExtType(builder, this.ext_type);
        commonpack.S_MSG.addMsgContent(builder, msg_content_offset);
        commonpack.S_MSG.addMsgTime(builder, this.msg_time);
        int s_msg_offset = commonpack.S_MSG.endS_MSG(builder);

        // 里层群用户结构体
        int user_list_offset = 0;
        if (user_info_list != null) {
            int nSize = user_info_list.size();
            int[] user_offset = new int[nSize];
            for (int i = 0; i < nSize; i++) {
                // 传群用户
                IMGroupUserInfo userInfo = user_info_list.get(i);
                int user_nick_offset = builder.createString(userInfo.user_nick_name);
                USER_BASE_INFO.startUSER_BASE_INFO(builder);
                USER_BASE_INFO.addUserNickName(builder, user_nick_offset);
                USER_BASE_INFO.addUserGroupIndex(builder, userInfo.user_group_index);
                USER_BASE_INFO.addUserId(builder, userInfo.user_id);
                user_offset[i] = USER_BASE_INFO.endUSER_BASE_INFO(builder);
            }
            user_list_offset = T_OPERATE_GROUP_MSG.createUserInfoListVector(builder, user_offset);
        }

        T_OPERATE_GROUP_MSG.startT_OPERATE_GROUP_MSG(builder);
        T_OPERATE_GROUP_MSG.addMsgTime(builder, this.msg_time);
        T_OPERATE_GROUP_MSG.addGroupModifyContent(builder, group_modify_offset);
        T_OPERATE_GROUP_MSG.addOperateUserName(builder, operate_name_offset);
        T_OPERATE_GROUP_MSG.addUserInfoList(builder, user_list_offset);
        int operate_offset = T_OPERATE_GROUP_MSG.endT_OPERATE_GROUP_MSG(builder);

        // 中间层操作结构体
        T_OFFLINE_GROUP_MSG.startT_OFFLINE_GROUP_MSG(builder);
        T_OFFLINE_GROUP_MSG.addSMsg(builder, s_msg_offset);
        T_OFFLINE_GROUP_MSG.addUserId(builder, this.user_id);
        T_OFFLINE_GROUP_MSG.addMessageId(builder, this.message_id);
        T_OFFLINE_GROUP_MSG.addMessageOldId(builder, this.message_old_id);
        T_OFFLINE_GROUP_MSG.addBigMsgType(builder, this.big_msg_type);
        T_OFFLINE_GROUP_MSG.addOperateGroupMsg(builder, operate_offset);
        int group_offset = T_OFFLINE_GROUP_MSG.endT_OFFLINE_GROUP_MSG(builder);

        // 最外层群操作结构体
        T_GROUP_LEADER_CHANGE_RQ.startT_GROUP_LEADER_CHANGE_RQ(builder);
        T_GROUP_LEADER_CHANGE_RQ.addGroupId(builder, this.group_id);
        T_GROUP_LEADER_CHANGE_RQ.addOfflineGroupMsg(builder, group_offset);
    }

    public boolean UnSerializeLeaderChangeRS(T_GROUP_LEADER_CHANGE_RS data) {
        if (data == null) {
            return false;
        }
        this.group_id = data.groupId();
        this.user_id = data.offlineGroupMsg().userId();
        this.message_id = data.offlineGroupMsg().messageId();
        this.big_msg_type = data.offlineGroupMsg().bigMsgType();
        this.message_old_id = data.offlineGroupMsg().messageOldId();

        if (data.offlineGroupMsg().sMsg() != null) {
            this.app_id = data.offlineGroupMsg().sMsg().appId();
            this.session_id = data.offlineGroupMsg().sMsg().sessionId();
            this.chat_type = data.offlineGroupMsg().sMsg().chatType();
            this.m_type = data.offlineGroupMsg().sMsg().mType();
            this.s_type = data.offlineGroupMsg().sMsg().sType();
            this.ext_type = data.offlineGroupMsg().sMsg().extType();
            this.msg_content = data.offlineGroupMsg().sMsg().msgContent();
            this.send_user_name = data.offlineGroupMsg().sMsg().sendUserName();
        }

        this.operate_user_name = data.offlineGroupMsg().operateGroupMsg().operateUserName();
        this.group_modify_content = data.offlineGroupMsg().operateGroupMsg().groupModifyContent();
        this.msg_time = data.offlineGroupMsg().operateGroupMsg().msgTime();
        int length = data.offlineGroupMsg().operateGroupMsg().userInfoListLength();
        this.user_info_list = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            USER_BASE_INFO t_group_user = data.offlineGroupMsg().operateGroupMsg().userInfoList(i);
            IMGroupUserInfo group_user = new IMGroupUserInfo();
            group_user.UnSerialize(t_group_user);
            this.user_info_list.add(group_user);
        }

        return true;
    }

    public boolean UnSerializeCreateRS(T_GROUP_CREATE_RS data) {
        if (data == null) {
            return false;
        }
        this.group_name = data.groupInfo().groupName();
        this.group_manager_user_id = data.groupInfo().groupManagerUserId();
        this.group_ct = data.groupInfo().groupCt();
        this.group_add_is_agree = data.groupInfo().groupAddIsAgree();
        this.group_count = data.groupInfo().groupCount();
        this.group_max_count = data.groupInfo().groupMaxCount();
        this.group_img_url= data.groupInfo().groupImgUrl();
        this.group_remark = data.groupInfo().groupRemark();
        this.group_id = data.groupInfo().groupId();
        this.group_type = data.groupType();
        this.group_add_max_count = data.groupInfo().groupAddMaxCount();
        this.message_status = data.groupInfo().messageStatus();

        this.user_id = data.offlineGroupMsg().userId();
        this.message_id = data.offlineGroupMsg().messageId();
        this.big_msg_type = data.offlineGroupMsg().bigMsgType();
        this.message_old_id = data.offlineGroupMsg().messageOldId();

        if (data.offlineGroupMsg().sMsg() != null) {
            this.app_id = data.offlineGroupMsg().sMsg().appId();
            this.session_id = data.offlineGroupMsg().sMsg().sessionId();
            this.chat_type = data.offlineGroupMsg().sMsg().chatType();
            this.m_type = data.offlineGroupMsg().sMsg().mType();
            this.s_type = data.offlineGroupMsg().sMsg().sType();
            this.ext_type = data.offlineGroupMsg().sMsg().extType();
            this.msg_content = data.offlineGroupMsg().sMsg().msgContent();
            this.send_user_name = data.offlineGroupMsg().sMsg().sendUserName();
        }

        this.operate_user_name = data.offlineGroupMsg().operateGroupMsg().operateUserName();
        this.group_modify_content = data.offlineGroupMsg().operateGroupMsg().groupModifyContent();
        this.msg_time = data.offlineGroupMsg().operateGroupMsg().msgTime();
        int length = data.offlineGroupMsg().operateGroupMsg().userInfoListLength();
        this.user_info_list = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            USER_BASE_INFO t_group_user = data.offlineGroupMsg().operateGroupMsg().userInfoList(i);
            IMGroupUserInfo group_user = new IMGroupUserInfo();
            group_user.UnSerialize(t_group_user);
            this.user_info_list.add(group_user);
        }

        return true;
    }

    public void SerializeModifyRQ(FlatBufferBuilder builder) {
        int operate_name_offset = builder.createString(this.operate_user_name);
        int group_modify_offset = builder.createString(this.group_modify_content);

        // 里层群用户结构体
        int user_list_offset = 0;
        if (user_info_list != null) {
            int nSize = user_info_list.size();
            int[] user_offset = new int[nSize];
            for (int i = 0; i < nSize; i++) {
                // 传群用户
                IMGroupUserInfo userInfo = user_info_list.get(i);
                int user_nick_offset = builder.createString(userInfo.user_nick_name);
                USER_BASE_INFO.startUSER_BASE_INFO(builder);
                USER_BASE_INFO.addUserNickName(builder, user_nick_offset);
                USER_BASE_INFO.addUserGroupIndex(builder, userInfo.user_group_index);
                USER_BASE_INFO.addUserId(builder, userInfo.user_id);
                user_offset[i] = USER_BASE_INFO.endUSER_BASE_INFO(builder);
            }
            user_list_offset = T_OPERATE_GROUP_MSG.createUserInfoListVector(builder, user_offset);
        }

        T_OPERATE_GROUP_MSG.startT_OPERATE_GROUP_MSG(builder);
        T_OPERATE_GROUP_MSG.addMsgTime(builder, this.msg_time);
        T_OPERATE_GROUP_MSG.addOperateUserName(builder, operate_name_offset);
        T_OPERATE_GROUP_MSG.addGroupModifyContent(builder, group_modify_offset);
        T_OPERATE_GROUP_MSG.addUserInfoList(builder, user_list_offset);
        int operate_offset = T_OPERATE_GROUP_MSG.endT_OPERATE_GROUP_MSG(builder);

        // 中间层操作结构体
        T_OFFLINE_GROUP_MSG.startT_OFFLINE_GROUP_MSG(builder);
        T_OFFLINE_GROUP_MSG.addUserId(builder, this.user_id);
        T_OFFLINE_GROUP_MSG.addMessageId(builder, this.message_id);
        T_OFFLINE_GROUP_MSG.addMessageOldId(builder, this.message_old_id);
        T_OFFLINE_GROUP_MSG.addBigMsgType(builder, this.big_msg_type);
        T_OFFLINE_GROUP_MSG.addOperateGroupMsg(builder, operate_offset);
        int group_offset = T_OFFLINE_GROUP_MSG.endT_OFFLINE_GROUP_MSG(builder);

        // 最外层群操作结构体
        T_GROUP_MODIFY_ChANGE_RQ.startT_GROUP_MODIFY_ChANGE_RQ(builder);
        T_GROUP_MODIFY_ChANGE_RQ.addGroupId(builder, this.group_id);
        T_GROUP_MODIFY_ChANGE_RQ.addOfflineGroupMsg(builder, group_offset);
    }

    public boolean UnSerializeModifyRS(T_GROUP_MODIFY_ChANGE_RS data) {
        if (data == null) {
            return false;
        }

        this.group_name = data.groupInfo().groupName();
        this.group_id = data.groupInfo().groupId();
        this.group_img_url = data.groupInfo().groupImgUrl();
        this.group_count = data.groupInfo().groupCount();
        this.group_manager_user_id = data.groupInfo().groupManagerUserId();
        this.group_remark = data.groupInfo().groupRemark();
        this.group_max_count = data.groupInfo().groupMaxCount();
        this.message_status = data.groupInfo().messageStatus();
        this.group_add_max_count = data.groupInfo().groupAddMaxCount();
        this.message_id = data.offlineGroupMsg().messageId();
        this.user_id = data.offlineGroupMsg().userId();
        this.group_add_is_agree = data.groupInfo().groupAddIsAgree();
        this.big_msg_type = data.offlineGroupMsg().bigMsgType();
        this.msg_time = data.offlineGroupMsg().operateGroupMsg().msgTime();
        this.operate_user_name = data.offlineGroupMsg().operateGroupMsg().operateUserName();
        this.group_modify_content = data.offlineGroupMsg().operateGroupMsg().groupModifyContent();
        this.message_old_id = data.offlineGroupMsg().messageOldId();

        int length = data.offlineGroupMsg().operateGroupMsg().userInfoListLength();
        this.user_info_list = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            USER_BASE_INFO t_group_user = data.offlineGroupMsg().operateGroupMsg().userInfoList(i);
            IMGroupUserInfo group_user = new IMGroupUserInfo();
            group_user.UnSerialize(t_group_user);
            this.user_info_list.add(group_user);
        }

        return true;
    }

    public boolean UnSerializeOfflineRQ(T_OFFLINE_GROUP_MSG data) {
        if (data == null) {
            return false;
        }

        this.message_id = data.messageId();
        this.user_id = data.userId();
        this.big_msg_type = data.bigMsgType();

        if (data.groupInfo() != null) {
            this.group_add_is_agree = data.groupInfo().groupAddIsAgree();
            this.group_add_max_count = data.groupInfo().groupAddMaxCount();
            this.group_id = data.groupInfo().groupId();
            this.group_name = data.groupInfo().groupName();
            this.group_img_url = data.groupInfo().groupImgUrl();
            this.group_count = data.groupInfo().groupCount();
            this.group_manager_user_id = data.groupInfo().groupManagerUserId();
            this.group_ct = data.groupInfo().groupCt();
            this.group_remark = data.groupInfo().groupRemark();
            this.group_max_count = data.groupInfo().groupMaxCount();
            this.message_status = data.groupInfo().messageStatus();
        }

        if (data.operateGroupMsg() != null) {
            this.operate_user_name = data.operateGroupMsg().operateUserName();
            this.group_modify_content = data.operateGroupMsg().groupModifyContent();
            int length = data.operateGroupMsg().userInfoListLength();
            this.user_info_list = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                USER_BASE_INFO t_group_user = data.operateGroupMsg().userInfoList(i);
                IMGroupUserInfo group_user = new IMGroupUserInfo();
                group_user.UnSerialize(t_group_user);
                this.user_info_list.add(group_user);
            }
        }

        if (data.sMsg() != null) {
            this.app_id = data.sMsg().appId();
            this.session_id = data.sMsg().sessionId();
            this.chat_type = data.sMsg().chatType();
            this.m_type = data.sMsg().mType();
            this.s_type = data.sMsg().sType();
            this.ext_type = data.sMsg().extType();
            this.msg_content = data.sMsg().msgContent();
            this.msg_time = data.sMsg().msgTime();
            this.send_user_name = data.sMsg().sendUserName();
        } else {
            this.msg_time = data.operateGroupMsg().msgTime();
        }

        return true;
    }

    public IMGroupInfo createGroupInfo() {
        IMGroupInfo groupInfo = new IMGroupInfo();
        groupInfo.group_name = this.group_name;
        groupInfo.group_remark = this.group_remark;
        groupInfo.group_id = this.group_id;
        groupInfo.group_add_is_agree = this.group_add_is_agree;
        groupInfo.group_ct = this.group_ct;
        groupInfo.group_img_url = this.group_img_url;
        groupInfo.group_count = this.group_count;
        groupInfo.group_max_count = this.group_max_count;
        groupInfo.group_manager_user_id = this.group_manager_user_id;
        groupInfo.group_add_max_count = this.group_add_max_count;
        groupInfo.notify_type = this.message_status;
        groupInfo.last_message_id = this.message_id;
        return groupInfo;
    }

    public IMGroupInfo updateGroupInfo(IMGroupInfo groupInfo) {
        groupInfo.group_remark = this.group_remark;
        groupInfo.group_add_is_agree = this.group_add_is_agree;
        groupInfo.group_ct = this.group_ct;
        groupInfo.group_img_url = this.group_img_url;
        groupInfo.group_count = this.group_count;
        groupInfo.group_max_count = this.group_max_count;
        groupInfo.group_manager_user_id = this.group_manager_user_id;
        groupInfo.group_add_max_count = this.group_add_max_count;
        groupInfo.notify_type = this.message_status;
        groupInfo.last_message_id = this.message_id;
        return groupInfo;
    }
}
