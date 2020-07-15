package com.qbao.newim.views.imgpicker.photoview;

import android.view.MotionEvent;

/**
 * Created by chenjian on 2017/5/2.
 */

public interface GestureDetector {
    boolean onTouchEvent(MotionEvent ev);

    boolean isScaling();

    boolean isDragging();

    void setOnGestureListener(OnGestureListener listener);
}
