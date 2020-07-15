package com.qbao.newim.constdef;

/**
 * Created by shiyunjie on 2017/10/12.
 */

public interface StateConstDef
{
    short
        EXCEPTION = -1,             //异常状态

        LOAD_DATA = 1,              //加载数据库数据状态--->CONNECT
        CONNECT = 2,                //连接服务器状态---->LOGIN
        LOGIN = 3,                  //登录状态------>USER_INFO
        USER_INFO = 4,              //上传下载用户信息状态------>FRIEND_INFO,GROUP_IDS,SC_OFFLINE_MSG
                                    //GC_OFFLINE_MSG,OC_OFFLINE_MSG
        FRIEND_INFO = 5,            //获取好友信息状态------>INFO_UPDATE_FINISHED

        GROUP_INFO = 7,             //获取群信息状态----->INFO_UPDATE_FINISHED

        SC_OFFLINE_MSG = 11,        //获取单聊离线状态---->INFO_UPDATE_FINISHED
        GC_OFFLINE_MSG = 12,        //群聊利息那状态---->INFO_UPDATE_FINISHED
        OC_OFFLINE_MSG = 13,        //获取公众号离线信息状态---->INFO_UPDATE_FINISHED

        INFO_UPDATE_FINISHED = 50,    //异步获取信息完成---->FINISHED

        FINISHED = 100;             //最终完结状态
}
