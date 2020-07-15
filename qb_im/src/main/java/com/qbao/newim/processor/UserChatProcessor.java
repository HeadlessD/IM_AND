package com.qbao.newim.processor;

import com.google.flatbuffers.FlatBufferBuilder;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.constdef.PackTypeDef;
import com.qbao.newim.constdef.StateConstDef;
import com.qbao.newim.manager.ChatMsgBuildManager;
import com.qbao.newim.manager.NIMFriendInfoManager;
import com.qbao.newim.manager.NIMMsgManager;
import com.qbao.newim.model.IMBusinessInfo;
import com.qbao.newim.model.IMFriendInfo;

import com.qbao.newim.model.NIM_Chat_ID;
import com.qbao.newim.model.message.ScMessageModel;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.netcenter.PackSessionMgr;
import com.qbao.newim.network.IProcessInterface;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import ecpack.T_EC_GETFREEWAITER_RQ;
import ecpack.T_EC_GETFREEWAITER_RS;
import scpack.T_CHAT_CLIENT_SEND_MESSAGE_RS;
import scpack.T_CHAT_GET_OFFLINE_MESSAGE_RQ;
import scpack.T_CHAT_GET_OFFLINE_MESSAGE_RS;
import scpack.T_CHAT_SERVER_SEND_MESSAGE_RQ;
import scpack.T_OFFLINE_MSG;

/**
 * Created by shiyunjie on 17/3/2.
 */

public class UserChatProcessor extends BaseProcessor {
    private static final String TAG = "ScProcessor";

    private ProcessChatClientSendMessageRS p_client_send_rs = new ProcessChatClientSendMessageRS();
    private ProcessChatServerSendMessageRQ p_server_send_rq = new ProcessChatServerSendMessageRQ();
    private ProcessGetOfflineMsgRS p_client_get_offline_rs = new ProcessGetOfflineMsgRS();
    private ProcessGetFreeWaiterRS p_client_business_rs = new ProcessGetFreeWaiterRS();
    private long offline_msg_count;

    public UserChatProcessor() {
        Init();
    }

