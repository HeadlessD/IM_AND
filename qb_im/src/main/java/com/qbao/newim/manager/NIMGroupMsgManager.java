package com.qbao.newim.manager;

import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.model.IMGetOfflineInfo;
import com.qbao.newim.model.NIM_Chat_ID;
import com.qbao.newim.model.message.BaseMessageModel;
import com.qbao.newim.model.message.GcDBMessageModel;
import com.qbao.newim.model.message.GcMessageModel;
import com.qbao.newim.qbdb.manager.GroupMsgDbManager;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by qlguoze on 17/9/25.
 */

public class NIMGroupMsgManager
{
    private static final String TAG = "NIM_GroupMsgManager";
    private static NIMGroupMsgManager _instance = null;
    public static final int LIMIT_ONE_PAGE = 20;

    private GroupMsgDbManager chatMsgDbManager = GroupMsgDbManager.getInstance();
    private HashMap<Long, Long> mapGroupNextMsgID = new HashMap<>();                    // group_id to next_message_id
    private HashMap<Long, ArrayList<GcMessageModel>> mapGroupListMsg = new HashMap<>();   // group_id to list group_id 对应全部消息列表
    public static final int LIMIT = 20;
    private HashMap<Integer, Integer> img_position = new HashMap<>();

    public static NIMGroupMsgManager getInstance()
    {
        if(null == _instance)
        {
            _instance = new NIMGroupMsgManager();
        }

        return _instance;
    }

    // 这个函数在客户端启动的时候调用
    public void init()
    {
        // 按照最新消息取出limit条消息,并反转数据，使最新的排在最后
        List<GcDBMessageModel> msgLists = chatMsgDbManager.getRecentGroupMessageList(LIMIT_ONE_PAGE);

        // 消息添加是由小到大排列
        for(int index = 0; index < msgLists.size(); index++)
        {
            Long group_id = msgLists.get(index).group_id;
            if(!mapGroupListMsg.containsKey(group_id))
            {
                mapGroupListMsg.put(group_id, new ArrayList<GcMessageModel>());
            }

            ArrayList<GcMessageModel> list_msg_info = mapGroupListMsg.get(group_id);
            GcMessageModel gcMessageModel = GcDbMsgCopy(msgLists.get(index));
            list_msg_info.add(0, gcMessageModel);
            setGroupNextMessageId(gcMessageModel, true);
        }
    }

    private void addGcMessageInfo(GcMessageModel message_info, ArrayList<GcMessageModel> arrayList, int index)
    {
        message_info.setUserIsSelf(NIMUserInfoManager.getInstance().GetSelfUserId());

        arrayList.add(index, message_info);
    }
    // 在需要历史消息的时候调用
    public ArrayList<GcMessageModel> loadGroupMsgFromDb(Long group_id)
    {
        if(!mapGroupListMsg.containsKey(group_id))
        {
            mapGroupListMsg.put(group_id, new ArrayList<GcMessageModel>());
        }

        // 按照最新消息取出limit条消息,并反转数据，使最新的排在最后
        int cur_size = mapGroupListMsg.size();
        List<GcDBMessageModel> msgLists = chatMsgDbManager.getMessageList(group_id, cur_size, LIMIT);
        ArrayList<GcMessageModel> list_msg_info = mapGroupListMsg.get(group_id);

        // 消息添加是由小到大排列
        for(int index = 0; index < msgLists.size(); index++)
        {
            GcMessageModel gcMessageModel = GcDbMsgCopy(msgLists.get(index));
            addGcMessageInfo(gcMessageModel, list_msg_info, 0);
            setGroupNextMessageId(gcMessageModel, true);
        }

        return list_msg_info;
    }

