package com.qbao.newim.helper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextPaint;
import android.view.View;

/**
 * Created by chenjian on 2017/4/13.
 */

public class PhoneClickableSpan extends QBClickableSpan {
    private String mPhone = null;

    public PhoneClickableSpan(Context context, String phone, int linkColor) {
        super(context, linkColor);
        mPhone = phone;
    }

    @Override
    public void onClick(View widget) {
        boolean shouldReturn = false;
        if (mClickableSpanListener != null) {
            shouldReturn = mClickableSpanListener.onClickableSpanListener(mPhone);
        }

        if (shouldReturn) return;
        Uri uri = Uri.parse("tel:" + mPhone);
        Intent intent = new Intent(Intent.ACTION_DIAL, uri);
        mContext.startActivity(intent);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
    }
}