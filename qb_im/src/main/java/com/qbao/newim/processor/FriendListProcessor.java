package com.qbao.newim.processor;

import android.text.TextUtils;

import com.google.flatbuffers.FlatBufferBuilder;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.FriendTypeDef;
import com.qbao.newim.constdef.PackTypeDef;
import com.qbao.newim.constdef.StateConstDef;
import com.qbao.newim.manager.NIMContactManager;
import com.qbao.newim.manager.NIMFriendInfoManager;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.model.IMUserInfo;
import com.qbao.newim.model.NIM_FriendInfo;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.network.IProcessInterface;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.Logger;
import com.qbao.newim.util.SharedPreferenceUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import friendpack.T_FRIEND_LIST_RQ;
import friendpack.T_FRIEND_LIST_RS;

/**
 * Created by chenjian on 2017/4/26.
 */

public class FriendListProcessor extends BaseProcessor{
    private static final String TAG = FriendListProcessor.class.getSimpleName();
    private ProcessorFriendRS p_friend_rs = new ProcessorFriendRS();
    private final static int NUMBER = 50;
    private ArrayList<NIM_FriendInfo> friendInfos = new ArrayList<>();
    private int nPage;

    public FriendListProcessor() {
        Init();
    }

    @Override
    protected void Init() {
        super.Init();
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_FRIEND_LIST_RS, p_friend_rs);
    }

    @Override
    protected void ProcessEvent() {
    }

    public boolean sendFriendsRQ(int page) {
        FlatBufferBuilder bufferBuilder = new FlatBufferBuilder();

        T_FRIEND_LIST_RQ.startT_FRIEND_LIST_RQ(bufferBuilder);
        T_FRIEND_LIST_RQ.addOffset(bufferBuilder, page * NUMBER);
        long token = SharedPreferenceUtil.getFriendsToken();
        T_FRIEND_LIST_RQ.addToken(bufferBuilder, token);
        super.SendRQ(PackTypeDef.NEW_DEF_FRIEND_LIST_RQ, bufferBuilder);
        return true;
    }

    class ProcessorFriendRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_FRIEND_LIST_RS friend_rs = T_FRIEND_LIST_RS.getRootAsT_FRIEND_LIST_RS(byte_buffer);

            if(BodyEmpty(friend_rs)) {
                Logger.error(TAG, "T_FRIEND_LIST_RS is null");
                return -1;
            }

            if(false == CheckHead(friend_rs.sRsHead())) {
                Logger.error(TAG, "client get friend list rs head is null");
                return -1;
            }


            int friend_length = friend_rs.friendListLength();

            if (friend_length == 0) {
                DataObserver.Notify(DataConstDef.EVENT_UPDATE_ALL_SESSION, true, null);
                //通知状态机状态完成
                DataObserver.Notify(DataConstDef.EVENT_STATE_MACHINE_FINISH, StateConstDef.FRIEND_INFO, null);
                return 1;
            }

            for (int i = 0; i < friend_length; i++) {
                friendpack.T_FREIND_INFO friend_info_pack = friend_rs.friendList(i);
                NIM_FriendInfo friendInfo = new NIM_FriendInfo();
                friendInfo.UnSerialize(friend_info_pack);
                friendInfos.add(friendInfo);
            }

            // 如果当前可操作的好友列表大于30个，则当前偏移增加，继续访问,否则存储token
            if (friend_length >= NUMBER) {
                nPage++;
                sendFriendsRQ(nPage);
            } else {
                handleFriendsInfo(friend_rs.token());
                DataObserver.Notify(DataConstDef.EVENT_UPDATE_ALL_SESSION, true, null);
                //通知状态机状态完成
                DataObserver.Notify(DataConstDef.EVENT_STATE_MACHINE_FINISH, StateConstDef.FRIEND_INFO, null);
            }

            return 1;
        }
    }

    private void handleFriendsInfo(long token) {
        SharedPreferenceUtil.saveFriendsToken(token);
        int size = friendInfos.size();
        // 好友操作消息有重复好友，需要去除重复好友id
        ArrayList<Long> user_list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            NIM_FriendInfo info = friendInfos.get(i);
            if (!user_list.contains(info.user_id)) {
                user_list.add(info.user_id);
                IMUserInfo userInfo = new IMUserInfo();
                userInfo.userId = info.user_id;
                NIMUserInfoManager.getInstance().AddIMUser(userInfo.userId, userInfo);
            }

            handleFriendOpt(info);
        }

        if (user_list.size() > 0 ) {
            long[] user_array = new long[user_list.size()];
            for (int i = 0; i < user_array.length; i++) {
                user_array[i] = user_list.get(i);
            }
            getFriendDetailInfo(user_array);
        }
    }

    private void handleFriendOpt(NIM_FriendInfo info) {
        IMFriendInfo friend_info = NIMFriendInfoManager.getInstance().getFriendUser(info.user_id);
        if (friend_info == null) {
            friend_info = new IMFriendInfo();
            friend_info.userId = info.user_id;
        }
        friend_info.source_type = info.source_type;
        // 判断黑名单类型
        if (info.black_type > FriendTypeDef.ACTIVE_TYPE.INVALID
                && info.opt_type != FriendTypeDef.FRIEND_LIST_TYPE.FD_BLACK_OP) {
            friend_info.black_type = info.black_type;
        }

        boolean delete = false;

        switch (info.opt_type) {
            case FriendTypeDef.FRIEND_LIST_TYPE.FD_FRIEND_OP:
                friend_info.status = FriendTypeDef.FRIEND_ADD_TYPE.FRIEND;
                friend_info.remark_name = info.remark_name;
                break;
            case FriendTypeDef.FRIEND_LIST_TYPE.FD_PEER_CONFIRM_OP:
                friend_info.status = FriendTypeDef.FRIEND_ADD_TYPE.PEER_CONFIRM;
                NIMFriendInfoManager.getInstance().AddFriend(friend_info);
                return;
            case FriendTypeDef.FRIEND_LIST_TYPE.FD_WAIT_CONFIRM_OP:
                friend_info.status = FriendTypeDef.FRIEND_ADD_TYPE.SEND_REQUEST;
                break;
            case FriendTypeDef.FRIEND_LIST_TYPE.FD_NEED_CONFIRM_OP:
                friend_info.status = FriendTypeDef.FRIEND_ADD_TYPE.ACCEPT_REQUEST;
                friend_info.opt_msg = info.op_msg;
                if (TextUtils.isEmpty(friend_info.opt_msg)) {
                    friend_info.opt_msg = friend_info.nickName + "请求添加你为好友";
                }
                NIMFriendInfoManager.getInstance().AddFriend(friend_info);
                NIMFriendInfoManager.getInstance().addUnreadCount();
                return;
            case FriendTypeDef.FRIEND_LIST_TYPE.FD_OWN_CONFIRM_OP:
                friend_info.status = FriendTypeDef.FRIEND_ADD_TYPE.OWN_CONFIRM;
                NIMFriendInfoManager.getInstance().AddFriend(friend_info);
                return;
            case FriendTypeDef.FRIEND_LIST_TYPE.FD_REMARK_OP:
                friend_info.remark_name = info.remark_name;
                friend_info.status = FriendTypeDef.FRIEND_ADD_TYPE.FRIEND;
                break;
            case FriendTypeDef.FRIEND_LIST_TYPE.FD_BLACK_OP:
                switch (info.black_type) {
                    case 0:                                                                         //主动清除黑名单
                        if (friend_info.black_type == FriendTypeDef.ACTIVE_TYPE.ACTIVE) {
                            friend_info.black_type = FriendTypeDef.ACTIVE_TYPE.INVALID;             /////////// 原主动变清除
                        } else if (friend_info.black_type == FriendTypeDef.ACTIVE_TYPE.EACH) {
                            friend_info.black_type = FriendTypeDef.ACTIVE_TYPE.PASSIVE;             /////////// 原互相变被动
                        }
                        break;
                    case 1:                                                                         //被动解除黑名单
                    if (friend_info.black_type == FriendTypeDef.ACTIVE_TYPE.PASSIVE) {
                            friend_info.black_type = FriendTypeDef.ACTIVE_TYPE.INVALID;             /////////// 原被动变清除
                        } else if (friend_info.black_type == FriendTypeDef.ACTIVE_TYPE.EACH) {
                            friend_info.black_type = FriendTypeDef.ACTIVE_TYPE.ACTIVE;              /////////// 原互相变主动
                        }
                        break;
                    case 2:                                                                         //主动加入黑名单
                        if (friend_info.black_type == FriendTypeDef.ACTIVE_TYPE.PASSIVE) {
                            friend_info.black_type = FriendTypeDef.ACTIVE_TYPE.EACH;                /////////// 原被动变互相
                        } else if (friend_info.black_type == FriendTypeDef.ACTIVE_TYPE.INVALID) {
                            friend_info.black_type = FriendTypeDef.ACTIVE_TYPE.ACTIVE;              /////////// 原清除变主动
                        }
                        break;
                    case 3:                                                                         //被动加入黑名单
                        if (friend_info.black_type == FriendTypeDef.ACTIVE_TYPE.INVALID) {
                            friend_info.black_type = FriendTypeDef.ACTIVE_TYPE.PASSIVE;             /////////// 原清除变被动
                        } else if (friend_info.black_type == FriendTypeDef.ACTIVE_TYPE.ACTIVE) {
                            friend_info.black_type = FriendTypeDef.ACTIVE_TYPE.EACH;                /////////// 原主动变互相
                        }
                        break;
                }
                break;
            case FriendTypeDef.FRIEND_LIST_TYPE.FD_PEER_DEL_OP:
                friend_info.delete_type = FriendTypeDef.ACTIVE_TYPE.PASSIVE;
                friend_info.status = FriendTypeDef.FRIEND_ADD_TYPE.FRIEND;
                break;
            case FriendTypeDef.FRIEND_LIST_TYPE.FD_OWN_DEL_OP:
                friend_info.delete_type = FriendTypeDef.ACTIVE_TYPE.ACTIVE;
                delete = true;
                break;
            case FriendTypeDef.FRIEND_LIST_TYPE.FD_INVALID_OP:
                friend_info.status = FriendTypeDef.FRIEND_ADD_TYPE.DELETE;
                break;
            case FriendTypeDef.FRIEND_LIST_TYPE.FD_RESTORE_OP:
                friend_info.status = FriendTypeDef.FRIEND_ADD_TYPE.RESTART_ADD;
                break;
        }

        if (delete) {
            NIMFriendInfoManager.getInstance().delFriend(info.user_id);
        } else {
            NIMFriendInfoManager.getInstance().AddFriend(friend_info);
        }
    }


    private void getFriendDetailInfo(long[] user_ids) {
        UserListProcessor user_list_processor = GlobalProcessor.getInstance().getUser_list_processor();
        user_list_processor.SendUserIdListRQ(user_ids);
        friendInfos.clear();
    }

