package com.qbao.newim.views.quick_bar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by chenjian on 2017/8/31.
 */

public class QuickSideBarTipsView extends RelativeLayout {
    private QuickSideBarTipsItemView mTipsView;

    public QuickSideBarTipsView(Context context) {
        this(context, null);
    }

    public QuickSideBarTipsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QuickSideBarTipsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mTipsView = new QuickSideBarTipsItemView(context,attrs);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(mTipsView,layoutParams);
    }


    public void setText(String text,int poistion, float y){
        LayoutParams layoutParams = (LayoutParams) mTipsView.getLayoutParams();
        layoutParams.topMargin = (int)(y - getWidth()/2.8);
        mTipsView.setLayoutParams(layoutParams);
        mTipsView.setText(text);
    }
}
