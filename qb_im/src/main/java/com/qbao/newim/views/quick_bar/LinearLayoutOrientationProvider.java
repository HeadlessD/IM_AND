package com.qbao.newim.views.quick_bar;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by chenjian on 2017/8/31.
 */

public class LinearLayoutOrientationProvider implements OrientationProvider {

    @Override
    public int getOrientation(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        throwIfNotLinearLayoutManager(layoutManager);
        return ((LinearLayoutManager) layoutManager).getOrientation();
    }

    @Override
    public boolean isReverseLayout(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        throwIfNotLinearLayoutManager(layoutManager);
        return ((LinearLayoutManager) layoutManager).getReverseLayout();
    }

    private void throwIfNotLinearLayoutManager(RecyclerView.LayoutManager layoutManager){
        if (!(layoutManager instanceof LinearLayoutManager)) {
            throw new IllegalStateException("StickyListHeadersDecoration can only be used with a " +
                    "LinearLayoutManager.");
        }
    }
}
