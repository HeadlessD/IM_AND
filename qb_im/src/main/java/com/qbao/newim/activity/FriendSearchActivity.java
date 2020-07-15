package com.qbao.newim.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.ErrorDetail;
import com.qbao.newim.constdef.FriendTypeDef;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.IMUserInfo;
import com.qbao.newim.permission.AndPermission;
import com.qbao.newim.permission.PermissionListener;
import com.qbao.newim.permission.Rationale;
import com.qbao.newim.permission.RationaleListener;
import com.qbao.newim.processor.GlobalProcessor;
import com.qbao.newim.processor.UserInfoGetProcessor;
import com.qbao.newim.qbdb.manager.UserInfoDbManager;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.BaseUtil;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.IDataObserver;
import com.qbao.newim.util.Logger;
import com.qbao.newim.util.ShowUtils;
import com.qbao.newim.views.MultiEditText;
import com.qbao.newim.views.ProgressDialog;
import com.qbao.newim.views.imgpicker.NIM_ToolbarAct;

import java.util.List;

/**
 * Created by chenjian on 2017/4/21.
 */

public class FriendSearchActivity extends NIM_ToolbarAct implements IDataObserver{

    private String key = "";
    private MultiEditText keyword;
    private TextView submit;
    private Button addFriendWithContacts;
    private Button addCongenial;
    private Button addPublisher;
    private Button scanQRCode;
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 97;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.search_friend_activity);
        keyword = (MultiEditText) findViewById(R.id.keyword);
        submit = (TextView) findViewById(R.id.submit);
        submit.setOnClickListener(this);
        addFriendWithContacts = (Button) findViewById(R.id.addFriendWithContacts);
        addPublisher = (Button) findViewById(R.id.addPublisher);
        addCongenial = (Button) findViewById(R.id.addCongenial);
        scanQRCode = (Button) findViewById(R.id.scanQRCode);

        addFriendWithContacts.setOnClickListener(this);
        addPublisher.setOnClickListener(this);
        addCongenial.setOnClickListener(this);
        scanQRCode.setOnClickListener(this);
    }

    @Override
    protected void setListener() {
        keyword.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH:
                        submitSearch();
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
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.nim_toolbar_title, menu);
        MenuItem menuItem = menu.findItem(R.id.item_toolbar);
        View actionView = menuItem.getActionView();

        TextView tvTitle = (TextView) actionView.findViewById(R.id.title_txt);
        tvTitle.setText("添加好友");

        actionView.findViewById(R.id.title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        return true;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() ==  R.id.addFriendWithContacts) {
            Intent openContacts = new Intent(this, PhoneContactsActivity.class);
            startActivity(openContacts);
        } else if (v.getId() == R.id.addCongenial) {
            Intent openContacts = new Intent(this, CongenialActivity.class);
            startActivity(openContacts);
        } else if (v.getId() == R.id.addPublisher) {
            showToastStr("进入公众号搜索");
        } else if (v.getId() == R.id.scanQRCode) {
            requestCameraPermission();
        } else if (v.getId() == R.id.submit) {
            submitSearch();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataObserver.Register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DataObserver.Cancel(this);
    }

    private void submitSearch(){
        ShowUtils.hideSoftInput(this);
        if(!checkInput()){
            showToastStr(R.string.nim_please_enter_the_input);
            return;
        }

        IMUserInfo self_info = UserInfoDbManager.getInstance().
                getSingleIMUser(NIMUserInfoManager.getInstance().GetSelfUserId());
        if (self_info != null) {
            String self_id = String.valueOf(self_info.userId);
            String user_name = self_info.user_name;
            long phone = self_info.mobile;
            String mail = self_info.mail;
            if (key.equals(self_id) || key.equals(user_name)
                    || key.equals(String.valueOf(phone)) || key.equals(mail)) {
                ProgressDialog.showCustomDialog(this, "无法通过搜索将自己添加到通讯录");
                return;
            }
        }

        showPreDialog("");
        UserInfoGetProcessor processor = GlobalProcessor.getInstance().getUser_processor();
        processor.SendUserInfoRQ(key);

    }

    private boolean checkInput(){
        key = keyword.getText().toString().trim();
        return !TextUtils.isEmpty(key);
    }

    @Override
    public void OnChange(int param1, Object param2, Object param3) {
        switch (param1) {
            case DataConstDef.EVENT_GET_USER_INFO:
                hidePreDialog();
                if ((boolean)param3) {
                    IMUserInfo info = (IMUserInfo)param2;
                    if (info == null) {
                        return;
                    }

                    if (info.userId == NIMUserInfoManager.getInstance().GetSelfUserId()) {
                        return;
                    }

                    Intent intent = new Intent(FriendSearchActivity.this, NIMUserInfoActivity.class);
                    String json_user = new Gson().toJson(info);
                    intent.putExtra("json_user", json_user);
                    intent.putExtra("source_type", FriendTypeDef.FRIEND_SOURCE_TYPE.SEARCH);
                    startActivity(intent);
                } else {
                    String msg = (String)param2;
                    showToastStr("用户" + msg + "不存在");
                }

                break;
            case DataConstDef.EVENT_NET_ERROR:
                Logger.error(TAG, "EVENT_NET_ERROR");
                if (is_active) {
                    int error_code = BaseUtil.MakeErrorResult((int) param2);
                    String error_msg = ErrorDetail.GetErrorDetail(error_code);
                    showToastStr(error_msg);
                }
                break;
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
                        AndPermission.rationaleDialog(FriendSearchActivity.this, tip, rationale).show();
                    }
                }).start();
    }

    private PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
            switch (requestCode) {
                case REQUEST_CODE_CAMERA_PERMISSION:
                    CaptureActivity.startScanQR(FriendSearchActivity.this);
                    break;
            }
        }

        @Override
        public void onCancel(int requestCode, Context context) {

        }
    };
}
