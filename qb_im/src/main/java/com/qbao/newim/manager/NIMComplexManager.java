package com.qbao.newim.manager;

import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.util.Utils;

/**
 * Created by shiyunjie on 2017/10/9.
 */

public class NIMComplexManager
{

    public static String TAG = NIMComplexManager.class.getSimpleName();
    private static NIMComplexManager instance;

    public static NIMComplexManager getInstance()
    {
        if(instance == null)
            instance = new NIMComplexManager();
        return instance;
    }


    //根据会话类型获取会话名字
    public String GetSessionName(long session_id, int chat_type)
    {
        String session_name = "";
        // 当前聊天类型
        switch (chat_type)
        {
            // 私聊
            case MsgConstDef.MSG_CHAT_TYPE.PRIVATE:
            {
                IMFriendInfo friend_info = NIMFriendInfoManager.getInstance().getFriendUser(session_id);
                if (friend_info != null)
                {
                    session_name = Utils.getUserShowName(new String[]{friend_info.remark_name, friend_info.nickName, friend_info.user_name});
                }
            }
            break;
            case MsgConstDef.MSG_CHAT_TYPE.GROUP:
            {
                IMGroupInfo group_info = NIMGroupInfoManager.getInstance().getGroupInfo(session_id);
                if (group_info != null)
                {
                    session_name = group_info.group_name;
                }
            }
            break;
            // 当前公众号类型
            case MsgConstDef.MSG_CHAT_TYPE.PUBLIC:
            {
            }
            break;
            // 当前系统消息
            case MsgConstDef.MSG_CHAT_TYPE.SYS:
            {
            }
            break;
            // 当前任务助手
            case MsgConstDef.MSG_CHAT_TYPE.TASK:
            {
            }
            break;
            // 当前订阅助手
            case MsgConstDef.MSG_CHAT_TYPE.SUBSCRIBE:
            {
            }
            break;
            case MsgConstDef.MSG_CHAT_TYPE.ASSIST:
            {
            }
            break;
        }

        return session_name;
    }
}
