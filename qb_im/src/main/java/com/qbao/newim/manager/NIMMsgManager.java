package com.qbao.newim.manager;

import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.model.MsgCountModel;
import com.qbao.newim.model.NIM_Chat_ID;
import com.qbao.newim.model.message.BaseMessageModel;
import com.qbao.newim.model.message.ScDBMessageModel;
import com.qbao.newim.model.message.ScMessageModel;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.qbdb.manager.ScDBMessageManager;
import com.qbao.newim.util.BaseUtil;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by shiyunjie on 17/3/2.
 */

public class NIMMsgManager {
    private static final String TAG = "NIMMsgManager";
    private static NIMMsgManager _instance = new NIMMsgManager();

    private HashMap<Long, HashMap<Long, Byte>> user_msg_info_map = new HashMap<>();
    private HashMap<Long, ArrayList<ScMessageModel>> message_all_list = new HashMap<>();

    private ScDBMessageManager m_sc_db_manager;
    public static final int LIMIT = 20;
    private HashMap<Integer, Integer> img_position = new HashMap<>();

    public static NIMMsgManager getInstance() {
        return _instance;
    }

    private NIMMsgManager() {
        m_sc_db_manager = ScDBMessageManager.getInstance();
    }

    public void init()
    {
        List<ScDBMessageModel> all_message = m_sc_db_manager.getRecentMessageList(LIMIT);
        for (int index = all_message.size() - 1; index >= 0; index--)
        {
            ScDBMessageModel msg = all_message.get(index);
            Logger.error(TAG, "msg_time = " + msg.msg_time + " op_user_id = " + msg.opt_user_id + " msg_content = " + msg.msg_content);
            ScMessageModel cache_msg = ConvertDB2Cache(msg);
            AddMessage(new NIM_Chat_ID(msg.opt_user_id, msg.message_id), cache_msg, false, false);
        }
    }

    private ScMessageModel ConvertDB2Cache(ScDBMessageModel db_model)
    {
        ScMessageModel cache_model = new ScMessageModel();
        cache_model.message_id = db_model.message_id;
        cache_model.opt_user_id = db_model.opt_user_id;
        cache_model.send_user_name = db_model.send_user_name;
        cache_model.is_self = db_model.is_self;
        cache_model.b_id = db_model.b_id;
        cache_model.w_id = db_model.w_id;
        cache_model.c_id = db_model.c_id;
        cache_model.app_id = db_model.app_id;
        cache_model.session_id = db_model.session_id;
        cache_model.chat_type = db_model.chat_type;
        cache_model.m_type = db_model.m_type;
        cache_model.s_type = db_model.s_type;
        cache_model.ext_type = db_model.ext_type;
        cache_model.msg_content = db_model.msg_content;
        cache_model.msg_time = db_model.msg_time;
        cache_model.msg_status = db_model.msg_status;
        cache_model.audio_path = db_model.audio_path;
        cache_model.pic_path = db_model.pic_path;
        cache_model.compress_path = db_model.compress_path;

        if(cache_model.msg_status == MsgConstDef.MSG_STATUS.INVALID)
        {
            Logger.error(TAG, "msg_status is invalid opt_user_id = " + cache_model.opt_user_id +
            " message_id = " + cache_model.message_id + "send_user_name = " + cache_model.send_user_name);
        }
        if(cache_model.is_self)
        {
            if(cache_model.msg_status == MsgConstDef.MSG_STATUS.SENDING ||
                    cache_model.msg_status == MsgConstDef.MSG_STATUS.UPLOADING)
            {
                cache_model.msg_status = MsgConstDef.MSG_STATUS.SEND_FAILED;
            }

        }
        else
        {
            if(cache_model.msg_status == MsgConstDef.MSG_STATUS.DOWNLOADING)
            {
                cache_model.msg_status = MsgConstDef.MSG_STATUS.DOWNLOAD_FAILED;
            }
        }

        return cache_model;
    }

