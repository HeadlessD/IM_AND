package com.qbao.newim.constdef;

/**
 * Created by shiyunjie on 17/3/1.
 */

public class MsgConstDef {

    public interface MSG_CHAT_TYPE {
        int
                INVALID = 0,                    //
                SYS = 1,                        // 系统消息
                GROUP = 2,                      // 群组消息
                PUBLIC = 3,                     // 公众号
                PRIVATE = 4,                    // 私聊
                ASSIST = 6,                     // 群助手，仅存本地
                TASK = 7,                       // 任务助手
                SUBSCRIBE = 8,                  // 订阅助手
                BUSINESS = 9;                   // 商家消息
    }


    public interface MSG_M_TYPE {
        int
                INVALID = 0,
                TEXT = 1,                        //纯文本，如："abcd emoji表情"
                HTML = 2,                        //html格式文本，如："abcd <b style="color:red">emoji表情</b>"
                IMAGE = 3,                       //图片，如：[img]http://im.qbao.com/upload/img/5fcab8bd82f04c8ab5904a03173935b3.jpg[/img]
                VOICE = 4,                       //语音，如：[voice]http://im.qbao.com/upload/voice/5fcab8bd82f04c8ab5904a03173935b3.mp3[/voice]
                SMILEY = 5,                      //动态贴图表情，如：[smiley]http://im.qbao.com/upload/smiley/5fcab8bd82f04c8ab5904a03173935b3.gif[/smiley]
                JSON = 6,                        //自定义json格式，通常用于系统消息，结合subtype解析
                MAP = 7;                         //地理位置经纬度，json格式的经纬度信息(address-位置名称，lng-经度, lat-维度) 如：'{"lat": 32.168954, "lng": 118.703751, "address": "江苏省南京市浦口区星火路9号"}'
    }

    public interface MSG_S_TYPE {
        int
                INVALID = 0,
                RED_PACKET = 1,                      //红包
                ORDER = 2,                           //订单
                VCARD = 3,                           //名片
                ITEM = 4,                            //商品
                TIP = 5,                             //群操作
                GROUP_NEED_AGREE = 6,                //需群主同意（去确认）
                GROUP_ADD_AGREE = 7;                 //群主已同意（已确认）
    }

    public interface MSG_EXT_TYPE {
        int
                INVALID = 0;
    }

    //消息状态
    public interface MSG_STATUS {
        short
                INVALID = 0,
                DRAFT = 1,                  // 草稿
                UPLOADING = 2,              // 上传
                UPLOAD_FAILED = 3,          // 上传失败
                UPLOAD_SUCCESS = 4,         // 上传成功
                SENDING = 5,                // 发送中
                SEND_FAILED = 6,            // 发送失败
                SEND_SUCCESS = 7,           // 发送成功

                SEND_STATUS_END = 99,

                UNREAD = 100,               // 未读
                READED = 101,               // 已读
                DOWNLOADING = 102,          // 下载
                DOWNLOAD_FAILED = 103,      // 下载失败
                DOWNLOAD_SUCCESS = 104,     // 下载成功
                PLAYED = 105,               // 已播放

                RECV_STATUS_END = 9999;
    }

    // 群操作类型
    public interface GROUP_OPERATE_TYPE {
        int
                INVALID = 0,
                GROUP_OFFLINE_CHAT_NORMAL = 1,            // 群聊
                GROUP_OFFLINE_CHAT_ADD_USER = 2,            // 邀请进群
                GROUP_OFFLINE_CHAT_KICK_USER = 3,            // 踢人
                GROUP_OFFLINE_CHAT_LEADER_CHANGE = 4,            // 群主转让
                GROUP_OFFLINE_CHAT_ENTER_AGREE = 5,            // 邀请需要群主统一
                GROUP_OFFLINE_CHAT_ENTER_DEFAULT = 6,            // 默认方式
                GROUP_OFFLINE_CHAT_ADD_USER_AGREE = 7,            // 邀请成员但是需要同意
                GROUP_OFFLINE_CHAT_AGREE = 8,            // 群主同意
                GROUP_OFFLINE_CHAT_SCANNING = 9,           // 通过扫二维码自己进入
                GROUP_OFFLINE_CHAT_CREATE = 10,   // 建群
                GROUP_OFFLINE_CHAT_MODIFY_GROUP_NAME = 11,   // 修改群名称
                GROUP_OFFLINE_CHAT_MODIFY_GROUP_REMARK = 12,   // 修改群公告
                GROUP_OFFLINE_CHAT_MODIFY_GROUP_USER_NAME = 13,  // 修改群用户备注
                GROUP_OFFLINE_CHAT_SCAN_ADD_USER = 14;			// 扫描进群
    }

    public interface GROUP_ADD_TYPE {
        int
                NOT_AGREE = 0,                           // 不需要同意
                NEED_AGREE = 1;                          // 需要群主同意才能进群
    }

    public interface GROUP_MESSAGE_STATUS {
        byte
                GROUP_MESSAGE_STATUS_NORMAL = 0,
                GROUP_MESSAGE_STATUS_NO_HIT = 1,                // 收消息不提醒
                GROUP_MESSAGE_IN_HELP_NO_HIT = 2;                // 收入群助手不提醒
    }

    public interface MSG_OP_TYPE
    {
        int
            INVALID = -1,
            ADD = 1,
            UPDATE = 2,
            DELETE = 3;
    }

    public interface MSG_GROUP_OP_TYPE
    {
        int
                INVALID = -1,
                ADD = 1,
                UPDATE = 2,
                DELETE = 3;
    }
}
