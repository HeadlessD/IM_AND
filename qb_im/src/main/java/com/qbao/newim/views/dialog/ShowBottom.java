package com.qbao.newim.views.dialog;

import android.view.View;

import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created by chenjian on 2017/7/25.
 */

public class ShowBottom extends BaseEffects{
    @Override
    protected void setupAnimation(View view) {
        getAnimatorSet().playTogether(
                ObjectAnimator.ofFloat(view, "translationY", 300, 0).setDuration(mDuration)
        );
    }
}
