package com.qbao.newim.configure;

import android.os.Environment;

/**
 * Created by chenjian on 2017/3/25.
 */

public class Constants {

    public static final String IM_SERVICE = "http://fim.qbao.com/";
    public static final String QB_USER_API = "http://api.user.qbao.com/";
    public static final String USER_QBCDN_COM = "http://user.qbcdn.com/";
    public static final String QB_SERVICE = "https://m.qbao.com/";
    public static final short APP_ID = 1002;
    public static final String MF_QBAO_COM = "http://fim.qbao.com/";

    /**
     * SDCARD 路径
     */
    public static final String SDCARD_PATH = Environment
            .getExternalStorageDirectory().getAbsolutePath();
    public static final String SYS_DATA_PATH = Environment.getDataDirectory()
            .getAbsolutePath();
    /**
     * 项目在sdcard上的跟目录
     */
    public static final String BASE_PATH = SDCARD_PATH + "/qianbao_IM";
    /**
     * 图片原图路径
     */
    public final static String ICON_PIC_DIR = BASE_PATH + "/img";
    /**
     * 图片缓存路径
     */
    public final static String ICON_CACHE_DIR = BASE_PATH + "/icon";
    /**
     * 图片缩略图路径
     */
    public final static String ICON_THUMB_DIR = ICON_CACHE_DIR + "/thumbnail";
    /**
     * 录音缓存路径
     */
    public final static String AUDIO_CACHE_DIR = BASE_PATH + "/audio";

    /**
     * 不随apk安装生命周期改变数据的路径
     */
    public final static String DATA_CACHE_DIR = BASE_PATH + "/data";

}
