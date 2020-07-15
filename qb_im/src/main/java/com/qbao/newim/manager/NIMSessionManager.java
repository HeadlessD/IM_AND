package com.qbao.newim.manager;

import com.qbao.newim.configure.GlobalVariable;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.model.SessionModel;
import com.qbao.newim.model.SessionResult;
import com.qbao.newim.model.message.GcMessageModel;
import com.qbao.newim.model.message.ScMessageModel;
import com.qbao.newim.qbdb.manager.SessionDbManager;
import com.qbao.newim.util.BaseUtil;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.Logger;
import com.qbao.newim.util.SharedPreferenceUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by chenjian on 2017/5/11.
 * 会话列表管理器
 */

public class NIMSessionManager {
    public static String TAG = NIMSessionManager.class.getSimpleName();
    private static NIMSessionManager instance;
    //总会话列表[任务助手，订阅助手，群助手，私聊会话，群聊会话，公众号会话]
    private ArrayList<SessionModel> m_g_session_list = new ArrayList<>();
    //固定会话列表[任务助手，订阅助手，群助手]
    private ArrayList<SessionModel> m_fixed_session_list = new ArrayList<>();
    //群助手会话列表
    private ArrayList<SessionModel> m_assist_session_list = new ArrayList<>();
    private int m_g_s_top_count = 0;
    private int m_assist_s_top_count = 0;

    private long cur_session_id = -1;
    public static NIMSessionManager getInstance() {
        if (instance == null) {
            instance = new NIMSessionManager();
        }

        return instance;
    }


    public SessionModel UpdateSession(SessionModel s_model)
    {
        // 当前聊天类型
        switch (s_model.chat_type)
        {
            // 私聊
            case MsgConstDef.MSG_CHAT_TYPE.PRIVATE:
                {
                    // 获取最新的1条消息
                    ScMessageModel sc_message_model = NIMMsgManager.getInstance().GetLastMessage(s_model.session_id);
                    if (null != sc_message_model)
                    {
                        s_model.msg_time = sc_message_model.msg_time;
                    }
                }
                break;
            case MsgConstDef.MSG_CHAT_TYPE.GROUP:
                {
                    GcMessageModel gc_m_model = NIMGroupMsgManager.getInstance().getLastMessageInfoByGroupID(s_model.session_id);
                    if (null != gc_m_model)
                    {
                        s_model.msg_time = gc_m_model.msg_time;
                    }
                }
                break;
            // 当前公众号类型
            case MsgConstDef.MSG_CHAT_TYPE.PUBLIC:
                // 当前系统消息
            case MsgConstDef.MSG_CHAT_TYPE.SYS:
                // 当前任务助手
            case MsgConstDef.MSG_CHAT_TYPE.TASK:
                // 当前订阅助手
            case MsgConstDef.MSG_CHAT_TYPE.SUBSCRIBE:
            case MsgConstDef.MSG_CHAT_TYPE.ASSIST:
                {
                    SessionModel last_assist_model = GetASessionByIndex(0);
                    if(null != last_assist_model)
                    {
                        s_model.msg_time = last_assist_model.msg_time;
                    }
                }
                break;
        }
        return s_model;
    }

    public void InitFixedSessionList()
    {
        m_fixed_session_list.add(CreateTaskModel());
        m_fixed_session_list.add(CreateSubscribeModel());
    }

    //初始化置顶列表
    public void InitTopSessionList()
    {
        List<SessionModel> top_list = SessionDbManager.getInstance().getTopSession();
        if(null != top_list && !top_list.isEmpty())
        {
            for(SessionModel top_model : top_list)
            {
                if(top_model.chat_type == MsgConstDef.MSG_CHAT_TYPE.GROUP)
                {
                    IMGroupInfo group_info = NIMGroupInfoManager.getInstance().getGroupInfo(top_model.session_id);
                    if(null == group_info)
                        continue;

                    if(group_info.notify_type == MsgConstDef.GROUP_MESSAGE_STATUS.GROUP_MESSAGE_IN_HELP_NO_HIT)
                    {
                        m_assist_session_list.add(top_model);
                        m_assist_s_top_count++;
                    }
                }
                else
                {
                    m_g_s_top_count++;
                    m_g_session_list.add(top_model);
                }
            }
        }
    }

