package com.qbao.newim.adapter.adapterhelper;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;

/**
 * Created by chenjian on 2017/3/22.
 * 从右边出来
 */

public class SlideInRightAnimation implements BaseAnimation {

    @Override
    public Animator[] getAnimators(View view) {
        return new Animator[]{
                ObjectAnimator.ofFloat(view, "translationX", view.getRootView().getWidth(), 0)
        };
    }
}