    // 添加消息
    // is_self_send 在获取群离线的时候调用填false  自己发送消息 true
    public void addGroupListMsgInfo(Long group_id, ArrayList<GcMessageModel> list_message_info,
                                    boolean is_self_send, boolean is_assist)
    {
        if(!mapGroupListMsg.containsKey(group_id))
        {
            mapGroupListMsg.put(group_id, new ArrayList<GcMessageModel>());
        }

        boolean is_current_id = NIMSessionManager.getInstance().GetCurSession() == group_id;
        ArrayList<GcMessageModel> list_exist_msg = mapGroupListMsg.get(group_id);
        int nUnreadCount = 0;
        for(int index_add = 0; index_add < list_message_info.size(); index_add++)
        {
            GcMessageModel messageModel = list_message_info.get(index_add);
            messageModel.GenPrimaryKey();

            int add_msg_pos = 0;
            int msg_type = MsgConstDef.MSG_GROUP_OP_TYPE.ADD;
            for(int index_exist = list_exist_msg.size() - 1; index_exist >= 0; index_exist--)
            {
                GcMessageModel exist_msg_mode = list_exist_msg.get(index_exist);
                if(messageModel.message_id == exist_msg_mode.message_id)
                {
                    add_msg_pos = index_exist;
                    list_exist_msg.remove(index_exist);
                    msg_type = MsgConstDef.MSG_GROUP_OP_TYPE.UPDATE;
                    break;
                }
                else if(messageModel.message_id > list_exist_msg.get(index_exist).message_id)
                {
                    add_msg_pos = index_exist + 1;
                    break;
                }
            }

            addGcMessageInfo(messageModel, list_exist_msg, add_msg_pos);
            setGroupNextMessageId(list_message_info.get(index_add), is_self_send);

            if(messageModel.user_id != NIMUserInfoManager.getInstance().GetSelfUserId()
                    && messageModel.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_NORMAL
                    && msg_type == MsgConstDef.MSG_GROUP_OP_TYPE.ADD)
            {
                nUnreadCount++;
            }

            if(messageModel.big_msg_type > 0 && messageModel.big_msg_type != MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_NORMAL)
            {
                ChatMsgBuildManager.buildGroupTipsMsg(list_message_info.get(index_add));
            }

            // 添加数据库
            chatMsgDbManager.insertOrReplace(GcMsgCopy(messageModel));

            // 通知聊天界面
            if (is_current_id)
            {
                DataObserver.Notify(DataConstDef.EVENT_GET_GROUP_ONE_MSG,
                        new NIM_Chat_ID(messageModel.group_id, add_msg_pos), msg_type);
            }
        }

        if(nUnreadCount > 0)
        {
            NIMMsgCountManager.getInstance().UpsertUnreadCount(group_id, MsgConstDef.MSG_CHAT_TYPE.GROUP, nUnreadCount);
            if(is_assist)
            {
                NIMMsgCountManager.getInstance().UpsertUnreadCount(group_id, MsgConstDef.MSG_CHAT_TYPE.ASSIST, 1);
            }
        }

        // 当前如果不在聊天界面，则直接抛出到界面
        DataObserver.Notify(DataConstDef.EVENT_GET_GROUP_MSG_SESSION, group_id, null);
    }

    // 添加消息 目前只有发送消息的时候会调用
    public void addGroupOneMsgInfo(Long group_id, GcMessageModel message_info, boolean is_self_send)
    {
        message_info.GenPrimaryKey();

        if(!mapGroupListMsg.containsKey(group_id))
        {
            mapGroupListMsg.put(group_id, new ArrayList<GcMessageModel>());
        }

        ArrayList<GcMessageModel> list_exist_msg = mapGroupListMsg.get(group_id);
        int msg_type = MsgConstDef.MSG_GROUP_OP_TYPE.ADD;
        int pos_index = 0;

        if(list_exist_msg.size() == 0)
        {
            addGcMessageInfo(message_info, list_exist_msg, 0);
            setGroupNextMessageId(message_info, is_self_send);
        }
        else
        {
            for(int index_exist = list_exist_msg.size() - 1; index_exist >= 0; index_exist--)
            {
                if(message_info.message_id == list_exist_msg.get(index_exist).message_id)
                {
                    list_exist_msg.remove(index_exist);
                    addGcMessageInfo(message_info, list_exist_msg, index_exist);
                    setGroupNextMessageId(message_info, is_self_send);

                    msg_type = MsgConstDef.MSG_GROUP_OP_TYPE.UPDATE;
                    pos_index = index_exist;

                    break;
                }

                Long message_id_before = list_exist_msg.get(index_exist).message_id;
                if(message_info.message_id > message_id_before)
                {
                    pos_index = index_exist + 1;
                    addGcMessageInfo(message_info, list_exist_msg, pos_index);
                    setGroupNextMessageId(message_info, is_self_send);


                    break;
                }

                if(index_exist == 0)
                {
                    addGcMessageInfo(message_info, list_exist_msg, index_exist);
                    setGroupNextMessageId(message_info, is_self_send);

                    pos_index = 0;
                    break;
                }
            }
        }

        if(message_info.big_msg_type > 0 && message_info.big_msg_type != MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_NORMAL)
        {
            ChatMsgBuildManager.buildGroupTipsMsg(message_info);
        }

        // 1.加数据库
        chatMsgDbManager.insertOrReplace(GcMsgCopy(message_info));

        // 2. 更新聊天界面
        DataObserver.Notify(DataConstDef.EVENT_GET_GROUP_ONE_MSG,
                new NIM_Chat_ID(message_info.group_id, pos_index), msg_type);

        // 3. 更新会话界面和群助手
        DataObserver.Notify(DataConstDef.EVENT_GET_GROUP_MSG_SESSION, group_id, null);
    }

