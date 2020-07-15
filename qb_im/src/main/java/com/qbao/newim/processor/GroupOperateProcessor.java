package com.qbao.newim.processor;

import com.google.flatbuffers.FlatBufferBuilder;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.constdef.PackTypeDef;
import com.qbao.newim.manager.NIMGroupInfoManager;
import com.qbao.newim.manager.NIMGroupMsgManager;
import com.qbao.newim.manager.NIMGroupUserManager;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.GroupOperateMode;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.model.IMGroupUserInfo;
import com.qbao.newim.model.message.GcMessageModel;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.network.IProcessInterface;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.Logger;

import java.nio.ByteBuffer;

import grouppack.T_GROUP_CREATE_RS;
import grouppack.T_GROUP_LEADER_CHANGE_RS;
import grouppack.T_GROUP_MODIFY_ChANGE_RS;

/**
 * Created by chenjian on 2017/6/22.
 */

public class GroupOperateProcessor extends BaseProcessor{
    private static final String TAG = GroupOperateProcessor.class.getSimpleName();
    private ProcessorGroupCreateRS p_group_create_rs = new ProcessorGroupCreateRS();
    private ProcessorGroupModifyRS p_group_modify_rs = new ProcessorGroupModifyRS();
//    private ProcessorGroupModifyRQ p_group_modify_rq = new ProcessorGroupModifyRQ();
    private ProcessorGroupLeaderChangeRS p_leader_change_rs = new ProcessorGroupLeaderChangeRS();

    public GroupOperateProcessor() {
        Init();
    }