    private ScDBMessageModel ConvertCache2DB(ScMessageModel cache_model)
    {
        ScDBMessageModel db_model = new ScDBMessageModel();
        db_model.message_id = cache_model.message_id;
        db_model.opt_user_id = cache_model.opt_user_id;
        db_model.send_user_name = cache_model.send_user_name;
        db_model.is_self = cache_model.is_self;
        db_model.b_id = cache_model.b_id;
        db_model.w_id = cache_model.w_id;
        db_model.c_id = cache_model.c_id;
        db_model.app_id = cache_model.app_id;
        db_model.session_id = cache_model.session_id;
        db_model.chat_type = cache_model.chat_type;
        db_model.m_type = cache_model.m_type;
        db_model.s_type = cache_model.s_type;
        db_model.ext_type = cache_model.ext_type;
        db_model.msg_content = cache_model.msg_content;
        db_model.msg_time = cache_model.msg_time;
        db_model.msg_status = cache_model.msg_status;
        db_model.audio_path = cache_model.audio_path;
        db_model.pic_path = cache_model.pic_path;
        db_model.compress_path = cache_model.compress_path;
        return db_model;
    }

    //加载更多消息[从DB获取]
    public ArrayList<BaseMessageModel> LoadMoreMessage(long session_id)
    {
        ArrayList<BaseMessageModel> ret_list = new ArrayList<>();
        ArrayList<ScMessageModel> cur_list = message_all_list.get(session_id);
        if(null == cur_list)
        {
            cur_list = new ArrayList<>();
            message_all_list.put(session_id, cur_list);
        }

        int cur_length = cur_list.size();
        List<ScDBMessageModel> db_msg_list = m_sc_db_manager.getMessageList(session_id, cur_length, LIMIT);
        if(null == db_msg_list || db_msg_list.isEmpty())
        {
            return ret_list;
        }

        for (int index = db_msg_list.size() - 1; index >= 0; index--)
        {
            ScDBMessageModel db_msg = db_msg_list.get(index);
            ScMessageModel cache_msg = ConvertDB2Cache(db_msg);
            ret_list.add(cache_msg);
            NIM_Chat_ID chat_id = new NIM_Chat_ID(db_msg.opt_user_id, 0);
            chat_id.message_id = cache_msg.message_id;
            AddMessage(chat_id, cache_msg, false, false);
        }

        return ret_list;
    }

    //获取消息列表
    public ArrayList<BaseMessageModel> GetMessageList(long session_id)
    {
        ArrayList<BaseMessageModel> ret_list = new ArrayList<>();
        if(!message_all_list.containsKey(session_id))
        {
            return ret_list;
        }
        ArrayList<ScMessageModel> src_list = message_all_list.get(session_id);
        for (ScMessageModel sc_model: src_list)
        {
            ret_list.add(sc_model);
        }
        return ret_list;
    }

