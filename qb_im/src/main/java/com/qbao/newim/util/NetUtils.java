package com.qbao.newim.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by chenjian on 2017/4/18.
 */

public class NetUtils {
    // 判断当前是否有网络链接，true表示有网络
    public static final boolean NetAvailable(Context context) {
        if (context == null) {
            return false;
        }
        boolean netSataus = false;
        ConnectivityManager cwjManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        cwjManager.getActiveNetworkInfo();

        if (cwjManager.getActiveNetworkInfo() != null) {
            netSataus = cwjManager.getActiveNetworkInfo().isAvailable();
        }

        return netSataus;
    }

    public static boolean isWifiConnected(Context ctx){
        boolean isConnected = false;
        ConnectivityManager connManager= (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo != null && networkInfo.isConnected()){
            isConnected = true;
        }
        return isConnected;
    }
}