//    private void AddFriendToMsg(NIM_FriendInfo info) {
//        IMFriendInfo friend_info;
//        switch (info.opt_type) {
//            case FriendTypeDef.FRIEND_LIST_TYPE.FRIEND_ADD_RQ:
//                NIM_FriendAddInfo add_info = new NIM_FriendAddInfo();
//                add_info.status = FriendTypeDef.FRIEND_ADD_TYPE.ACCEPT_REQUEST;
//                add_info.peer_user_id = info.user_id;
//                add_info.source_type = info.source_type;
//                add_info.op_msg = info.op_msg;
//
//                NIMFriendAddManager.getInstance().addFriendRequest(info.user_id, add_info);
//                break;
//            case FriendTypeDef.FRIEND_LIST_TYPE.FRIEND_DELETE:
//                friend_info = FriendUserDbManager.getInstance().getSingleFriend(info.user_id);
//
//                if (friend_info == null) {
//                    return;
//                }
//
//                FriendUserDbManager.getInstance().delete(friend_info);
//                break;
//            case FriendTypeDef.FRIEND_LIST_TYPE.FRIEND_REMARK:
//                friend_info = FriendUserDbManager.getInstance().getSingleFriend(info.user_id);
//
//                if (friend_info == null) {
//                    friend_info = new IMFriendInfo();
//                    friend_info.remark_name = info.remark_name;
//                    friend_info.pinyin = Utils.converterToSpell(friend_info.remark_name);
//                    NIMFriendInfoManager.getInstance().AddFriend(info.user_id, friend_info);
//                } else {
//                    friend_info.remark_name = info.remark_name;
//                    friend_info.pinyin = Utils.converterToSpell(friend_info.remark_name);
//                    NIMFriendInfoManager.getInstance().updateFriend(friend_info);
//                }
//
//                break;
//            case FriendTypeDef.FRIEND_LIST_TYPE.FRIEND_REFUSE:
//                NIM_FriendAddInfo add_info_refuse = new NIM_FriendAddInfo();
//                add_info_refuse.status = FriendTypeDef.FRIEND_ADD_TYPE.ACCEPT_REFUSE;
//                add_info_refuse.peer_user_id = info.user_id;
//                add_info_refuse.source_type = info.source_type;
//                add_info_refuse.result = FriendTypeDef.FRIEND_RESULT.REFUSE;
//
//                NIMFriendAddManager.getInstance().addFriendRequest(info.user_id, add_info_refuse);
//                break;
//        }
//    }
}
