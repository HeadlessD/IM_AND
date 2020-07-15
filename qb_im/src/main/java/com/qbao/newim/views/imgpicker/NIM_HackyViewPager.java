package com.qbao.newim.views.imgpicker;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by chenjian on 2017/5/2.
 */

public class NIM_HackyViewPager extends ViewPager {

    public NIM_HackyViewPager(Context context) {
        super(context);
    }

    public NIM_HackyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }
}