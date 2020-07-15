package com.qbao.newim.constdef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.qbao.newim.constdef.ErrorCodeDef.RET_FRIEND_BE_DELETE_ERROR;
import static com.qbao.newim.constdef.ErrorCodeDef.RET_FRIEND_HAVE_BLACK_ERROR;
import static com.qbao.newim.constdef.ErrorCodeDef.RET_GETUSERINFO_BASE;
import static com.qbao.newim.constdef.ErrorCodeDef.RET_USERINFO_BASE;

/**
 * Created by shiyunjie on 17/3/1.
 */

public class NetConstDef {
    public final static String host = "192.168.131.41";
    public final static List<Integer> port_list = Arrays.asList(8002);
    public final static boolean domain = false;
    public static int MAX_TIME_OUT = 16;                    //default 16 seconds
    public static int MAX_RESEND_TIME = 3;                  //default 3 times
    public static int MAX_RECON_TIME = 3;                   //default 3 times
    public static int HEART_BEAT_TIME = 3 * 60;             //default 3 * 60 seconds

    public interface PLATFORM {
        byte
                INVALID = 0,
                APP = 0x01,
                WEB = 0x10;
    }

    public interface NET_TYPE {
        byte
                NETWORK_TYPE_NONE = 0,
                NETWORK_TYPE_2G = 1,
                NETWORK_TYPE_3G = 2,
                NETWORK_TYPE_4G = 3,
                NETWORK_TYPE_5G = 4,                        //5G目前为猜测结果
                NETWORK_TYPE_WIFI = 5;
    }

    public enum E_NET_STATUS {
        CONNECTING,
        CONNECTED,
        LOGINING,
        LOGINED,
        DISCONNECT,
        CLOSING,
        CLOSED,
        BEKICKED,

        //delegate使用
        UPDATE_FINISHED,
        ERROR,
        CONNECT_FAIL,

        MAX_NET_STATUS
    }


    public static boolean checkSpecialCode(int error_code) {
        ArrayList<Integer> special_code_list = new ArrayList<>();
        special_code_list.add(RET_USERINFO_BASE);
        special_code_list.add(RET_GETUSERINFO_BASE);
        special_code_list.add(RET_FRIEND_HAVE_BLACK_ERROR);
        special_code_list.add(RET_FRIEND_BE_DELETE_ERROR);
        for (Integer code : special_code_list) {
            if (error_code == code) {
                return true;
            }
        }

        return false;
    }
}
