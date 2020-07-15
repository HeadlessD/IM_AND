package com.qbao.newim.views.imgpicker;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by chenjian on 2017/5/2.
 */

public class NIM_SpaceItemDecoration extends RecyclerView.ItemDecoration {
    private int mSpace;

    public NIM_SpaceItemDecoration(int space) {
        mSpace = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = mSpace;
        outRect.right = mSpace;
        outRect.top = mSpace;
        outRect.bottom = mSpace;
    }
}