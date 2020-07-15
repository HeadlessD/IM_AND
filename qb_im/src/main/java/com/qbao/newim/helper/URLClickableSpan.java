package com.qbao.newim.helper;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.qbao.newim.activity.NIMWebViewActivity;

/**
 * Created by chenjian on 2017/4/13.
 */

public class URLClickableSpan extends QBClickableSpan{
    private String mURL = null;
    private String mUserId = null;

    public URLClickableSpan(Context context, String url, int linkColor)
    {
        this(context, url, null, linkColor);
    }

    public URLClickableSpan(Context context, String url, String userId, int linkColor)
    {
        super(context, linkColor);
        mURL = url;
        mUserId = userId;
    }

    @Override
    public void onClick(View widget)
    {
        boolean shouldReturn = false;
        if(mClickableSpanListener != null)
        {
            shouldReturn = mClickableSpanListener.onClickableSpanListener(mURL);
        }

        if(shouldReturn) return;

        Intent intent = new Intent(mContext, NIMWebViewActivity.class);
        intent.putExtra("url", mURL);
        mContext.startActivity(intent);
    }
}
