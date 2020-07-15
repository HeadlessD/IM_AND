package com.qbao.newim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.ErrorDetail;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.manager.NIMGroupInfoManager;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.GroupOperateMode;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.util.NIMStartActivityUtil;
import com.qbao.newim.processor.GlobalProcessor;
import com.qbao.newim.processor.GroupOperateProcessor;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.BaseUtil;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.IDataObserver;
import com.qbao.newim.util.Logger;
import com.qbao.newim.views.SwitchButton;
import com.qbao.newim.views.imgpicker.NIM_ToolbarAct;

import static com.qbao.newim.constdef.MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_ENTER_DEFAULT;

/**
 * Created by chenjian on 2017/7/3.
 */

public class GroupManagerActivity extends NIM_ToolbarAct implements IDataObserver{

    private long group_id;
    private IMGroupInfo groupInfo;
    private SwitchButton iv_switch;
    private RelativeLayout layout;
    private int nType;
    private static final int GROUP_PERMISSION_CODE = 100;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.nim_activity_group_manager);
        if (getIntent() != null) {
            group_id = getIntent().getLongExtra("group_id", 0);
            groupInfo = NIMGroupInfoManager.getInstance().getGroupInfo(group_id);
            nType = groupInfo.group_add_is_agree;
        }

        iv_switch = (SwitchButton) findViewById(R.id.group_manager_switch);
        layout = (RelativeLayout) findViewById(R.id.group_manager_move);
    }

    @Override
    protected void setListener() {
        iv_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    createOperate(GROUP_OFFLINE_CHAT_ENTER_DEFAULT);
                } else {
                    createOperate(MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_ENTER_AGREE);
                }
            }
        });
        layout.setOnClickListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        DataObserver.Register(this);
        updateView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.nim_toolbar_title, menu);
        MenuItem menuItem = menu.findItem(R.id.item_toolbar);
        View actionView = menuItem.getActionView();

        TextView tvTitle = (TextView) actionView.findViewById(R.id.title_txt);
        tvTitle.setText("群管理");

        actionView.findViewById(R.id.title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataObserver.Cancel(this);
    }

    private void updateView() {
        // 邀请需要同意
        if (nType == MsgConstDef.GROUP_ADD_TYPE.NEED_AGREE) {
            iv_switch.setChecked(true);
        } else {
            iv_switch.setChecked(false);
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        NIMStartActivityUtil.startToNGMAForResult(GroupManagerActivity.this,
                group_id, GroupMemberActivity.GROUP_PERMISSION, GROUP_PERMISSION_CODE);
    }

    private void createOperate(int type) {
        GroupOperateMode model = new GroupOperateMode();
        model.group_id = group_id;
        model.user_id = NIMUserInfoManager.getInstance().GetSelfUserId();
        model.message_id = NetCenter.getInstance().CreateGroupMsgId();
        model.big_msg_type = type;
        model.msg_time = BaseUtil.GetServerTime();
        model.operate_user_name = NIMUserInfoManager.getInstance().GetSelfUserName();

        GroupOperateProcessor processor = GlobalProcessor.getInstance().getGroupOperateProcessor();
        processor.sendGroupModifyRQ(model);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GROUP_PERMISSION_CODE) {
                finish();
            }
        }
    }

    @Override
    public void OnChange(int param1, Object param2, Object param3) {
        if (param1 == DataConstDef.EVENT_GROUP_OPERATE) {
            GroupOperateMode operateMode = (GroupOperateMode)param2;
            if (operateMode == null) {
                updateView();
                return;
            }
            if (operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_ENTER_AGREE
                    || operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_ENTER_DEFAULT) {
                nType = operateMode.group_add_is_agree;
                groupInfo.group_add_is_agree = nType;
                NIMGroupInfoManager.getInstance().AddGroup(groupInfo);
            }
        } else if (param1 == DataConstDef.EVENT_NET_ERROR){
            Logger.error(TAG, "EVENT_NET_ERROR");
            if (is_active) {
                int error_code = BaseUtil.MakeErrorResult((int) param2);
                String error_msg = ErrorDetail.GetErrorDetail(error_code);
                showToastStr(error_msg);
            }
        }
    }
}