    //初始化非置顶列表
    public void InitNormalSessionList()
    {
        List<SessionModel> normal_list = SessionDbManager.getInstance().getNormalSession();
        if(null != normal_list && !normal_list.isEmpty())
        {
            for(SessionModel normal_model : normal_list)
            {
                if(normal_model.chat_type == MsgConstDef.MSG_CHAT_TYPE.GROUP)
                {
                    IMGroupInfo group_info = NIMGroupInfoManager.getInstance().getGroupInfo(normal_model.session_id);
                    if(null == group_info)
                        continue;

                    if(group_info.notify_type == MsgConstDef.GROUP_MESSAGE_STATUS.GROUP_MESSAGE_IN_HELP_NO_HIT)
                    {
                        m_assist_session_list.add(normal_model);
                    }
                    else
                    {
                        m_g_session_list.add(normal_model);
                    }
                }
                else
                {
                    m_g_session_list.add(normal_model);
                }
            }
        }
    }


    public void init() {
        InitFixedSessionList();
        InitTopSessionList();
        InitNormalSessionList();
    }

    public void clear() {
        m_assist_s_top_count = 0;
        m_assist_session_list.clear();
        m_fixed_session_list.clear();
        m_g_s_top_count = 0;
        m_g_session_list.clear();
    }

    private SessionModel CreateTaskModel() {
        SessionModel sessionModel = new SessionModel();
        sessionModel.session_id = GlobalVariable.TASK_SESSION_ID;
        sessionModel.chat_type = MsgConstDef.MSG_CHAT_TYPE.TASK;
        long time = SharedPreferenceUtil.getTaskTime();
        if (time == 0) {
            time = BaseUtil.GetServerTime();
            SharedPreferenceUtil.saveTaskTime(time);
        }
        sessionModel.msg_time = time;
        return sessionModel;
    }

    private SessionModel CreateSubscribeModel() {
        SessionModel sessionModel = new SessionModel();
        sessionModel.session_id = GlobalVariable.SUBSCRIBE_SESSION_ID;
        sessionModel.chat_type = MsgConstDef.MSG_CHAT_TYPE.SUBSCRIBE;
        long time = SharedPreferenceUtil.getSubscribeTime();
        if (time == 0) {
            time = BaseUtil.GetServerTime();
            SharedPreferenceUtil.saveSubscribeTime(time);
        }
        sessionModel.msg_time = time;
        return sessionModel;
    }

    private SessionModel CreateAssistModel() {
        SessionModel sessionModel = new SessionModel();
        sessionModel.session_id = GlobalVariable.GROUP_ASSIST_SESSION_ID;
        sessionModel.chat_type = MsgConstDef.MSG_CHAT_TYPE.ASSIST;
        sessionModel.msg_time = BaseUtil.GetServerTime();
        return sessionModel;
    }

/****************************m_g_session_list start************************************************/
    private ArrayList<SessionModel> GetFixedSessionList()
    {
        ArrayList<SessionModel> list = (ArrayList<SessionModel>)m_fixed_session_list.clone();
        return list;
    }

    private SessionModel GetFixedSessionByIndex(int index)
    {
        if(index < 0 || index >= m_fixed_session_list.size())
        {
            return null;
        }

        return m_fixed_session_list.get(index);
    }

    private int FixedSessionCount(){return m_fixed_session_list.size();}

    //获取整体会话列表----对应原getSessionData和getSessionMode
    public ArrayList<SessionModel> GetGSessionList(boolean contain_fixed)
    {
        ArrayList<SessionModel> l_list2 = (ArrayList<SessionModel>)m_g_session_list.clone();
        if(contain_fixed)
        {
            ArrayList<SessionModel> l_list1 = GetFixedSessionList();
            l_list1.addAll(l_list2);
            return l_list1;
        }
        else
        {
            return  l_list2;
        }
    }

    //对应原来-----getTopSessionCount
    private int GlobalTopCount() {return m_g_s_top_count;}

    private int GlobalSessionCount(){return m_g_session_list.size();}

