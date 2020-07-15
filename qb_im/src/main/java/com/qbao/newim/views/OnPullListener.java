package com.qbao.newim.views;

import android.view.View;

/**
 * Created by Cmad on 2015/5/12.
 */
public interface OnPullListener {
    void onPulling(View view);
    void onCanRefreshing(View view);
    void onRefreshing(View view);
}
