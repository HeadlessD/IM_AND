package com.qbao.newim.views;

import android.content.Context;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.View;

import com.qbao.newim.adapter.ChatAdapter;
import com.qbao.newim.helper.PhoneClickableSpan;
import com.qbao.newim.helper.QBClickableSpan;
import com.qbao.newim.helper.SnsPattern;
import com.qbao.newim.helper.URLClickableSpan;

import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.message.BaseMessageModel;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.FaceUtil;

import java.util.regex.Matcher;

/**
 * Created by chenjian on 2017/4/13.
 */

public abstract class ChatTextView extends ChatView {
    protected FaceTextView txtBody;
    private boolean longClicked = false;
    private QBClickableSpan.ClickableSpanListener mClickableSpanListener;
    private static final Handler mHandler = new Handler();

    public ChatTextView(Context context) {
        super(context);
    }

    public ChatTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatTextView(Context context, ChatAdapter adapter) {
        super(context, adapter);
    }

    @Override
    public void initViews(Context context, View root) {
        super.initViews(context, root);
        viewContent.setBackgroundColor(ContextCompat.getColor(this.getContext(), android.R.color.transparent));
        txtBody = (FaceTextView) findViewById(R.id.chat_tv_msg);
        txtBody.setMovementMethod(LinkMovementMethod.getInstance());

        int clickableBgColor = ContextCompat.getColor(this.getContext(), R.color.clickable_bg_color);
        txtBody.setHighlightColor(clickableBgColor);
    }

    @Override
    public void bindListener() {
        super.bindListener();
        txtBody.setOnLongClickListener(this);

        mClickableSpanListener = new QBClickableSpan.ClickableSpanListener() {
            @Override
            public boolean onClickableSpanListener(String key) {
                boolean tLongClicked = longClicked;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        longClicked = false;
                    }
                }, 500L);
                return tLongClicked;
            }
        };
    }

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public boolean onLongClick(View v) {
        longClicked = true;
        return super.onLongClick(v);
    }

    @Override
    public void setMessage(final int position, final BaseMessageModel chatMsg) {
        super.setMessage(position, chatMsg);

        final CharSequence body = FaceUtil.getInstance().formatTextToFace(entry.msg_content, FaceUtil.FACE_TYPE.CHAT_TEXTVIEW);
        if (body instanceof Spannable) {
            Spannable spannable = (Spannable) body;
            filterURL(spannable);
			filterPhone(spannable);
            txtBody.setText(spannable);
        } else {
            SpannableString spannable = new SpannableString(body);
            filterURL(spannable);
			filterPhone(spannable);
            txtBody.setText(spannable);
        }
        txtBody.setText(body);
    }

    private void filterURL(Spannable spannable) {
        Matcher m = SnsPattern.urlMatcher(spannable.toString());
        while (m.find()) {
            String text = m.group();
            URLClickableSpan clickableSpan = new URLClickableSpan(mActivity, text, "" +
                    NIMUserInfoManager.getInstance().GetSelfUserId(), R.color.link_color);
            clickableSpan.setClickableSpanListener(mClickableSpanListener);
            spannable.setSpan(clickableSpan, m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void filterPhone(Spannable spannable) {
        Matcher m = SnsPattern.phoneMatcher(spannable.toString());
        while (m.find()) {
            String text = m.group();
            PhoneClickableSpan clickableSpan = new PhoneClickableSpan(mActivity, text, R.color.link_color);
            clickableSpan.setClickableSpanListener(mClickableSpanListener);
            spannable.setSpan(clickableSpan, m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}
