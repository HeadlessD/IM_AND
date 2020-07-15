package com.qbao.newim.processor;

import com.google.flatbuffers.FlatBufferBuilder;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.PackTypeDef;
import com.qbao.newim.manager.NIMContactManager;
import com.qbao.newim.manager.NIMFriendInfoManager;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.model.IMUserInfo;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.network.IProcessInterface;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.Logger;

import java.nio.ByteBuffer;

import scpack.T_GET_USER_STATUS_RQ;
import scpack.T_GET_USER_STATUS_RS;
import userpack.T_GET_USERINFO;
import userpack.T_GET_USERLST_INFO_RQ;
import userpack.T_GET_USERLST_INFO_RS;

/**
 * Created by chenjian on 2017/5/24.
 */

public class UserListProcessor extends BaseProcessor {

    private static final String TAG = UserListProcessor.class.getSimpleName();
    private ProcessorGetListRS p_user_list_rs = new ProcessorGetListRS();
    private ProcessorGetMsgStatusListRS p_user_chat_status_rs = new ProcessorGetMsgStatusListRS();
    private boolean is_query;

    public UserListProcessor() {
        Init();
    }

    @Override
    protected void Init() {
        super.Init();
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_USERLST_INFO_RS, p_user_list_rs);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_GET_USER_STATUS_RS, p_user_chat_status_rs);
    }

    public boolean SendUserIdListRQ(long[] ids) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int ids_offset = T_GET_USERLST_INFO_RQ.createUserLstVector(builder, ids);
        T_GET_USERLST_INFO_RQ.startT_GET_USERLST_INFO_RQ(builder);
        T_GET_USERLST_INFO_RQ.addUserLst(builder, ids_offset);
        super.SendRQ(PackTypeDef.NEW_DEF_USERLST_INFO_RQ, builder);
        return true;
    }

    public boolean SendUserMobileListRQ(long[] mobiles) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int ids_offset = T_GET_USERLST_INFO_RQ.createMobileLstVector(builder, mobiles);
        T_GET_USERLST_INFO_RQ.startT_GET_USERLST_INFO_RQ(builder);
        T_GET_USERLST_INFO_RQ.addMobileLst(builder, ids_offset);
        super.SendRQ(PackTypeDef.NEW_DEF_USERLST_INFO_RQ, builder);
        is_query = true;
        return true;
    }

    public boolean GetUserMsgStatusRQ() {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        T_GET_USER_STATUS_RQ.startT_GET_USER_STATUS_RQ(builder);
        super.SendRQ(PackTypeDef.NEW_DEF_GET_USER_STATUS_RQ, builder);
        return true;
    }

    class ProcessorGetListRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_GET_USERLST_INFO_RS user_list_rs = T_GET_USERLST_INFO_RS.getRootAsT_GET_USERLST_INFO_RS(byte_buffer);

            if(BodyEmpty(user_list_rs)) {
                Logger.error(TAG, "T_GET_USERLST_INFO_RS is null");
            }

            if(false == CheckHead(user_list_rs.sRsHead())) {
                Logger.error(TAG, "client get user lists rs head is null");
                int result = user_list_rs.sRsHead().result();
                String error = Integer.toHexString(result);
                Logger.error(TAG, error);
                return -1;
            }

            int length = user_list_rs.userLstInfoLength();


            for (int i = 0; i < length; i++) {
                T_GET_USERINFO t_user = user_list_rs.userLstInfo(i);

                if (t_user == null) {
                    Logger.error(TAG, "recv error head = " + user_list_rs.sRsHead());
                }

                IMUserInfo info = NIMUserInfoManager.getInstance().getIMUser(t_user.userId());
                if (info != null) {
                    info.userId = t_user.userId();
                    info.user_name = t_user.userName();
                    info.nickName = t_user.nickName();
                    info.mobile = t_user.mobile();
                    NIMUserInfoManager.getInstance().AddIMUser(info.userId, info);
                }

                // 好友列表拉取，需要存入数据库
                IMFriendInfo friendInfo = NIMFriendInfoManager.getInstance().getFriendUser(t_user.userId());
                if (friendInfo != null) {
                    friendInfo.user_name = t_user.userName();
                    friendInfo.nickName = t_user.nickName();
                    friendInfo.mobile = t_user.mobile();
                    NIMFriendInfoManager.getInstance().updateFriend(friendInfo);
                }
                // 更新用户信息
                if (is_query) {
                    IMFriendInfo contact_friend = new IMFriendInfo();
                    contact_friend.userId = t_user.userId();
                    contact_friend.nickName = t_user.nickName();
                    contact_friend.user_name = t_user.userName();
                    contact_friend.mobile = t_user.mobile();
                    contact_friend.opt_msg = "你的通讯录好友" + NIMContactManager.getInstance().getContactName(t_user.mobile());
                    contact_friend.is_select = true;
                    NIMContactManager.getInstance().addContact(contact_friend);
                }
            }

            if (is_query) {
                is_query = false;
                DataObserver.Notify(DataConstDef.EVENT_CONTACT_FRIEND, true, null);
            } else {
                // 好友列表拉取完后，获取好友免打扰状态
                GetUserMsgStatusRQ();
            }

            return 1;
        }
    }

    class ProcessorGetMsgStatusListRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_GET_USER_STATUS_RS user_status_rs = T_GET_USER_STATUS_RS.getRootAsT_GET_USER_STATUS_RS(byte_buffer);

            if(BodyEmpty(user_status_rs)) {
                Logger.error(TAG, "T_GET_USER_STATUS_RS is null");
            }

            if(false == CheckHead(user_status_rs.sRsHead())) {
                Logger.error(TAG, "client get user msg status lists rs head is null");
                int result = user_status_rs.sRsHead().result();
                String error = Integer.toHexString(result);
                Logger.error(TAG, error);
                return -1;
            }
            
            int length = user_status_rs.listUserIdsLength();
            for (int i = 0; i < length; i++) {
                long user_id = user_status_rs.listUserIds(i);
                IMFriendInfo friend_info = NIMFriendInfoManager.getInstance().getFriendUser(user_id);
                if (friend_info != null) {
                    friend_info.notify = false;
                    NIMFriendInfoManager.getInstance().updateFriend(friend_info);
                }
            }


            NIMContactManager.getInstance().getRandomContact();
            return 1;
        }
    }
}
