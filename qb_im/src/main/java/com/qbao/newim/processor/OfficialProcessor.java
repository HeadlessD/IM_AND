package com.qbao.newim.processor;

import com.google.flatbuffers.FlatBufferBuilder;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.constdef.PackTypeDef;
import com.qbao.newim.constdef.StateConstDef;
import com.qbao.newim.manager.NIMMsgManager;
import com.qbao.newim.model.IMOfficialInfo;
import com.qbao.newim.model.NIM_Chat_ID;
import com.qbao.newim.model.message.OcMessageModel;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.netcenter.PackSessionMgr;
import com.qbao.newim.network.IProcessInterface;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.Logger;
import com.qbao.newim.util.ShowUtils;

import java.nio.ByteBuffer;

import offcialpack.T_CLIENT_FANS_GET_OFFLINE_MESSAGE_RQ;
import offcialpack.T_CLIENT_FANS_GET_OFFLINE_MESSAGE_RS;
import offcialpack.T_CLIENT_FANS_GET_SYS_MSG_RQ;
import offcialpack.T_CLIENT_FANS_GET_SYS_MSG_RS;
import offcialpack.T_CLIENT_FANS_SEND_MESSAGE_RS;
import offcialpack.T_CLIENT_OFFCIAL_GET_OFFLINE_MESSAGE_RQ;
import offcialpack.T_CLIENT_OFFCIAL_GET_OFFLINE_MESSAGE_RS;
import offcialpack.T_CLIENT_OFFCIAL_SEND_MESSAGE_RQ;
import offcialpack.T_CLIENT_OFFCIAL_SEND_MESSAGE_RS;
import offcialpack.T_CLIENT_OFFCIAL_SEND_SYS_MSG_RQ;
import offcialpack.T_CLIENT_OFFCIAL_SEND_SYS_MSG_RS;
import offcialpack.T_OFFCIAL_BASE_REQUEST;
import offcialpack.T_SERVER_FANS_MESSAGE_RQ;
import offcialpack.T_SERVER_OFFCIAL_MESSAGE_RQ;

/**
 * Created by chenjian on 2017/8/22.
 */

public class OfficialProcessor extends BaseProcessor {
    private static final String TAG = "OfficialProcessor";
    private ProcessorOfflineMsgRS p_offline_msg_rs = new ProcessorOfflineMsgRS();
    private ProcessorFansSendMsgRS p_fans_msg_rs = new ProcessorFansSendMsgRS();
    private ProcessServerSendMsgRQ p_server_msg_rq = new ProcessServerSendMsgRQ();
    private ProcessOfficialSendAllMsg p_official_msg_rs = new ProcessOfficialSendAllMsg();
    private ProcessOfficialServerMsgRQ p_official_msg_rq = new ProcessOfficialServerMsgRQ();
    private ProcessOfficialOfflineRS p_official_offline_rs = new ProcessOfficialOfflineRS();
    private ProcessFansSysMsgRS p_fans_sys_msg = new ProcessFansSysMsgRS();
    private ProcessOfficialSysMsgRS p_official_sys_msg = new ProcessOfficialSysMsgRS();

    public OfficialProcessor() {
        Init();
    }

