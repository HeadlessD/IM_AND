package com.qbao.newim.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qbao.newim.adapter.SessionAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.configure.GlobalVariable;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.ErrorDetail;
import com.qbao.newim.constdef.FriendTypeDef;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.constdef.NetConstDef;
import com.qbao.newim.manager.NIMContactManager;
import com.qbao.newim.manager.NIMFriendInfoManager;
import com.qbao.newim.manager.NIMGroupInfoManager;
import com.qbao.newim.manager.NIMGroupMsgManager;
import com.qbao.newim.manager.NIMMsgCountManager;
import com.qbao.newim.manager.NIMMsgManager;
import com.qbao.newim.manager.NIMSessionManager;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.model.MsgCountModel;
import com.qbao.newim.model.NIM_Chat_ID;
import com.qbao.newim.model.SessionModel;
import com.qbao.newim.model.SessionResult;
import com.qbao.newim.model.message.GcMessageModel;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.permission.AndPermission;
import com.qbao.newim.permission.PermissionListener;
import com.qbao.newim.permission.Rationale;
import com.qbao.newim.permission.RationaleListener;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.IDataObserver;
import com.qbao.newim.util.Logger;
import com.qbao.newim.util.NIMStartActivityUtil;
import com.qbao.newim.util.SharedPreferenceUtil;
import com.qbao.newim.util.ShowUtils;
import com.qbao.newim.util.Utils;
import com.qbao.newim.views.PopupMenu;
import com.qbao.newim.views.ProgressDialog;
import com.qbao.newim.views.imgpicker.NIM_ToolbarAct;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenjian on 2017/8/17.
 */

public class NIMSessionActivity extends NIM_ToolbarAct implements IDataObserver {

    private RelativeLayout layout_img_right;
    private TextView tv_right;
    private TextView tv_title;
    private ImageView ivFriend;
    private ImageView ivAddFriend;
    private TextView tv_friend_unread;

    private RecyclerView mRecyclerView;
    private SessionAdapter mAdapter;
    private TextView tvNetWork;

    private static final int REQUEST_CODE_CAMERA_PERMISSION = 93;