    // 删除单个消息
    public void delGroupMessageInfoByMsgID(Long group_id, Long message_id)
    {
        String key = String.format("%d_%d", group_id, message_id);
        chatMsgDbManager.deleteByKey(key);

        ArrayList<GcMessageModel> list_message_info = mapGroupListMsg.get(group_id);
        if(null == list_message_info || list_message_info.size() <= 0)
        {
            Logger.error(TAG, "key = " + key);
            return ;
        }

        for(int index = list_message_info.size() - 1; index >= 0; index--)
        {
            if(list_message_info.get(index).message_id == message_id)
            {
                list_message_info.remove(index);

                DataObserver.Notify(DataConstDef.EVENT_GET_GROUP_ONE_MSG,
                                new NIM_Chat_ID(group_id, index),
                                MsgConstDef.MSG_GROUP_OP_TYPE.DELETE);

                // 删除最后一条需要更新会话界面消息
                if(index == list_message_info.size() - 1)
                {
                    DataObserver.Notify(DataConstDef.EVENT_GET_GROUP_MSG_SESSION, group_id, null);
                }
                break;
            }
        }

        Logger.error(TAG, "message_id = " + String.valueOf(message_id) + " group_id = " + String.valueOf(group_id));
    }

    // 更新单个消息
    public void updateGroupMessageInfoByMsgID(Long group_id, GcMessageModel message_info)
    {
        if(null == message_info)
        {
            return ;
        }

        ArrayList<GcMessageModel> list_message_info = mapGroupListMsg.get(group_id);
        if(null == list_message_info || list_message_info.size() <= 0)
        {
            Logger.error(TAG, "group_id = " + String.valueOf(group_id));
            return ;
        }

        for(int index = list_message_info.size() - 1; index >= 0; index--)
        {
            if(list_message_info.get(index).message_id == message_info.message_id)
            {
                list_message_info.remove(index);
                addGcMessageInfo(message_info, list_message_info, index);
                chatMsgDbManager.insertOrReplace(GcMsgCopy(message_info));

                DataObserver.Notify(DataConstDef.EVENT_GET_GROUP_ONE_MSG, new NIM_Chat_ID(group_id, index),
                        MsgConstDef.MSG_GROUP_OP_TYPE.UPDATE);
                return ;
            }
        }

        Logger.error(TAG, "message_id = " + String.valueOf(message_info.message_id) + " group_id = " + String.valueOf(group_id));
    }

    // 获取单个消息
    public GcMessageModel getGroupMessageInfoByMsgID(Long group_id, Long message_id)
    {
        ArrayList<GcMessageModel> list_message_info = mapGroupListMsg.get(group_id);
        if(null == list_message_info)
        {
            return null;
        }

        for(int index = list_message_info.size() - 1; index >= 0; index--)
        {
            if(list_message_info.get(index).message_id == message_id)
            {
                return list_message_info.get(index);
            }
        }

        Logger.error(TAG, "message_id = " + String.valueOf(message_id) + " group_id = " + String.valueOf(group_id));
        return null;
    }

    // 获取群消息列表
    public ArrayList<BaseMessageModel> getListGroupMsgByGroupID(Long group_id)
    {
        ArrayList<GcMessageModel> list_message_info = mapGroupListMsg.get(group_id);
        ArrayList<BaseMessageModel> ret_list = new ArrayList<>();
        if(null == list_message_info || list_message_info.size() <= 0)
        {
            return ret_list;
        }

        ArrayList<GcMessageModel> src_list = list_message_info;
        for (GcMessageModel gc_model: src_list)
        {
            ret_list.add(gc_model);
        }

        return ret_list;
    }

