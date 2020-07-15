package com.qbao.newim.constdef;

/**
 * Created by chenjian on 2017/6/7.
 */

public class FriendTypeDef {

    public interface FRIEND_SOURCE_TYPE {
        byte
                CONTACTS = 0,                       // 通讯录匹配
                CONGENIAL = 1,                      // 趣味相投的人
                NEARBY = 2,                         // 附近的人
                QRCODE = 3,                         // 扫描二维码
                SEARCH = 4,                         // 搜索添加
                VCARD = 5,                          // 名片分享
                SAME_TASK = 6,                      // 相同任务领取人
                PUBLIC_FOLLOW = 7,                  // 公众号关注者添加
                TASK_COMMENT = 8,                   // 任务评论者添加
                PUBLISHER = 9,                      // 动态发布者添加
                PUBLISH_COMMENT = 10,               // 动态评论者添加
                PUBLISH_LIKE = 11,                  // 动态点赞者添加
                CHATTING = 12,                      // 聊天会话页添加
                PUBLIC_COMMENT = 13,                // 公众号文章评论者添加
                COMMODITY_COMMENT = 14,             // 商品评论者添加
                SEVRICE_LIST = 15,                  // 服务号列表页添加
                SEARCHER = 16,                      // 图文页点击服务号名称添加
                RECOMMEND = 17,                     // 推荐人自动加好友
                SYSTEM_RANDOM = 18;                 // 系统随机推荐
    }

    public interface FRIEND_ADD_TYPE {
        int
                INVALID = 0,
                SEND_REQUEST = 1,        // 发送好友请求给对方
                ACCEPT_REQUEST = 2,      // 收到对方的好友请求
                DELETE = 3,              // 删除好友请求
                TIME_OUT = 4,            // 好友请求已过期
                OWN_CONFIRM = 5,         // 自己同意好友请求
                PEER_CONFIRM = 6,        // 对方同意好友请求
                RESTART_ADD = 7,         // 重新添加
                FRIEND = 8;
    }

    // 离线拉取好友列表
    public interface FRIEND_LIST_TYPE {
        int
                FD_FRIEND_OP = 0,               // 收到成为好友的操作
                FD_PEER_CONFIRM_OP = 1,          // 收到对端确认成为好友的操作
                FD_WAIT_CONFIRM_OP = 2,          // 收到申请对端为好友的操作
                FD_NEED_CONFIRM_OP = 3,          // 收到对端请求为好友的操作
                FD_OWN_CONFIRM_OP = 4,           // 收到自己确认成为好友的操作
                FD_REMARK_OP = 5,               // 收到自己修改好友备注的操作
                FD_BLACK_OP = 6,                // 黑名单操作
                FD_PEER_DEL_OP = 7,             // 对端删除好友的操作
                FD_OWN_DEL_OP = 8,              // 删除好友的操作
                FD_INVALID_OP = 9,              // 无效的好友请求，本地存在需要删除
                FD_RESTORE_OP = 10;             // 恢复好友关系
    }

    // 行为操作类型，主动还是被动
    public interface ACTIVE_TYPE {
        int
                INVALID = 0,                    // 正常好友关系
                PASSIVE = 1,                    // 被动，如被动黑名单，被动删除
                ACTIVE = 2,                     // 主动，如主动拉入黑名单，主动删除好友
                EACH = 3;                       // 互相，主要用于互相黑名单
    }
}
