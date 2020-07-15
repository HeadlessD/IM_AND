package com.qbao.newim.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.qbao.newim.configure.GlobalVariable;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.processor.FriendAddProcessor;
import com.qbao.newim.processor.GlobalProcessor;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.Utils;
import com.qbao.newim.views.imgpicker.NIM_ToolbarAct;

/**
 * Created by chenjian on 2017/6/2.
 */

public class FriendVerifyActivity extends NIM_ToolbarAct {

    private byte source_type;      // 好友来源
    private long user_id;          // 好友ID
    private String str_verify_msg; // 发送验证消息
    private EditText addDesc;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.nim_toolbar_title, menu);
        MenuItem menuItem = menu.findItem(R.id.item_toolbar);
        View actionView = menuItem.getActionView();

        TextView tvRight = (TextView) actionView.findViewById(R.id.title_right);
        tvRight.setVisibility(View.VISIBLE);
        tvRight.setText("发送");

        TextView tvTitle = (TextView) actionView.findViewById(R.id.title_txt);
        tvTitle.setText("验证消息");
        actionView.findViewById(R.id.title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerifyMsg();
            }
        });
        return true;
    }

    private void sendVerifyMsg() {
        str_verify_msg = addDesc.getText().toString().trim();
        if (str_verify_msg.length() > GlobalVariable.VERIFY_MAX_LENGTH) {
            showToastStr("验证消息最长26个字符");
        }
        IMFriendInfo info = new IMFriendInfo();
        info.opt_msg = str_verify_msg;
        info.userId = user_id;
        info.source_type = source_type;
        info.nickName = NIMUserInfoManager.getInstance().GetSelfUserName();

        FriendAddProcessor processor = GlobalProcessor.getInstance().getFriendAddProcessor();
        processor.sendFriendAddRQ(info);
        setResult(RESULT_OK);
        onBackPressed();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.nim_request_friend);
        if (getIntent() != null) {
            source_type = getIntent().getByteExtra("source_type", (byte) -1);
            user_id = getIntent().getLongExtra("user_id", 0);
        }

        addDesc = (EditText) findViewById(R.id.addDesc);
        Utils.setEditCursor(addDesc, R.drawable.nim_cursor_green);
        addDesc.setText("我是" + NIMUserInfoManager.getInstance().GetSelfUserName());
        addDesc.setSelection(addDesc.length());
    }

    @Override
    protected void setListener() {
        addDesc.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEND:
                        sendVerifyMsg();
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {

    }
}
