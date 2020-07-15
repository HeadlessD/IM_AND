package com.qbao.newim.views.quick_bar;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Created by chenjian on 2017/8/31.
 */

public interface StickyRecyclerHeadersAdapter<VH extends RecyclerView.ViewHolder> {
    long getHeaderId(int position);
    VH onCreateHeaderViewHolder(ViewGroup parent);
    void onBindHeaderViewHolder(VH holder, int position);
    int getItemCount();
}