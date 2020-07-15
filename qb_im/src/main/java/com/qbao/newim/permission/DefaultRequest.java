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

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;

import com.qbao.newim.permission.target.Target;
import com.qbao.newim.util.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>Request permission and callback.</p>
 */
class DefaultRequest implements
        Request,
        Rationale,
        PermissionActivity.PermissionCancel,
        PermissionActivity.RationaleListener,
        PermissionActivity.PermissionListener {

    private Target target;

    private int mRequestCode;
    private String[] mPermissions;
    private PermissionListener mCallback;
    private String tips;
    private RationaleListener mRationaleListener;

    private String[] mDeniedPermissions;

    DefaultRequest(Target target) {
        if (target == null)
            throw new IllegalArgumentException("The target can not be null.");
        this.target = target;
    }

    @NonNull
    @Override
    public Request permission(String... permissions) {
        this.mPermissions = permissions;
        return this;
    }

    @NonNull
    @Override
    public Request permission(String[]... permissionsArray) {
        List<String> permissionList = new ArrayList<>();
        for (String[] permissions : permissionsArray) {
            for (String permission : permissions) {
                permissionList.add(permission);
            }
        }
        this.mPermissions = permissionList.toArray(new String[permissionList.size()]);
        return this;
    }

    @NonNull
    @Override
    public Request requestCode(int requestCode) {
        this.mRequestCode = requestCode;
        return this;
    }

    @Override
    public Request callback(PermissionListener callback) {
        this.mCallback = callback;
        return this;
    }

    @Override
    public Request failTips(String txt) {
        this.tips = txt;
        return this;
    }

    @NonNull
    @Override
    public Request rationale(RationaleListener listener) {
        this.mRationaleListener = listener;
        return this;
    }

    @Override
    public void start() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            callbackSucceed();
        } else {
            mDeniedPermissions = getDeniedPermissions(target.getContext(), mPermissions);
            // Denied mPermissions size > 0.
            if (mDeniedPermissions.length > 0) {
                // Remind users of the purpose of mPermissions.
                PermissionActivity.setRationaleListener(this);
                PermissionActivity.setPermissionCancel(this);
                Intent intent = new Intent(target.getContext(), PermissionActivity.class);
                intent.putExtra(PermissionActivity.KEY_INPUT_PERMISSIONS, mDeniedPermissions);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                target.startActivity(intent);
            } else { // All permission granted.
                callbackSucceed();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static String[] getDeniedPermissions(Context context, @NonNull String... permissions) {
        List<String> deniedList = new ArrayList<>(1);
        for (String permission : permissions)
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED)
                deniedList.add(permission);
        return deniedList.toArray(new String[deniedList.size()]);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRationaleResult(boolean showRationale) {
        Logger.error("permissions", "onRationaleResult");
        if (showRationale && mRationaleListener != null) {
            mRationaleListener.showRequestPermissionRationale(mRequestCode, this);
        } else {
            resume();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void resume() {
        Logger.error("permissions", "resume");
        PermissionActivity.setPermissionListener(this);
        PermissionActivity.setPermissionCancel(this);
        Intent intent = new Intent(target.getContext(), PermissionActivity.class);
        intent.putExtra(PermissionActivity.KEY_INPUT_PERMISSIONS, mDeniedPermissions);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        target.startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(boolean succeed) {
        Logger.error("permissions", "onRequestPermissionsResult");
        if (succeed)
            callbackSucceed();
        else
            callbackFailed();
    }

    private void callbackSucceed() {
        if (mCallback != null) {
            mCallback.onSucceed(mRequestCode, Arrays.asList(mPermissions));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void callbackFailed() {
        if (mCallback != null) {
            PermissionActivity.setPermissionListener(this);
            PermissionActivity.setPermissionCancel(this);
            Intent intent = new Intent(target.getContext(), PermissionActivity.class);
            intent.putExtra(PermissionActivity.KEY_INPUT_PERMISSIONS, mDeniedPermissions);
            intent.putExtra(PermissionActivity.KEY_INPUT_TIP, tips);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            target.startActivity(intent);
        }
    }

    @Override
    public void onPermissionCancel() {
        if (mCallback != null) {
            mCallback.onCancel(mRequestCode, target.getContext());
        }
    }
}