    @Override
    protected void Init() {
        super.Init();
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_CLIENT_FANS_GET_OFFLINE_MESSAGE_RS, p_offline_msg_rs);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_CLIENT_FANS_SEND_MESSAGE_RS, p_fans_msg_rs);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_SERVER_OFFCIAL_MESSAGE_ID, p_server_msg_rq);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_CLIENT_OFFCIAL_SEND_MESSAGE_RS, p_official_msg_rs);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_SERVER_FANS_MESSAGE_RQ, p_official_msg_rq);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_CLIENT_OFFCIAL_GET_OFFLINE_MESSAGE_RS, p_official_offline_rs);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_CLIENT_FANS_GET_SYS_MESSAGE_RS, p_fans_sys_msg);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_CLIENT_OFFCIAL_SEND_SYS_MESSAGE_RS, p_official_sys_msg);
    }

    public boolean getOfficialOfflineMsg(long official_id) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        T_CLIENT_OFFCIAL_GET_OFFLINE_MESSAGE_RQ.startT_CLIENT_OFFCIAL_GET_OFFLINE_MESSAGE_RQ(builder);
        T_CLIENT_OFFCIAL_GET_OFFLINE_MESSAGE_RQ.addOffcialId(builder, official_id);
        T_CLIENT_OFFCIAL_GET_OFFLINE_MESSAGE_RQ.addMessageId(builder, 0);
        super.SendRQ(PackTypeDef.NEW_DEF_CLIENT_OFFCIAL_GET_OFFLINE_MESSAGE_RQ, builder);
        return true;
    }

    // 粉丝获取系统消息
    public boolean getFanSysMsg() {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        T_CLIENT_FANS_GET_SYS_MSG_RQ.startT_CLIENT_FANS_GET_SYS_MSG_RQ(builder);
        T_CLIENT_FANS_GET_SYS_MSG_RQ.addMessageId(builder, 0);
        super.SendRQ(PackTypeDef.NEW_DEF_CLIENT_FANS_GET_SYS_MESSAGE_RQ, builder);
        return true;
    }

    // 公众号发送系统消息
    public boolean sendOfficialSysMsg(OcMessageModel messageModel) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int send_user_name_offset = builder.createString(messageModel.official_name);
        int msg_content_offset = builder.createString(messageModel.msg_content);

        commonpack.S_MSG.startS_MSG(builder);
        commonpack.S_MSG.addSendUserName(builder, send_user_name_offset);
        commonpack.S_MSG.addAppId(builder, messageModel.app_id);
        commonpack.S_MSG.addChatType(builder, messageModel.chat_type);
        commonpack.S_MSG.addMType(builder, messageModel.m_type);
        commonpack.S_MSG.addSType(builder, messageModel.s_type);
        commonpack.S_MSG.addExtType(builder, messageModel.ext_type);
        commonpack.S_MSG.addMsgContent(builder, msg_content_offset);
        commonpack.S_MSG.addMsgTime(builder, messageModel.msg_time);
        int s_msg_offset = commonpack.S_MSG.endS_MSG(builder);
        T_CLIENT_OFFCIAL_SEND_SYS_MSG_RQ.startT_CLIENT_OFFCIAL_SEND_SYS_MSG_RQ(builder);
        T_CLIENT_OFFCIAL_SEND_SYS_MSG_RQ.addSMsg(builder, s_msg_offset);
        super.SendRQ(PackTypeDef.NEW_DEF_CLIENT_OFFCIAL_SEND_SYS_MESSAGE_RQ, builder);
        return true;
    }

    // 公众号发送消息给粉丝
    public boolean sendOfficialMsg(OcMessageModel messageModel) {
        FlatBufferBuilder builder = new FlatBufferBuilder();

        int send_user_name_offset = builder.createString(messageModel.official_name);
        int msg_content_offset = builder.createString(messageModel.msg_content);
        long[] user_ids = new long[2];
        user_ids[0] = 5504150;
        user_ids[1] = 5504144;
        int user_offset = T_CLIENT_OFFCIAL_SEND_MESSAGE_RQ.createToUserLstVector(builder, user_ids);

        commonpack.S_MSG.startS_MSG(builder);
        commonpack.S_MSG.addSendUserName(builder, send_user_name_offset);
        commonpack.S_MSG.addAppId(builder, messageModel.app_id);
        commonpack.S_MSG.addChatType(builder, messageModel.chat_type);
        commonpack.S_MSG.addMType(builder, messageModel.m_type);
        commonpack.S_MSG.addSType(builder, messageModel.s_type);
        commonpack.S_MSG.addExtType(builder, messageModel.ext_type);
        commonpack.S_MSG.addMsgContent(builder, msg_content_offset);
        commonpack.S_MSG.addMsgTime(builder, messageModel.msg_time);
        int s_msg_offset = commonpack.S_MSG.endS_MSG(builder);
        T_CLIENT_OFFCIAL_SEND_MESSAGE_RQ.startT_CLIENT_OFFCIAL_SEND_MESSAGE_RQ(builder);
        T_CLIENT_OFFCIAL_SEND_MESSAGE_RQ.addSMsg(builder, s_msg_offset);
        T_CLIENT_OFFCIAL_SEND_MESSAGE_RQ.addMessageId(builder, messageModel.message_id);
        T_CLIENT_OFFCIAL_SEND_MESSAGE_RQ.addOffcialId(builder, messageModel.official_id);
        T_CLIENT_OFFCIAL_SEND_MESSAGE_RQ.addToUserLst(builder, user_offset);
        int pack_session_id = super.SendRQ(PackTypeDef.NEW_DEF_CLIENT_OFFCIAL_SEND_MESSAGE_RQ, builder);
        PackSessionMgr.getInstance().AddPackMsg(pack_session_id, new NIM_Chat_ID(messageModel.official_id, messageModel.message_id, messageModel.chat_type));
        return true;
    }

    // 获取公众号发送的离线消息
    public boolean GetOfficialMsg(IMOfficialInfo[] array) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int length = array.length;
        int[] offset_array = new int[length];
        for (int i = 0; i < length; i++) {
            T_OFFCIAL_BASE_REQUEST.startT_OFFCIAL_BASE_REQUEST(builder);
            T_OFFCIAL_BASE_REQUEST.addNextMessageId(builder, array[i].last_msg_id);
            T_OFFCIAL_BASE_REQUEST.addOffcialId(builder, array[i].official_id);
            offset_array[i] = T_OFFCIAL_BASE_REQUEST.endT_OFFCIAL_BASE_REQUEST(builder);
        }

        int offset = T_CLIENT_FANS_GET_OFFLINE_MESSAGE_RQ.createListOffcialOfflineMsgRequestVector(builder, offset_array);
        T_CLIENT_FANS_GET_OFFLINE_MESSAGE_RQ.startT_CLIENT_FANS_GET_OFFLINE_MESSAGE_RQ(builder);
        T_CLIENT_FANS_GET_OFFLINE_MESSAGE_RQ.addListOffcialOfflineMsgRequest(builder, offset);
        super.SendRQ(PackTypeDef.NEW_DEF_CLIENT_FANS_GET_OFFLINE_MESSAGE_RQ, builder);
        return true;
    }

    // 粉丝发送消息给公众号
    public boolean SendFansMsg(OcMessageModel messageModel) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        messageModel.SerializeRQ(builder);
        int pack_session_id = super.SendRQ(PackTypeDef.NEW_DEF_CLIENT_FANS_SEND_MESSAGE_RQ, builder);
        PackSessionMgr.getInstance().AddPackMsg(pack_session_id, new NIM_Chat_ID(messageModel.official_id, messageModel.message_id, messageModel.chat_type));
        return true;
    }

    // 粉丝获取公众号离线消息
    class ProcessorOfflineMsgRS implements IProcessInterface{

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_CLIENT_FANS_GET_OFFLINE_MESSAGE_RS offline_message_rs = T_CLIENT_FANS_GET_OFFLINE_MESSAGE_RS.
                    getRootAsT_CLIENT_FANS_GET_OFFLINE_MESSAGE_RS(byte_buffer);

            if(BodyEmpty(offline_message_rs)) {
                Logger.error(TAG, "T_CLIENT_FANS_GET_OFFLINE_MESSAGE_RS is null");
                return -1;
            }

            if(false == CheckHead(offline_message_rs.sRsHead())) {
                Logger.error(TAG, "client get fans offline msg rs head is null");
                return -1;
            }

            int private_length = offline_message_rs.listPrivateMsgResponseLength();
            int offline_length = offline_message_rs.listOffcialOfflineMsgResponseLength();
            for (int i = 0; i < private_length; i++) {
                OcMessageModel offline_msg = new OcMessageModel();
                offline_msg.UnSerializeRS(offline_message_rs.listPrivateMsgResponse(i));
//                NIMMsgManager.getInstance().AddMessage(new NIM_Chat_ID(offline_msg.official_id, offline_msg.message_id),
//                        offline_msg);
            }

            for (int i = 0; i < offline_length; i++) {
                int single_length = offline_message_rs.listOffcialOfflineMsgResponse(i).sMsgLength();
                long official_id = offline_message_rs.listOffcialOfflineMsgResponse(i).offcialId();
                for (int j = 0; j < single_length; j++) {
                    OcMessageModel offline_msg = new OcMessageModel();
                    offline_msg.UnSerializeRS(offline_message_rs.listOffcialOfflineMsgResponse(i).sMsg(j));
//                    NIMMsgManager.getInstance().AddMessage(new NIM_Chat_ID(offline_msg.official_id, offline_msg.message_id),
//                            offline_msg);
                }
            }

            //通知状态机状态完成
            DataObserver.Notify(DataConstDef.EVENT_STATE_MACHINE_FINISH, StateConstDef.OC_OFFLINE_MSG, null);
            return 1;
        }
    }

    // 粉丝发送消息给公众号
    class ProcessorFansSendMsgRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_CLIENT_FANS_SEND_MESSAGE_RS fans_send_message_rs = T_CLIENT_FANS_SEND_MESSAGE_RS.
                    getRootAsT_CLIENT_FANS_SEND_MESSAGE_RS(byte_buffer);
            if (BodyEmpty(fans_send_message_rs)) {
                Logger.error(TAG, "client send message rs is null");
                return -1;
            }
            long message_id = -1;
            long session_id = -1;
            if(fans_send_message_rs.sRsHead() != null) {
                int pack_session_id = fans_send_message_rs.sRsHead().packSessionId();
                NIM_Chat_ID chat_id = PackSessionMgr.getInstance().GetPackMsg(pack_session_id);
                message_id = chat_id.message_id;
                session_id = chat_id.session_id;
                Logger.error("public_msg", "receive message_id" + chat_id.message_id);
                PackSessionMgr.getInstance().DelPackMsg(pack_session_id);
            }

            if(false == CheckHead(fans_send_message_rs.sRsHead())) {
                NIMMsgManager.getInstance().SetMessageStatus(message_id, session_id,
                        MsgConstDef.MSG_STATUS.SEND_FAILED);
                Logger.error(TAG, "client send message rs head is null");
                return -1;
            }

            NIMMsgManager.getInstance().SetMessageStatus(session_id, message_id,
                    MsgConstDef.MSG_STATUS.SEND_SUCCESS);

            return 1;
        }
    }

    // 粉丝被动收到公众号的消息
    class ProcessServerSendMsgRQ implements IProcessInterface {
        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_SERVER_OFFCIAL_MESSAGE_RQ message_rq =
                    T_SERVER_OFFCIAL_MESSAGE_RQ.getRootAsT_SERVER_OFFCIAL_MESSAGE_RQ(byte_buffer);
            if (BodyEmpty(message_rq)) {
                Logger.error(TAG, "server send message rq is null");
                return -1;
            }

            if (null == message_rq.sRqHead()) {
                Logger.error(TAG, "server send message rq head is null");
                return -1;
            }

//            OcMessageModel message_model = new OcMessageModel();
//            boolean result = message_model.UnSerializePublic(message_rq);
//            if (!result) {
//                Logger.error(TAG, "recv error head = " + message_rq.sRqHead());
//                return -1;
//            }
//
//            boolean is_fans = NIMOfficialManager.getInstance().isOfficialFans(message_model.chat_session_id);
//            if (!is_fans) {
//                Logger.error(TAG, "not fans");
//                return -1;
//            }
//
//            NIMMsgManager.getInstance().AddMessage(new NIM_Chat_ID(message_model.chat_session_id, message_model.message_id), message_model);
            return 1;
        }
    }

    /**
     * 模拟公众号端发收消息
     * 公众号给所有粉丝发消息
     */
    class ProcessOfficialSendAllMsg implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_CLIENT_OFFCIAL_SEND_MESSAGE_RS offcial_send_rs = T_CLIENT_OFFCIAL_SEND_MESSAGE_RS.getRootAsT_CLIENT_OFFCIAL_SEND_MESSAGE_RS(byte_buffer);

            if(BodyEmpty(offcial_send_rs)) {
                return -1;
            }

            long message_id = -1;
            long session_id = -1;
            if(offcial_send_rs.sRsHead() != null) {
                int pack_session_id = offcial_send_rs.sRsHead().packSessionId();
                NIM_Chat_ID chat_id = PackSessionMgr.getInstance().GetPackMsg(pack_session_id);
                message_id = chat_id.message_id;
                session_id = chat_id.session_id;
                PackSessionMgr.getInstance().DelPackMsg(pack_session_id);
            }

