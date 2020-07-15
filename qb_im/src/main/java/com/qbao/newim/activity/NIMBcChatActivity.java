package com.qbao.newim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.manager.NIMFriendInfoManager;
import com.qbao.newim.manager.NIMMsgManager;
import com.qbao.newim.manager.NIMSessionManager;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.manager.ScChatSendManager;
import com.qbao.newim.model.IMBusinessInfo;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.model.message.BaseMessageModel;
import com.qbao.newim.model.message.ScMessageModel;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.processor.GlobalProcessor;
import com.qbao.newim.processor.UserChatProcessor;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.Utils;

import java.util.ArrayList;

/**
 * Created by chenjian on 2017/9/26.
 */

public class NIMBcChatActivity extends NIMChatActivity {

    private long seller_id;
    private IMBusinessInfo businessInfo;
    private IMFriendInfo friendInfo;

    @Override
    protected void initView(Bundle savedInstanceState) {
        seller_id = getIntent().getLongExtra("id", 0);
        super.initView(savedInstanceState);
        // 设置当前聊天会话ID，区别存储未读消息
        NIMSessionManager.getInstance().SetCurSession(seller_id);
        getBusinessInfo();
        friendInfo = NIMFriendInfoManager.getInstance().getFriendUser(seller_id);
    }

    private void getBusinessInfo() {
        long[] wid_arr = new long[5];
        for (int i = 0; i < 5; i++) {
            wid_arr[i] = 10001l + i;
        }

        businessInfo = new IMBusinessInfo();
        businessInfo.name = "大保健";

        UserChatProcessor processor = GlobalProcessor.getInstance().GetScProcessor();
        processor.getBusinessInfo(seller_id, wid_arr);
    }

    @Override
    public int getChatType() {
        return MsgConstDef.MSG_CHAT_TYPE.BUSINESS;
    }

    
    //// TODO: 2017/10/9 史云杰 完善逻辑，这个界面应该分私聊或者公众号，需要讨论
    @Override
    public ArrayList<BaseMessageModel> getData() {
        ArrayList<BaseMessageModel> list = new ArrayList<>();
        list.addAll(NIMMsgManager.getInstance().LoadMoreMessage(seller_id));
        return list;
    }

    @Override
    public ArrayList<BaseMessageModel> initData()
    {
        return null;
    }

    @Override
    public void sendMessage(BaseMessageModel msg) {
        if (businessInfo.wid == 0) {
            showToastStr("商家消息获取失败，无法发送");
            return;
        }
        ScMessageModel sc_msg = (ScMessageModel) msg;
        msg.chat_type = MsgConstDef.MSG_CHAT_TYPE.PRIVATE;
        msg.w_id = businessInfo.wid;
        msg.c_id = NIMUserInfoManager.getInstance().GetSelfUserId();
        msg.b_id = businessInfo.bid;

        sc_msg.message_id = NetCenter.getInstance().CreateMsgID();
        sc_msg.opt_user_id = businessInfo.wid;
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
                Intent intent = new Intent(NIMBcChatActivity.this, ChatSettingActivity.class);
                intent.putExtra("id", seller_id);
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
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ScChatSendManager.getInstance().close();
    }
}
