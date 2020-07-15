package com.qbao.newim.views.imgpicker.photoview;

import android.content.Context;

/**
 * Created by chenjian on 2017/5/2.
 */

public class IcsScroller extends GingerScroller {

    public IcsScroller(Context context) {
        super(context);
    }

    @Override
    public boolean computeScrollOffset() {
        return mScroller.computeScrollOffset();
    }

}
