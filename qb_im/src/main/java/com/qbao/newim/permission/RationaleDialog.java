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
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.qbao.newim.qbim.R;
import com.qbao.newim.views.dialog.Effectstype;
import com.qbao.newim.views.dialog.NiftyDialogBuilder;

/**
 * <p>Default Rationale Dialog.</p>
 */
public class RationaleDialog {

    private NiftyDialogBuilder dialogBuilder;
    private Rationale mRationale;

    RationaleDialog(@NonNull final Context context, String tips, @NonNull Rationale rationale) {
        dialogBuilder = new NiftyDialogBuilder(context);
        dialogBuilder
                .setCustomView(null)
                .withTitle(context.getString(R.string.nim_permission_title_permission_failed))
                .withTitleColor("#333333")
                .withDividerColor(ContextCompat.getColor(context, R.color.colorAccent))
                .withDialogColor("#FFFFFFFF")
                .withMessage(tips)
                .withMessageColor("#333333")
                .setCustomListView(context, null, null)
                .withButton1Text(context.getString(R.string.nim_permission_cancel))
                .withButton2Text(context.getString(R.string.nim_permission_setting))
                .setButton1Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogBuilder.dismiss();
                    }
                })
                .setButton2Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogBuilder.dismiss();
                        mRationale.resume();
                    }
                })
                .isCancelableOnTouchOutside(false)
                .withDuration(400)
                .withEffect(Effectstype.SlideBottom);
        dialogBuilder.setCancelable(false);
        this.mRationale = rationale;
    }

    @NonNull
    public RationaleDialog setTitle(@NonNull String title) {
        dialogBuilder.setTitle(title);
        return this;
    }

    @NonNull
    public RationaleDialog setTitle(@StringRes int title) {
        dialogBuilder.setTitle(title);
        return this;
    }

    @NonNull
    public RationaleDialog setMessage(@NonNull String message) {
        dialogBuilder.withMessage(message);
        return this;
    }

    @NonNull RationaleDialog setFirstClickListener(final View.OnClickListener listener) {
        dialogBuilder.setButton1Click(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(v);
                }
            }
        });

        return this;
    }


    public void show() {
        dialogBuilder.show();
    }

}
