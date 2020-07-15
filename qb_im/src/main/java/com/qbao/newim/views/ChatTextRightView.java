package com.qbao.newim.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.qbao.newim.adapter.ChatAdapter;
import com.qbao.newim.qbim.R;

/**
 * Created by chenjian on 2017/4/13.
 */

public class ChatTextRightView extends ChatTextView{
    public ChatTextRightView(Context context) {
        super(context);
    }

    public ChatTextRightView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatTextRightView(Context context, ChatAdapter adapter) {
        super(context, adapter);
    }

    @Override
    public void initViews(Context context, View root)
    {
        super.initViews(context, root);
        txtBody.setBackgroundResource(R.drawable.nim_chat_send_bg);
    }

    @Override
    protected int getTemplateLayoutId()
    {
        return R.layout.nim_chat_send_template;
    }

    @Override
    public int getLayoutId()
    {
        return R.layout.nim_chat_text_right;
    }
}
