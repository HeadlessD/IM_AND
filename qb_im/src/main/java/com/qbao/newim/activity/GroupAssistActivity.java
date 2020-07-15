package com.qbao.newim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.qbao.newim.adapter.AssistAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.configure.GlobalVariable;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.ErrorDetail;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.manager.NIMGroupInfoManager;
import com.qbao.newim.manager.NIMGroupMsgManager;
import com.qbao.newim.manager.NIMMsgCountManager;
import com.qbao.newim.manager.NIMSessionManager;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.model.MsgCountModel;
import com.qbao.newim.model.NIM_Chat_ID;
import com.qbao.newim.model.SessionModel;
import com.qbao.newim.model.SessionResult;
import com.qbao.newim.model.message.GcMessageModel;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.BaseUtil;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.IDataObserver;
import com.qbao.newim.util.Logger;
import com.qbao.newim.views.ProgressDialog;
import com.qbao.newim.views.imgpicker.NIM_ToolbarAct;

import java.util.ArrayList;

/**
 * Created by chenjian on 2017/7/10.
 */

public class GroupAssistActivity extends NIM_ToolbarAct implements IDataObserver{

    private RecyclerView recyclerView;
    private AssistAdapter mAdapter;
    private static final int REFRESH_SESSION_ADAPTER = 0x10001;
    private static final int ENTER_GROUP_ASSIST = 0x10002;
    private static final int LEAVE_GROUP_ASSIST = 0x10003;

    public Handler m_handler = new Handler() {
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case REFRESH_SESSION_ADAPTER:
                    {
                        SessionResult s_result = (SessionResult) msg.obj;
                        int size = mAdapter.getData().size();
                        if (s_result.remove_index >= 0 && s_result.remove_index < size) {
                            mAdapter.remove(s_result.remove_index);
                        }

                        if (s_result.add_index >= 0) {
                            mAdapter.addData(s_result.add_index, s_result.op_s_model);
                        }
                    }
                    break;
                case LEAVE_GROUP_ASSIST:
                    {
                        ArrayList<SessionResult> s_result_list = (ArrayList<SessionResult>)msg.obj;
                        if(s_result_list == null || s_result_list.size() < 2)
                        {
                            break;
                        }
                        SessionResult s_result = s_result_list.get(0);
                        SessionResult assist_result = s_result_list.get(1);
                        int size = mAdapter.getData().size();
                        //删除全局群会话
                        if(s_result.remove_index >= 0 && s_result.remove_index < size)
                        {
                            mAdapter.remove(s_result.remove_index);
                        }
                    }
                    break;
                case ENTER_GROUP_ASSIST:
                    {
                        ArrayList<SessionResult> s_result_list = (ArrayList<SessionResult>)msg.obj;
                        if(s_result_list == null || s_result_list.size() < 2)
                        {
                            break;
                        }
                        SessionResult s_result = s_result_list.get(0);
                        SessionResult assist_result = s_result_list.get(1);
                        int size = mAdapter.getData().size();
                        //添加全局群会话
                        if(s_result.add_index >= 0)
                        {
                            mAdapter.addData(s_result.add_index, s_result.op_s_model);
                        }
                    }
                break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.nim_activity_assist);
        recyclerView = getViewById(R.id.assist_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void setListener() {
        mAdapter = new AssistAdapter(this, null);
        recyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener()
        {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position)
            {
                SessionModel entity = mAdapter.getData().get(position);
                int unread_count = NIMMsgCountManager.getInstance().GetUnreadCount(entity.session_id, entity.chat_type);
                if (unread_count > 0)
                {
                    MsgCountModel m_c_model = new MsgCountModel();
                    m_c_model.session_id = entity.session_id;
                    m_c_model.chat_type = entity.chat_type;
                    m_c_model.unread_count = 0;
                    NIMMsgCountManager.getInstance().RemoveUnreadCount(m_c_model);
                    mAdapter.setData(position, entity);
                }
                Intent intent_gc = new Intent(GroupAssistActivity.this, NIMGcChatActivity.class);
                intent_gc.putExtra("id", entity.session_id);
                startActivity(intent_gc);
            }
        });

        mAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                showEntityDialog(position);
                return false;
            }
        });
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        ArrayList<SessionModel> data = NIMSessionManager.getInstance().GetASessionList();
        mAdapter.addData(data);
        DataObserver.Register(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.nim_toolbar_title, menu);
        MenuItem menuItem = menu.findItem(R.id.item_toolbar);
        View actionView = menuItem.getActionView();

        TextView tvTitle = (TextView) actionView.findViewById(R.id.title_txt);
        tvTitle.setText(R.string.group_assist);

        actionView.findViewById(R.id.title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        return true;
    }

    @Override
    public void onDestroy() {

        DataObserver.Notify(DataConstDef.EVENT_UNREAD_CLEAR, GlobalVariable.GROUP_ASSIST_SESSION_ID,
                MsgConstDef.MSG_CHAT_TYPE.ASSIST);

        super.onDestroy();
        DataObserver.Cancel(this);
    }

    //这个函数里面不应该直接有对界面的操作[大部分都是子线程调用的这个函数，所以需要抛到主线程执行]
    @Override
    public void OnChange(int param1, Object param2, Object param3) {
        switch (param1) {
            case DataConstDef.EVENT_NET_ERROR:
                Logger.error(TAG, "EVENT_NET_ERROR");
                if (is_active) {
                    int error_code = BaseUtil.MakeErrorResult((int) param2);
                    String error_msg = ErrorDetail.GetErrorDetail(error_code);
                    showToastStr(error_msg);
                }
                break;
            case DataConstDef.EVENT_MESSAGE_TIME_OUT:
                {
                    // 超时如果在群助手里面，更新对应会话列表[注意需要保证先更新消息状态]
                    Logger.error(TAG, "EVENT_MESSAGE_TIME_OUT");
                    NIM_Chat_ID out_chat = (NIM_Chat_ID) param2;
                    if(out_chat.chat_type != MsgConstDef.MSG_CHAT_TYPE.GROUP)
                        break;

                    //如果不是群助手不管
                    IMGroupInfo group_info = NIMGroupInfoManager.getInstance().getGroupInfo(out_chat.session_id);
                    if (null == group_info || group_info.notify_type != MsgConstDef.GROUP_MESSAGE_STATUS.GROUP_MESSAGE_IN_HELP_NO_HIT)
                    {
                        break;
                    }

                    SessionModel session_model = new SessionModel();
                    session_model.session_id = out_chat.session_id;
                    session_model.chat_type = MsgConstDef.MSG_CHAT_TYPE.GROUP;
                    SessionResult s_result = NIMSessionManager.getInstance().UpSertAssistSession(-1, session_model);
                    Message msg = new Message();
                    msg.what = REFRESH_SESSION_ADAPTER;
                    msg.obj = s_result;
                    m_handler.sendMessage(msg);
                }
                break;
            case DataConstDef.EVENT_GROUP_DELETE:
            {
                long group_id = (long)param2;
                if (mAdapter.isExist(group_id)) {
                    deleteSession(group_id);
                    NIMGroupInfoManager.getInstance().DeleteGroup(group_id);
                }
                break;
            }
            case DataConstDef.EVENT_GROUP_OPERATE:
                boolean client = (boolean) param3;
                if (client) {
                    return;
                }
                GcMessageModel operateMode = (GcMessageModel) param2;
                if (NIMSessionManager.getInstance().GetCurSession() != operateMode.group_id) {
                    operateGroup(operateMode);
                }
                break;
            case DataConstDef.EVENT_UNREAD_CLEAR:
                {
                    long group_id = (long)param2;
                    int chat_type = (int)param3;
                    if(chat_type == MsgConstDef.MSG_CHAT_TYPE.ASSIST)
                    {
                        break;
                    }

                    IMGroupInfo group_info = NIMGroupInfoManager.getInstance().getGroupInfo(group_id);
                    if(null == group_info)
                    {
                        break;
                    }

                    if (group_info.notify_type != MsgConstDef.GROUP_MESSAGE_STATUS.GROUP_MESSAGE_IN_HELP_NO_HIT)
                    {
                        break;
                    }

                    int unread_count = NIMMsgCountManager.getInstance().GetUnreadCount(group_id, chat_type);
                    if(unread_count <= 0)
                    {
                        break;
                    }

                    MsgCountModel m_c_model = new MsgCountModel();
                    m_c_model.session_id = group_id;
                    m_c_model.chat_type = chat_type;
                    m_c_model.unread_count = 0;
                    NIMMsgCountManager.getInstance().RemoveUnreadCount(m_c_model);

                    SessionModel session_model = new SessionModel();
                    session_model.session_id = group_id;
                    session_model.chat_type = chat_type;
                    SessionResult s_result = NIMSessionManager.getInstance().UpSertAssistSession(-1, session_model);
                    Message msg = new Message();
                    msg.what = REFRESH_SESSION_ADAPTER;
                    msg.obj = s_result;
                    m_handler.sendMessage(msg);
                }
                break;
            case DataConstDef.EVENT_GET_GROUP_MSG_SESSION:
                {
                    long group_id = (long) param2;
                    //如果不是群助手不管
                    IMGroupInfo group_info = NIMGroupInfoManager.getInstance().getGroupInfo(group_id);
                    if (null == group_info || group_info.notify_type != MsgConstDef.GROUP_MESSAGE_STATUS.GROUP_MESSAGE_IN_HELP_NO_HIT)
                    {
                        break;
                    }
                    SessionModel session_model = new SessionModel();
                    session_model.session_id = group_id;
                    session_model.chat_type = MsgConstDef.MSG_CHAT_TYPE.GROUP;
                    SessionResult s_result = NIMSessionManager.getInstance().UpSertAssistSession(-1, session_model);
                    Message msg = new Message();
                    msg.what = REFRESH_SESSION_ADAPTER;
                    msg.obj = s_result;
                    m_handler.sendMessage(msg);
                }
                break;
            case DataConstDef.EVENT_SESSION_TOP:
                {
                    long session_id = (long) param2;
                    boolean is_top = (boolean) param3;

                    //如果不是群助手不管
                    IMGroupInfo group_info = NIMGroupInfoManager.getInstance().getGroupInfo(session_id);
                    if (null == group_info || group_info.notify_type != MsgConstDef.GROUP_MESSAGE_STATUS.GROUP_MESSAGE_IN_HELP_NO_HIT) {
                        break;
                    }
                    SessionModel session_model = new SessionModel();
                    session_model.session_id = session_id;
                    session_model.is_top = is_top;
                    session_model.chat_type = MsgConstDef.MSG_CHAT_TYPE.GROUP;
                    SessionResult s_result = NIMSessionManager.getInstance().UpSertAssistSession(-1, session_model);
                    Message msg = new Message();
                    msg.what = REFRESH_SESSION_ADAPTER;
                    msg.obj = s_result;
                    m_handler.sendMessage(msg);
                }
                break;
            case DataConstDef.EVENT_ENTER_ASSIST:          //加入群助手
            {
                m_handler.sendMessage(m_handler.obtainMessage(ENTER_GROUP_ASSIST, param2));
            }
            break;
            case DataConstDef.EVENT_LEAVE_ASSIST:
            {
                m_handler.sendMessage(m_handler.obtainMessage(LEAVE_GROUP_ASSIST, param2));
            }
            break;
        }
    }

    private void showEntityDialog(int position) {
        SessionModel entity = mAdapter.getItem(position);
        ArrayList<String> dialog_list = new ArrayList<>();

        int unread_count = NIMMsgCountManager.getInstance().GetUnreadCount(entity.session_id, entity.chat_type);
        dialog_list.add(unread_count > 0 ? getString(R.string.msg_readed) : getString(R.string.msg_unread));
        dialog_list.add(entity.is_top ? getString(R.string.session_canncel_top) : getString(R.string.session_top));
        dialog_list.add(getString(R.string.clear_this_chat));
        dialog_list.add(getString(R.string.nim_permission_cancel));

        createEntityDialog(position, entity, dialog_list);
    }


    private void createEntityDialog(final int pos, final SessionModel entity_model, final ArrayList<String> list) {
        ProgressDialog.showCustomDialog(GroupAssistActivity.this, getString(R.string.operater), list, new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                String s_click = list.get(position);
                if(s_click.equals(getString(R.string.msg_readed)))
                {
                    MsgCountModel m_c_model = new MsgCountModel();
                    m_c_model.session_id = entity_model.session_id;
                    m_c_model.chat_type = entity_model.chat_type;
                    NIMMsgCountManager.getInstance().RemoveUnreadCount(m_c_model);
                    mAdapter.setData(pos, entity_model);
                }
                else if(s_click.equals(getString(R.string.msg_unread)))
                {
                    MsgCountModel m_c_model = new MsgCountModel();
                    m_c_model.session_id = entity_model.session_id;
                    m_c_model.chat_type = entity_model.chat_type;
                    m_c_model.unread_count = 1;
                    NIMMsgCountManager.getInstance().UpsertUnreadCount(m_c_model);
                    mAdapter.setData(pos, entity_model);
                }
                else if(s_click.equals(getString(R.string.session_canncel_top)))
                {
                    if(!entity_model.is_top)
                    {
                        return;
                    }
                    SessionModel seesion_model = new SessionModel();
                    seesion_model.session_id = entity_model.session_id;
                    seesion_model.chat_type = entity_model.chat_type;
                    seesion_model.msg_time = 0;
                    seesion_model.is_top = false;
                    SessionResult s_result = NIMSessionManager.getInstance().UpSertAssistSession(pos, seesion_model);
                    int size = mAdapter.getData().size();
                    if(s_result.remove_index >= 0 && s_result.remove_index < size)
                    {
                        mAdapter.remove(s_result.remove_index);
                    }

                    if(s_result.add_index >= 0)
                    {
                        mAdapter.addData(s_result.add_index, s_result.op_s_model);
                    }
                }
                else if(s_click.equals(getString(R.string.session_top)))
                {
                    if(entity_model.is_top)
                    {
                        return;
                    }
                    SessionModel session_model = new SessionModel();
                    session_model.session_id = entity_model.session_id;
                    session_model.chat_type = entity_model.chat_type;
                    session_model.is_top = true;
                    SessionResult s_result = NIMSessionManager.getInstance().UpSertAssistSession(pos, session_model);
                    int size = mAdapter.getData().size();
                    if(s_result.remove_index >= 0 && s_result.remove_index < size)
                    {
                        mAdapter.remove(s_result.remove_index);
                    }

                    if(s_result.add_index >= 0)
                    {
                        mAdapter.addData(s_result.add_index, s_result.op_s_model);
                    }
                }
                else if(s_click.equals(getString(R.string.clear_this_chat)))
                {
                    NIMGroupMsgManager.getInstance().delGroupMessageInfoByGroupID(entity_model.session_id, false);
                    SessionModel session_model = new SessionModel();
                    session_model.session_id = entity_model.session_id;
                    session_model.chat_type = entity_model.chat_type;
                    session_model.is_top = entity_model.is_top;
                    SessionResult s_result = NIMSessionManager.getInstance().DelAssitSession(pos, session_model.session_id, true);
                    int size = mAdapter.getData().size();
                    if(s_result.remove_index >= 0 && s_result.remove_index < size)
                    {
                        mAdapter.remove(s_result.remove_index);
                    }

                    if(s_result.add_index > 0)
                    {
                        mAdapter.addData(s_result.add_index, s_result.op_s_model);
                    }
                }
            }
        });
    }

    // 界面删除会话，并数据库删除会话，并删除所有消息
    public void deleteSession(long session_id) {
    }

    public void deleteSession(long session_id, boolean delete_session, boolean delete_msg) {
        int position = getSessionPositionById(session_id);
        if (position >= 0) {
            deleteSession(position, session_id, delete_session, delete_msg);
        }
    }

    public void deleteSession(int position, long session_id, boolean delete_session, boolean delete_msg) {
        if (position >= 0) {
            mAdapter.remove(position);
            mAdapter.removeSession(session_id);
            if (delete_session) {
            }
        }
    }

    public int getSessionPositionById(long session_id) {
        int nSize = mAdapter.getData().size();
        for (int i = 0; i < nSize; i++) {
            if (mAdapter.getData().get(i).session_id == session_id) {
                return i;
            }
        }

        return -1;
    }

    // TODO: 2017/9/28 史云杰，确认这个函数的功能以及是否需要 
    private void operateGroup(GcMessageModel operateMode ) {

//        IMGroupInfo group_info = NIMGroupInfoManager.getInstance().getGroupInfo(operateMode.group_id);
//        if (group_info.notify_type != MsgConstDef.GROUP_MESSAGE_STATUS.GROUP_MESSAGE_IN_HELP_NO_HIT) {
//            return;
//        }
//
//        // 自己被群主剔除
//        if (operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_KICK_USER) {
//            if (!operateMode.user_info_list.isEmpty()) {
//                boolean is_kick = operateMode.user_info_list.get(0).user_id == NIMUserInfoManager.getInstance().GetSelfUserId();
//                if (is_kick) {
//                    if (mAdapter.isExist(operateMode.group_id)) {
//                        deleteSession(operateMode.group_id);
//                        NIMGroupInfoManager.getInstance().DeleteGroup(operateMode.group_id);
//                    }
//                    return;
//                }
//            }
//        }
//
//        if (operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_MODIFY_GROUP_NAME) {
//            SessionEntity entity = getSessionById(operateMode.group_id);
//            entity.opt_name = operateMode.group_modify_content;
//            if (entity != null) {
//                updateSession(entity);
//            }
//        }
    }
}
