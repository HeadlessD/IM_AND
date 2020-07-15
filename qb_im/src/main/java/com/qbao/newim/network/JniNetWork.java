package com.qbao.newim.network;

/**
 * Created by shiyunjie on 17/3/3.
 */

public class JniNetWork
{
    static
    {
        System.loadLibrary("network-lib");
    }

    public static native boolean create();
    public static native boolean Connect(String host, int port, boolean domain);
    public static native boolean InitCallBack(IProtocolCallBack callback);
    public static native boolean Close();
    public static native int SendPack(int package_id, byte[] buff, int len);
    public static native boolean Update();
    public static native byte[] EncryptBody(byte[] input, int len);
    public static native byte[] EncryptCookie(byte[] input, int len);
    public static native long GetMicrosecond();
}
