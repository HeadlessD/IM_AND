package com.qbao.newim.processor;

import com.google.flatbuffers.FlatBufferBuilder;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.FriendTypeDef;
import com.qbao.newim.constdef.NetConstDef;
import com.qbao.newim.constdef.PackTypeDef;
import com.qbao.newim.manager.NIMFriendInfoManager;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.network.IProcessInterface;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.Logger;
import com.qbao.newim.util.SharedPreferenceUtil;

import java.nio.ByteBuffer;

import friendpack.T_CLIENT_FRIEND_BLACKLIST_RQ;
import friendpack.T_CLINET_FRIEND_BLACKLIST_RS;
import friendpack.T_FRIEND_DEL_RQ;
import friendpack.T_FRIEND_DEL_RS;
import friendpack.T_FRIEND_REMARK_RQ;
import friendpack.T_FRIEND_REMARK_RS;
import friendpack.T_FRIEND_SERVER_DEL_RQ;
import friendpack.T_FRIEND_UPDATE_RQ;
import friendpack.T_FRIEND_UPDATE_RS;
import friendpack.T_SERVER_FRIEND_BLACKLIST_RQ;

/**
 * Created by chenjian on 2017/6/9.
 */

public class FriendDelProcessor extends BaseProcessor{
    private static final String TAG = FriendDelProcessor.class.getSimpleName();
    private ProcessorClientFriendDelRS p_friend_del_client_rs = new ProcessorClientFriendDelRS();
    private ProcessorServiceFriendDelRQ p_friend_del_service_rq = new ProcessorServiceFriendDelRQ();
    private ProcessClientEditNameRS p_friend_edit_rs = new ProcessClientEditNameRS();
    private ProcessClientBlackRS p_client_black_rs = new ProcessClientBlackRS();
    private ProcessServerBlackRQ p_server_black_rq = new ProcessServerBlackRQ();
    private ProcessClientDeleteReqRS p_client_delete_rs = new ProcessClientDeleteReqRS();

    public FriendDelProcessor() {
        Init();
    }