//            if(false == CheckHead(offcial_send_rs.sRsHead())) {
//                NIMMsgManager.getInstance().SetMessageStatus(message_id, session_id,
//                        MsgConstDef.MSG_STATUS.SEND_FAILED);
//                return -1;
//            }
//
//            NIMMsgManager.getInstance().SetMessageStatus(session_id, message_id,
//                    MsgConstDef.MSG_STATUS.SEND_SUCCESS);
            return 1;
        }
    }

    // 公众号收到粉丝发送的消息
    class ProcessOfficialServerMsgRQ implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_SERVER_FANS_MESSAGE_RQ message_rq =
                    T_SERVER_FANS_MESSAGE_RQ.getRootAsT_SERVER_FANS_MESSAGE_RQ(byte_buffer);
            if (BodyEmpty(message_rq)) {
                Logger.error(TAG, "server send message rq is null");
                return -1;
            }

            if (null == message_rq.sRqHead()) {
                Logger.error(TAG, "server send message rq head is null");
                return -1;
            }

//            MessageModel message_model = new MessageModel();
//            message_model.chat_session_id = message_rq.fansId();
//            message_model.chat_type = message_rq.sMsg().chatType();
//            message_model.m_type = message_rq.sMsg().mType();
//            message_model.s_type = message_rq.sMsg().sType();
//            message_model.ext_type = message_rq.sMsg().extType();
//            message_model.msg_content = message_rq.sMsg().msgContent();
//            message_model.send_user_name = message_rq.sMsg().sendUserName();
//            message_model.app_id = message_rq.sMsg().appId();
//            message_model.msg_time = message_rq.sMsg().msgTime();