    public SessionModel GetGSessionByID(long session_id)
    {
        for(SessionModel session_model : m_g_session_list)
        {
            if(session_model.session_id == session_id)
            {
                return session_model;
            }
        }

        return null;
    }

    private SessionModel GetGSessionByIndex(int index)
    {
        SessionModel session_model = GetFixedSessionByIndex(index);
        if(session_model != null)
        {
            return session_model;
        }

        index -= FixedSessionCount();
        if(index < 0 || index >= m_g_session_list.size())
        {
            return null;
        }

        return m_g_session_list.get(index);
    }

    public SessionResult DelGlobalSession(int del_index, long session_id, boolean operate_db)
    {
        SessionResult s_result = new SessionResult();
        //优先按索引删除[为了提高效率]
        del_index -= FixedSessionCount();
        if(del_index >= 0 && del_index < m_g_session_list.size())
        {
            SessionModel del_model = m_g_session_list.get(del_index);
            if(del_model.is_top)
            {
                m_g_s_top_count--;
            }
            m_g_session_list.remove(del_index);
            //从db中删除
            if(operate_db)
                SessionDbManager.getInstance().deleteByKey(del_model.session_id);
            s_result.remove_index = del_index + FixedSessionCount();
            s_result.op_s_model = del_model;
            return s_result;
        }


        //按session_id删除
        for (int index = 0; index < m_g_session_list.size(); index++) {
            SessionModel del_model = m_g_session_list.get(index);
            if(del_model.session_id == session_id)
            {
                if(del_model.is_top)
                {
                    m_g_s_top_count--;
                }
                m_g_session_list.remove(index);
                //从db中删除
                if(operate_db)
                    SessionDbManager.getInstance().deleteByKey(session_id);
                s_result.remove_index = index + FixedSessionCount();
                s_result.op_s_model = del_model;
                return s_result;
            }
        }

        s_result.remove_index = -1;
        return s_result;
    }

    private int AddGlobalSession(SessionModel session_model, boolean operate_db)
    {
        UpdateSession(session_model);
        int insert_index = 0;
        for (int index = 0; index < m_g_session_list.size(); index++) {
            SessionModel value = m_g_session_list.get(index);
            //有序插入
            if(session_model.is_top && value.is_top)
            {
                if(value.msg_time > session_model.msg_time)
                {
                    insert_index++;
                }
                else
                {
                    insert_index = index;
                    break;
                }
            }

            if(!session_model.is_top &&
                    !value.is_top)
            {
                if(value.msg_time > session_model.msg_time)
                {
                    insert_index++;
                }
                else
                {
                    insert_index = index;
                    break;
                }
            }
        }

        if(session_model.is_top)
        {
            m_g_s_top_count++;
        }

        if(insert_index >= m_g_session_list.size())
        {
            m_g_session_list.add(session_model);
            insert_index =  m_g_session_list.size() - 1;
        }
        else
        {
            m_g_session_list.add(insert_index, session_model);
        }

        if(operate_db)
            SessionDbManager.getInstance().insert(session_model);

        return insert_index + FixedSessionCount();
    }

    //添加或者更新
    public SessionResult UpSertGlobalSession(int del_index, SessionModel session_model)
    {
        SessionResult s_result = new SessionResult();
        s_result.op_s_model = session_model;

        //先删除,无需操作DB，下面会统一操作，减少一次DB操作
        SessionResult del_result = DelGlobalSession(del_index, session_model.session_id, false);
        s_result.remove_index = del_result.remove_index;
        //时间可以用原来的时间
        if(del_result.remove_index > 0 &&
                del_result.op_s_model != null)
        {
            session_model.msg_time = del_result.op_s_model.msg_time;
        }

        //再添
        s_result.add_index = AddGlobalSession(session_model, false);

        //同步db
        SessionDbManager.getInstance().insertOrReplace(session_model);
        return s_result;
    }

/****************************m_g_session_list end**************************************************/


/****************************m_assist_session_list start*******************************************/
    // 获取群助手----对应原getAssistData
    public ArrayList<SessionModel> GetASessionList() {
        ArrayList<SessionModel> l_list = (ArrayList<SessionModel>)m_assist_session_list.clone();
        return l_list;
    }

