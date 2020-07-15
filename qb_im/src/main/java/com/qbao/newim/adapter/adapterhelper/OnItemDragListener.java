package com.qbao.newim.adapter.adapterhelper;

import android.support.v7.widget.RecyclerView;

/**
 * Created by chenjian on 2017/3/22.
 */

public interface OnItemDragListener {
    void onItemDragStart(RecyclerView.ViewHolder viewHolder, int pos);
    void onItemDragMoving(RecyclerView.ViewHolder source, int from, RecyclerView.ViewHolder target, int to);
    void onItemDragEnd(RecyclerView.ViewHolder viewHolder, int pos);
}
