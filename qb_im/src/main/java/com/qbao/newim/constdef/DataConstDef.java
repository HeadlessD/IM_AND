package com.qbao.newim.constdef;

/**
 * Created by shiyunjie on 17/3/1.
 */

public interface DataConstDef {
    int
    EVENT_LOGIN_STATUS = 0x0001,
    EVENT_PROCESS_MESSAGE_TIME_OUT = 0x0002,
    EVENT_MESSAGE_TIME_OUT = 0x0003,
    EVENT_NET_ERROR = 0x0004,
    EVENT_FRIEND_ADD_REQUEST = 0x0005,         // 好友申请
    EVENT_UPLOAD_STATUS = 0x0006,
    EVENT_FiLE_PROGRESS = 0x0007,
    EVENT_GET_USER_INFO = 0x0008,              // 获取用户信息
    EVENT_GROUP_DELETE = 0x0009,               // 群删除
    EVENT_FRIEND_DEL = 0x0010,                 // 主动删除好友
    EVENT_GROUP_CREATE = 0x0012,               // 建群
    EVENT_GROUP_DETAIL = 0x0013,               // 群详情
    EVENT_GROUP_OPERATE = 0x0014,              // 群操作
    EVENT_SESSION_NAME = 0x0015,               // 修改会话名称
    EVENT_REMARK_DETAIL = 0x0016,              // 群公告详情
    EVENT_MESSAGE_STATUS = 0x0017,             // 群消息提醒设置
    EVENT_SESSION_TOP = 0x0020,                // 会话置顶
    EVENT_CONTACT_FRIEND = 0x0022,             // 手机号码批量后推送

    // 初始化操作
    EVENT_FRIEND_MSG_STATUS = 0x0024,          // 设置好友消息免打扰状态

    EVENT_SCAN_GROUP = 0x0027,                 // 扫描获取群信息
    EVENT_SCAN_GROUP_USER = 0x0028,            // 扫描获取群用户

    EVENT_FRIEND_EDIT = 0x0029,                // 好友备注名字修改
    EVENT_GROUP_CREATE_TYPE = 0x0030,          // 建群时获取到建群类型
    EVENT_GET_BLACK_LIST = 0x0032,             // 发送黑名单操作
    EVENT_FRIEND_REQUEST_DELETE = 0x0033,      // 删除好友请求
    EVENT_FRIEND_CONFIRM = 0x0034,             // 收到好友确认
    EVENT_MESSAGE_FAIL = 0x0035,               // 私聊消息发送失败
    EVENT_VOICE_MODE = 0x0036,                 // 语音播放模式切换
    EVENT_FRIEND_REQ_TIMEOUT = 0x0037,         // 好友请求超时
    EVENT_SAVE_CONTACT = 0x0038,               // 是否保存到通讯录
    EVENT_OFFICIAL_MSG = 0x0039,               // 公众号消息

    EVENT_ENTER_ASSIST = 0x0041,               // 进入群助手
    EVENT_LEAVE_ASSIST = 0x0042,               // 离开群助手

    // add by guoze
    EVENT_GET_GROUP_ONE_MSG = 0x0044,          // 群消息单条消息           // 在群聊天界面只有这一个通知
    EVENT_GET_GROUP_MSG_SESSION = 0x0045,         // 更新会话窗口和群助手界面
    // end

    /*******message change process********/
    //sc相关
    EVENT_SC_CHAT_MESSAGE = 0x1000,            // 更新消息界面
    EVENT_SC_CHAT_SESSION = 0x1001,            // 更新会话界面
    EVENT_UPDATE_ALL_SESSION = 0x1002,         // 全局更新会话界面
    /*******message change process********/


    EVENT_STATE_MACHINE_FINISH = 0X2001,       //状态机完成通知


    EVENT_UNREAD_CLEAR = 0x20000,              // 未读书清空

    END_OF_EVENT = 0xFFFFFFFF;

}
