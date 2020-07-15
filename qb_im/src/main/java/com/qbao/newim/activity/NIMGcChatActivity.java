package com.qbao.newim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;

import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.manager.GcChatSendManager;
import com.qbao.newim.manager.NIMGroupInfoManager;
import com.qbao.newim.manager.NIMGroupMsgManager;
import com.qbao.newim.manager.NIMSessionManager;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.model.NIM_Chat_ID;
import com.qbao.newim.model.message.BaseMessageModel;
import com.qbao.newim.model.message.GcMessageModel;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.views.ProgressDialog;
import com.qbao.newim.views.dialog.NiftyDialogBuilder;

import java.util.ArrayList;

/**
 * Created by chenjian on 2017/9/26.
 */

public class NIMGcChatActivity extends NIMChatActivity {
    private static final String TAG = NIMGcChatActivity.class.getSimpleName();
    private long group_id;
    private IMGroupInfo groupInfo;
    private NiftyDialogBuilder kick_dialog;
    public boolean is_kicked;

    @Override
    public int getChatType() {
        return MsgConstDef.MSG_CHAT_TYPE.GROUP;
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        group_id = getIntent().getLongExtra("id", 0);
        super.processLogic(savedInstanceState);
        // 设置当前聊天会话ID，区别存储未读消息
        NIMSessionManager.getInstance().SetCurSession(group_id);
        groupInfo = NIMGroupInfoManager.getInstance().getGroupInfo(group_id);
        setShowNickname(groupInfo.is_show_nick);
    }

    @Override
    public ArrayList<BaseMessageModel> getData() {
        ArrayList<BaseMessageModel> list = new ArrayList<>();
        list.addAll(NIMGroupMsgManager.getInstance().loadGroupMsgFromDb(group_id));
        return list;
    }

    @Override
    public ArrayList<BaseMessageModel> initData()
    {
        return NIMGroupMsgManager.getInstance().getListGroupMsgByGroupID(group_id);
    }

    @Override
    public void sendMessage(BaseMessageModel msg) {
        GcMessageModel gc_msg = (GcMessageModel)msg;
        gc_msg.chat_type = MsgConstDef.MSG_CHAT_TYPE.GROUP;
        gc_msg.message_id = NetCenter.getInstance().CreateGroupMsgId();
        gc_msg.group_id = group_id;
        gc_msg.group_info = groupInfo;
        gc_msg.send_user_name = NIMUserInfoManager.getInstance().getSelfName();
        gc_msg.user_id = NIMUserInfoManager.getInstance().GetSelfUserId();
        GcChatSendManager.getInstance().send(gc_msg);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        ivSetting.setImageResource(R.mipmap.nim_groupchat_setting);
        if (groupInfo.isMember == 0) {
            is_kicked = true;
            ivSetting.setVisibility(View.GONE);
        }

        tvName.setText(groupInfo.group_name);

        ivSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NIMGcChatActivity.this, ChatSettingActivity.class);
                intent.putExtra("id", group_id);
                intent.putExtra("group", true);
                startActivityForResult(intent, REQUEST_CODE_CHAT_SETTING);
            }
        });

        return true;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (is_kicked) {
            showKickDialog();
            return;
        }
    }

    @Override
    public void OnChange(int param1, Object param2, Object param3) {
        super.OnChange(param1, param2, param3);
        switch (param1) {
            case DataConstDef.EVENT_GET_GROUP_ONE_MSG:
                NIM_Chat_ID chat_id = (NIM_Chat_ID)param2;
                if (chat_id.session_id != group_id)
                {
                    return;
                }

                handGcChatMsg(chat_id, (int)param3);
                break;
            case DataConstDef.EVENT_GROUP_DELETE:              // 收到被群主踢出，直接关闭
                boolean finish = (boolean) param3;
                if (finish) {
                    finish();
                } else {
                    is_kicked = true;
                    showKickDialog();
                }
                break;
            case DataConstDef.EVENT_SESSION_NAME:             // 更改了群名
                String remark_name = (String)param2;
                if (!TextUtils.isEmpty(remark_name)) {
                    tvName.setText(remark_name);
                }
                break;
            case DataConstDef.EVENT_FiLE_PROGRESS:            // 上传进度条监听
                GcMessageModel gc_msg_progress = (GcMessageModel) param2;
                if (gc_msg_progress.group_id != group_id) {
                    return;
                }
                handlerUploadProgress(gc_msg_progress);
                break;
            case DataConstDef.EVENT_UPLOAD_STATUS:            // 上传状态监听
                GcMessageModel gc_msg_status = (GcMessageModel) param2;
                if (gc_msg_status.group_id != group_id) {
                    return;
                }
                handlerUploadStatus(gc_msg_status, (boolean)param3);
                break;
        }
    }

    private void handGcChatMsg(NIM_Chat_ID chat_id, int msg_type)
    {
        switch (msg_type)
        {
            case MsgConstDef.MSG_GROUP_OP_TYPE.ADD:
            {
                GcMessageModel msg_model = NIMGroupMsgManager.getInstance().GetMessageByChatID(chat_id);
                if (msg_model == null)
                {
                    return ;
                }
                addNewMsgToView(msg_model, chat_id.index);
                break;
            }
            case MsgConstDef.MSG_GROUP_OP_TYPE.UPDATE:
            {
                GcMessageModel msg_model = NIMGroupMsgManager.getInstance().GetMessageByChatID(chat_id);
                if (msg_model == null)
                {
                    return ;
                }
                updateMsgToView(msg_model, chat_id.index);
                break;
            }
            case MsgConstDef.MSG_GROUP_OP_TYPE.DELETE:
                deleteMsgFromView(chat_id.index);
                break;
        }
    }

    private void showKickDialog() {
        kick_dialog = ProgressDialog.showSingleDialog(this, "你已经被群主踢出该群", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (kick_dialog != null && kick_dialog.isShowing()) {
                onBackPressed();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        GcChatSendManager.getInstance().close();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        DataObserver.Notify(DataConstDef.EVENT_UNREAD_CLEAR, group_id, MsgConstDef.MSG_CHAT_TYPE.GROUP);
    }
}