    @Override
    protected void Init() {
        super.Init();
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_FRIEND_DEL_RS, p_friend_del_client_rs);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_SERVER_FRIEND_DEL_RQ, p_friend_del_service_rq);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_FRIEND_REMARK_RS, p_friend_edit_rs);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_CLIENT_FRIEND_BLACKLIST_RS, p_client_black_rs);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_SERVER_FRIEND_BLACKLIST_RQ, p_server_black_rq);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_FRIEND_UPDATE_RS, p_client_delete_rs);
    }

    public boolean sendFriendDelRQ(long user_id) {
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder();
        T_FRIEND_DEL_RQ.startT_FRIEND_DEL_RQ(flatBufferBuilder);
        T_FRIEND_DEL_RQ.addPeerUserId(flatBufferBuilder, user_id);
        super.SendRQ(PackTypeDef.NEW_DEF_FRIEND_DEL_RQ, flatBufferBuilder);
        return true;
    }

    public boolean sendFriendEditRQ(long user_id, String remark) {
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder();
        int remark_offset = flatBufferBuilder.createString(remark);
        T_FRIEND_REMARK_RQ.startT_FRIEND_REMARK_RQ(flatBufferBuilder);
        T_FRIEND_REMARK_RQ.addPeerUserId(flatBufferBuilder, user_id);
        T_FRIEND_REMARK_RQ.addPeerRemarkName(flatBufferBuilder, remark_offset);
        super.SendRQ(PackTypeDef.NEW_DEF_FRIEND_REMARK_RQ, flatBufferBuilder);
        return true;
    }

    public boolean sendBlackTypeRQ(long user_id, int type) {
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder();
        T_CLIENT_FRIEND_BLACKLIST_RQ.startT_CLIENT_FRIEND_BLACKLIST_RQ(flatBufferBuilder);
        T_CLIENT_FRIEND_BLACKLIST_RQ.addPeerUserId(flatBufferBuilder, user_id);
        T_CLIENT_FRIEND_BLACKLIST_RQ.addType(flatBufferBuilder, type);
        super.SendRQ(PackTypeDef.NEW_DEF_CLIENT_FRIEND_BLACKLIST_RQ, flatBufferBuilder);
        return true;
    }

    public boolean sendReqDeleteRQ(long user_id) {
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder();
        T_FRIEND_UPDATE_RQ.startT_FRIEND_UPDATE_RQ(flatBufferBuilder);
        T_FRIEND_UPDATE_RQ.addPeerUserId(flatBufferBuilder, user_id);
        super.SendRQ(PackTypeDef.NEW_DEF_FRIEND_UPDATE_RQ, flatBufferBuilder);
        return true;
    }

    // 主动删除好友
    class ProcessorClientFriendDelRS implements IProcessInterface{

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_FRIEND_DEL_RS t_friend_del_rs = T_FRIEND_DEL_RS.getRootAsT_FRIEND_DEL_RS(byte_buffer);

            if(BodyEmpty(t_friend_del_rs)) {
                Logger.error(TAG, "T_FRIEND_DEL_RS is null");
                return -1;
            }

            if(false == CheckHead(t_friend_del_rs.sRsHead())) {
                Logger.error(TAG, "client get friend del rs head is null");
                return -1;
            }

            long user_id = t_friend_del_rs.peerUserId();
            SharedPreferenceUtil.saveFriendsToken(t_friend_del_rs.token());
            NIMFriendInfoManager.getInstance().delFriend(user_id);
            DataObserver.Notify(DataConstDef.EVENT_FRIEND_DEL, user_id, true);

            return 1;
        }
    }

    // 收到服务器的好友删除
    class ProcessorServiceFriendDelRQ implements IProcessInterface{

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_FRIEND_SERVER_DEL_RQ t_friend_server_del_rq = T_FRIEND_SERVER_DEL_RQ.
                    getRootAsT_FRIEND_SERVER_DEL_RQ(byte_buffer);
            if(BodyEmpty(t_friend_server_del_rq)) {
                Logger.error(TAG, "T_FRIEND_DEL_RQ is null");
                return -1;
            }

            if(null == t_friend_server_del_rq.sRqHead()) {
                Logger.error(TAG, "service get friend del rq head is null");
                return -1;
            }

            long user_id = t_friend_server_del_rq.peerUserId();
            SharedPreferenceUtil.saveFriendsToken(t_friend_server_del_rq.token());
            NIMFriendInfoManager.getInstance().removeFriend(user_id, t_friend_server_del_rq.token());

            return 1;
        }
    }

    class ProcessClientEditNameRS implements IProcessInterface {
        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_FRIEND_REMARK_RS remark_rs = T_FRIEND_REMARK_RS.getRootAsT_FRIEND_REMARK_RS(byte_buffer);

            if(BodyEmpty(remark_rs)) {
                Logger.error(TAG, "T_FRIEND_REMARK_RS is null");
                return -1;
            }

            if(false == CheckHead(remark_rs.sRsHead())) {
                Logger.error(TAG, "service get friend edit rq head is null");
                return -1;
            }

            IMFriendInfo friendInfo = NIMFriendInfoManager.getInstance().getFriendUser(remark_rs.peerUserId());
            if (friendInfo == null) {
                return -1;
            }

            friendInfo.remark_name = remark_rs.peerRemarkName();
            friendInfo.friend_token = remark_rs.token();
            SharedPreferenceUtil.saveFriendsToken(remark_rs.token());
            NIMFriendInfoManager.getInstance().updateFriend(friendInfo);

            DataObserver.Notify(DataConstDef.EVENT_FRIEND_EDIT, remark_rs.peerRemarkName(), remark_rs.peerUserId());
            return 1;
        }
    }

    // 客户端主动添加黑名单或者移除黑名单
    class ProcessClientBlackRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_CLINET_FRIEND_BLACKLIST_RS friend_blacklist_rs = T_CLINET_FRIEND_BLACKLIST_RS.
                    getRootAsT_CLINET_FRIEND_BLACKLIST_RS(byte_buffer);
            if(BodyEmpty(friend_blacklist_rs)) {
                Logger.error(TAG, "T_CLIENT_FRIEND_BLACKLIST_RS is null");
                return -1;
            }

            if(false == CheckHead(friend_blacklist_rs.sRsHead())){
                Logger.error(TAG, "client get friend black rq head is null");
                return -1;
            }

            IMFriendInfo friendInfo = NIMFriendInfoManager.getInstance().getFriendUser(friend_blacklist_rs.peerUserId());
            if (friendInfo == null) {
                return -1;
            }

            if (friend_blacklist_rs.type() == 0) {
                if (friendInfo.black_type == FriendTypeDef.ACTIVE_TYPE.ACTIVE) {
                    friendInfo.black_type = FriendTypeDef.ACTIVE_TYPE.INVALID;
                } else if (friendInfo.black_type == FriendTypeDef.ACTIVE_TYPE.EACH) {
                    friendInfo.black_type = FriendTypeDef.ACTIVE_TYPE.PASSIVE;
                }
            } else {
                if (friendInfo.black_type == FriendTypeDef.ACTIVE_TYPE.INVALID) {
                    friendInfo.black_type = FriendTypeDef.ACTIVE_TYPE.ACTIVE;
                } else if (friendInfo.black_type == FriendTypeDef.ACTIVE_TYPE.PASSIVE) {
                    friendInfo.black_type = FriendTypeDef.ACTIVE_TYPE.EACH;
                }
            }

            friendInfo.friend_token = friend_blacklist_rs.token();
            SharedPreferenceUtil.saveFriendsToken(friendInfo.friend_token);
            NIMFriendInfoManager.getInstance().setBlackStatus(friendInfo);
            DataObserver.Notify(DataConstDef.EVENT_GET_BLACK_LIST, friendInfo.userId, friendInfo.black_type);
            return 1;
        }
    }

    // 收到服务器被拉黑，或者移除黑名单
    class ProcessServerBlackRQ implements IProcessInterface {
        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_SERVER_FRIEND_BLACKLIST_RQ friend_blacklist_rq = T_SERVER_FRIEND_BLACKLIST_RQ.
                    getRootAsT_SERVER_FRIEND_BLACKLIST_RQ(byte_buffer);
            if(BodyEmpty(friend_blacklist_rq)) {
                Logger.error(TAG, "T_SERVER_FRIEND_BLACKLIST_RS is null");
                return -1;
            }

            if(null == friend_blacklist_rq.sRqHead()) {
                Logger.error(TAG, "client get friend black rq head is null");
                return -1;
            }

            IMFriendInfo friendInfo = NIMFriendInfoManager.getInstance().getFriendUser(friend_blacklist_rq.peerUserId());
            if (friendInfo == null) {
                return -1;
            }

            if (friend_blacklist_rq.type() == 0) {
                if (friendInfo.black_type == FriendTypeDef.ACTIVE_TYPE.EACH) {
                    friendInfo.black_type = FriendTypeDef.ACTIVE_TYPE.ACTIVE;
                } else if (friendInfo.black_type == FriendTypeDef.ACTIVE_TYPE.PASSIVE) {
                    friendInfo.black_type = FriendTypeDef.ACTIVE_TYPE.INVALID;
                }
            } else {
                if (friendInfo.black_type == FriendTypeDef.ACTIVE_TYPE.INVALID) {
                    friendInfo.black_type = FriendTypeDef.ACTIVE_TYPE.PASSIVE;
                } else if (friendInfo.black_type == FriendTypeDef.ACTIVE_TYPE.ACTIVE) {
                    friendInfo.black_type = FriendTypeDef.ACTIVE_TYPE.EACH;
                }
            }

            friendInfo.friend_token = friend_blacklist_rq.token();
            SharedPreferenceUtil.saveFriendsToken(friendInfo.friend_token);
            NIMFriendInfoManager.getInstance().setBlackStatus(friendInfo);
            return 1;
        }
    }

    class ProcessClientDeleteReqRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_FRIEND_UPDATE_RS friend_update_rs = T_FRIEND_UPDATE_RS.
                    getRootAsT_FRIEND_UPDATE_RS(byte_buffer);
            if(BodyEmpty(friend_update_rs)) {
                Logger.error(TAG, "T_FRIEND_UPDATE_RS is null");
                return -1;
            }

            if(false == CheckHead(friend_update_rs.sRsHead())){
                Logger.error(TAG, "client delete friend rq head is null");
                return -1;
            }

            IMFriendInfo friendInfo = NIMFriendInfoManager.getInstance().getFriendUser(friend_update_rs.peerUserId());
            if (friendInfo == null) {
                return -1;
            }

            friendInfo.friend_token = friend_update_rs.token();
            friendInfo.status = FriendTypeDef.FRIEND_ADD_TYPE.DELETE;
            NIMFriendInfoManager.getInstance().updateFriend(friendInfo);
            DataObserver.Notify(DataConstDef.EVENT_FRIEND_REQUEST_DELETE, friendInfo.userId, null);
            return 1;
        }
    }

//    public boolean FriendDelServerRS(int pack_session_id) {
//        FlatBufferBuilder builder = new FlatBufferBuilder();
//        T_FRIEND_SERVER_DEL_RS.startT_FRIEND_SERVER_DEL_RS(builder);
//        super.SendRS(PackTypeDef.NEW_DEF_SERVER_FRIEND_DEL_RS, pack_session_id, builder);
//        return true;
//    }
}