//            ChatServerSendMessageRS(message_rq.sRqHead().packSessionId(), message_model);
            return 1;
        }
    }

    // 公众号收到粉丝发送的离线消息
    class ProcessOfficialOfflineRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_CLIENT_OFFCIAL_GET_OFFLINE_MESSAGE_RS official_offline_rs =
                    T_CLIENT_OFFCIAL_GET_OFFLINE_MESSAGE_RS.getRootAsT_CLIENT_OFFCIAL_GET_OFFLINE_MESSAGE_RS(byte_buffer);
            if(BodyEmpty(official_offline_rs)) {
                return -1;
            }

            if(false == CheckHead(official_offline_rs.sRsHead())) {
                return -1;
            }
            
//            int length = official_offline_rs.listGroupOfflineMsgResponseLength();
//            for (int i = 0; i < length; i++) {
//                T_FANS_OFFLINE_MESSAGE fans_msg = official_offline_rs.listGroupOfflineMsgResponse(i);
//                MessageModel message_model = new MessageModel();
//                message_model.chat_session_id = official_offline_rs.offcialId();
//                message_model.chat_type = fans_msg.sMsg().chatType();
//                message_model.m_type = fans_msg.sMsg().mType();
//                message_model.s_type = fans_msg.sMsg().sType();
//                message_model.ext_type = fans_msg.sMsg().extType();
//                message_model.msg_content = fans_msg.sMsg().msgContent();
//                message_model.send_user_name = fans_msg.sMsg().sendUserName();
//                message_model.app_id = fans_msg.sMsg().appId();
//                message_model.msg_time = fans_msg.sMsg().msgTime();
//            }
            return 1;
        }
    }

    // 粉丝获取系统消息
    class ProcessFansSysMsgRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_CLIENT_FANS_GET_SYS_MSG_RS fans_sys_rs =
                    T_CLIENT_FANS_GET_SYS_MSG_RS.getRootAsT_CLIENT_FANS_GET_SYS_MSG_RS(byte_buffer);

            if(BodyEmpty(fans_sys_rs)) {
                return -1;
            }

            if(false == CheckHead(fans_sys_rs.sRsHead())) {
                return -1;
            }