    /**
     * 添加单条消息
     * @param chat_id 会话id以及消息id
     * @param message_model 消息
     * @param write_sql 是否写入数据库
     * @param notify 是否通知到界面
     * @return 1
     */
    public int AddMessage(NIM_Chat_ID chat_id, ScMessageModel message_model, boolean write_sql, boolean notify)
    {
        if (chat_id.session_id <= 0 || chat_id.message_id <= 0 || message_model == null)
        {
            Logger.error(TAG, "invalid message session_id = " + chat_id.session_id + " message_id = " + chat_id.message_id);
            return -1;
        }

        HashMap<Long, Byte> msg_info_map = user_msg_info_map.get(chat_id.session_id);
        if(null == msg_info_map)
        {
            msg_info_map = new HashMap<>();
            user_msg_info_map.put(chat_id.session_id, msg_info_map);
        }

        if(msg_info_map.containsKey(chat_id.message_id))
        {
            Logger.warning(TAG, "message is exist: " + chat_id.message_id);
            return -1;
        }
        msg_info_map.put(chat_id.message_id, (byte) 1);


        ArrayList<ScMessageModel> cache_msg_list = message_all_list.get(chat_id.session_id);
        if(null == cache_msg_list)
        {
            cache_msg_list = new ArrayList<>();
            message_all_list.put(chat_id.session_id, cache_msg_list);
        }

        if(chat_id.index == 0)
        {
            //从前往后添加
            cache_msg_list.add(0, message_model);
            chat_id.index = 0;
        }
        else
        {
            //从后往前添加
            cache_msg_list.add(message_model);
            chat_id.index = (cache_msg_list.size() - 1);
        }


        if (write_sql)
        {
            //添加未读数
            if(!message_model.is_self && !(message_model.m_type == MsgConstDef.MSG_M_TYPE.TEXT
                    && message_model.s_type == MsgConstDef.MSG_S_TYPE.TIP))
            {
                MsgCountModel msg_c_model = new MsgCountModel();
                msg_c_model.session_id = message_model.opt_user_id;
                msg_c_model.chat_type = MsgConstDef.MSG_CHAT_TYPE.PRIVATE;
                msg_c_model.unread_count = 1;
                NIMMsgCountManager.getInstance().UpsertUnreadCount(msg_c_model);
            }

            ScDBMessageModel db_model = ConvertCache2DB(message_model);
            m_sc_db_manager.insertOrReplace(db_model);
        }

        //是否需要抛通知消息
        if(notify)
        {
            //通知私聊消息界面
            DataObserver.Notify(DataConstDef.EVENT_SC_CHAT_MESSAGE, new NIM_Chat_ID(chat_id.session_id, chat_id.index),
                    MsgConstDef.MSG_OP_TYPE.ADD);
            //通知会话界面
            if(message_model.m_type == MsgConstDef.MSG_M_TYPE.TEXT &&
                    message_model.s_type == MsgConstDef.MSG_S_TYPE.TIP)
            {
                DataObserver.Notify(DataConstDef.EVENT_SC_CHAT_SESSION, chat_id.session_id, false);
            }
            else
            {
                DataObserver.Notify(DataConstDef.EVENT_SC_CHAT_SESSION, chat_id.session_id, true);
            }
        }

        return chat_id.index;
    }

    /**
     * @param list private message list
     * @return 1
     */
    public int AddMessageList(List<ScMessageModel> list)
    {
        ArrayList<ScDBMessageModel> db_list = new ArrayList<>();
        for (ScMessageModel msg : list)
        {
            db_list.add(ConvertCache2DB(msg));
            //只需要通知，存DB统一存
            AddMessage(new NIM_Chat_ID(msg.opt_user_id, msg.message_id), msg, false, true);
        }

        m_sc_db_manager.insertList(db_list);
        return 1;
    }

    //根据指定chat_type获取图片列表
    public ArrayList<String> GetPicPathList(long session_id)
    {
        ArrayList<String> ret_list = new ArrayList<>();
        img_position.clear();
        if(!message_all_list.containsKey(session_id))
        {
            return ret_list;
        }
        ArrayList<ScMessageModel> src_list = message_all_list.get(session_id);
        for (int i = 0; i < src_list.size(); i++)
        {
            ScMessageModel sc_model = src_list.get(i);
            if(sc_model.m_type == MsgConstDef.MSG_M_TYPE.IMAGE) {
                if (sc_model.is_self) {
                    ret_list.add(sc_model.compress_path);
                } else {
                    ret_list.add(sc_model.msg_content);
                }

                img_position.put(i, ret_list.size() - 1);
            }
        }
        return ret_list;
    }

    public HashMap<Integer, Integer> getImgPosition() {
        return img_position;
    }

    /**
     * 首次进入会话界面获取最后一条消息
     * @param session_id 界面id
     * @return private message
     */
    public ScMessageModel GetLastMessage(long session_id) {
        // 内存
        if (message_all_list.get(session_id) == null) {
            return null;
        }
        int size = message_all_list.get(session_id).size();
        if (size > 0) {
            return message_all_list.get(session_id).get(size - 1);
        }
        return null;
    }

    /**
     * 添加一条消息
     *
     * @param chat_id       session_id and message_id
     * @param message_model 消息数据
     * @return 1
     */
    public int AddMessage(NIM_Chat_ID chat_id, ScMessageModel message_model) {
        return AddMessage(chat_id, message_model, true, true);
    }

