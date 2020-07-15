package com.qbao.newim.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by chenjian on 2017/4/6.
 * 存储键盘的高度到SharedPreference
 */

public class SharedPreferenceUtil {
    private final static String FILE_NAME = "sp.key.file";

    private final static String KEY_KEYBOARD_HEIGHT = "sp.key.keyboard.height";

    private final static String FRIENDS_TOKEN = "friends.token";

    private final static String FRIENDS_UNREAD = "friends.unread";

    private final static String MSG_VOICE = "msg.voice";

    private final static String MSG_VIBRATE = "msg.vibrate";

    private final static String MSG_PLAY = "msg.PLAY";

    private final static String USER_NAME = "user.name";

    private final static String USER_PWD = "user.password";

    private final static String USER_ID = "user.id";

    private final static String NET_HOST = "host";

    private final static String NET_PORT = "port";

    private final static String NET_DOMAIN = "domain";

    private final static String TASK_TIME = "task_time";

    private final static String SUBSCRIBE_TIME = "subscribe_time";

    private volatile static SharedPreferences SP;

    public static boolean saveNetHost(String host) {
        return with().edit().putString(NET_HOST, host).commit();
    }

    public static boolean saveTaskTime(long time) {
        return with().edit().putLong(TASK_TIME, time).commit();
    }

    public static long getTaskTime() {
        return with().getLong(TASK_TIME, 0);
    }

    public static boolean saveSubscribeTime(long time) {
        return with().edit().putLong(SUBSCRIBE_TIME, time).commit();
    }

    public static long getSubscribeTime() {
        return with().getLong(SUBSCRIBE_TIME, 0);
    }

    public static String getNetHost() {
        return with().getString(NET_HOST, "");
    }

    public static int getNetPort() {
        return with().getInt(NET_PORT, 0);
    }

    public static boolean saveNetPort(int port) {
        return with().edit().putInt(NET_PORT, port).commit();
    }

    public static boolean getNetDomain() {
        return with().getBoolean(NET_DOMAIN, false);
    }

    public static boolean saveNetDomain(boolean domain) {
        return with().edit().putBoolean(NET_DOMAIN, domain).commit();
    }

    public static long getUserId() {
        return with().getLong(USER_ID, 0);
    }

    public static boolean saveUserId(long user_id) {
        return with().edit().putLong(USER_ID, user_id).commit();
    }

    public static String getUserName() {
        return with().getString(USER_NAME, "");
    }

    public static boolean saveUserName(String user_name) {
        return with().edit().putString(USER_NAME, user_name).commit();
    }

    public static String getUserPassword() {
        return with().getString(USER_PWD, "");
    }

    public static boolean saveUserPassword(String user_pwd) {
        return with().edit().putString(USER_PWD, user_pwd).commit();
    }

    public static long getFriendsToken() {
        return with().getLong(FRIENDS_TOKEN, 0);
    }

    public static boolean saveFriendsToken(long token) {
        return with().edit().putLong(FRIENDS_TOKEN, token).commit();
    }

    public static int getFriendUnread() {
        return with().getInt(FRIENDS_UNREAD, 0);
    }

    public static boolean saveFriendUnread(int count) {
        return with().edit().putInt(FRIENDS_UNREAD, count).commit();
    }

    // 消息是否震动提示，默认震动提示
    public static boolean getMsgVibrate() {
        return with().getBoolean(MSG_VIBRATE, true);
    }
    public static void saveMsgVibrate(boolean is_active) {
        with().edit().putBoolean(MSG_VIBRATE, is_active).commit();
    }

    // 语音消息是否听筒播放，默认听筒播放
    public static int getAudioMode() {
        return with().getInt(MSG_PLAY, 0);
    }
    public static void saveAudioMode(int mode) {
        with().edit().putInt(MSG_PLAY, mode).commit();
    }


    // 消息提示音，默认声音提示
    public static boolean getMsgVoice() {
       return with().getBoolean(MSG_VOICE, true);
    }
    public static void saveMsgVoice(boolean is_active) {
        with().edit().putBoolean(MSG_VOICE, is_active).commit();
    }

    public static boolean saveKeyBoardHeight(int keyboardHeight) {
        return with().edit()
                .putInt(KEY_KEYBOARD_HEIGHT, keyboardHeight)
                .commit();
    }

    private static SharedPreferences with() {
        if (SP == null) {
            synchronized (SharedPreferenceUtil.class) {
                if (SP == null) {
                    SP = AppUtil.GetContext().
                            getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
                }
            }
        }

        return SP;
    }

    public static int getKeyBoardHeight(final int defaultHeight) {
        return with().getInt(KEY_KEYBOARD_HEIGHT, defaultHeight);
    }

    public static void clear() {
        saveFriendsToken(0);
        saveAudioMode(0);
        saveMsgVibrate(true);
        saveMsgVoice(true);
        saveFriendUnread(0);
    }
}
