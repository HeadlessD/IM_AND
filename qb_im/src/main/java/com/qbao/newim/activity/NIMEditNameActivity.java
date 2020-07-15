package com.qbao.newim.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.qbao.newim.configure.GlobalVariable;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.GroupOperateMode;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.processor.FriendDelProcessor;
import com.qbao.newim.processor.GlobalProcessor;
import com.qbao.newim.processor.GroupOperateProcessor;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.BaseUtil;
import com.qbao.newim.views.MultiEditText;
import com.qbao.newim.views.imgpicker.NIM_ToolbarAct;

/**
 * Created by chenjian on 2017/6/30.
 */

public class NIMEditNameActivity extends NIM_ToolbarAct {

    public static final int GROUP_EDIT_NAME = 1;
    public static final int GROUP_USER_EDIT_NAME = 2;
    public static final int FRIEND_EDIT_NAME = 3;
    private int nType;
    private MultiEditText editText;
    private String origin_content;
    private long setting_id;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.nim_activity_edit_name);
        if (getIntent() != null) {
            nType = getIntent().getIntExtra("type", 0);
            origin_content = getIntent().getStringExtra("content");
            setting_id = getIntent().getLongExtra("setting_id", 0);
        }

        editText = (MultiEditText) findViewById(R.id.edit_name);
        editText.setText(origin_content);
        editText.setDrawableType(MultiEditText.DEFALUT);
        editText.setIs_show_img(false);
        editText.setSelection(editText.getText().toString().length());
    }

    @Override
    protected void setListener() {
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEND:
                        submitReset();
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nim_toolbar_title, menu);
        MenuItem menuItem = menu.findItem(R.id.item_toolbar);
        View actionView = menuItem.getActionView();

        TextView tvRight = (TextView) actionView.findViewById(R.id.title_right);
        tvRight.setVisibility(View.VISIBLE);
        tvRight.setText("修改");

        TextView tvTitle = (TextView) actionView.findViewById(R.id.title_txt);

        if (nType == 0) {
            nType = getIntent().getIntExtra("type", 0);
        }
        String title = "";
        switch (nType) {
            case GROUP_EDIT_NAME:
                title = "修改群名";
                break;
            case GROUP_USER_EDIT_NAME:
                title = "修改群昵称";
                break;
            case FRIEND_EDIT_NAME:
                title = "修改好友备注";
                break;
        }
        tvTitle.setText(title);
        actionView.findViewById(R.id.title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitReset();
            }
        });

        return true;
    }

    private void submitReset() {
        showPreDialog("");
        String reset_content = editText.getText().toString().trim();
        if (reset_content.equals(origin_content)) {
            hidePreDialog();
            onBackPressed();
            return;
        }

        switch (nType) {
            case GROUP_EDIT_NAME:
            case GROUP_USER_EDIT_NAME:
                if (TextUtils.isEmpty(reset_content)) {
                    hidePreDialog();
                    showToastStr("修改内容不能为空");
                    return;
                }
                createGroupOperate(reset_content);
                break;
            case FRIEND_EDIT_NAME:
                editFriendName(reset_content);
                break;
        }
    }

    private void createGroupOperate(String name) {
        GroupOperateMode mode1 = new GroupOperateMode();
        mode1.group_id = setting_id;

        if (nType == GROUP_EDIT_NAME) {
            if (name.length() > GlobalVariable.REMARK_GROUP_LENGTH) {
                hidePreDialog();
                showToastStr("群组名字不能超过" + GlobalVariable.REMARK_GROUP_LENGTH + "个字符");
                return;
            }
            mode1.big_msg_type = MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_MODIFY_GROUP_NAME;
        } else if (nType == GROUP_USER_EDIT_NAME) {
            if (name.length() > GlobalVariable.REMARK_GROUP_NICK_LENGTH) {
                hidePreDialog();
                showToastStr("群昵称不能超过" + GlobalVariable.REMARK_GROUP_NICK_LENGTH + "个字符");
                return;
            }
            mode1.big_msg_type = MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_MODIFY_GROUP_USER_NAME;
        }

        mode1.group_modify_content = name;
        mode1.msg_time = BaseUtil.GetServerTime();
        mode1.message_id = NetCenter.getInstance().CreateGroupMsgId();
        mode1.operate_user_name = NIMUserInfoManager.getInstance().GetSelfUserName();
        mode1.user_id = NIMUserInfoManager.getInstance().GetSelfUserId();

        GroupOperateProcessor operateProcessor = GlobalProcessor.getInstance().getGroupOperateProcessor();
        operateProcessor.sendGroupModifyRQ(mode1);
        onBackPressed();
    }

    private void editFriendName(String content) {
        if (content.length() > GlobalVariable.REMARK_MAX_LENGTH) {
            hidePreDialog();
            showToastStr("好友备注名不能超过" + GlobalVariable.REMARK_MAX_LENGTH + "个字符");
            return;
        }
        FriendDelProcessor processor = GlobalProcessor.getInstance().getFriendDelProcessor();
        processor.sendFriendEditRQ(setting_id, content);
        onBackPressed();
    }
}
