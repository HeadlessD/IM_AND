package com.qbao.newim.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.ErrorDetail;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.manager.NIMGroupInfoManager;
import com.qbao.newim.manager.NIMGroupUserManager;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.GroupOperateMode;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.model.IMGroupUserInfo;
import com.qbao.newim.model.IMRemarkDetail;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.processor.GlobalProcessor;
import com.qbao.newim.processor.GroupGetProcessor;
import com.qbao.newim.processor.GroupOperateProcessor;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.BaseUtil;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.DateUtil;
import com.qbao.newim.util.IDataObserver;
import com.qbao.newim.util.KeyboardUtil;
import com.qbao.newim.util.Logger;
import com.qbao.newim.views.ProgressDialog;
import com.qbao.newim.views.imgpicker.NIM_ToolbarAct;

/**
 * Created by chenjian on 2017/7/3.
 */

public class GroupRemarkActivity extends NIM_ToolbarAct implements IDataObserver{

    private boolean is_manager;
    private long group_id;
    TextView tvRight;
    private EditText editText;
    private String origin_txt = "";
    private IMRemarkDetail remark_detail;
    private ImageView iv_opt_user;
    private TextView tv_opt_user;
    private TextView tv_opt_time;
    private RelativeLayout origin_layout;
    private LinearLayout tips_layout;
    private TextView tv_content;
    private View layout_txt;
    private static final int REMARK_COUNT = 500;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.nim_activity_group_remark);

        if (getIntent() != null) {
            is_manager = getIntent().getBooleanExtra("manager", false);
            group_id = getIntent().getLongExtra("group_id", 0);
            origin_txt = getIntent().getStringExtra("remark");
        }

        editText = (EditText) findViewById(R.id.group_remark_edit);

        iv_opt_user = (ImageView) findViewById(R.id.remark_head);
        tv_opt_user = (TextView) findViewById(R.id.remark_name);
        tv_opt_time = (TextView) findViewById(R.id.remark_time);
        origin_layout = (RelativeLayout) findViewById(R.id.origin_layout);
        tips_layout = (LinearLayout) findViewById(R.id.group_remark_tips);
        tv_content = (TextView) findViewById(R.id.group_remark_txt);
        layout_txt = findViewById(R.id.group_remark_layout);
    }

    @Override
    protected void setListener() {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                if (input.equals(origin_txt)) {
                    tvRight.setEnabled(false);
                } else {
                    tvRight.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        DataObserver.Register(this);
        IMGroupInfo groupInfo = NIMGroupInfoManager.getInstance().getGroupInfo(group_id);
        if (TextUtils.isEmpty(groupInfo.group_remark)) {
            origin_layout.setVisibility(View.GONE);
            editText.setVisibility(View.VISIBLE);
            layout_txt.setVisibility(View.GONE);
            KeyboardUtil.showKeyboard(editText);
        } else {
            editText.setVisibility(View.GONE);
            layout_txt.setVisibility(View.VISIBLE);
            GroupGetProcessor processor = GlobalProcessor.getInstance().getGroupGetProcessor();
            processor.SendRemarkDetailRQ(group_id);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataObserver.Cancel(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.nim_toolbar_title, menu);
        MenuItem menuItem = menu.findItem(R.id.item_toolbar);
        View actionView = menuItem.getActionView();

        tvRight = (TextView) actionView.findViewById(R.id.title_right);
        tvRight.setVisibility(View.VISIBLE);
        if (is_manager) {
            tvRight.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(origin_txt)) {
                tvRight.setText("完成");
                tvRight.setEnabled(false);
            } else {
                tvRight.setText("编辑");
                tvRight.setEnabled(true);
            }
            tips_layout.setVisibility(View.GONE);
        } else {
            tvRight.setVisibility(View.GONE);
            tips_layout.setVisibility(View.VISIBLE);
        }

        TextView tvTitle = (TextView) actionView.findViewById(R.id.title_txt);
        tvTitle.setText("群公告");
        actionView.findViewById(R.id.title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getVisibility() == View.VISIBLE) {
                    KeyboardUtil.hideKeyboard(v);
                    showRemarkTips();
                } else {
                    tvRight.setText("完成");
                    editText.setVisibility(View.VISIBLE);
                    layout_txt.setVisibility(View.GONE);
                    editText.setText(origin_txt);
                    editText.setSelection(origin_txt.length());
                    editText.requestFocus();
                    KeyboardUtil.showKeyboard(v);
                }
            }
        });
        return true;
    }

    private void updateView() {
        origin_txt = remark_detail.op_remark;
        Glide.with(this).load(AppUtil.getHeadUrl(remark_detail.op_user_id)).placeholder(R.mipmap.nim_head).into(iv_opt_user);
        IMGroupUserInfo opt_user = NIMGroupUserManager.getInstance().getGroupUserInfo(group_id, remark_detail.op_user_id);
        if (opt_user != null) {
            tv_opt_user.setText(opt_user.user_nick_name);
        }
        tv_opt_time.setText(DateUtil.formatLongToString(remark_detail.op_ct * 1000));

        tv_content.setText(origin_txt);
    }
    private void showRemarkTips() {
        String msg;
        if (editText.getText().toString().length() >= REMARK_COUNT) {
            showToastStr("群公告最大字数限制为500");
            return;
        }
        if (TextUtils.isEmpty(editText.getText().toString())) {
            msg = "确定清空群公告?";
        } else {
            msg = "该公告会通知全部群成员，是否发布?";
        }

        ProgressDialog.showCustomDialog(this, msg, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createRemarkOpt(editText.getText().toString());
            }
        });
    }

    private void createRemarkOpt(String content) {
        GroupOperateMode mode1 = new GroupOperateMode();
        mode1.group_id = group_id;
        mode1.big_msg_type = MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_MODIFY_GROUP_REMARK;

        mode1.group_modify_content = content;
        mode1.msg_time = BaseUtil.GetServerTime();
        mode1.message_id = NetCenter.getInstance().CreateGroupMsgId();
        mode1.operate_user_name = NIMUserInfoManager.getInstance().GetSelfUserName();
        mode1.user_id = NIMUserInfoManager.getInstance().GetSelfUserId();

        GroupOperateProcessor operateProcessor = GlobalProcessor.getInstance().getGroupOperateProcessor();
        operateProcessor.sendGroupModifyRQ(mode1);
        onBackPressed();
    }

    @Override
    public void OnChange(int param1, Object param2, Object param3) {
        if (param1 == DataConstDef.EVENT_REMARK_DETAIL) {
            remark_detail = (IMRemarkDetail) param2;
            updateView();
        } else if (param1 == DataConstDef.EVENT_NET_ERROR) {
            Logger.error(TAG, "EVENT_NET_ERROR");
            if (is_active) {
                int error_code = BaseUtil.MakeErrorResult((int) param2);
                String error_msg = ErrorDetail.GetErrorDetail(error_code);
                showToastStr(error_msg);
            }
        }
    }
}
