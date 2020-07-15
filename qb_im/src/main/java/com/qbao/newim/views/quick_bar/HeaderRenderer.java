package com.qbao.newim.views.quick_bar;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by chenjian on 2017/8/31.
 */

public class HeaderRenderer {
    private final DimensionCalculator mDimensionCalculator;
    private final OrientationProvider mOrientationProvider;
    private final Rect mTempRect = new Rect();

    public HeaderRenderer(OrientationProvider orientationProvider) {
        this(orientationProvider, new DimensionCalculator());
    }

    private HeaderRenderer(OrientationProvider orientationProvider,
                           DimensionCalculator dimensionCalculator) {
        mOrientationProvider = orientationProvider;
        mDimensionCalculator = dimensionCalculator;
    }

    public void drawHeader(RecyclerView recyclerView, Canvas canvas, View header, Rect offset) {
        canvas.save();

        if (recyclerView.getLayoutManager().getClipToPadding()) {
            // Clip drawing of headers to the padding of the RecyclerView. Avoids drawing in the padding
            initClipRectForHeader(mTempRect, recyclerView, header);
            canvas.clipRect(mTempRect);
        }

        canvas.translate(offset.left, offset.top);

        header.draw(canvas);
        canvas.restore();
    }

    private void initClipRectForHeader(Rect clipRect, RecyclerView recyclerView, View header) {
        mDimensionCalculator.initMargins(clipRect, header);
        if (mOrientationProvider.getOrientation(recyclerView) == LinearLayout.VERTICAL) {
            clipRect.set(
                    recyclerView.getPaddingLeft(),
                    recyclerView.getPaddingTop(),
                    recyclerView.getWidth() - recyclerView.getPaddingRight() - clipRect.right,
                    recyclerView.getHeight() - recyclerView.getPaddingBottom());
        } else {
            clipRect.set(
                    recyclerView.getPaddingLeft(),
                    recyclerView.getPaddingTop(),
                    recyclerView.getWidth() - recyclerView.getPaddingRight(),
                    recyclerView.getHeight() - recyclerView.getPaddingBottom() - clipRect.bottom);
        }
    }
}
