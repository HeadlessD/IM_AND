/*
 * Copyright © Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qbao.newim.permission;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;

import com.qbao.newim.util.Logger;
import com.qbao.newim.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Request permission.
 * </p>
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class PermissionActivity extends Activity {

    static final String KEY_INPUT_PERMISSIONS = "KEY_INPUT_PERMISSIONS";
    static final String KEY_INPUT_TIP = "KEY_INPUT_TIP";

    private static RationaleListener mRationaleListener;
    private static PermissionListener mPermissionListener;
    private static PermissionCancel permissionCancel;
    private String tips;
    private String[] permissions;
    private static final int REQUEST_CODE_SETTING = 300;

    public static void setRationaleListener(RationaleListener rationaleListener) {
        PermissionActivity.mRationaleListener = rationaleListener;
    }

    public static void setPermissionListener(PermissionListener permissionListener) {
        if (mPermissionListener == null)
            mPermissionListener = permissionListener;
    }

    public static void setPermissionCancel(PermissionCancel permissionCancel) {
        PermissionActivity.permissionCancel = permissionCancel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.error("permissions", "onCreate");
        Intent intent = getIntent();
        permissions = intent.getStringArrayExtra(KEY_INPUT_PERMISSIONS);
        tips = intent.getStringExtra(KEY_INPUT_TIP);

        if (permissions == null) {
            mRationaleListener = null;
            mPermissionListener = null;
            finish();
            return;
        }

        if (mRationaleListener != null) {
            boolean rationale = false;
            for (String permission : permissions) {
                rationale = shouldShowRequestPermissionRationale(permission);
                if (rationale) break;
            }
            mRationaleListener.onRationaleResult(rationale);
            mRationaleListener = null;
            finish();
            return;
        }

        if (mPermissionListener != null && TextUtils.isEmpty(tips))
            requestPermissions(permissions, 1);

        if (!TextUtils.isEmpty(tips) && mRationaleListener == null) {
            AndPermission.defaultSettingDialog(this, tips, REQUEST_CODE_SETTING)
                    .setFirstClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (permissionCancel != null) {
                                permissionCancel.onPermissionCancel();
                                permissionCancel = null;
                            }
                            mPermissionListener = null;
                            tips = null;
                            finish();
                        }
                    })
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Logger.error("permissions", "onRequestPermissionsResult");
        if (mPermissionListener != null) {
            boolean succeed = checkPermission(permissions, grantResults);
            mPermissionListener.onRequestPermissionsResult(succeed);
            if (succeed) {
                mPermissionListener = null;
                permissionCancel = null;
            }
        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.error("permissions", "" + "onActivityResult");
        switch (requestCode) {
            case REQUEST_CODE_SETTING:
                int[] results = new int[permissions.length];
                for (int i = 0; i < permissions.length; i++)
                    results[i] = ContextCompat.checkSelfPermission(this, permissions[i]);

                if (mPermissionListener != null) {
                    boolean first_check = checkPermission(permissions, results);
                    Logger.error("permissions", "" + first_check);
                    boolean next_check = AndPermission.hasPermission(PermissionActivity.this, permissions);
                    Logger.error("permissions", "" + next_check);
                    if (first_check) {
                        if (next_check) {
                            mPermissionListener.onRequestPermissionsResult(next_check);
                            mPermissionListener = null;
                            permissionCancel = null;
                            finish();
                        } else {
                            Utils.startActivityToSetting(this, REQUEST_CODE_SETTING);
                        }
                    } else {
                        mPermissionListener.onRequestPermissionsResult(first_check);
                        finish();
                    }
                } else {
                    finish();
                }

                break;
        }
    }

    public boolean checkPermission(@NonNull String[] permissions, @NonNull int[] grantResults) {
        List<String> deniedList = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++)
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                deniedList.add(permissions[i]);

        if (deniedList.isEmpty())
            return true;
        else
            return false;
    }

    interface RationaleListener {
        void onRationaleResult(boolean showRationale);
    }

    interface PermissionListener {
        void onRequestPermissionsResult(boolean succeed);
    }

    interface PermissionCancel {
        void onPermissionCancel();
    }
}