    public ScMessageModel GetMessage(NIM_Chat_ID chat_id)
    {
        if(chat_id.session_id <= 0)
        {
            return null;
        }

        ScMessageModel msg_model;
        //优先按索引查找
        if(chat_id.index >= 0 && message_all_list.size() > 0 &&
                message_all_list.get(chat_id.session_id).size() > chat_id.index)
        {
            msg_model = message_all_list.get(chat_id.session_id).get(chat_id.index);
            return msg_model;
        }


        return GetMessageByMessageID(chat_id);
    }

    // 删除一条消息
    public int RemoveMessage(NIM_Chat_ID chat_id)
    {
        if (chat_id.session_id <= 0)
        {
            Logger.error(TAG, "invalid message");
        }

        HashMap<Long, Byte> msg_info_map = user_msg_info_map.get(chat_id.session_id);
        ArrayList<ScMessageModel> s_model_list = message_all_list.get(chat_id.session_id);
        if(null == s_model_list || s_model_list.isEmpty())
        {
            Logger.error(TAG, "not in s_model_list session_id = " + chat_id.session_id + " index = " + chat_id.index +
                    " message_id = " + chat_id.message_id);
            return -1;
        }

        if(null == msg_info_map)
        {
            Logger.error(TAG, "msg_info_map is null session_id = " + chat_id.session_id + " index = " + chat_id.index +
            " message_id = " + chat_id.message_id);
            return -1;
        }

        //优先按索引删除
        if(chat_id.index >= 0 && chat_id.index < s_model_list.size())
        {
            ScMessageModel remove_obj = s_model_list.get(chat_id.index);
            s_model_list.remove(chat_id.index);
            msg_info_map.remove(remove_obj.message_id);
            //同步DB
            m_sc_db_manager.deleteByKey(remove_obj.message_id);
            DataObserver.Notify(DataConstDef.EVENT_SC_CHAT_MESSAGE, chat_id, MsgConstDef.MSG_OP_TYPE.DELETE);
            //删除的是最后一条消息,通知会话界面
            int list_size = s_model_list.size();
            if(chat_id.index >= list_size)
            {
                DataObserver.Notify(DataConstDef.EVENT_SC_CHAT_SESSION, chat_id.session_id, null);
            }
            return 1;
        }

        if(chat_id.message_id == -1 || !msg_info_map.containsKey(chat_id.message_id))
        {
            Logger.error(TAG, "not in msg_info_map session_id = " + chat_id.session_id + " index = " + chat_id.index +
                    " message_id = " + chat_id.message_id);
            return -1;
        }

        int del_index = 0;
        for (; del_index < s_model_list.size(); del_index++)
        {
            ScMessageModel remove_obj = s_model_list.get(del_index);
            if(remove_obj.message_id == chat_id.message_id)
            {
                s_model_list.remove(del_index);
                msg_info_map.remove(remove_obj.message_id);
                //同步DB
                m_sc_db_manager.deleteByKey(remove_obj.message_id);
                chat_id.index = del_index;
                break;
            }
        }

        DataObserver.Notify(DataConstDef.EVENT_SC_CHAT_MESSAGE, chat_id, MsgConstDef.MSG_OP_TYPE.DELETE);
        //删除的是最后一条消息,通知会话界面
        int list_size = s_model_list.size();
        if(chat_id.index >= list_size)
        {
            DataObserver.Notify(DataConstDef.EVENT_SC_CHAT_SESSION, chat_id.session_id, null);
        }
        return -1;
    }

    // 清空当前会话所有消息
    public boolean ClearMessage(long session_id, boolean is_notify)
    {
        if (session_id <= 0)
        {
            Logger.error(TAG, "invalid message");
            return false;
        }

        HashMap<Long, Byte> msg_info_map = user_msg_info_map.get(session_id);
        if(msg_info_map != null)
            msg_info_map.clear();

        ArrayList<ScMessageModel> message_list = message_all_list.get(session_id);
        if(message_list != null)
            message_list.clear();

        //从DB中删除
        m_sc_db_manager.deleteAllMessage(session_id);

        if(is_notify)
            DataObserver.Notify(DataConstDef.EVENT_SC_CHAT_SESSION, session_id, null);
        return true;
    }

