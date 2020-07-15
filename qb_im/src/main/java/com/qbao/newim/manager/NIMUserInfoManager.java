package com.qbao.newim.manager;

import android.text.TextUtils;

import com.qbao.newim.constdef.NetConstDef;
import com.qbao.newim.model.IMUserInfo;
import com.qbao.newim.model.LoginModel;
import com.qbao.newim.qbdb.manager.UserInfoDbManager;
import com.qbao.newim.util.Logger;
import com.qbao.newim.util.SharedPreferenceUtil;

import java.util.HashMap;

/**
 * Created by chenjian on 2017/6/19.
 */

public class NIMUserInfoManager {
    private static final String TAG = "NIMUserInfoManager";
    private static NIMUserInfoManager _instance = new NIMUserInfoManager();

    public static NIMUserInfoManager getInstance() {
        return _instance;
    }

    private LoginModel self_login_model = new LoginModel();
    private HashMap<Long, IMUserInfo> im_user_map = new HashMap<>();

    // 添加本地登录用户
    public void SetLoginModel(long user_id, String tgt) {
        SharedPreferenceUtil.saveUserId(user_id);
        SharedPreferenceUtil.saveUserPassword(tgt);
        self_login_model.user_id = user_id;
        self_login_model.tgt = tgt;
    }

    public long GetSelfUserId()
    {
        return GetLoginModel().user_id;
    }

    public LoginModel GetLoginModel() {
        //lazy init
        if(self_login_model.user_id == LoginModel.UN_INIT_ID)
        {
            self_login_model.user_id = SharedPreferenceUtil.getUserId();
            self_login_model.tgt = SharedPreferenceUtil.getUserPassword();
            //just for test
            self_login_model.host = SharedPreferenceUtil.getNetHost();
            self_login_model.port = SharedPreferenceUtil.getNetPort();
            self_login_model.domain = SharedPreferenceUtil.getNetDomain();
        }

        return self_login_model;
    }

    public void SetConnectInfo(String host, int port, boolean domain)
    {
        self_login_model.host = host;
        self_login_model.port = port;
        self_login_model.domain = domain;
        SharedPreferenceUtil.saveNetHost(host);
        SharedPreferenceUtil.saveNetPort(port);
        SharedPreferenceUtil.saveNetDomain(domain);
    }

    public void AddIMUser(long user_id, IMUserInfo info) {
        AddIMUser(user_id, info, true);
    }

    // 添加某个用户,是否存进数据库
    public void AddIMUser(long user_id, IMUserInfo info, boolean bWriteSQL) {
        if (user_id <= 0 || info == null) {
            Logger.warning(TAG, "invalid user");
            return;
        }

        if (im_user_map.containsKey(user_id)) {
            Logger.warning(TAG, "user_id = " + user_id + "is exist");
            im_user_map.remove(user_id);
        }

        im_user_map.put(user_id, info);

        if (bWriteSQL) {
            UserInfoDbManager.getInstance().insertOrReplace(info);
        }
    }

    // 删除某个用户
    public void delIMUser(long user_id) {
        if (user_id < 0) {
            return;
        }

        IMUserInfo info = UserInfoDbManager.getInstance().getSingleIMUser(user_id);

        if (info == null) {
            return;
        }

        if (im_user_map.containsKey(user_id)) {
            im_user_map.remove(user_id);
            UserInfoDbManager.getInstance().delete(info);
        }
    }

    // 获取当前登录用户信息
    public IMUserInfo getSelfUser() {
        return getIMUser(GetSelfUserId());
    }

    // 获取某个用户信息
    public IMUserInfo getIMUser(long user_id) {
        if (user_id <= 0) {
            Logger.warning(TAG, "user not exist");
            return null;
        }

        IMUserInfo info ;
        if (!im_user_map.containsKey(user_id)) {
            info = UserInfoDbManager.getInstance().getSingleIMUser(user_id);
        } else {
            info = im_user_map.get(user_id);
        }

        return info;
    }


    public String getSelfName() {
        return getIMUser(GetSelfUserId()).nickName;
    }

    // 更新某个用户信息
    public void updateUser(IMUserInfo userInfo) {
        if (userInfo != null) {
            im_user_map.put(userInfo.userId, userInfo);
            UserInfoDbManager.getInstance().update(userInfo);
        }
    }


    // 获取当前登录用户名字
    public String GetSelfUserName() {
        // 优先 显示顺序 nick_name > username
        String show_name;
        IMUserInfo userInfo = getIMUser(GetSelfUserId());
        if (userInfo == null) {
            return "";
        }

        if (TextUtils.isEmpty(userInfo.nickName)) {
            show_name = userInfo.user_name;
        } else {
            show_name = userInfo.nickName;
        }
        return show_name;
    }


    public void clear() {
        im_user_map.clear();
        self_login_model = null;
        self_login_model = new LoginModel();
    }
}