    //对应原来----getTopAssistCount
    public int AssistTopCount() {
        return m_assist_s_top_count;
    }

    public int AssistSessionCount()
    {
        return m_assist_session_list.size();
    }

    private SessionModel GetASessionByID(long session_id)
    {
        for(SessionModel session_model : m_assist_session_list)
        {
            if(session_model.session_id == session_id)
            {
                return session_model;
            }
        }

        return null;
    }

    public SessionModel GetASessionByIndex(int index)
    {
        if(index < 0 || index >= m_assist_session_list.size())
        {
            return null;
        }

        return m_assist_session_list.get(index);
    }

    public SessionResult DelAssitSession(int del_index, long session_id, boolean operate_db)
    {
        SessionResult s_result = new SessionResult();
        //优先按索引删除[为了提高效率]
        if(del_index >= 0 && del_index < m_assist_session_list.size())
        {
            SessionModel del_model = m_assist_session_list.get(del_index);
            if(del_model.is_top)
            {
                m_assist_s_top_count--;
            }
            m_assist_session_list.remove(del_index);
            //从db中删除
            if(operate_db)
                SessionDbManager.getInstance().deleteByKey(del_model.session_id);
            s_result.remove_index = del_index;
            s_result.op_s_model = del_model;
            return s_result;
        }


        //按session_id删除
        for (int index = 0; index < m_assist_session_list.size(); index++) {
            SessionModel del_model = m_assist_session_list.get(index);
            if(del_model.session_id == session_id)
            {
                if(del_model.is_top)
                {
                    m_assist_s_top_count--;
                }
                m_assist_session_list.remove(index);
                //从db中删除
                if(operate_db)
                    SessionDbManager.getInstance().deleteByKey(session_id);
                s_result.remove_index = index;
                s_result.op_s_model = del_model;
                return s_result;
            }
        }

        s_result.remove_index = -1;
        return s_result;
    }

    private int AddAssistSession(SessionModel session_model, boolean operate_db)
    {
        UpdateSession(session_model);
        int insert_index = 0;
        for (int index = 0; index < m_assist_session_list.size(); index++)
        {
            SessionModel value = m_assist_session_list.get(index);
            //有序插入
            if(session_model.is_top && value.is_top)
            {
                if(value.msg_time > session_model.msg_time)
                {
                    insert_index++;
                }
                else
                {
                    insert_index = index;
                    break;
                }
            }

            if(!session_model.is_top &&
                    !value.is_top)
            {
                if(value.msg_time > session_model.msg_time)
                {
                    insert_index++;
                }
                else
                {
                    insert_index = index;
                    break;
                }
            }
        }

        if(session_model.is_top)
        {
            m_g_s_top_count++;
        }

        if(insert_index >= m_assist_session_list.size())
        {
            m_assist_session_list.add(session_model);
            insert_index =  m_assist_session_list.size() - 1;
        }
        else
        {
            m_assist_session_list.add(insert_index, session_model);
        }

        if(operate_db)
            SessionDbManager.getInstance().insert(session_model);

        return insert_index;
    }

    //对应原receiveAssistMsgToShow
    public SessionResult UpSertAssistSession(int del_index, SessionModel session_model)
    {
        SessionResult s_result = new SessionResult();
        s_result.op_s_model = session_model;

        //先删除,无需操作DB，下面会统一操作，减少一次DB操作
        SessionResult del_result = DelAssitSession(del_index, session_model.session_id, false);
        s_result.remove_index = del_result.remove_index;
        //可以暂时用原来的时间
        if(del_result.remove_index >= 0 &&
                del_result.op_s_model != null)
        {
            session_model.msg_time = del_result.op_s_model.msg_time;
        }

        //再添加
        s_result.add_index = AddAssistSession(session_model, false);

        //同步db
        SessionDbManager.getInstance().insertOrReplace(session_model);
        return s_result;
    }

/****************************m_assist_session_list end*********************************************/
    // 获取会话列表----getSessionMode
    public SessionModel GetSessionByID(long session_id) {
        SessionModel session_model = GetGSessionByID(session_id);
        if(null != session_model)
        {
            return session_model;
        }

        session_model = GetASessionByID(session_id);
        if(null != session_model)
        {
            return session_model;
        }

        return null;
    }

