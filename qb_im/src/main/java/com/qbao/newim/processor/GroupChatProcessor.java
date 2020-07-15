package com.qbao.newim.processor;

import com.google.flatbuffers.FlatBufferBuilder;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.constdef.PackTypeDef;
import com.qbao.newim.constdef.StateConstDef;
import com.qbao.newim.manager.NIMGroupInfoManager;
import com.qbao.newim.manager.NIMGroupMsgManager;
import com.qbao.newim.manager.NIMGroupUserManager;
import com.qbao.newim.manager.NIMSessionManager;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.IMGetOfflineInfo;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.model.IMGroupUserInfo;
import com.qbao.newim.model.IMOfflineUnreadInfo;
import com.qbao.newim.model.NIM_Chat_ID;
import com.qbao.newim.model.SessionModel;
import com.qbao.newim.model.message.GcMessageModel;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.netcenter.PackSessionMgr;
import com.qbao.newim.network.IProcessInterface;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import grouppack.T_GROUP_ALL_OFFLINE_MSG;
import grouppack.T_GROUP_BASE_REQUEST;
import grouppack.T_GROUP_CHAT_NOTIFY_RQ;
import grouppack.T_GROUP_CLIENT_SEND_MESSAGE_RS;
import grouppack.T_GROUP_GET_OFFLINE_MESSAGE_RQ;
import grouppack.T_GROUP_GET_OFFLINE_MESSAGE_RS;
import grouppack.T_GROUP_MESSAGE_STATUS_RQ;
import grouppack.T_GROUP_MESSAGE_STATUS_RS;
import grouppack.T_OFFLINE_GROUP_MSG;

/**
 * Created by chenjian on 2017/5/17.
 * 群聊收发处理
 */

public class GroupChatProcessor extends BaseProcessor {
    private static final String TAG = "GcProcessor";
    private ProcessGroupChatSendMsgRS p_client_send_rs = new ProcessGroupChatSendMsgRS();
    private ProcessorGroupOfflineMsgRS p_offline_rs = new ProcessorGroupOfflineMsgRS();
    private ProcessorMsgStatusRS p_client_status_rs = new ProcessorMsgStatusRS();
    private ProcessorMsgNotifyRQ p_server_notify_rq = new ProcessorMsgNotifyRQ();
    private HashMap<Long, ArrayList<GcMessageModel>> offline_msg_list = new HashMap<>();
    private ArrayList<IMOfflineUnreadInfo> unread_list = new ArrayList<>();
    private static final byte GROUP_OFFLINE_MSG_NO_FINISH = 1;
    private static final byte GROUP_OFFLINE_MSG_FINISH = 2;

    public GroupChatProcessor() {
        Init();
    }

