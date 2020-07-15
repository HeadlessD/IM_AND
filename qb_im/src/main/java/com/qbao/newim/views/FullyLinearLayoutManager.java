package com.qbao.newim.views;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.qbao.newim.util.ScreenUtils;

/**
 * Created by chenjian on 2015/11/16.
 */
public class FullyLinearLayoutManager extends LinearLayoutManager {
    private static final String TAG = FullyLinearLayoutManager.class.getSimpleName();
    private Context mContext;
    private int nFootHeight;
    private int nHeadHeight;

    public FullyLinearLayoutManager(Context context) {
        super(context);
        mContext = context;
    }

    public FullyLinearLayoutManager(Context context, int footHeight) {
        super(context);
        this.mContext = context;
        this.nFootHeight = footHeight;
    }

    public FullyLinearLayoutManager(Context context, int footHeight, int headHeight) {
        super(context);
        this.mContext = context;
        this.nFootHeight = footHeight;
        this.nHeadHeight = headHeight;
    }

    public FullyLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    private int[] mMeasuredDimension = new int[2];

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state,
                          int widthSpec, int heightSpec) {

        final int widthMode = View.MeasureSpec.getMode(widthSpec);
        final int heightMode = View.MeasureSpec.getMode(heightSpec);
        final int widthSize = View.MeasureSpec.getSize(widthSpec);
        final int heightSize = View.MeasureSpec.getSize(heightSpec);

        int count = getItemCount();
        int width = 0;
        int height = 0;
        for (int i = 0; i < getItemCount(); i++) {
            measureScrapChild(recycler, i,
                    View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
                    mMeasuredDimension);

            if (getOrientation() == HORIZONTAL) {
                width = width + mMeasuredDimension[0];
                if (i == 0) {
                    height = mMeasuredDimension[1];
                }
            } else {
                if (i == 0) {
                    width = mMeasuredDimension[0];
                }

                if (i == getItemCount() - 1) {
                    if (nFootHeight > 0) {
                        height = height + ScreenUtils.dp2px(mContext, nFootHeight);
                    } else {
                        height = height + mMeasuredDimension[1];
                    }
                } else {
                    height = height + mMeasuredDimension[1];
                }
            }
        }
        switch (widthMode) {
            case View.MeasureSpec.EXACTLY:
                width = widthSize;
            case View.MeasureSpec.AT_MOST:
            case View.MeasureSpec.UNSPECIFIED:
        }

        switch (heightMode) {
            case View.MeasureSpec.EXACTLY:
                height = heightSize;
            case View.MeasureSpec.AT_MOST:
            case View.MeasureSpec.UNSPECIFIED:
        }

        setMeasuredDimension(width, height);
    }

    private void measureScrapChild(RecyclerView.Recycler recycler, int position, int widthSpec,
                                   int heightSpec, int[] measuredDimension) {
        try {
            View view = recycler.getViewForPosition(position);//fix 动态添加时报IndexOutOfBoundsException

            if (view != null) {
                RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) view.getLayoutParams();

                int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec,
                        getPaddingLeft() + getPaddingRight(), p.width);

                int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec,
                        getPaddingTop() + getPaddingBottom(), p.height);

                view.measure(childWidthSpec, childHeightSpec);
                measuredDimension[0] = view.getMeasuredWidth() + p.leftMargin + p.rightMargin;
                measuredDimension[1] = view.getMeasuredHeight() + p.bottomMargin + p.topMargin;
                recycler.recycleView(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    @Override
    public boolean canScrollVertically() {
        return false;
    }
}
