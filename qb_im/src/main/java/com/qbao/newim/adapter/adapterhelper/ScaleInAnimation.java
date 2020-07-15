package com.qbao.newim.adapter.adapterhelper;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;

/**
 * Created by chenjian on 2017/3/22.
 * 放大缩小动画
 */
public class ScaleInAnimation implements BaseAnimation {

    private static final float DEFAULT_SCALE_FROM = .5f;
    private final float mFrom;

    public ScaleInAnimation() {
        this(DEFAULT_SCALE_FROM);
    }

    public ScaleInAnimation(float from) {
        mFrom = from;
    }

    @Override
    public Animator[] getAnimators(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", mFrom, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", mFrom, 1f);
        return new ObjectAnimator[] { scaleX, scaleY };
    }
}
