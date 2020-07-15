package com.qbao.newim.processor;

import android.text.TextUtils;

import com.google.flatbuffers.FlatBufferBuilder;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.ErrorCodeDef;
import com.qbao.newim.constdef.FriendTypeDef;
import com.qbao.newim.constdef.NetConstDef;
import com.qbao.newim.constdef.PackTypeDef;
import com.qbao.newim.manager.NIMFriendInfoManager;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.model.IMUserInfo;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.network.IProcessInterface;
import com.qbao.newim.util.BaseUtil;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.Logger;
import com.qbao.newim.util.SharedPreferenceUtil;
import com.qbao.newim.util.ShowUtils;

import java.nio.ByteBuffer;

import friendpack.T_FRIEND_CLIENT_ADD_RS;
import friendpack.T_FRIEND_CLIENT_CONFIRM_RS;
import friendpack.T_FRIEND_SERVER_ADD_RQ;
import friendpack.T_FRIEND_SERVER_CONFIRM_RQ;
import friendpack.T_FRIEND_SERVER_RECOVER_RQ;

/**
 * Created by chenjian on 2017/6/1.
 */

public class FriendAddProcessor extends BaseProcessor{
    private static final String TAG = FriendAddProcessor.class.getSimpleName();
    private ProcessorClientFriendAddRS p_friend_add_client_rs = new ProcessorClientFriendAddRS();
    private ProcessorServiceFriendAddRQ p_friend_add_service_rq = new ProcessorServiceFriendAddRQ();
    private ProcessorClientFriendAcceptRS p_friend_accept_client_rs = new ProcessorClientFriendAcceptRS();
    private ProcessorServiceFriendAcceptRQ p_friend_accept_service_rs = new ProcessorServiceFriendAcceptRQ();
    private ProcessorServiceFriendRestoreRQ p_friend_restore_service_rq = new ProcessorServiceFriendRestoreRQ();

    public FriendAddProcessor() {
        Init();
    }

