package com.qbao.newim.util;

import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * Created by chenjian on 2017/5/26.
 */

public enum PixValue {
    dip
            {
                @Override
                public int valueOf(float value)
                {
                    return Math.round(value * m.density);
                }
            },
    sp
            {
                @Override
                public int valueOf(float value)
                {
                    return Math.round(value * m.scaledDensity);
                }
            };
    public static DisplayMetrics m = Resources.getSystem().getDisplayMetrics();

    public abstract int valueOf(float value);
}
