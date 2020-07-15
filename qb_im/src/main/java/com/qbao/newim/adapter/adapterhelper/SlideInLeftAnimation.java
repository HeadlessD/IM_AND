package com.qbao.newim.adapter.adapterhelper;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;

/**
 * Created by chenjian on 2017/3/22.
 * 从左边出来
 */

public class SlideInLeftAnimation implements BaseAnimation {
    @Override
    public Animator[] getAnimators(View view) {
        return new Animator[] {
                ObjectAnimator.ofFloat(view, "translationX", -view.getRootView().getWidth(), 0)
        };
    }
}