    // 获取其中某一条消息
    public GcMessageModel GetMessageByChatID(NIM_Chat_ID chat_id)
    {
        if(null == mapGroupListMsg.get(chat_id.session_id))
        {
            Logger.error(TAG, "group_id is invalid group_id = " + String.valueOf(chat_id.session_id));
            return null;
        }

        if(chat_id.index < 0 || chat_id.index >= mapGroupListMsg.get(chat_id.session_id).size())
        {
            Logger.error(TAG, "group_id = " + String.valueOf(chat_id.session_id) + " index = " + String.valueOf(chat_id.index)
                        + " size = " + String.valueOf(mapGroupListMsg.get(chat_id.session_id).size()));
            return null;
        }

        return mapGroupListMsg.get(chat_id.session_id).get(chat_id.index);
    }

    // 获取最后一条消息
    public GcMessageModel getLastMessageInfoByGroupID(Long group_id)
    {
        ArrayList<GcMessageModel> list_message_info = mapGroupListMsg.get(group_id);
        if(list_message_info == null)
        {
            return null;
        }

        if(list_message_info.size() > 0)
        {
            return list_message_info.get(list_message_info.size() - 1);
        }

        return null;
    }
    // 删除所有的消息
    public boolean delGroupMessageInfoByGroupID(Long group_id, boolean is_notify)
    {
        chatMsgDbManager.deleteAllMessage(group_id);

        mapGroupListMsg.remove(group_id);

        if(is_notify)
            DataObserver.Notify(DataConstDef.EVENT_GET_GROUP_MSG_SESSION, group_id, null);
        return true;
        // next_message_id 暂定不删除
    }


    // 设置群next_message_id
    public void setGroupNextMessageId(GcMessageModel message_info, boolean is_self_send)
    {
        if(is_self_send && message_info.is_self)
        {
            return ;
        }

        long group_id = message_info.group_id;
        long next_message_id = 0;
        if(mapGroupNextMsgID.containsKey(group_id))
        {
            next_message_id = mapGroupNextMsgID.get(group_id);
        }

        if(message_info.message_id > next_message_id)
        {
            next_message_id = message_info.message_id;
        }

        mapGroupNextMsgID.put(group_id, next_message_id);
    }

    public int SetMessageStatus(long session_id, long message_id, short msg_status)
    {
        if (!mapGroupListMsg.containsKey(session_id))
        {
            return -1;
        }

        ArrayList<GcMessageModel> message_info_list = mapGroupListMsg.get(session_id);
        int nSize = message_info_list.size();
        //从后往前找
        for (int index = nSize - 1; index >= 0; index--)
        {
            GcMessageModel model = message_info_list.get(index);
            if (model.message_id == message_id)
            {
                model.msg_status = msg_status;
                //同步到DB
                chatMsgDbManager.update(GcMsgCopy(model));
                DataObserver.Notify(DataConstDef.EVENT_GET_GROUP_ONE_MSG,
                        new NIM_Chat_ID(session_id, index), MsgConstDef.MSG_GROUP_OP_TYPE.UPDATE);
                DataObserver.Notify(DataConstDef.EVENT_GET_GROUP_MSG_SESSION, model.group_id, null);
                return index;
            }
        }

        return -1;
    }

    //根据指定chat_type获取图片列表
    public ArrayList<String> GetPicPathList(long session_id)
    {
        ArrayList<String> ret_list = new ArrayList<>();
        if(!mapGroupListMsg.containsKey(session_id))
        {
            return ret_list;
        }
        img_position.clear();
        ArrayList<GcMessageModel> src_list = mapGroupListMsg.get(session_id);
        for (int i = 0; i < src_list.size(); i++)
        {
            GcMessageModel gc_model = src_list.get(i);
            if(gc_model.chat_type == MsgConstDef.MSG_M_TYPE.IMAGE) {
                if (gc_model.is_self) {
                    ret_list.add(gc_model.compress_path);
                } else {
                    ret_list.add(gc_model.msg_content);
                }
                img_position.put(i, ret_list.size() - 1);
            }
        }
        return ret_list;
    }

    public HashMap<Integer, Integer> getImgPosition() {
        return img_position;
    }