    public ScMessageModel GetMessageByMessageID(NIM_Chat_ID chat_id)
    {
        HashMap<Long, Byte> msg_info_map = user_msg_info_map.get(chat_id.session_id);
        if (null == msg_info_map || !msg_info_map.containsKey(chat_id.message_id))
        {
            return null;
        }

        ArrayList<ScMessageModel> message_info_list = message_all_list.get(chat_id.session_id);

        int nSize = message_info_list.size();
        //从后往前找
        for (int index = nSize - 1; index >= 0; index--)
        {
            ScMessageModel model = message_info_list.get(index);
            if (model.message_id == chat_id.message_id)
            {
                chat_id.index = index;
                return model;
            }
        }

        return null;
    }

    public int SetMessageStatus(long session_id, long message_id, short msg_status)
    {
        HashMap<Long, Byte> msg_info_map = user_msg_info_map.get(session_id);
        if (null == msg_info_map || !msg_info_map.containsKey(message_id))
        {
            return -1;
        }

        ArrayList<ScMessageModel> message_info_list = message_all_list.get(session_id);
        int nSize = message_info_list.size();
        //从后往前找
        for (int index = nSize - 1; index >= 0; index--)
        {
            ScMessageModel model = message_info_list.get(index);
            if (model.message_id == message_id)
            {
                if(model.msg_status == msg_status)
                    return index;

                model.msg_status = msg_status;
                //同步到DB
                m_sc_db_manager.update(ConvertCache2DB(model));
                DataObserver.Notify(DataConstDef.EVENT_SC_CHAT_MESSAGE, new NIM_Chat_ID(session_id, index), MsgConstDef.MSG_OP_TYPE.UPDATE);
                if(index == nSize - 1)
                {
                    DataObserver.Notify(DataConstDef.EVENT_SC_CHAT_SESSION, session_id, null);
                }
                return index;
            }
        }

        return -1;
    }

    // 生成一条私聊提示信息
    public void GenTipsMessage(long user_id, String tips)
    {
        ScMessageModel messageModel = new ScMessageModel();
        messageModel.chat_type = MsgConstDef.MSG_CHAT_TYPE.PRIVATE;
        messageModel.m_type = MsgConstDef.MSG_M_TYPE.TEXT;
        messageModel.s_type = MsgConstDef.MSG_S_TYPE.TIP;
        messageModel.msg_status = MsgConstDef.MSG_STATUS.SEND_SUCCESS;
        messageModel.msg_time = BaseUtil.GetServerTime();
        messageModel.message_id = NetCenter.getInstance().CreateMsgID();
        messageModel.opt_user_id = user_id;
        messageModel.msg_content = tips;
        NIMMsgManager.getInstance().AddMessage(new NIM_Chat_ID(user_id
                , messageModel.message_id), messageModel, true, true);
    }

    public void GenFriendTipsMessage(long user_id, String msg_content)
    {
        ScMessageModel messageModel = new ScMessageModel();
        messageModel.chat_type = MsgConstDef.MSG_CHAT_TYPE.PRIVATE;
        messageModel.m_type = MsgConstDef.MSG_M_TYPE.TEXT;
        messageModel.msg_status = MsgConstDef.MSG_STATUS.UNREAD;
        messageModel.msg_time = BaseUtil.GetServerTime();
        messageModel.message_id = NetCenter.getInstance().CreateMsgID();
        messageModel.opt_user_id = user_id;
        messageModel.is_self = false;
        messageModel.msg_content = msg_content;
        NIMMsgManager.getInstance().AddMessage(new NIM_Chat_ID(user_id
                , messageModel.message_id ), messageModel, true, true);
    }


    public void clear() {
        user_msg_info_map.clear();
        message_all_list.clear();
    }
}