//            int length = fans_sys_rs.listSysMsgResponseLength();
//            for (int i = 0; i < length; i++) {
//                T_OFFCIAL_MESSAGE official_msg = fans_sys_rs.listSysMsgResponse(i);
//                MessageModel message_model = new MessageModel();
//                message_model.chat_session_id = official_msg.offcialId();
//                message_model.chat_type = official_msg.sMsg().chatType();
//                message_model.m_type = official_msg.sMsg().mType();
//                message_model.s_type = official_msg.sMsg().sType();
//                message_model.ext_type = official_msg.sMsg().extType();
//                message_model.msg_content = official_msg.sMsg().msgContent();
//                message_model.send_user_name = official_msg.sMsg().sendUserName();
//                message_model.app_id = official_msg.sMsg().appId();
//                message_model.msg_time = official_msg.sMsg().msgTime();
//            }

            return 1;
        }
    }

    // 公众号发送系统消息RS
    class ProcessOfficialSysMsgRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_CLIENT_OFFCIAL_SEND_SYS_MSG_RS official_sys_rs =
                    T_CLIENT_OFFCIAL_SEND_SYS_MSG_RS.getRootAsT_CLIENT_OFFCIAL_SEND_SYS_MSG_RS(byte_buffer);
            if(BodyEmpty(official_sys_rs)) {
                return -1;
            }

            if(false == CheckHead(official_sys_rs.sRsHead())) {
                return -1;
            }

            ShowUtils.showToast(official_sys_rs.sMsg().msgContent());
            return 1;
        }
    }

