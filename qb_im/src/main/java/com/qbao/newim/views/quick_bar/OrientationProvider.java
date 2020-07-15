package com.qbao.newim.views.quick_bar;

import android.support.v7.widget.RecyclerView;

/**
 * Created by chenjian on 2017/8/31.
 */

public interface OrientationProvider {

    int getOrientation(RecyclerView recyclerView);

    boolean isReverseLayout(RecyclerView recyclerView);
}
