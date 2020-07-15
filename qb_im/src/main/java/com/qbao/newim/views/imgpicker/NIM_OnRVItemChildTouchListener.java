package com.qbao.newim.views.imgpicker;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by chenjian on 2017/5/2.
 */

public interface NIM_OnRVItemChildTouchListener {
    boolean onRvItemChildTouch(NIM_RecyclerViewHolder holder, View childView, MotionEvent event);
}
