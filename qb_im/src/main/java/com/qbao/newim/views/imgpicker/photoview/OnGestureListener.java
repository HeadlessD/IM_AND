package com.qbao.newim.views.imgpicker.photoview;

/**
 * Created by chenjian on 2017/5/2.
 */

public interface OnGestureListener {

    void onDrag(float dx, float dy);

    void onFling(float startX, float startY, float velocityX,
                 float velocityY);

    void onScale(float scaleFactor, float focusX, float focusY);

}