//    public boolean ChatServerSendMessageRS(int pack_session_id, MessageModel message_model) {
//        FlatBufferBuilder builder = new FlatBufferBuilder();
//        int send_user_name_offset = builder.createString(message_model.send_user_name);
//        int msg_content_offset = builder.createString(message_model.msg_content);
//        commonpack.S_MSG.startS_MSG(builder);
//        commonpack.S_MSG.addSendUserName(builder, send_user_name_offset);
//        commonpack.S_MSG.addAppId(builder, message_model.app_id);
//        commonpack.S_MSG.addChatType(builder, message_model.chat_type);
//        commonpack.S_MSG.addMType(builder, message_model.m_type);
//        commonpack.S_MSG.addSType(builder, message_model.s_type);
//        commonpack.S_MSG.addExtType(builder, message_model.ext_type);
//        commonpack.S_MSG.addMsgContent(builder, msg_content_offset);
//        commonpack.S_MSG.addMsgTime(builder, message_model.msg_time);
//        int s_msg_offset = commonpack.S_MSG.endS_MSG(builder);
//
//        T_SERVER_FANS_MESSAGE_RS.startT_SERVER_FANS_MESSAGE_RS(builder);
//        T_SERVER_FANS_MESSAGE_RS.addSMsg(builder, s_msg_offset);
//        T_SERVER_FANS_MESSAGE_RS.addFansId(builder, message_model.chat_session_id);
//        T_SERVER_FANS_MESSAGE_RS.addMessageId(builder, message_model.message_id);
//        super.SendRS(PackTypeDef.NEW_DEF_SERVER_FANS_MESSAGE_RS, pack_session_id, builder);
//        return true;
//    }
}
