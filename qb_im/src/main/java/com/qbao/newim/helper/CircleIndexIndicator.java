package com.qbao.newim.helper;

import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by chenjian on 2017/7/24.
 */

public class CircleIndexIndicator implements IIndexIndicator {

    private CircleIndicator circleIndicator;

    @Override
    public void attach(FrameLayout parent) {
        FrameLayout.LayoutParams indexLp = new FrameLayout.LayoutParams(WRAP_CONTENT, 48);
        indexLp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        indexLp.bottomMargin = 10;

        circleIndicator = new CircleIndicator(parent.getContext());
        circleIndicator.setGravity(Gravity.CENTER_VERTICAL);
        circleIndicator.setLayoutParams(indexLp);

        parent.addView(circleIndicator);
    }

    @Override
    public void onShow(ViewPager viewPager) {
        circleIndicator.setVisibility(View.VISIBLE);
        circleIndicator.setViewPager(viewPager);
    }

    @Override
    public void onHide() {
        if (circleIndicator == null) return;
        circleIndicator.setVisibility(View.GONE);
    }

    @Override
    public void onRemove() {
        if (circleIndicator == null) return;
        ViewGroup vg = (ViewGroup) circleIndicator.getParent();
        if (vg != null) {
            vg.removeView(circleIndicator);
        }
    }
}