    @Override
    protected void Init() {
        super.Init();
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_GROUP_CREATE_RS, p_group_create_rs);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_GROUP_MODIFY_CHANGE_RS, p_group_modify_rs);
//        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_GROUP_MODIFY_SERVER_RQ, p_group_modify_rq);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_GROUP_LEADER_CHANGE_RS, p_leader_change_rs);
    }

    public boolean sendGroupCreateRQ (GroupOperateMode mode) {
        FlatBufferBuilder bufferBuilder = new FlatBufferBuilder();
        mode.SerializeCreateRQ(bufferBuilder);
        super.SendRQ(PackTypeDef.NEW_DEF_GROUP_CREATE_RQ, bufferBuilder);
        return true;
    }

    public boolean sendGroupModifyRQ(GroupOperateMode mode) {
        FlatBufferBuilder bufferBuilder = new FlatBufferBuilder();
        mode.SerializeModifyRQ(bufferBuilder);
        super.SendRQ(PackTypeDef.NEW_DEF_GROUP_MODIFY_CHANGE_RQ, bufferBuilder);
        return true;
    }

    public boolean sendGroupLeaderChangeRQ(GroupOperateMode mode) {
        FlatBufferBuilder bufferBuilder = new FlatBufferBuilder();
        mode.SerializeLeaderChangeRQ(bufferBuilder);
        super.SendRQ(PackTypeDef.NEW_DEF_GROUP_LEADER_CHANGE_RQ, bufferBuilder);
        return true;
    }

    class ProcessorGroupCreateRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_GROUP_CREATE_RS create_rs = T_GROUP_CREATE_RS.getRootAsT_GROUP_CREATE_RS(byte_buffer);
            if(BodyEmpty(create_rs)) {
                Logger.error(TAG, "T_GROUP_CREATE_RS is null");
                return -1;
            }

            if(!CheckHead(create_rs.sRsHead()))
            {
                Logger.error(TAG, "client get group create rs head is null");
                return -1;
            }

            IMGroupInfo group_info = new IMGroupInfo();
            group_info.UnSerialize(create_rs.groupInfo());

            GcMessageModel operateMode = new GcMessageModel();
            boolean result = operateMode.UnSerializeRS(create_rs.offlineGroupMsg());
            if (!result)
            {
                return -1;
            }

            group_info.isMember = 1;
            operateMode.group_id = group_info.group_id;
            operateMode.group_info = group_info;
            if(group_info == null)
            {
                Logger.error(TAG, "create group failed");
                return -1;
            }

            NIMGroupInfoManager.getInstance().AddGroup(group_info);
            NIMGroupMsgManager.getInstance().addGroupOneMsgInfo(operateMode.group_id, operateMode, false);

            DataObserver.Notify(DataConstDef.EVENT_GROUP_CREATE, operateMode, operateMode.big_msg_type);
            return 1;
        }
    }

    class ProcessorGroupModifyRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_GROUP_MODIFY_ChANGE_RS kick_rs = T_GROUP_MODIFY_ChANGE_RS.getRootAsT_GROUP_MODIFY_ChANGE_RS(byte_buffer);

            if(BodyEmpty(kick_rs))
            {
                Logger.error(TAG, "T_GROUP_Modify_RS is null");
                return -1;
            }

            if(!CheckHead(kick_rs.sRsHead()))
            {
                Logger.error(TAG, "client get group modify rs head is null");
                DataObserver.Notify(DataConstDef.EVENT_GROUP_OPERATE, null,
                        true);
                return -1;
            }

            IMGroupInfo group_info = new IMGroupInfo();
            group_info.UnSerialize(kick_rs.groupInfo());

            GcMessageModel operateMode = new GcMessageModel();
            boolean result = operateMode.UnSerializeRS(kick_rs.offlineGroupMsg());
            if (!result)
            {
                return -1;
            }

            operateMode.group_id = group_info.group_id;
            operateMode.group_info = group_info;

            IMGroupInfo group_exist_info = NIMGroupInfoManager.getInstance().getGroupInfo(operateMode.group_id);
            if(operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_SCAN_ADD_USER)
            {
                group_exist_info = group_info;
            }

            if(group_exist_info == null)
            {
                Logger.error(TAG, "group_id is invalid group_id = " + String.valueOf(operateMode.group_id));
                return -1;
            }
            operateMode.group_info = group_info;

            // 需要修改群信息
            if(operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_ADD_USER ||
                    operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_KICK_USER ||
                    operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_SCANNING ||
                    operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_SCAN_ADD_USER)
            {
                group_exist_info.isMember = 1;
                group_exist_info.group_count = group_info.group_count;

                if(operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_KICK_USER)
                {
                    if(operateMode.user_info_list != null)
                    {
                        for(int index = 0; index < operateMode.user_info_list.size(); index++)
                        {
                            if(NIMUserInfoManager.getInstance().GetSelfUserId() == operateMode.user_info_list.get(index).getUser_id())
                            {
                                group_exist_info.isMember = 0;
                            }
                        }
                    }
                }

                NIMGroupInfoManager.getInstance().AddGroup(group_exist_info);
            }


            if(operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_ENTER_DEFAULT ||
                    operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_ADD_USER_AGREE)
            {
                group_exist_info.group_add_is_agree = group_info.group_add_is_agree;
                NIMGroupInfoManager.getInstance().AddGroup(group_exist_info);
            }

            if(operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_MODIFY_GROUP_NAME)
            {
                group_exist_info.group_name = group_info.group_name;
                NIMGroupInfoManager.getInstance().AddGroup(group_exist_info);
            }

            if(operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_MODIFY_GROUP_REMARK)
            {
                group_exist_info.group_remark = group_info.group_remark;
                NIMGroupInfoManager.getInstance().AddGroup(group_exist_info);
            }

            if(operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_MODIFY_GROUP_USER_NAME)
            {
                IMGroupUserInfo name_Info = NIMGroupUserManager.getInstance().getGroupUserInfo(operateMode.group_id, operateMode.user_id);
                if (name_Info == null)
                {
                    name_Info = new IMGroupUserInfo();
                    name_Info.user_id = operateMode.user_id;
                    name_Info.group_id = operateMode.group_id;
                }

                name_Info.user_nick_name = operateMode.group_modify_content;
                NIMGroupUserManager.getInstance().AddGroupUser(operateMode.group_id, name_Info);
            }


            NIMGroupMsgManager.getInstance().addGroupOneMsgInfo(operateMode.group_id, operateMode, true);

            // 当前群主同意邀请，直接更新在聊天界面
            if (operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_ADD_USER)
            {
                DataObserver.Notify(DataConstDef.EVENT_GROUP_OPERATE, operateMode, false);
                return 1;
            }

            // 当前群主转让，直接更新在聊天界面
            if (operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_LEADER_CHANGE)
            {
                DataObserver.Notify(DataConstDef.EVENT_GROUP_OPERATE, operateMode, false);
                return 1;
            }

            // true表示客户端修改
            DataObserver.Notify(DataConstDef.EVENT_GROUP_OPERATE, operateMode, true);
            return 1;
        }
    }

    class ProcessorGroupLeaderChangeRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_GROUP_LEADER_CHANGE_RS leader_change_rs = T_GROUP_LEADER_CHANGE_RS.getRootAsT_GROUP_LEADER_CHANGE_RS(byte_buffer);

            if(BodyEmpty(leader_change_rs)) {
                Logger.error(TAG, "T_GROUP_LEADER_CHANGE_RS is null");
                return -1;
            }

            if(false == CheckHead(leader_change_rs.sRsHead())) {
                Logger.error(TAG, "client get group leader change rs head is null");

                return -1;
            }
            return 1;

        }
    }
}