    @Override
    protected void Init() {
        super.Init();
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_GROUP_CLIENT_SEND_MESSAGE_RS, p_client_send_rs);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_GROUP_MESSAGE_STATUS_RS, p_client_status_rs);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_GROUP_CHAT_NOTIFY_SIMPLE_RQ, p_server_notify_rq);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_GROUP_GET_OFFLINE_MESSAGE_RS, p_offline_rs);
    }

    public boolean GroupChatSendMsgRQ(GcMessageModel message) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        message.SerializeRQ(builder);
        int pack_session_id = super.SendRQ(PackTypeDef.NEW_DEF_GROUP_CLIENT_SEND_MESSAGE_RQ, builder);
        PackSessionMgr.getInstance().AddPackMsg(pack_session_id, new NIM_Chat_ID(message.group_id, message.message_id, message.chat_type));
        return true;
    }

    // 发送消息
    class ProcessGroupChatSendMsgRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_GROUP_CLIENT_SEND_MESSAGE_RS group_msg_rs = T_GROUP_CLIENT_SEND_MESSAGE_RS.
                    getRootAsT_GROUP_CLIENT_SEND_MESSAGE_RS(byte_buffer);
            if (BodyEmpty(group_msg_rs)) {
                Logger.error(TAG, "client send message rs is null");
                return -1;
            }

            long message_id = -1;
            long session_id = -1;
            if(group_msg_rs.sRsHead() != null) {
                int pack_session_id = group_msg_rs.sRsHead().packSessionId();
                NIM_Chat_ID chat_id = PackSessionMgr.getInstance().GetPackMsg(pack_session_id);
                message_id = chat_id.message_id;
                session_id = chat_id.session_id;
                Logger.error("group_msg", "receive message_id" + chat_id.message_id);
                PackSessionMgr.getInstance().DelPackMsg(pack_session_id);
            }

            if(false == CheckHead(group_msg_rs.sRsHead())) {
                NIMGroupMsgManager.getInstance().SetMessageStatus(message_id, session_id,
                        MsgConstDef.MSG_STATUS.SEND_FAILED);
                Logger.error(TAG, "client send message rs head is null");
                return -1;
            }

            int index = NIMGroupMsgManager.getInstance().SetMessageStatus(session_id, message_id,
                    MsgConstDef.MSG_STATUS.SEND_SUCCESS);
            if(index >= 0)
            {
                GcMessageModel gc_model = NIMGroupMsgManager.getInstance().GetMessageByChatID(new NIM_Chat_ID(session_id, index));
                if(null != NetCenter.getInstance().GetNetDelegate())
                    NetCenter.getInstance().GetNetDelegate().ProcessGcMessage(gc_model);
            }
            return 1;
        }
    }

    public boolean sendMsgStatusRQ(long group_id, byte status) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        T_GROUP_MESSAGE_STATUS_RQ.startT_GROUP_MESSAGE_STATUS_RQ(builder);
        T_GROUP_MESSAGE_STATUS_RQ.addGroupId(builder, group_id);
        T_GROUP_MESSAGE_STATUS_RQ.addMessageStatus(builder, status);
        super.SendRQ(PackTypeDef.NEW_DEF_GROUP_MESSAGE_STATUS_RQ, builder);
        return true;
    }

    class ProcessorMsgStatusRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_GROUP_MESSAGE_STATUS_RS message_status_rs = T_GROUP_MESSAGE_STATUS_RS.getRootAsT_GROUP_MESSAGE_STATUS_RS(byte_buffer);

            if(BodyEmpty(message_status_rs)) {
                Logger.error(TAG, "T_GROUP_MESSAGE_STATUS_RS is null");
                return -1;
            }

            if(false == CheckHead(message_status_rs.sRsHead())) {
                Logger.error(TAG, "client get group message status rs head is null");
                return -1;
            }

            IMGroupInfo group_info = NIMGroupInfoManager.getInstance().getGroupInfo(message_status_rs.groupId());
            if (group_info == null)
            {
                Logger.error(TAG, "assist convert group_id is not exsist: " + message_status_rs.groupId());
                return -1;
            }

            group_info.notify_type =  message_status_rs.messageStatus();
            NIMGroupInfoManager.getInstance().AddGroup(group_info);

            SessionModel session_model = new SessionModel();
            session_model.session_id = message_status_rs.groupId();
            session_model.chat_type = MsgConstDef.MSG_CHAT_TYPE.GROUP;

            if (message_status_rs.messageStatus() == MsgConstDef.GROUP_MESSAGE_STATUS.GROUP_MESSAGE_IN_HELP_NO_HIT)
            {
                NIMSessionManager.getInstance().EnterAssist(session_model);
            }
            else
            {
                NIMSessionManager.getInstance().LeaveAssist(session_model);
            }
            DataObserver.Notify(DataConstDef.EVENT_MESSAGE_STATUS, message_status_rs.messageStatus(), message_status_rs.groupId());
            return 1;
        }
    }

    class ProcessorMsgNotifyRQ implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_GROUP_CHAT_NOTIFY_RQ chat_notify_rq = T_GROUP_CHAT_NOTIFY_RQ.getRootAsT_GROUP_CHAT_NOTIFY_RQ(byte_buffer);

            if (BodyEmpty(chat_notify_rq)) {
                Logger.error(TAG, "server send message rq is null");
                return -1;
            }

            long group_id = chat_notify_rq.groupId();
            String group_name = chat_notify_rq.groupName();

            IMGroupInfo groupInfo = NIMGroupInfoManager.getInstance().getGroupInfo(group_id);
            IMGetOfflineInfo[] get_offline_arr = new IMGetOfflineInfo[1];
            long msg_id = 0;
            if (groupInfo != null)
            {
                // 修改为只要收到通知包 就获取群离线消息
                if (groupInfo.last_message_id > 0)
                {
                    msg_id = groupInfo.last_message_id + 1;
                }
            }
            else
            {
                IMGroupInfo new_group = new IMGroupInfo();
                new_group.group_id = group_id;
                new_group.group_name = group_name;
                NIMGroupInfoManager.getInstance().AddGroup(new_group);
            }

            get_offline_arr[0] = new IMGetOfflineInfo(group_id, msg_id);
            sendGroupOfflineMsg(get_offline_arr);

            return 1;
        }
    }

    public boolean sendGroupOfflineMsg(IMGetOfflineInfo[] group_arr) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int[] group_arr_offset = new int[group_arr.length];
        for (int i = 0; i < group_arr.length; i++) {
            T_GROUP_BASE_REQUEST.startT_GROUP_BASE_REQUEST(builder);
            T_GROUP_BASE_REQUEST.addGroupId(builder, group_arr[i].group_id);
            T_GROUP_BASE_REQUEST.addNextMessageId(builder, group_arr[i].last_message_id);
            group_arr_offset[i] = T_GROUP_BASE_REQUEST.endT_GROUP_BASE_REQUEST(builder);

        }
        int group_list_offset = T_GROUP_GET_OFFLINE_MESSAGE_RQ.createListGroupOfflineMsgRequestVector(builder, group_arr_offset);
        T_GROUP_GET_OFFLINE_MESSAGE_RQ.startT_GROUP_GET_OFFLINE_MESSAGE_RQ(builder);
        T_GROUP_GET_OFFLINE_MESSAGE_RQ.addListGroupOfflineMsgRequest(builder, group_list_offset);
        super.SendRQ(PackTypeDef.NEW_DEF_GROUP_GET_OFFLINE_MESSAGE_RQ, builder);
        return true;
    }

    class ProcessorGroupOfflineMsgRS implements IProcessInterface{
        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_GROUP_GET_OFFLINE_MESSAGE_RS get_offline_message_rs = T_GROUP_GET_OFFLINE_MESSAGE_RS.
                    getRootAsT_GROUP_GET_OFFLINE_MESSAGE_RS(byte_buffer);

            if(BodyEmpty(get_offline_message_rs))
            {
                Logger.error(TAG, "T_GROUP_GET_OFFLINE_MESSAGE_RS is null");
                return -1;
            }

            if(!CheckHead(get_offline_message_rs.sRsHead()))
            {
                Logger.error(TAG, "client get group offline msg rs head is null");
                return -1;
            }

            int length = get_offline_message_rs.listGroupOfflineMsgResponseLength();
            ArrayList<IMGetOfflineInfo> next_list = new ArrayList<>();

            for (int i = 0; i < length; i++)
            {
                long next_msg_id = 0;
                T_GROUP_ALL_OFFLINE_MSG all_msg = get_offline_message_rs.listGroupOfflineMsgResponse(i);
                long group_id = all_msg.groupId();
                byte is_finish = all_msg.isFinish();
                int msg_length = all_msg.sOfflineMsgListLength();

                // 正常情况msg_length 肯定是大于0的
                // 1.保存离线消息到内存 2.记录next_message_id
                for (int j = 0; j < msg_length; j++)
                {
                    T_OFFLINE_GROUP_MSG single_msg = all_msg.sOfflineMsgList(j);
                    GcMessageModel message_info = new GcMessageModel();
                    message_info.UnSerializeRS(single_msg);
                    message_info.group_id = group_id;

                    if (offline_msg_list.containsKey(group_id))
                    {
                        offline_msg_list.get(group_id).add(message_info);
                    }
                    else
                    {
                        ArrayList<GcMessageModel> model_list = new ArrayList<>();
                        model_list.add(message_info);
                        offline_msg_list.put(group_id, model_list);
                    }

                    if (j == msg_length - 1)
                    {
                        next_msg_id = message_info.message_id;
                    }
                }

                // 如果消息获取完成 直接保存到管理类
                if (is_finish == GROUP_OFFLINE_MSG_FINISH)
                {
                    if (offline_msg_list.containsKey(group_id))
                    {
                        // 需要添加群操作处理
                        ArrayList<GcMessageModel> list_message_info = offline_msg_list.get(group_id);
                        for(int index_check = 0; index_check < list_message_info.size(); index_check++)
                        {
                            GcMessageModel operateMode = list_message_info.get(index_check);
                            IMGroupInfo group_info = operateMode.group_info;
                            if(operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_CREATE)
                            {
                                NIMGroupInfoManager.getInstance().AddGroup(group_info);
                                continue;
                            }

                            IMGroupInfo group_exist_info = NIMGroupInfoManager.getInstance().getGroupInfo(operateMode.group_id);
                            if(group_exist_info == null)
                            {
                                Logger.error(TAG, "group_id is invalid group_id = " + String.valueOf(operateMode.group_id));
                                continue;
                            }

                            // 需要修改群信息
                            if(operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_ADD_USER ||
                                    operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_KICK_USER ||
                                    operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_SCANNING ||
                                    operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_SCAN_ADD_USER)
                            {
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
                        }

                        NIMGroupMsgManager.getInstance().addGroupListMsgInfo(group_id,
                                offline_msg_list.get(group_id), false,
                                NIMGroupInfoManager.getInstance().checkIsInAssist(group_id));
                    }
                }
                // 未完成需要添加到列表 继续获取
                else if (is_finish == GROUP_OFFLINE_MSG_NO_FINISH)
                {
                    if (next_msg_id > 0)
                    {
                        IMGetOfflineInfo next_info = new IMGetOfflineInfo(group_id, next_msg_id + 1);
                        next_list.add(next_info);
                    }
                }
            }

            // 如果size 为0 则说明获取完成
            int size = next_list.size();
            if (size == 0)
            {
                unread_list.clear();
                offline_msg_list.clear();

                //通知状态机状态完成
                DataObserver.Notify(DataConstDef.EVENT_STATE_MACHINE_FINISH, StateConstDef.GC_OFFLINE_MSG, null);
                return 0;
            }

            IMGetOfflineInfo[] next_arr = next_list.toArray(new IMGetOfflineInfo[size]);
            sendGroupOfflineMsg(next_arr);

            return 1;
        }
    }
}
