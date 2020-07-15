package com.qbao.newim.views.imgpicker;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;

/**
 * Created by chenjian on 2017/5/2.
 */

public class NIM_RVOnScrollListener extends RecyclerView.OnScrollListener {
    private Activity mActivity;

    public NIM_RVOnScrollListener(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            NIM_Image.resume(mActivity);
        } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
            NIM_Image.pause(mActivity);
        }
    }
}
