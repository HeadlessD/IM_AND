package com.qbao.newim.processor;

import android.text.TextUtils;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.qbao.newim.business.ApiRequest;
import com.qbao.newim.configure.GlobalVariable;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.ErrorCodeDef;
import com.qbao.newim.constdef.NetConstDef;
import com.qbao.newim.constdef.PackTypeDef;
import com.qbao.newim.constdef.StateConstDef;
import com.qbao.newim.manager.NIMFriendInfoManager;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.HttpUserInfo;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.model.IMUserInfo;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.network.IProcessInterface;
import com.qbao.newim.util.BaseUtil;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.Logger;
import com.qbao.newim.util.ShowUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import scpack.T_SINGLE_CHAT_STATUS_RQ;
import scpack.T_SINGLE_CHAT_STATUS_RS;
import userpack.T_GET_ME_INFO_RQ;
import userpack.T_GET_ME_INFO_RS;
import userpack.T_GET_USER_INFO_RQ;
import userpack.T_GET_USER_INFO_RS;
import userpack.T_USER_COMPLAINT_RQ;

/**
 * Created by chenjian on 2017/5/10.
 */

public class UserInfoGetProcessor extends BaseProcessor {
    private static final String TAG = UserInfoGetProcessor.class.getSimpleName();
    private ProcessorUserRS p_user_rs = new ProcessorUserRS();
    private ProcessorSelfRS p_self_rs = new ProcessorSelfRS();
    private ProcessorUserMsgStatusRS p_msg_status_rs = new ProcessorUserMsgStatusRS();

    public UserInfoGetProcessor() {
        Init();
    }