    @Override
    protected void Init() {
        super.Init();
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_CLIENT_FRIEND_ADD_RS, p_friend_add_client_rs);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_SERVER_FRIEND_ADD_RQ, p_friend_add_service_rq);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_CLINET_FRIEND_CONFIRM_RS, p_friend_accept_client_rs);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_SERVER_FRIEND_CONFIRM_RQ, p_friend_accept_service_rs);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_FRIEND_RESTORE_RQ, p_friend_restore_service_rq);
    }

    @Override
    protected void ProcessEvent() {
    }

    public boolean sendFriendAddRQ(IMFriendInfo info) {
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder();
        info.SerializeADD(flatBufferBuilder);
        super.SendRQ(PackTypeDef.NEW_DEF_CLIENT_FRIEND_ADD_RQ, flatBufferBuilder);
        return true;
    }

    public boolean sendFriendAcceptRQ(IMFriendInfo info) {
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder();
        info.SerializeAccept(flatBufferBuilder);
        super.SendRQ(PackTypeDef.NEW_DEF_CLIENT_FRIEND_CONFIRM_RQ, flatBufferBuilder);
        return true;
    }

    // 客户端主动添加好友
    class ProcessorClientFriendAddRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_FRIEND_CLIENT_ADD_RS t_friend_add_rs = T_FRIEND_CLIENT_ADD_RS.getRootAsT_FRIEND_CLIENT_ADD_RS(byte_buffer);
            if(BodyEmpty(t_friend_add_rs)) {
                Logger.error(TAG, "T_FRIEND_ADD_RS is null");
                return -1;
            }

            if(false == CheckHead(t_friend_add_rs.sRsHead())) {
                Logger.error(TAG, "client get friend add rs head is null");
                return -1;
            }

            int error_code = BaseUtil.MakeErrorResult(t_friend_add_rs.sRsHead().result());
            if (error_code == ErrorCodeDef.RET_FRIEND_HAVE_BLACK_ERROR) {
                DataObserver.Notify(DataConstDef.EVENT_FRIEND_ADD_REQUEST, null, false);
                return -1;
            }

            IMFriendInfo info = new IMFriendInfo();
            boolean result = info.UnSerializeADDRS(t_friend_add_rs);
            if (!result) {
                Logger.error(TAG, "recv error head = " + t_friend_add_rs.sRsHead());
                return -1;
            }

            // 如果当前已存在的是好友，则不做处理
            IMFriendInfo exist_info = NIMFriendInfoManager.getInstance().getFriendUser(info.userId);
            if (exist_info != null && exist_info.status >= FriendTypeDef.FRIEND_ADD_TYPE.OWN_CONFIRM) {
                return 1;
            }

            IMUserInfo userInfo = NIMUserInfoManager.getInstance().getIMUser(info.userId);
            if (userInfo != null) {
                NIMFriendInfoManager.getInstance().userToFriend(userInfo, info);
            }

            if (error_code == ErrorCodeDef.RET_FRIEND_RELATION_ERROR) {
                return 1;
            }

            if (error_code == ErrorCodeDef.RET_FRIEND_BE_DELETE_ERROR) {
                info.status = FriendTypeDef.FRIEND_ADD_TYPE.RESTART_ADD;         // 恢复好友
            } else {
                info.status = FriendTypeDef.FRIEND_ADD_TYPE.SEND_REQUEST;        // 待验证状态
                ShowUtils.showToast("已发送好友申请");
            }
            SharedPreferenceUtil.saveFriendsToken(info.friend_token);
            NIMFriendInfoManager.getInstance().AddFriend(info);
            return 1;
        }
    }

    // 客户端收到好友请求
    class ProcessorServiceFriendAddRQ implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_FRIEND_SERVER_ADD_RQ t_friend_server_add_rq =
                    T_FRIEND_SERVER_ADD_RQ.getRootAsT_FRIEND_SERVER_ADD_RQ(byte_buffer);

            if (BodyEmpty(t_friend_server_add_rq)) {
                Logger.error(TAG, "server send friend add rq is null");
                return -1;
            }

            if (null == t_friend_server_add_rq.sRqHead()) {
                Logger.error(TAG, "server send friend add rq head is null");
                return -1;
            }

            IMFriendInfo info = new IMFriendInfo();
            boolean result = info.UnSerializeADDRQ(t_friend_server_add_rq);
            if (!result) {
                Logger.error(TAG, "recv error head = " + t_friend_server_add_rq.sRqHead());
                return -1;
            }

            if (TextUtils.isEmpty(info.opt_msg)) {
                info.opt_msg = info.nickName + "请求添加你为好友";
            }
            long token = t_friend_server_add_rq.token();
            SharedPreferenceUtil.saveFriendsToken(token);  // 更新好友操作token
            info.status = FriendTypeDef.FRIEND_ADD_TYPE.ACCEPT_REQUEST;           // 待同意状态

            NIMFriendInfoManager.getInstance().AddFriend(info);

            return 1;
        }
    }

    // 客户端同意好友请求
    class ProcessorClientFriendAcceptRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_FRIEND_CLIENT_CONFIRM_RS t_friend_accept_rs = T_FRIEND_CLIENT_CONFIRM_RS.getRootAsT_FRIEND_CLIENT_CONFIRM_RS(byte_buffer);
            if(BodyEmpty(t_friend_accept_rs)) {
                Logger.error(TAG, "T_FRIEND_CLIENT_CONFIRM_RS is null");
                return -1;
            }

            if(false == CheckHead(t_friend_accept_rs.sRsHead())) {
                int error_code = BaseUtil.MakeErrorResult(t_friend_accept_rs.sRsHead().result());
                if (error_code == ErrorCodeDef.RET_FRIEND_ALREADY_EXISTED) {
                    ShowUtils.showToast("好友已存在");
                    return -1;
                }

                if (error_code == ErrorCodeDef.RET_FRIEND_CONFIRM_TIMEOUT_ERROR) {
                    IMFriendInfo origin_info = NIMFriendInfoManager.getInstance().getFriendReqInfo(t_friend_accept_rs.peerUserId());
                    origin_info.status = FriendTypeDef.FRIEND_ADD_TYPE.TIME_OUT;
                    NIMFriendInfoManager.getInstance().AddFriend(origin_info);
                    return -1;
                }
                Logger.error(TAG, "client get friend accept rs head is null");
                return -1;
            }

            long token = t_friend_accept_rs.token();

            IMFriendInfo origin_info = NIMFriendInfoManager.getInstance().getFriendReqInfo(t_friend_accept_rs.peerUserId());
            if (origin_info == null) {
                return -1;
            }
            origin_info.status = FriendTypeDef.FRIEND_ADD_TYPE.OWN_CONFIRM; // 同意别人为好友
            origin_info.friend_token = token;

            IMUserInfo userInfo = NIMUserInfoManager.getInstance().getIMUser(origin_info.userId);
            if (userInfo != null) {
                NIMFriendInfoManager.getInstance().userToFriend(userInfo, origin_info, false);
            }

            SharedPreferenceUtil.saveFriendsToken(token);
            NIMFriendInfoManager.getInstance().AddFriend(origin_info);
            return 1;
        }
    }

    // 服务端同意好友请求
    class ProcessorServiceFriendAcceptRQ implements IProcessInterface{

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_FRIEND_SERVER_CONFIRM_RQ t_friend_server_confirm_rq =
                    T_FRIEND_SERVER_CONFIRM_RQ.getRootAsT_FRIEND_SERVER_CONFIRM_RQ(byte_buffer);

            if (BodyEmpty(t_friend_server_confirm_rq)) {
                Logger.error(TAG, "server send friend confirm rq is null");
                return -1;
            }

            if (null == t_friend_server_confirm_rq.sRqHead()) {
                Logger.error(TAG, "server send friend confirm rq head is null");
                return -1;
            }

            IMFriendInfo info = new IMFriendInfo();
            boolean result = info.UnSerializeAcceptRQ(t_friend_server_confirm_rq);
            if (!result) {
                Logger.error(TAG, "recv error head = " + t_friend_server_confirm_rq.sRqHead());
                return -1;
            }

            info.status = FriendTypeDef.FRIEND_ADD_TYPE.PEER_CONFIRM;       // 对方同意为好友
            SharedPreferenceUtil.saveFriendsToken(info.friend_token);

            IMUserInfo userInfo = NIMUserInfoManager.getInstance().getIMUser(info.userId);
            if (userInfo != null) {
                NIMFriendInfoManager.getInstance().userToFriend(userInfo, info, false);
            }

            NIMFriendInfoManager.getInstance().AddFriend(info);

            return 1;
        }
    }

    // 收到服务器重新恢复好友关系(被删除后对方重新添加)
    class ProcessorServiceFriendRestoreRQ implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_FRIEND_SERVER_RECOVER_RQ recover_rq = T_FRIEND_SERVER_RECOVER_RQ.getRootAsT_FRIEND_SERVER_RECOVER_RQ(byte_buffer);
            if (BodyEmpty(recover_rq)) {
                Logger.error(TAG, "server send friend recover_rq is null");
                return -1;
            }

            if (null == recover_rq.sRqHead()) {
                Logger.error(TAG, "server send friend recover_rq head is null");
                return -1;
            }

            IMFriendInfo friendInfo = NIMFriendInfoManager.getInstance().getFriendUser(recover_rq.peerUserId());
            if (friendInfo != null) {
                friendInfo.delete_type = FriendTypeDef.ACTIVE_TYPE.INVALID;
                friendInfo.friend_token = recover_rq.token();
                SharedPreferenceUtil.saveFriendsToken(friendInfo.friend_token);
                NIMFriendInfoManager.getInstance().updateFriend(friendInfo);
            }
            return 1;
        }
    }
}