    public ArrayList<IMGetOfflineInfo> getGroupAllNextMessageID()
    {
        ArrayList<IMGetOfflineInfo> list_next_message_id = new ArrayList<>();

        Iterator iter = mapGroupNextMsgID.entrySet().iterator();

        while (iter.hasNext())
        {
            Map.Entry entry = (Map.Entry) iter.next();
            Long group_id = (Long)entry.getKey();
            Long next_message_id = (Long)entry.getValue();

            IMGetOfflineInfo msg_next_id = new IMGetOfflineInfo(group_id, next_message_id);

            list_next_message_id.add(msg_next_id);
        }

        return list_next_message_id;
    }

    public void clear()
    {
        mapGroupNextMsgID.clear();
        mapGroupListMsg.clear();
        img_position.clear();
    }

    // copy db to cache
    public GcMessageModel GcDbMsgCopy(GcDBMessageModel dbMessageModel) {
        GcMessageModel gcMessageModel = new GcMessageModel();
        gcMessageModel.group_id = dbMessageModel.group_id;
        gcMessageModel.app_id = dbMessageModel.app_id;
        gcMessageModel.message_id = dbMessageModel.message_id;
        gcMessageModel.big_msg_type = dbMessageModel.big_msg_type;
        gcMessageModel.group_modify_content = dbMessageModel.group_modify_content;
        gcMessageModel.msg_content = dbMessageModel.msg_content;
        gcMessageModel.message_old_id = dbMessageModel.message_old_id;
        gcMessageModel.operate_user_name = dbMessageModel.operate_user_name;
        gcMessageModel.str_user_list = dbMessageModel.str_user_list;
        gcMessageModel.user_id = dbMessageModel.user_id;
        gcMessageModel.b_id = dbMessageModel.b_id;
        gcMessageModel.w_id = dbMessageModel.w_id;
        gcMessageModel.c_id = dbMessageModel.c_id;
        gcMessageModel.ext_type = dbMessageModel.ext_type;
        gcMessageModel.audio_path = dbMessageModel.audio_path;
        gcMessageModel.pic_path = dbMessageModel.pic_path;
        gcMessageModel.primary_key = dbMessageModel.primary_key;
        gcMessageModel.send_user_name = dbMessageModel.send_user_name;
        gcMessageModel.is_self = dbMessageModel.is_self;
        gcMessageModel.session_id = dbMessageModel.session_id;
        gcMessageModel.chat_type = dbMessageModel.chat_type;
        gcMessageModel.m_type = dbMessageModel.m_type;
        gcMessageModel.s_type = dbMessageModel.s_type;
        gcMessageModel.msg_time = dbMessageModel.msg_time;
        gcMessageModel.msg_status = dbMessageModel.msg_status;
        gcMessageModel.compress_path = dbMessageModel.compress_path;

        return gcMessageModel;
    }

    // copy cache to db
    public GcDBMessageModel GcMsgCopy(GcMessageModel messageModel) {
        GcDBMessageModel dbMessageModel = new GcDBMessageModel();
        dbMessageModel.group_id = messageModel.group_id;
        dbMessageModel.app_id = messageModel.app_id;
        dbMessageModel.message_id = messageModel.message_id;
        dbMessageModel.big_msg_type = messageModel.big_msg_type;
        dbMessageModel.group_modify_content = messageModel.group_modify_content;
        dbMessageModel.msg_content = messageModel.msg_content;
        dbMessageModel.message_old_id = messageModel.message_old_id;
        dbMessageModel.operate_user_name = messageModel.operate_user_name;
        dbMessageModel.str_user_list = messageModel.str_user_list;
        dbMessageModel.user_id = messageModel.user_id;
        dbMessageModel.b_id = messageModel.b_id;
        dbMessageModel.w_id = messageModel.w_id;
        dbMessageModel.c_id = messageModel.c_id;
        dbMessageModel.ext_type = messageModel.ext_type;
        dbMessageModel.audio_path = messageModel.audio_path;
        dbMessageModel.pic_path = messageModel.pic_path;
        dbMessageModel.primary_key = messageModel.primary_key;
        dbMessageModel.send_user_name = messageModel.send_user_name;
        dbMessageModel.is_self = messageModel.is_self;
        dbMessageModel.session_id = messageModel.session_id;
        dbMessageModel.chat_type = messageModel.chat_type;
        dbMessageModel.m_type = messageModel.m_type;
        dbMessageModel.s_type = messageModel.s_type;
        dbMessageModel.msg_time = messageModel.msg_time;
        dbMessageModel.msg_status = messageModel.msg_status;
        dbMessageModel.compress_path = messageModel.compress_path;

        return dbMessageModel;
    }
}

