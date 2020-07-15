package com.qbao.newim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.manager.NIMFriendInfoManager;
import com.qbao.newim.manager.NIMMsgManager;
import com.qbao.newim.manager.NIMSessionManager;
import com.qbao.newim.manager.ScChatSendManager;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.model.NIM_Chat_ID;
import com.qbao.newim.model.message.BaseMessageModel;
import com.qbao.newim.model.message.ScMessageModel;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.Logger;
import com.qbao.newim.util.Utils;

import java.util.ArrayList;


public class NIMScChatActivity extends NIMChatActivity{

    private static final String TAG = NIMScChatActivity.class.getSimpleName();
    private long opt_user_id;
    private IMFriendInfo friendInfo;

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        opt_user_id = getIntent().getLongExtra("id", 0);
        super.processLogic(savedInstanceState);
        // 设置当前聊天会话ID，区别存储未读消息
        NIMSessionManager.getInstance().SetCurSession(opt_user_id);
        friendInfo = NIMFriendInfoManager.getInstance().getFriendUser(opt_user_id);
    }

    @Override
    public int getChatType() {
        return MsgConstDef.MSG_CHAT_TYPE.PRIVATE;
    }

    @Override
    public ArrayList<BaseMessageModel> getData()
    {
        return NIMMsgManager.getInstance().LoadMoreMessage(opt_user_id);
    }

    @Override
    public ArrayList<BaseMessageModel> initData()
    {
        return NIMMsgManager.getInstance().GetMessageList(opt_user_id);
    }

    @Override
    public void sendMessage(BaseMessageModel msg) {
        ScMessageModel sc_msg = (ScMessageModel) msg;
        sc_msg.chat_type = MsgConstDef.MSG_CHAT_TYPE.PRIVATE;
        sc_msg.message_id = NetCenter.getInstance().CreateMsgID();
        sc_msg.opt_user_id = opt_user_id;
        ScChatSendManager.getInstance().send(sc_msg);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        ivSetting.setImageResource(R.mipmap.nim_toolbar_chat_setting);

        String name = Utils.getUserShowName(new String[] {friendInfo.remark_name, friendInfo.nickName, friendInfo.user_name});
        tvName.setText(name);

        ivSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NIMScChatActivity.this, ChatSettingActivity.class);
                intent.putExtra("id", opt_user_id);
                intent.putExtra("group", false);
                startActivityForResult(intent, REQUEST_CODE_CHAT_SETTING);
            }
        });
        return true;
    }

    @Override
    public void OnChange(int param1, Object param2, Object param3) {
        super.OnChange(param1, param2, param3);
        switch (param1) {
            case DataConstDef.EVENT_SC_CHAT_MESSAGE:
                NIM_Chat_ID chat_id = (NIM_Chat_ID)param2;
                if (chat_id.session_id != opt_user_id) {
                    return;
                }
                handScChatMsg(chat_id, (int)param3);
                break;
            case DataConstDef.EVENT_FRIEND_EDIT:   // 修改备注名
                String remark = (String) param2;
                long remark_id = (long) param3;
                if (remark_id == opt_user_id) {
                    tvName.setText(remark);
                }
                break;
            case DataConstDef.EVENT_FiLE_PROGRESS:            // 上传进度条监听
                ScMessageModel sc_msg_progress = (ScMessageModel) param2;
                if (sc_msg_progress.opt_user_id != opt_user_id) {
                    return;
                }
                handlerUploadProgress(sc_msg_progress);
                break;
            case DataConstDef.EVENT_UPLOAD_STATUS:            // 上传状态监听
                ScMessageModel sc_msg_status = (ScMessageModel) param2;
                if (sc_msg_status.opt_user_id != opt_user_id) {
                    return;
                }
                handlerUploadStatus(sc_msg_status, (boolean)param3);
                break;
        }
    }

    private void handScChatMsg(NIM_Chat_ID success_msg, int op_type)
    {
        switch (op_type)
        {
            case MsgConstDef.MSG_OP_TYPE.ADD:
            {
                ScMessageModel msg_model = NIMMsgManager.getInstance().GetMessage(success_msg);
                if (msg_model == null)
                {
                    Logger.error(TAG, "msg_model is null, message_id = " + success_msg.message_id + " index = " + success_msg.index +
                            " chat_type = " + success_msg.chat_type + " op_type = " + op_type);
                    return;
                }
                addNewMsgToView(msg_model);
                break;
            }
            case MsgConstDef.MSG_OP_TYPE.DELETE:
            {
                deleteMsgFromView(success_msg.index);
                break;
            }
            case MsgConstDef.MSG_OP_TYPE.UPDATE:
            {
                ScMessageModel msg_model = NIMMsgManager.getInstance().GetMessage(success_msg);
                if (msg_model == null)
                {
                    Logger.error(TAG, "msg_model is null, message_id = " + success_msg.message_id + " index = " + success_msg.index +
                            " chat_type = " + success_msg.chat_type + " op_type = " + op_type);
                    return;
                }
                updateMsgToView(msg_model, success_msg.index);
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ScChatSendManager.getInstance().close();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        DataObserver.Notify(DataConstDef.EVENT_UNREAD_CLEAR, opt_user_id, MsgConstDef.MSG_CHAT_TYPE.PRIVATE);
    }
}
