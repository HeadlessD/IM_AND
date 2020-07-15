package com.qbao.newim.views.imgpicker;

import android.view.View;

/**
 * Created by chenjian on 2017/5/2.
 */

public abstract class NIM_OnNoDoubleClickListener implements View.OnClickListener {
    private int mThrottleFirstTime = 1000;
    private long mLastClickTime = 0;

    public NIM_OnNoDoubleClickListener() {
    }

    public NIM_OnNoDoubleClickListener(int throttleFirstTime) {
        mThrottleFirstTime = throttleFirstTime;
    }

    @Override
    public void onClick(View v) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastClickTime > mThrottleFirstTime) {
            mLastClickTime = currentTime;
            onNoDoubleClick(v);
        }
    }

    public abstract void onNoDoubleClick(View v);
}