    private static final int REQUEST_CODE_FRIEND = 401;
    private static final int REFRESH_SESSION_ADAPTER = 0x10001;
    private static final int ENTER_GROUP_ASSIST = 0x10002;
    private static final int LEAVE_GROUP_ASSIST = 0x10003;
    private static final int DEL_SESSION_ADAPTER = 0x10004;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.nim_actvity_session);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tvNetWork = (TextView) findViewById(R.id.tv_network_error);
    }

    @Override
    protected void setListener() {

        mAdapter = new SessionAdapter(this, null);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                SessionModel entity = mAdapter.getData().get(position);

                switch (entity.chat_type) {
                    case MsgConstDef.MSG_CHAT_TYPE.ASSIST:
                        Intent intent = new Intent(NIMSessionActivity.this, GroupAssistActivity.class);
                        startActivity(intent);
                        break;
                    case MsgConstDef.MSG_CHAT_TYPE.TASK:
                    case MsgConstDef.MSG_CHAT_TYPE.SUBSCRIBE:
                    case MsgConstDef.MSG_CHAT_TYPE.PUBLIC:
                        {
                            NIMStartActivityUtil.startToOfficialContact(NIMSessionActivity.this);
                            break;
                        }
                    case MsgConstDef.MSG_CHAT_TYPE.PRIVATE:
                        Intent intent_sc = new Intent(NIMSessionActivity.this, NIMScChatActivity.class);
                        intent_sc.putExtra("id", entity.session_id);
                        startActivity(intent_sc);
                        break;
                    case MsgConstDef.MSG_CHAT_TYPE.GROUP:
                        Intent intent_gc = new Intent(NIMSessionActivity.this, NIMGcChatActivity.class);
                        intent_gc.putExtra("id", entity.session_id);
                        startActivity(intent_gc);
                        break;
                    case MsgConstDef.MSG_CHAT_TYPE.BUSINESS:
                        Intent intent_bc = new Intent(NIMSessionActivity.this, NIMBcChatActivity.class);
                        intent_bc.putExtra("id", entity.session_id);
                        startActivity(intent_bc);
                        break;
                }

                NIMSessionManager.getInstance().SetCurSession(entity.session_id);
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
            }
        });

        tvNetWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(NetCenter.getInstance().IsReConnecting())
                    return;

                if(!NetCenter.getInstance().IsConnected())
                {
                    NetCenter.getInstance().CacheConnect();
                    return;
                }

                NetCenter.getInstance().DisConnect();
                NetCenter.getInstance().CacheConnect();
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
        // 注册消息监听
        DataObserver.Register(this);
        InitSessionData();
        add_activity = false;
    }

    private void InitSessionData()
    {
        mAdapter.getData().clear();
        mAdapter.setNewData(NIMSessionManager.getInstance().GetGSessionList(true));
    }

    private void showEntityDialog(int position) {
        SessionModel entity = mAdapter.getItem(position);
        ArrayList<String> dialog_list = new ArrayList<>();

        int unread_count = NIMMsgCountManager.getInstance().GetUnreadCount(entity.session_id, entity.chat_type);
        switch (entity.chat_type) {
            case MsgConstDef.MSG_CHAT_TYPE.PRIVATE:
            case MsgConstDef.MSG_CHAT_TYPE.GROUP:
                dialog_list.add(unread_count > 0 ? getString(R.string.msg_readed) : getString(R.string.msg_unread));
                dialog_list.add(entity.is_top ? getString(R.string.session_canncel_top) : getString(R.string.session_top));
                dialog_list.add(getString(R.string.clear_this_chat));
                dialog_list.add(getString(R.string.nim_permission_cancel));
                break;
            case MsgConstDef.MSG_CHAT_TYPE.ASSIST:
                dialog_list.add(getString(R.string.clear_this_chat));
                dialog_list.add(getString(R.string.nim_permission_cancel));
                break;
            default:
                return;
        }

        createEntityDialog(position, entity, dialog_list);
    }

    private void createEntityDialog(final int pos, final SessionModel entity_model, final ArrayList<String> list) {
        ProgressDialog.showCustomDialog(NIMSessionActivity.this, getString(R.string.operater), list, new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position)
            {
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
                    SessionResult s_result = NIMSessionManager.getInstance().UpSertGlobalSession(pos, seesion_model);
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
                    SessionResult s_result = NIMSessionManager.getInstance().UpSertGlobalSession(pos, session_model);
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
                    switch (entity_model.chat_type)
                    {
                        case MsgConstDef.MSG_CHAT_TYPE.PRIVATE:
                        {
                            NIMGroupMsgManager.getInstance().delGroupMessageInfoByGroupID(entity_model.session_id, false);
                        }
                        break;
                        case MsgConstDef.MSG_CHAT_TYPE.GROUP:
                        {
                            NIMMsgManager.getInstance().ClearMessage(entity_model.session_id, false);
                        }
                        break;
                    }


                    SessionModel session_model = new SessionModel();
                    session_model.session_id = entity_model.session_id;
                    session_model.chat_type = entity_model.chat_type;
                    session_model.is_top = entity_model.is_top;
                    SessionResult s_result = NIMSessionManager.getInstance().DelGlobalSession(pos, session_model.session_id, true);
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
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        tvNetWork.setVisibility(NetCenter.getInstance().IsLogined() ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.nim_toolbar_title, menu);
        MenuItem menuItem = menu.findItem(R.id.item_toolbar);
        View actionView = menuItem.getActionView();

        tv_title = (TextView) actionView.findViewById(R.id.title_txt);
        tv_title.setText("消息");

        tv_right = (TextView) actionView.findViewById(R.id.title_right);
        tv_right.setVisibility(View.GONE);
        layout_img_right = (RelativeLayout) actionView.findViewById(R.id.title_right_img_layout);
        layout_img_right.setVisibility(View.VISIBLE);

        ivFriend = (ImageView) actionView.findViewById(R.id.friend_img);
        tv_friend_unread = (TextView) actionView.findViewById(R.id.friend_unread);
        ivAddFriend = (ImageView) actionView.findViewById(R.id.add_chat_friend);
        setFriendUnread(SharedPreferenceUtil.getFriendUnread());

        ivFriend.setOnClickListener(this);
        ivAddFriend.setOnClickListener(this);

        actionView.findViewById(R.id.title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        return true;
    }

    private void setFriendUnread(int unread) {
        if (tv_friend_unread == null) {
            return;
        }
        if (unread == 0) {
            tv_friend_unread.setVisibility(View.GONE);
        } else {
            tv_friend_unread.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode)
            {
                case REQUEST_CODE_FRIEND:
                    setFriendUnread(NIMFriendInfoManager.getInstance().getUnread_count());
                    break;
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        DataObserver.Cancel(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.add_chat_friend) {
            PopupMenu popupMenu = new PopupMenu(NIMSessionActivity.this);
            popupMenu.showLocation(R.id.add_chat_friend);
            popupMenu.setOnItemClickListener(new PopupMenu.OnItemClickListener() {
                @Override
                public void onClick(int pos) {
                    switch (pos) {
                        case 0:
                            NIMStartActivityUtil.startToNIMGroupCreateActivity(NIMSessionActivity.this
                                    , 0, GroupCreateActivity.MODE_CREATE_GROUP);
                            break;
                        case 1:
                            startActivity(new Intent(NIMSessionActivity.this, FriendSearchActivity.class));
                            break;
                        case 2:
                            requestCameraPermission();
                            break;
                        case 3:
                            startActivity(new Intent(NIMSessionActivity.this, NIMSettingActivity.class));
                            break;
                    }
                }
            });
        } else if (v.getId() == R.id.friend_img) {
            Intent intent = new Intent(NIMSessionActivity.this, NIMContactActivity.class);
            startActivityForResult(intent, REQUEST_CODE_FRIEND);
        }
    }

    private void requestCameraPermission() {
        final String tip = getString(R.string.nim_permission_camera_fail);
        AndPermission.with(this)
                .requestCode(REQUEST_CODE_CAMERA_PERMISSION)
                .permission(Manifest.permission.CAMERA)
                .failTips(tip)
                .callback(permissionListener)
                // rationale作用是：用户拒绝一次权限，再次申请时先征求用户同意，再打开授权对话框，避免用户勾选不再提示。
                .rationale(new RationaleListener() {
                    @Override
                    public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
                        AndPermission.rationaleDialog(NIMSessionActivity.this, tip, rationale).show();
                    }
                }).start();
    }

    private PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
            switch (requestCode) {
                case REQUEST_CODE_CAMERA_PERMISSION:
                    CaptureActivity.startScanQR(NIMSessionActivity.this);
                    break;
            }
        }

        @Override
        public void onCancel(int requestCode, Context context) {

        }
    };

    @Override
    public void OnChange(int param1, Object param2, Object param3) {
        switch (param1) {
            case DataConstDef.EVENT_UPDATE_ALL_SESSION:
                mAdapter.notifyDataSetChanged();
                break;
            case DataConstDef.EVENT_MESSAGE_TIME_OUT:         // 超时
                {
                    // TODO: 2017/9/27 史云杰 补充公众号超时处理
                    // 超时，更新对应会话列表[注意需要保证先更新消息状态]
                    Logger.error(TAG, "EVENT_MESSAGE_TIME_OUT");
                    NIM_Chat_ID out_chat = (NIM_Chat_ID) param2;
                    if(out_chat == null)
                        break;


                    SessionResult s_result;
                    SessionModel session_model = new SessionModel();
                    session_model.session_id = out_chat.session_id;
                    if(out_chat.chat_type == MsgConstDef.MSG_CHAT_TYPE.PRIVATE)
                    {
                        session_model.chat_type = MsgConstDef.MSG_CHAT_TYPE.PRIVATE;
                        s_result = NIMSessionManager.getInstance().UpSertGlobalSession(-1, session_model);

                        Message msg = new Message();
                        msg.what = REFRESH_SESSION_ADAPTER;
                        msg.obj = s_result;
                        mHandler.sendMessage(msg);
                    }
                    else if(out_chat.chat_type == MsgConstDef.MSG_CHAT_TYPE.GROUP)
                    {
                        IMGroupInfo group_info = NIMGroupInfoManager.getInstance().getGroupInfo(out_chat.session_id);

                        if (null == group_info)
                        {
                            break;
                        }

                        if(group_info.notify_type != MsgConstDef.GROUP_MESSAGE_STATUS.GROUP_MESSAGE_IN_HELP_NO_HIT)
                        {
                            session_model.chat_type = MsgConstDef.MSG_CHAT_TYPE.GROUP;
                        }
                        else
                        {
                            session_model.session_id = GlobalVariable.GROUP_ASSIST_SESSION_ID;
                            session_model.chat_type = MsgConstDef.MSG_CHAT_TYPE.ASSIST;
                        }
                        s_result = NIMSessionManager.getInstance().UpSertGlobalSession(-1, session_model);

                        Message msg = new Message();
                        msg.what = REFRESH_SESSION_ADAPTER;
                        msg.obj = s_result;
                        mHandler.sendMessage(msg);
                    }
                    else if(out_chat.chat_type == MsgConstDef.MSG_CHAT_TYPE.PUBLIC)
                    {

                    }

                }
                break;
            case DataConstDef.EVENT_SC_CHAT_SESSION:
                {
                    //私聊消息更新,更新对应的会话框
                    Logger.error(TAG, "EVENT_SC_CHAT_SESSION");
                    SessionModel session_model = new SessionModel();
                    session_model.session_id = (long)param2;
                    session_model.chat_type = MsgConstDef.MSG_CHAT_TYPE.PRIVATE;
                    SessionResult s_result = NIMSessionManager.getInstance().UpSertGlobalSession(-1, session_model);
                    Message msg = new Message();
                    msg.what = REFRESH_SESSION_ADAPTER;
                    msg.obj = s_result;
                    mHandler.sendMessage(msg);
                }
                break;
            case DataConstDef.EVENT_UNREAD_CLEAR:
                {
                    long group_id = (long)param2;
                    int chat_type = (int)param3;
                    if(chat_type == MsgConstDef.MSG_CHAT_TYPE.GROUP)
                    {
                        IMGroupInfo group_info = NIMGroupInfoManager.getInstance().getGroupInfo(group_id);
                        if(null == group_info)
                        {
                            break;
                        }

                        if (group_info.notify_type == MsgConstDef.GROUP_MESSAGE_STATUS.GROUP_MESSAGE_IN_HELP_NO_HIT)
                        {
                            break;
                        }
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
                    SessionResult s_result = NIMSessionManager.getInstance().UpSertGlobalSession(-1, session_model);
                    Message msg = new Message();
                    msg.what = REFRESH_SESSION_ADAPTER;
                    msg.obj = s_result;
                    mHandler.sendMessage(msg);
                }
                break;
            case DataConstDef.EVENT_GET_GROUP_MSG_SESSION:
                {
                    long group_id = (long) param2;
                    //如果不是群助手不管
                    IMGroupInfo group_info = NIMGroupInfoManager.getInstance().getGroupInfo(group_id);
                    SessionModel session_model = new SessionModel();
                    if(null == group_info)
                    {
                        break;
                    }

                    if (group_info.notify_type == MsgConstDef.GROUP_MESSAGE_STATUS.GROUP_MESSAGE_IN_HELP_NO_HIT)
                    {
                        session_model.session_id = GlobalVariable.GROUP_ASSIST_SESSION_ID;
                        session_model.chat_type = MsgConstDef.MSG_CHAT_TYPE.ASSIST;
                    }
                    else
                    {
                        session_model.session_id = group_id;
                        session_model.chat_type = MsgConstDef.MSG_CHAT_TYPE.GROUP;
                    }
                    
                    SessionResult s_result = NIMSessionManager.getInstance().UpSertGlobalSession(-1, session_model);
                    Message msg = new Message();
                    msg.what = REFRESH_SESSION_ADAPTER;
                    msg.obj = s_result;
                    mHandler.sendMessage(msg);
                }
                break;
            case DataConstDef.EVENT_FRIEND_ADD_REQUEST:          // 新的好友请求
                if (!(boolean) param3) {
                    return;
                }

                int  req_count = NIMFriendInfoManager.getInstance().getUnread_count();
                req_count += NIMContactManager.getInstance().getCount();

                setFriendUnread(req_count);

                break;
            case DataConstDef.EVENT_FRIEND_CONFIRM:        // 好友确认
                {
                    IMFriendInfo friend_info = NIMFriendInfoManager.getInstance().getFriendReqInfo((long) param2);
                    if (null == friend_info) {
                        break;
                    }
                    String send_user_name = Utils.getUserShowName(new String[]{friend_info.nickName, friend_info.user_name});
                    String msg_content;
                    if (friend_info.status == FriendTypeDef.FRIEND_ADD_TYPE.PEER_CONFIRM) {
                        msg_content = "我通过了你的好友验证请求，现在我们可以开始聊天了";
                        NIMMsgManager.getInstance().GenFriendTipsMessage(friend_info.userId, msg_content);
                    } else if (friend_info.status == FriendTypeDef.FRIEND_ADD_TYPE.OWN_CONFIRM) {
                        msg_content = friend_info.opt_msg;
                        NIMMsgManager.getInstance().GenFriendTipsMessage(friend_info.userId, msg_content);
                        msg_content = "以上是打招呼内容";
                        NIMMsgManager.getInstance().GenTipsMessage(friend_info.userId, msg_content);
                        msg_content = "你已添加了" + send_user_name + "为好友, 现在可以开始聊天了";
                        NIMMsgManager.getInstance().GenTipsMessage(friend_info.userId, msg_content);
                    } else if (friend_info.status == FriendTypeDef.FRIEND_ADD_TYPE.RESTART_ADD) {
                        msg_content = send_user_name + "已通过你的好友验证，现在可以开始聊天了";
                        NIMMsgManager.getInstance().GenTipsMessage(friend_info.userId, msg_content);
                        return;
                    }

                    SessionModel session_model = new SessionModel();
                    session_model.session_id = friend_info.userId;
                    session_model.chat_type = MsgConstDef.MSG_CHAT_TYPE.PRIVATE;
                    SessionResult s_result = NIMSessionManager.getInstance().UpSertGlobalSession(-1, session_model);
                    Message msg = new Message();
                    msg.what = REFRESH_SESSION_ADAPTER;
                    msg.obj = s_result;
                    mHandler.sendMessage(msg);
                }
                break;
            case DataConstDef.EVENT_FRIEND_DEL:            // 好友删除
                {
                    if (param2 == null)
                    {
                        return;
                    }
                    long user_id = (long) param2;
                    SessionResult s_result = NIMSessionManager.getInstance().DelGlobalSession(-1, user_id, true);
                    Message msg = new Message();
                    msg.what = DEL_SESSION_ADAPTER;
                    msg.arg1 = s_result.remove_index;
                    mHandler.sendMessage(msg);
                }
                break;
            case DataConstDef.EVENT_GROUP_DELETE:          // 群删除
                {
                    long group_id = (long) param2;
                    NIMGroupInfoManager.getInstance().DeleteGroup(group_id);
                    SessionResult s_result = NIMSessionManager.getInstance().DelGlobalSession(-1, group_id, true);
                    Message msg = new Message();
                    msg.what = DEL_SESSION_ADAPTER;
                    msg.arg1 = s_result.remove_index;
                    mHandler.sendMessage(msg);
                }
                break;
            case DataConstDef.EVENT_GROUP_OPERATE:         // 群操作
                {
                    GcMessageModel operateMode = (GcMessageModel) param2;
                    boolean client = (boolean) param3;
                    if (operateMode == null) {
                        showToastStr("用户操作无效");
                        return;
                    }

                    if (client) {
                        return;
                    }
                    if (NIMSessionManager.getInstance().GetCurSession() != operateMode.group_id) {
                        operateGroup(operateMode);
                    }
                }
                break;
            case DataConstDef.EVENT_ENTER_ASSIST:          //加入群助手
                {
                    mHandler.sendMessage(mHandler.obtainMessage(ENTER_GROUP_ASSIST, param2));
                }
                break;
            case DataConstDef.EVENT_LEAVE_ASSIST:
                {
                    mHandler.sendMessage(mHandler.obtainMessage(LEAVE_GROUP_ASSIST, param2));
                }
                break;
            case DataConstDef.EVENT_SESSION_TOP:           // 设置里置顶
                {
                    long session_id = (long) param2;
                    boolean is_top = (boolean) param3;

                    // TODO: 2017/9/27  史云杰这里需要区分是群，公众号，私聊
                    SessionModel session_model = new SessionModel();
                    session_model.session_id = session_id;
                    session_model.is_top = is_top;
                    IMGroupInfo group_info = NIMGroupInfoManager.getInstance().getGroupInfo(session_id);
                    if (null != group_info)
                    {
                        session_model.chat_type = MsgConstDef.MSG_CHAT_TYPE.GROUP;
                    }
                    else
                    {
                        session_model.chat_type = MsgConstDef.MSG_CHAT_TYPE.PRIVATE;
                    }

                    SessionResult s_result = NIMSessionManager.getInstance().UpSertGlobalSession(-1, session_model);
                    Message msg = new Message();
                    msg.what = REFRESH_SESSION_ADAPTER;
                    msg.obj = s_result;
                    mHandler.sendMessage(msg);
                }
                break;
            case DataConstDef.EVENT_CONTACT_FRIEND:
                int  count = NIMFriendInfoManager.getInstance().getUnread_count();
                count += NIMContactManager.getInstance().getCount();
                setFriendUnread(count);
                break;
            case DataConstDef.EVENT_FRIEND_EDIT:   // 修改备注名
                {
                    long remark_id = (long) param3;
                    SessionModel session_model = NIMSessionManager.getInstance().GetGSessionByID(remark_id);
                    if (session_model == null) {
                        return;
                    }
                    session_model.session_id = remark_id;
                    session_model.chat_type = MsgConstDef.MSG_CHAT_TYPE.PRIVATE;
                    SessionResult s_result = NIMSessionManager.getInstance().UpSertGlobalSession(-1, session_model);
                    Message msg = new Message();
                    msg.what = REFRESH_SESSION_ADAPTER;
                    msg.obj = s_result;
                    mHandler.sendMessage(msg);
                }
                break;
            case DataConstDef.EVENT_LOGIN_STATUS:
            {
                Message msg = new Message();
                msg.what = DataConstDef.EVENT_LOGIN_STATUS;
                msg.obj = param2;
                if(null != param3)
                    msg.arg1 = (int)param3;
                mHandler.sendMessage(msg);
            }
            break;
        }
    }

    private void operateGroup(GcMessageModel operateMode)
    {
        // 收到建群操作或者拉入新群
        IMGroupInfo group_info = operateMode.group_info;
        if(null == group_info)
        {
            return ;
        }

        if(operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_CREATE)
        {
            group_info.isMember = 1;
            NIMGroupInfoManager.getInstance().AddGroup(group_info);
            return;
        }

        IMGroupInfo new_group = NIMGroupInfoManager.getInstance().getGroupInfo(operateMode.group_id);
        if(null == new_group)
        {
            Logger.error(TAG, "group_info not find group_id = " + String.valueOf(operateMode.group_id));
            return ;
        }


        new_group.isMember = 1;
        if (operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_ADD_USER)
        {
            new_group.group_count = group_info.group_count;
            NIMGroupInfoManager.getInstance().AddGroup(new_group);
            return ;
        }

        if (operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_MODIFY_GROUP_NAME) {
            SessionModel session_model = new SessionModel();
            session_model.session_id = operateMode.group_id;
            session_model.chat_type = MsgConstDef.MSG_CHAT_TYPE.GROUP;
            SessionResult s_result = NIMSessionManager.getInstance().UpSertGlobalSession(-1, session_model);
            Message msg = new Message();
            msg.what = REFRESH_SESSION_ADAPTER;
            msg.obj = s_result;
            mHandler.sendMessage(msg);
        }
    }

    protected Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_SESSION_ADAPTER:
                    {
                        SessionResult s_result = (SessionResult) msg.obj;
                        int size = mAdapter.getData().size();
                        if (s_result.remove_index == s_result.add_index && s_result.remove_index >= 0) {
                            mAdapter.setData(s_result.remove_index, s_result.op_s_model);
                        } else {
                            if (s_result.remove_index >= 0 && s_result.remove_index < size) {
                                mAdapter.remove(s_result.remove_index);
                            }

                            if (s_result.add_index >= 0) {
                                mAdapter.addData(s_result.add_index, s_result.op_s_model);
                            }
                        }
                    }
                    break;
                case DEL_SESSION_ADAPTER:
                    {
                        SessionResult s_result = (SessionResult) msg.obj;
                        int size = mAdapter.getData().size();
                        if (msg.arg1 >= 0 && msg.arg1 < size)
                        {
                            mAdapter.remove(msg.arg1);
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
                        //删除全局群会话
                        if(s_result.remove_index >= 0 && s_result.remove_index < size)
                        {
                            mAdapter.remove(s_result.remove_index);
                        }
                        size = mAdapter.getData().size();
                        //更新群助手
                        if(assist_result.remove_index >= 0 && assist_result.remove_index < size)
                        {
                            mAdapter.remove(assist_result.remove_index);
                        }

                        if(assist_result.add_index >= 0)
                        {
                            mAdapter.addData(assist_result.add_index, assist_result.op_s_model);
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
                        //添加全局群会话
                        if(s_result.add_index >= 0)
                        {
                            mAdapter.addData(s_result.add_index, s_result.op_s_model);
                        }
                        int size = mAdapter.getData().size();
                        //更新群助手
                        if(assist_result.remove_index >= 0 && assist_result.remove_index < size)
                        {
                            mAdapter.remove(assist_result.remove_index);
                        }
                        if(assist_result.add_index >= 0)
                        {
                            mAdapter.addData(assist_result.add_index, assist_result.op_s_model);
                        }
                    }
                    break;
                case DataConstDef.EVENT_LOGIN_STATUS:
                    // TODO: 2017/10/12 史云杰 完善登录显示状态[未连接，未登录，加载中，加载完成等]
                    NetConstDef.E_NET_STATUS e_status = (NetConstDef.E_NET_STATUS)msg.obj;
                    switch (e_status)
                    {
                        case CLOSED:
                        {
                            ShowUtils.showToast(getString(R.string.nim_connect_closed));
                            tvNetWork.setVisibility(View.VISIBLE);
                        }
                        break;
                        case CONNECTED:
                        {
                            tvNetWork.setVisibility(View.GONE);
                        }
                        break;
                        case LOGINED:
                        {
                            tvNetWork.setVisibility(View.GONE);
                        }
                        break;
                        case BEKICKED:
                        {
                            tvNetWork.setVisibility(View.VISIBLE);
                            ShowUtils.showToast(getString(R.string.nim_be_kicked, ErrorDetail.GetErrorDetail(msg.arg1)));
                        }
                        break;
                        case ERROR:
                        {
                            ShowUtils.showToast(getString(R.string.nim_connect_error));
                        }
                        break;
                        case CONNECT_FAIL:
                        {
                            ShowUtils.showToast(getString(R.string.nim_connect_failed));
                        }
                        case UPDATE_FINISHED:
                        {
                            
                        }
                        break;
                    }
                    break;
            }
            return false;
        }
    });
}
