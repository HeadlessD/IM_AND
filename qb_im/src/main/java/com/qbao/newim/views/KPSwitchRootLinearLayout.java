package com.qbao.newim.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.qbao.newim.helper.KPSwitchRootLayoutHandler;

/**
 * Created by chenjian on 2017/4/6.
 */

public class KPSwitchRootLinearLayout extends LinearLayout {
    private KPSwitchRootLayoutHandler conflictHandler;

    public KPSwitchRootLinearLayout(Context context) {
        super(context);
        init();
    }

    public KPSwitchRootLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public KPSwitchRootLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public KPSwitchRootLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        conflictHandler = new KPSwitchRootLayoutHandler(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        conflictHandler.handleBeforeMeasure(MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec));
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
