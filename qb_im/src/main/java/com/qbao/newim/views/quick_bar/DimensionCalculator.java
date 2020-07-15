package com.qbao.newim.views.quick_bar;

import android.graphics.Rect;
import android.view.View;
import static android.view.ViewGroup.LayoutParams;
import static android.view.ViewGroup.MarginLayoutParams;

/**
 * Created by chenjian on 2017/8/31.
 */

public class DimensionCalculator {
    public void initMargins(Rect margins, View view) {
        LayoutParams layoutParams = view.getLayoutParams();

        if (layoutParams instanceof MarginLayoutParams) {
            MarginLayoutParams marginLayoutParams = (MarginLayoutParams) layoutParams;
            initMarginRect(margins, marginLayoutParams);
        } else {
            margins.set(0, 0, 0, 0);
        }
    }

    private void initMarginRect(Rect marginRect, MarginLayoutParams marginLayoutParams) {
        marginRect.set(
                marginLayoutParams.leftMargin,
                marginLayoutParams.topMargin,
                marginLayoutParams.rightMargin,
                marginLayoutParams.bottomMargin
        );
    }
}