    @Override
    protected void Init() {
        super.Init();
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_USER_INFO_RS, p_user_rs);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_ME_INFO_RS, p_self_rs);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_SINGLE_CHAT_STATUS_RS, p_msg_status_rs);
    }

    public boolean SendUserInfoRQ(String msg) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int msg_offset = builder.createString(msg);
        T_GET_USER_INFO_RQ.startT_GET_USER_INFO_RQ(builder);
        T_GET_USER_INFO_RQ.addUserMsg(builder, msg_offset);
        super.SendRQ(PackTypeDef.NEW_DEF_USER_INFO_RQ, builder);
        return true;
    }

    public boolean SendSelfInfoRQ(String token) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int token_offset = builder.createString(token);
        T_GET_ME_INFO_RQ.startT_GET_ME_INFO_RQ(builder);
        T_GET_ME_INFO_RQ.addToken(builder, token_offset);
        super.SendRQ(PackTypeDef.NEW_DEF_ME_INFO_RQ, builder);
        return true;
    }

    public boolean SendMsgStatus(long user_id, byte status) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        T_SINGLE_CHAT_STATUS_RQ.startT_SINGLE_CHAT_STATUS_RQ(builder);
        T_SINGLE_CHAT_STATUS_RQ.addOpUserId(builder, user_id);
        T_SINGLE_CHAT_STATUS_RQ.addChatStatus(builder, status);
        super.SendRQ(PackTypeDef.NEW_DEF_SINGLE_CHAT_STATUS_RQ, builder);
        return true;
    }

    public boolean SendReportUser(long user_id, byte type, String reason) {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        int reason_offset = builder.createString(reason);
        T_USER_COMPLAINT_RQ.startT_USER_COMPLAINT_RQ(builder);
        T_USER_COMPLAINT_RQ.addUserId(builder, user_id);
        T_USER_COMPLAINT_RQ.addType(builder, type);
        T_USER_COMPLAINT_RQ.addReason(builder, reason_offset);
        super.SendRQ(PackTypeDef.NEW_DEF_USER_COMPLAINT_RQ, builder);
        return true;
    }

    class ProcessorUserRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_GET_USER_INFO_RS user_rs = T_GET_USER_INFO_RS.getRootAsT_GET_USER_INFO_RS(byte_buffer);

            if (BodyEmpty(user_rs)) {
                Logger.error(TAG, "T_GET_USER_INFO_RS is null");
                return -1;
            }

            if (false == CheckHead(user_rs.sRsHead())) {
                Logger.error(TAG, "client get user info rs head is null");
                return -1;
            }

            IMUserInfo userInfo = new IMUserInfo();
            boolean result = userInfo.UnUserSerialize(user_rs);
            if (!result) {
                Logger.error(TAG, "recv error head = " + user_rs.sRsHead());
                NetCenter.getInstance().DisConnect();
                return -1;
            }

            int error_code = BaseUtil.MakeErrorResult(user_rs.sRsHead().result());
            if (error_code == ErrorCodeDef.RET_USERINFO_BASE) {
                String return_msg = user_rs.userMsg();
                DataObserver.Notify(DataConstDef.EVENT_GET_USER_INFO, return_msg, false);
                NetCenter.getInstance().DisConnect();
                return -1;
            }

            DataObserver.Notify(DataConstDef.EVENT_GET_USER_INFO, userInfo, true);
            IMFriendInfo friendInfo = NIMFriendInfoManager.getInstance().getFriendUser(userInfo.userId);
            if (friendInfo != null) {
                NIMFriendInfoManager.getInstance().userToFriend(userInfo, friendInfo);
            }

            NIMUserInfoManager.getInstance().AddIMUser(userInfo.userId, userInfo);
            return 1;
        }
    }

    class ProcessorSelfRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_GET_ME_INFO_RS self_rs = T_GET_ME_INFO_RS.getRootAsT_GET_ME_INFO_RS(byte_buffer);

            if (BodyEmpty(self_rs)) {
                Logger.error(TAG, "T_GET_SELF_INFO_RS is null");
                return -1;
            }

            if (false == CheckHead(self_rs.sRsHead())) {
                Logger.error(TAG, "client get self info rs head is null");
                return -1;
            }

            IMUserInfo userInfo = new IMUserInfo();
            userInfo.UnSelfSerialize(self_rs);


            int error_code = BaseUtil.MakeErrorResult(self_rs.sRsHead().result());
            if (error_code == ErrorCodeDef.RET_USERINFO_BASE) {
                NIMUserInfoManager.getInstance().AddIMUser(userInfo.userId, userInfo);
                getUserInfoFromHttp();
                return 1;
            }

            //通知状态机状态完成
            DataObserver.Notify(DataConstDef.EVENT_STATE_MACHINE_FINISH, StateConstDef.USER_INFO, null);
            if (error_code == ErrorCodeDef.RET_GETUSERINFO_BASE) {
                return 1;
            }
            NIMUserInfoManager.getInstance().AddIMUser(userInfo.userId, userInfo);
            return 1;
        }
    }

    class ProcessorUserMsgStatusRS implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_SINGLE_CHAT_STATUS_RS chat_status_rs = T_SINGLE_CHAT_STATUS_RS.getRootAsT_SINGLE_CHAT_STATUS_RS(byte_buffer);

            if (BodyEmpty(chat_status_rs)) {
                DataObserver.Notify(DataConstDef.EVENT_FRIEND_MSG_STATUS, null, null);
                Logger.error(TAG, "T_SINGLE_CHAT_STATUS_RS is null");
                return -1;
            }

            if (false == CheckHead(chat_status_rs.sRsHead())) {
                DataObserver.Notify(DataConstDef.EVENT_FRIEND_MSG_STATUS, null, null);
                Logger.error(TAG, "client get msg status rs head is null");
                return -1;
            }

            IMFriendInfo friendInfo = NIMFriendInfoManager.getInstance().getFriendUser(chat_status_rs.opUserId());
            if (friendInfo != null) {
                friendInfo.notify = chat_status_rs.chatStatus() == 0;
                NIMFriendInfoManager.getInstance().updateFriend(friendInfo);
            }

            DataObserver.Notify(DataConstDef.EVENT_FRIEND_MSG_STATUS, friendInfo.userId, friendInfo.notify);
            return 1;
        }
    }

    // 获取部分用户信息
    private void getUserInfoFromHttp() {
        String url = "http://ucslaveapi.qbao.com/api/load/userInfo/userId";
        HashMap<String, String> maps = new HashMap<>();
        maps.put("userId", "" + NIMUserInfoManager.getInstance().GetSelfUserId());
        maps.put("appId", "im_service");
        Call<ResponseBody> call = ApiRequest.getApiQbao().getUserInfo(url, maps);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String dataString = response.body().string();
                        JsonObject obj = new JsonParser().parse(dataString).getAsJsonObject();
                        JsonElement element = obj.get("data");
                        if (element == null || element.toString().equals("null")) {
                            ShowUtils.showToast("userInfo用户数据为空");
                            getUserInfoFromHttp(new IMUserInfo());
                            return;
                        }
                        JsonObject data = obj.get("data").getAsJsonObject();
                        HttpUserInfo httpUserInfo = new Gson().fromJson(data, HttpUserInfo.class);
                        IMUserInfo info = new IMUserInfo();
                        info.nickName = httpUserInfo.getNickName();
                        info.userId = NIMUserInfoManager.getInstance().GetSelfUserId();
                        info.locationPro = httpUserInfo.getLocationPlaceProvince();
                        info.birthday = httpUserInfo.getBirthday();
                        info.locationCity = httpUserInfo.getLocationPlaceCity();
                        info.mail = httpUserInfo.getEmail() == null ? "" : httpUserInfo.getEmail();
                        if (httpUserInfo.getSex() == null) {
                            info.sex = 2;
                        } else {
                            info.sex = httpUserInfo.getSex().equals("M") ? 1 : 2;
                        }
                        info.signature = httpUserInfo.getSelfIntroduction() == null ? "" : httpUserInfo.getSelfIntroduction();
                        info.user_name = NIMUserInfoManager.getInstance().GetSelfUserName();
                        getUserInfoFromHttp(info);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Logger.error(TAG, "");
            }
        });
    }

    // 获取另一部分用户信息
    private void getUserInfoFromHttp(final IMUserInfo userInfo) {
        String url = "http://ucslaveapi.qbao.com/api/load/userDetail/userId";
        HashMap<String, String> maps = new HashMap<>();
        maps.put("userId", "" + NIMUserInfoManager.getInstance().GetSelfUserId());
        maps.put("appId", "im_service");
        Call<ResponseBody> call = ApiRequest.getApiQbao().getUserInfo(url, maps);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String dataString = response.body().string();
                        JsonObject obj = new JsonParser().parse(dataString).getAsJsonObject();
                        if (obj.get("data").getAsJsonObject() == null) {
                            ShowUtils.showToast("userDetail用户数据为空");
                            // TODO: 2017/9/20 by 史云杰 陈建确认下这边逻辑是否可以注解
//                            if (TextUtils.isEmpty(userInfo.nickName) && TextUtils.isEmpty(userInfo.user_name)) {
//                                GlobalVariable.is_connected = false;
//                            }
                            return;
                        }
                        JsonObject data = obj.get("data").getAsJsonObject();
                        HttpUserInfo httpUserInfo = new Gson().fromJson(data, HttpUserInfo.class);
                        userInfo.user_name = httpUserInfo.getUsername();
                        userInfo.mail = httpUserInfo.getEmail() == null ? "" : httpUserInfo.getEmail();
                        userInfo.mobile = Long.parseLong(httpUserInfo.getMobile());
                        userInfo.userId = NIMUserInfoManager.getInstance().GetSelfUserId();
                        addUserToTcpService(userInfo);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Logger.error(TAG, "");
            }
        });
    }

    private void addUserToTcpService(IMUserInfo info) {
//        IMUserInfo info1 = new IMUserInfo();
//        info1.user_name = "u5504160";
//        info1.nickName = "姚琉灵";
//        info1.signature = "海底月是天上月，眼前人是心上人";
//        info1.mail = "yll_160@qbao.com";
//        info1.birthday = 495872073000l;
//        info1.locationCity = "杭州";
//        info1.locationPro = "浙江";
//        info1.sex = 2;
//        info1.mobile = 18000000000l;
//        info1.user_name = "u_5504144";
//        info1.nickName = "姚思思144";
//        info1.signature = "来都来了，就住一晚吧";
//        info1.mail = "yss150@qbao.com";
//        info1.birthday = 643197041;
//        info1.locationCity = "杭州";
//        info1.locationPro = "浙江";
//        info1.sex = 2;
//        info1.mobile = "15044444444";
//        info1.user_name = "u_5504149";
//        info1.nickName = "姚仕玖149";
//        info1.signature = "当时明月在，曾照彩云归";
//        info1.mail = "ysj149@qbao.com";
//        info1.birthday = 643197041;
//        info1.locationCity = "浦东新区";
//        info1.locationPro = "上海";
//        info1.sex = 1;
//        info1.mobile = "15014900149";
//        info1.user_name = "u_5504151";
//        info1.nickName = "姚舞瑶151";
//        info1.signature = "衣不如新，人不如故";
//        info1.mail = "ywy151@qbao.com";
//        info1.birthday = 643197041;
//        info1.locationCity = "徐汇区";
//        info1.locationPro = "上海";
//        info1.sex = 2;
//        info1.mobile = "15015100151";
        UserUpdateProcessor processor = GlobalProcessor.getInstance().getUpload_user_processor();
        processor.uploadUserInfo(info, true);
    }
}
