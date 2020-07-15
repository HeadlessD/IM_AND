package com.qbao.newim.util;

import android.os.SystemClock;

import com.qbao.newim.constdef.ErrorCodeDef;
import com.qbao.newim.netcenter.NetCenter;

/**
 * Created by shiyunjie on 17/3/1.
 */
public class BaseUtil {
    //设置于服务器的时间差
    private static long server_delta_time = 0;

    public static boolean CheckResult(int result) {
        return (result == ErrorCodeDef.RET_SUCCESS);
    }

    public static int GetResult(int result) {
        return (result & 0x7FFFFFFF);
    }

    public static int MakeSuccessResult() {
        return ErrorCodeDef.RET_SUCCESS;
    }

    public static int MakeErrorResult(int error_code) {
        return (error_code & 0x7FFFFFFF);
    }


    //返回秒级系统启动时间
    public static long GetTickCount() {
        return (SystemClock.elapsedRealtime() / 1000);
    }

    public static String SubString(String str, int toCount) {
        int reInt = 0;
        String reStr = "";
        if (str == null)
            return "";
        char[] tempChar = str.toCharArray();
        for (int kk = 0; (kk < tempChar.length && toCount > reInt); kk++) {
            String s1 = String.valueOf(tempChar[kk]);
            byte[] b = s1.getBytes();
            if (reInt + b.length >= toCount)
                break;
            reInt += b.length;
            reStr += tempChar[kk];
        }

        byte[] tempbufer = reStr.getBytes();
        int liCount = tempbufer.length;
        if (liCount >= toCount) {
            reStr = reStr.substring(0, toCount - 1);
        }

        return reStr;
    }

    //返回秒级系统时间
    public static long GetSecondTime() {
        return System.currentTimeMillis() / 1000;
    }

    //返回微级系统时间
    public static long GetMsTime() {
        return NetCenter.getInstance().getJniTime();
    }

    //服务器时间相关
    public static void SetServerTime(long time) {
        server_delta_time = GetMsTime() - time;
    }

    public static long GetDeltaTime() {
        return server_delta_time;
    }

    public static long GetServerTime() {
        return (GetMsTime() - GetDeltaTime()) / 1000;
    }

    public static long GetServerMicroTime() {
        return GetMsTime() - GetDeltaTime();
    }

    /**
     * 把一个char转换成字节数组
     *
     * @param c 字符
     * @return 字节数组，2字节大小
     */
    public static byte[] ConvertCharToBytes(char c) {
        byte[] b = new byte[2];
        b[0] = (byte) ((c & 0xFF00) >>> 8);
        b[1] = (byte) (c & 0xFF);
        return b;
    }


    /**
     * 比较两个字节数组的内容是否相等
     *
     * @param b1 字节数组1
     * @param b2 字节数组2
     * @return true表示相等
     */
    public static boolean IsByteArrayEqual(byte[] b1, byte[] b2) {
        if (b1.length != b2.length)
            return false;

        for (int i = 0; i < b1.length; i++) {
            if (b1[i] != b2[i])
                return false;
        }
        return true;
    }
}