    @Override
    protected void Init() {
        super.Init();
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_CHAT_CLIENT_SEND_MESSAGE_RS, p_client_send_rs);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_CHAT_SERVER_SEND_MESSAGE_RQ, p_server_send_rq);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_CHAT_GET_OFFLINE_MESSAGE_RS, p_client_get_offline_rs);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_BUSINESS_GETFREEWAITER_RS, p_client_business_rs);
    }

    @Override
    protected void ProcessEvent() {
    }

    //主动发消息
    public boolean ChatClientSendMessageRQ(ScMessageModel message_model) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        message_model.SerializeRQ(builder);
        int pack_session_id = super.SendRQ(PackTypeDef.NEW_DEF_CHAT_CLIENT_SEND_MESSAGE_RQ, builder);
        PackSessionMgr.getInstance().AddPackMsg(pack_session_id, new NIM_Chat_ID(message_model.opt_user_id,
                message_model.message_id, message_model.chat_type));
        return true;
    }

    // 主动获取离线消息
    public boolean SendOfflineMsgRQ() {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        T_CHAT_GET_OFFLINE_MESSAGE_RQ.startT_CHAT_GET_OFFLINE_MESSAGE_RQ(builder);
        T_CHAT_GET_OFFLINE_MESSAGE_RQ.addNextMessageId(builder, offline_msg_count);
        super.SendRQ(PackTypeDef.NEW_DEF_CHAT_GET_OFFLINE_MESSAGE_RQ, builder);
        return true;
    }

    // 主动获取商家小二信息
    public boolean getBusinessInfo(long bid, long[] wid) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int wid_offset = T_EC_GETFREEWAITER_RQ.createWIdListVector(builder, wid);
        T_EC_GETFREEWAITER_RQ.startT_EC_GETFREEWAITER_RQ(builder);
        T_EC_GETFREEWAITER_RQ.addBId(builder, bid);
        T_EC_GETFREEWAITER_RQ.addWIdList(builder, wid_offset);
        super.SendRQ(PackTypeDef.NEW_DEF_BUSINESS_GETFREEWAITER_RQ, builder);
        return true;
    }

    class ProcessChatClientSendMessageRS implements IProcessInterface {
        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_CHAT_CLIENT_SEND_MESSAGE_RS message_rs =
                    T_CHAT_CLIENT_SEND_MESSAGE_RS.getRootAsT_CHAT_CLIENT_SEND_MESSAGE_RS(byte_buffer);
            if (BodyEmpty(message_rs)) {
                Logger.error(TAG, "client send message rs is null");
                return -1;
            }

            long message_id = -1;
            long session_id = -1;
            if (message_rs.sRsHead() != null) {
                int pack_session_id = message_rs.sRsHead().packSessionId();
                NIM_Chat_ID chat_id = PackSessionMgr.getInstance().GetPackMsg(pack_session_id);
                message_id = chat_id.message_id;
                session_id = chat_id.session_id;
                PackSessionMgr.getInstance().DelPackMsg(pack_session_id);
            }

            if (false == CheckHead(message_rs.sRsHead())) {
                NIMMsgManager.getInstance().SetMessageStatus(message_id, session_id,
                        MsgConstDef.MSG_STATUS.SEND_FAILED);
                Logger.error(TAG, "client send message rs head is null");
                return -1;
            }

            int index = NIMMsgManager.getInstance().SetMessageStatus(session_id, message_id,
                    MsgConstDef.MSG_STATUS.SEND_SUCCESS);
            if(index >= 0)
            {
                ScMessageModel sc_model = NIMMsgManager.getInstance().GetMessage(new NIM_Chat_ID(session_id, index));
                if(null != NetCenter.getInstance().GetNetDelegate())
                    NetCenter.getInstance().GetNetDelegate().ProcessScMessage(sc_model);
            }
            return 1;
        }
    }


    //被动收消息
    class ProcessChatServerSendMessageRQ implements IProcessInterface {
        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            Logger.info("SysProcessor", "ProcessChatServerSendMessageRQ");
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_CHAT_SERVER_SEND_MESSAGE_RQ message_rq =
                    T_CHAT_SERVER_SEND_MESSAGE_RQ.getRootAsT_CHAT_SERVER_SEND_MESSAGE_RQ(byte_buffer);
            if (BodyEmpty(message_rq)) {
                Logger.error(TAG, "server send message rq is null");
                return -1;
            }

            if (null == message_rq.sRqHead()) {
                Logger.error(TAG, "server send message rq head is null");
                return -1;
            }

            ScMessageModel message_model = new ScMessageModel();
            message_model.UnSerializeRQ(message_rq);
            message_model.msg_status = MsgConstDef.MSG_STATUS.UNREAD;

            IMFriendInfo friendInfo = NIMFriendInfoManager.getInstance().getFriendUser(message_model.opt_user_id);
            if (friendInfo == null && (message_model.b_id <= 0 || message_model.w_id <= 0)) {
                Logger.error(TAG, "not friend");
                return -1;
            }

            NIMMsgManager.getInstance().AddMessage(new NIM_Chat_ID(message_model.opt_user_id, message_model.message_id), message_model);

            ChatServerSendMessageRS(message_rq.sRqHead().packSessionId(), message_model);
            return 1;
        }
    }

    // 获取离线消息
    class ProcessGetOfflineMsgRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_CHAT_GET_OFFLINE_MESSAGE_RS t_rs =
                    T_CHAT_GET_OFFLINE_MESSAGE_RS.getRootAsT_CHAT_GET_OFFLINE_MESSAGE_RS(byte_buffer);

            if (BodyEmpty(t_rs)) {
                Logger.error(TAG, "client get offline message rq is null");
                return -1;
            }

            if(false == CheckHead(t_rs.sRsHead()))
            {
                Logger.error(TAG, "client get offline message rq head is null");
                return -1;
            }

            int length = t_rs.sOfflineMsgListLength();
            if(length <= 0)
            {
                //没有离线消息了
                //通知状态机状态完成
                DataObserver.Notify(DataConstDef.EVENT_STATE_MACHINE_FINISH, StateConstDef.SC_OFFLINE_MSG, null);
                return 1;
            }

            offline_msg_count = t_rs.nextMessageId();
            offline_msg_count++;
            List<ScMessageModel> list = new ArrayList<>();
            for (int i = 0; i < length; i++)
            {
                T_OFFLINE_MSG t_msg = t_rs.sOfflineMsgList(i);
                ScMessageModel off_msg = new ScMessageModel();
                off_msg.UnSerializeRS(t_msg);

                list.add(off_msg);
            }

            NIMMsgManager.getInstance().AddMessageList(list);

            SendOfflineMsgRQ();

            return 1;
        }
    }

    class ProcessGetFreeWaiterRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_EC_GETFREEWAITER_RS getfreewaiter_rs =
                    T_EC_GETFREEWAITER_RS.getRootAsT_EC_GETFREEWAITER_RS(byte_buffer);

            if (BodyEmpty(getfreewaiter_rs)) {
                Logger.error(TAG, "client get free waiter rs is null");
                return -1;
            }

            if(false == CheckHead(getfreewaiter_rs.sRsHead())){
                Logger.error(TAG, "client get free waiter rs head is null");
                return -1;
            }

            IMBusinessInfo info = new IMBusinessInfo();
            boolean result = info.UnSerialize(getfreewaiter_rs);
            if (!result) {
                return -1;
            }

            String tips = ChatMsgBuildManager.GenWaiterTips(info.bid, info.wid, info.name);
            NIMMsgManager.getInstance().GenTipsMessage(info.bid, tips);
            return 1;
        }
    }

    public boolean ChatServerSendMessageRS(int pack_session_id, ScMessageModel message_model) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        message_model.SerializeRS(builder);
        super.SendRS(PackTypeDef.NEW_DEF_CHAT_SERVER_SEND_MESSAGE_RS, pack_session_id, builder);
        return true;
    }
}
