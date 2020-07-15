package com.qbao.newim.model;

/**
 * Created by shiyunjie on 17/3/2.
 */

import com.google.flatbuffers.FlatBufferBuilder;
import com.qbao.newim.configure.Constants;
import com.qbao.newim.configure.GlobalVariable;
import com.qbao.newim.constdef.NetConstDef;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.BaseUtil;

import java.util.Arrays;
import java.util.List;

import syspack.T_LOGIN_RQ;

//对应fb_login_rq fbs
public class LoginModel {
    public static long UN_INIT_ID = -9999;
    public long user_id = UN_INIT_ID;
    public int ap_id = Constants.APP_ID;
    public byte[] ap_cookie;
    public String client_version = GlobalVariable.VERSION_NAME;
    public byte platform = NetConstDef.PLATFORM.APP;
    public String device_code = AppUtil.GetDeviceCode();
    public String os_type = AppUtil.GetDeviceOSType();
    public byte net_type = AppUtil.GetNetworkType();
    public String tgt = "";
    public long client_time = BaseUtil.GetMsTime();

    //just for test
    public String host = "";
    public int port = 0;
    public boolean domain = false;

    public void Serialize(FlatBufferBuilder builder) {
        String cookie = user_id + tgt + device_code + client_time;
        ap_cookie = NetCenter.getInstance().EncryptCookie(cookie.getBytes());
        int cookie_offset = builder.createByteVector(this.ap_cookie);
        int client_version_offset = builder.createString(this.client_version);
        int device_code_offset = builder.createString(this.device_code);
        int os_type_offset = builder.createString(this.os_type);
        int tgt_offset = builder.createString(this.tgt);

        T_LOGIN_RQ.startT_LOGIN_RQ(builder);
        T_LOGIN_RQ.addCookie(builder, cookie_offset);
        T_LOGIN_RQ.addClientTime(builder, this.client_time);
        T_LOGIN_RQ.addApId(builder, this.ap_id);
        T_LOGIN_RQ.addClientVersion(builder, client_version_offset);
        T_LOGIN_RQ.addDeviceCode(builder, device_code_offset);
        T_LOGIN_RQ.addNetType(builder, this.net_type);
        T_LOGIN_RQ.addOsType(builder, os_type_offset);
        T_LOGIN_RQ.addPlatform(builder, this.platform);
        T_LOGIN_RQ.addTgt(builder, tgt_offset);
    }

    public void UnSerialize() {

    }
}
