package com.qbao.newim.views.quick_bar;

/**
 * Created by chenjian on 2017/8/31.
 */

public interface OnQuickSideBarTouchListener {
    void onLetterChanged(String letter,int position,float y);
    void onLetterTouching(boolean touching);
}
