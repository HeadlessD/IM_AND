package com.qbao.newim.manager;

import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.model.MsgCountModel;
import com.qbao.newim.qbdb.manager.MsgCountDBManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by shiyunjie on 2017/9/25.
 */

public class NIMMsgCountManager
{
    public static String TAG = NIMMsgCountManager.class.getSimpleName();
    private static NIMMsgCountManager _instance = new NIMMsgCountManager();

    private HashMap<Long, Integer> sc_count_map = new HashMap<>();      // 私聊未读消息
    private HashMap<Long, Integer> gc_count_map = new HashMap<>();      // 群聊未读消息
    private HashMap<Long, Integer> oc_count_map = new HashMap<>();      // 公众号未读消息
    private HashMap<Long, Integer> gc_help_count_map = new HashMap<>(); // 群助手未读消息
	public static NIMMsgCountManager getInstance() {
        return _instance;
    }

    public void init()
    {
        List<MsgCountModel> msg_count_list = MsgCountDBManager.getInstance().GetMsgCountList();
        for (MsgCountModel msg_c_model: msg_count_list)
        {
            switch (msg_c_model.chat_type)
            {
                case MsgConstDef.MSG_CHAT_TYPE.PRIVATE:
                {
                    sc_count_map.put(msg_c_model.session_id, msg_c_model.unread_count);
                    break;
                }
                case MsgConstDef.MSG_CHAT_TYPE.GROUP:
                {
                    gc_count_map.put(msg_c_model.session_id, msg_c_model.unread_count);
                    break;
                }
                case MsgConstDef.MSG_CHAT_TYPE.PUBLIC:
                {
                    oc_count_map.put(msg_c_model.session_id, msg_c_model.unread_count);
                    break;
                }
                case MsgConstDef.MSG_CHAT_TYPE.ASSIST:
                {
                    gc_help_count_map.put(msg_c_model.session_id, msg_c_model.unread_count);
                    break;
                }
                default:
                    break;
            }
        }
    }

    public void clear()
    {
        sc_count_map.clear();
        gc_count_map.clear();
        oc_count_map.clear();
        gc_help_count_map.clear();
    }

    public void UpsertUnreadCount(long session_id, int chat_type, int unread_count)
    {
        MsgCountModel msg_c_model = new MsgCountModel();
        msg_c_model.setChat_type(chat_type);
        msg_c_model.setSession_id(session_id);
        msg_c_model.setUnread_count(unread_count);

        UpsertUnreadCount(msg_c_model);
    }

    //更新未读数
    public void UpsertUnreadCount(MsgCountModel msg_c_model)
    {
        msg_c_model.GenPrimaryKey();
        switch (msg_c_model.chat_type)
        {
            case MsgConstDef.MSG_CHAT_TYPE.PRIVATE:
            {
                Integer unread = sc_count_map.get(msg_c_model.session_id);
                if (unread == null) {
                    unread = 0;
                }
                msg_c_model.unread_count += unread;
                sc_count_map.put(msg_c_model.session_id, msg_c_model.unread_count);
                break;
            }
            case MsgConstDef.MSG_CHAT_TYPE.GROUP:
            {
                Integer unread = gc_count_map.get(msg_c_model.session_id);
                if (unread == null) {
                    unread = 0;
                }
                msg_c_model.unread_count += unread;
                gc_count_map.put(msg_c_model.session_id, msg_c_model.unread_count);
                break;
            }
            case MsgConstDef.MSG_CHAT_TYPE.ASSIST:
            {
                if(gc_help_count_map.containsKey(msg_c_model.session_id))
                {
                    break;
                }

                Integer unread = gc_help_count_map.get(msg_c_model.session_id);
                if (unread == null)
                {
                    unread = 0;
                }
                msg_c_model.unread_count += unread;
                gc_help_count_map.put(msg_c_model.session_id, msg_c_model.unread_count);
                break;
            }
            case MsgConstDef.MSG_CHAT_TYPE.PUBLIC:
            {
                Integer unread = oc_count_map.get(msg_c_model.session_id);
                if (unread == null) {
                    unread = 0;
                }
                msg_c_model.unread_count += unread;
                oc_count_map.put(msg_c_model.session_id, msg_c_model.unread_count);
                break;
            }
            default:
                break;
        }

        MsgCountDBManager.getInstance().insertOrReplace(msg_c_model);
    }


    // 缓存移除未读消息，并更新数据库
    public void RemoveUnreadCount(MsgCountModel msg_c_model)
    {
        msg_c_model.GenPrimaryKey();
        switch (msg_c_model.chat_type)
        {
            case MsgConstDef.MSG_CHAT_TYPE.PRIVATE:
                {
                    sc_count_map.remove(msg_c_model.session_id);
                    break;
                }
            case MsgConstDef.MSG_CHAT_TYPE.GROUP:
                {
                    gc_count_map.remove(msg_c_model.session_id);
                    break;
                }
            case MsgConstDef.MSG_CHAT_TYPE.PUBLIC:
                {
                    oc_count_map.remove(msg_c_model.session_id);
                    break;
                }
            case MsgConstDef.MSG_CHAT_TYPE.ASSIST:
                {
                    gc_help_count_map.clear();
                    MsgCountDBManager.getInstance().deleteAllUnreadInfoByType(MsgConstDef.MSG_CHAT_TYPE.ASSIST);
                    return ;
                }
            default:
                break;
        }

        MsgCountDBManager.getInstance().deleteByKey(msg_c_model.primary_key);
    }

    public int GetUnreadCount(long session_id, int chat_type)
    {
        Integer count = null;
        switch (chat_type)
        {
            case MsgConstDef.MSG_CHAT_TYPE.PRIVATE:
            {
                count = sc_count_map.get(session_id);
                break;
            }
            case MsgConstDef.MSG_CHAT_TYPE.GROUP:
            {
                count = gc_count_map.get(session_id);
                break;
            }
            case MsgConstDef.MSG_CHAT_TYPE.PUBLIC:
            {
                count = oc_count_map.get(session_id);
                break;
            }
            case MsgConstDef.MSG_CHAT_TYPE.ASSIST:
            {
                count = gc_help_count_map.size();
                break;
            }
            default:
                break;
        }

        return count == null ? 0 : count;
    }


    public int SumSCCount() {
        int count = 0;
        Iterator iterator = sc_count_map.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry entry = (HashMap.Entry) iterator.next();
            Integer value = (Integer) entry.getValue();
            count += value;
        }
        return count;
    }

    public int SumGCCount() {
        int count = 0;
        Iterator iterator = gc_count_map.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry entry = (HashMap.Entry) iterator.next();
            Integer value = (Integer) entry.getValue();
            count += value;
        }
        return count;
    }

    public int SumOCCount() {
        int count = 0;
        Iterator iterator = oc_count_map.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry entry = (HashMap.Entry) iterator.next();
            Integer value = (Integer) entry.getValue();
            count += value;
        }
        return count;
    }
    public int GetAllUnreadCount() {
        int count = 0;
        count += SumSCCount();
        count += SumGCCount();
        count += SumOCCount();
        return count;
    }
}
