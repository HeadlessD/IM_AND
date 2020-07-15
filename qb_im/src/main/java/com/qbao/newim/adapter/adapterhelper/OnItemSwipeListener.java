package com.qbao.newim.adapter.adapterhelper;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;

/**
 * Created by chenjian on 2017/3/22.
 */

public interface OnItemSwipeListener {
    void onItemSwipeStart(RecyclerView.ViewHolder viewHolder, int pos);
    void clearView(RecyclerView.ViewHolder viewHolder, int pos);
    void onItemSwiped(RecyclerView.ViewHolder viewHolder, int pos);
    void onItemSwipeMoving(Canvas canvas, RecyclerView.ViewHolder viewHolder, float dX, float dY, boolean isCurrentlyActive);
}
