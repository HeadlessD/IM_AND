package com.qbao.newim.helper;

import android.content.Context;
import android.text.TextPaint;
import android.text.style.ClickableSpan;

/**
 * Created by chenjian on 2017/4/13.
 */

public abstract class QBClickableSpan extends ClickableSpan {
    protected Context mContext = null;
    private int mLinkColor = 0;
    protected ClickableSpanListener mClickableSpanListener = null;

    public interface ClickableSpanListener {
        boolean onClickableSpanListener(String key);
    }

    public void setClickableSpanListener(ClickableSpanListener clickableSpanListener) {
        mClickableSpanListener = clickableSpanListener;
    }

    public QBClickableSpan(Context context, int linkColor) {
        mContext = context;
        mLinkColor = mContext.getResources().getColor(linkColor);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setUnderlineText(false);
        ds.setColor(mLinkColor);
    }
}