    // 更新会话列表-----对应原setSessionMode receiveMsgToShow
    public SessionResult UpSertSession(long session_id, SessionModel model) {
        IMGroupInfo groupInfo = NIMGroupInfoManager.getInstance().getGroupInfo(session_id);
        if (groupInfo != null && groupInfo.notify_type == MsgConstDef.GROUP_MESSAGE_STATUS.GROUP_MESSAGE_IN_HELP_NO_HIT) {
            return UpSertAssistSession(-1, model);
        }

        return UpSertGlobalSession(-1, model);
    }



    /**
     * 从会话界面转到群助手
     * @return
     */
    //对应原来switchToAssist
    public ArrayList<SessionResult> EnterAssist(SessionModel session_model) {
        if (session_model.session_id <= 0) {
            Logger.error(TAG, "invalid session");
            return null;
        }

        ArrayList<SessionResult> s_result_list = new ArrayList<>();
        SessionResult s_result = new SessionResult();
        //删除原来m_g_session_list中的session_model
        SessionResult del_result = DelGlobalSession(-1, session_model.session_id, true);
        s_result.remove_index = del_result.remove_index;

        //可以暂时用原来的时间
        if(del_result.remove_index >= 0 &&
                del_result.op_s_model != null)
        {
            session_model.msg_time = del_result.op_s_model.msg_time;
        }

        //添加session_model到m_assist_session_list
        SessionResult u_s_result = UpSertAssistSession(-1, session_model);
        s_result.add_index = u_s_result.add_index;
        s_result.op_s_model = session_model;

        //添加[更新]群助手session_model到m_g_session_list
        SessionModel assist_model = CreateAssistModel();
        SessionResult assist_result = UpSertGlobalSession(-1, assist_model);

        s_result_list.add(s_result);
        s_result_list.add(assist_result);
        DataObserver.Notify(DataConstDef.EVENT_ENTER_ASSIST, s_result_list, null);
        return s_result_list;
    }

    /**
     * 从群助手转到会话界面
     * @return
     */
    //对应原来switchToSession
    public ArrayList<SessionResult> LeaveAssist(SessionModel session_model) {
        if (session_model.session_id <= 0) {
            Logger.error(TAG, "invalid session");
            return null;
        }

        ArrayList<SessionResult> s_result_list = new ArrayList<>();
        SessionResult s_result = new SessionResult();
        //删除原来m_assist_session_list中的群session_model
        SessionResult del_result = DelAssitSession(-1, session_model.session_id, true);
        s_result.remove_index = del_result.remove_index;
        //可以暂时用原来的时间
        if(del_result.remove_index >= 0 &&
                del_result.op_s_model != null)
        {
            session_model.msg_time = del_result.op_s_model.msg_time;
        }

        //添加session_model到m_g_session_list
        SessionResult u_s_result = UpSertGlobalSession(-1, session_model);
        s_result.add_index = u_s_result.add_index;
        s_result.op_s_model = session_model;

        //添加[更新]群助手session_model到m_g_session_list
        SessionModel assist_model = CreateAssistModel();
        SessionResult assist_result = UpSertGlobalSession(-1, assist_model);

        s_result_list.add(s_result);
        s_result_list.add(assist_result);
        DataObserver.Notify(DataConstDef.EVENT_LEAVE_ASSIST, s_result_list, null);
        return s_result_list;
    }

    public long GetCurSession() {
        return cur_session_id;
    }

    public void SetCurSession(long cur_session_id) {
        this.cur_session_id = cur_session_id;
    }

    /**
     * 删除会话功能模块
     */
    // 删除会话,原来驱动删除消息，这个需要提到高层(界面层去删除)----deleteSession驱动deleteSessionFromSql和deleteSessionFromCache
    // 再次驱动了ChatMsgDbManager.getInstance().deleteAllMessage(session_id);

    // 置顶功能setTopSession可以直接调用对应的UpsertXXXXXX函数



    // 标记未读已读-----setFlagRead, 这个需要抛到界面层去驱动消息管理器
}
