package com.qbao.newim.views.quick_bar;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by chenjian on 2017/8/31.
 */

public interface HeaderProvider {
    public View getHeader(RecyclerView recyclerView, int position);
    void invalidate();
}