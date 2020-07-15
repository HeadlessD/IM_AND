package com.qbao.newim.configure;

/**
 * Created by chenjian on 2017/3/25.
 */

public class GlobalVariable {

    public static String VERSION_NAME = "1.1.100";
    public static int VERSION_CODE = 1;

    /**
     * 是否正在录音
     */
    public static boolean isRecording = false;
    /**
     * 是否允许将日志写入文件
     */
    public static boolean isAllowWriteLogFile =  Configuration.DEBUG;
    /**
     * user-agent
     */
    public static  String USER_AGENT = "qbaonew-android/";
    public static final boolean is_CheckSSL = false;
    // 正在聊天的friend jid
    public static String thread = "";
    public final static String PKG_NAME = "com.qianwang.qianbao.im";

    //系统消息固定ID[和服务器对应]
    public static final long SYS_MESSAGE_SESSION_ID = 1000L;

    // 群助手会话ID，固定ID
    public static final long GROUP_ASSIST_SESSION_ID = 1003l;
    // 任务助手会话ID，固定ID
    public static final long TASK_SESSION_ID = 1004l;
    // 订阅助手会话ID,固定ID
    public static final long SUBSCRIBE_SESSION_ID = 1005l;
    // 公众号会话ID，固定ID
    public static final long PUBLIC_SESSION_ID = 1006l;
    // 好友个数上线
    public static final int FRIEND_MAX_COUNT = 2000;
    // 验证消息字数限制
    public static final int VERIFY_MAX_LENGTH = 26;
    // 好友备注字数限制
    public static final int REMARK_MAX_LENGTH = 24;
    // 群组名字字数限制
    public static final int REMARK_GROUP_LENGTH = 32;
    // 群组昵称名字字数限制
    public static final int REMARK_GROUP_NICK_LENGTH = 24;
    // 群主一次最多踢人数
    public static final int MANAGER_KICK_COUNT = 50;
    // 群离线个数一次最多获取
    public static final int GROUP_OFFLINE_COUNT = 10;
    // 举报用户输入字数限制
    public static final int REPORT_USER_COUNT = 140;
    // 群详情获取个数一次最多
    public static final int GROUP_DETAIL_COUNT = 20;
    // 聊天消息个数一次输入最多
    public static final int SEND_MESSAGE_LENGTH = 500;
}
