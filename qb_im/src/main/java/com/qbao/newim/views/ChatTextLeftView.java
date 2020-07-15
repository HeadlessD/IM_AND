package com.qbao.newim.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.qbao.newim.adapter.ChatAdapter;
import com.qbao.newim.qbim.R;

/**
 * Created by chenjian on 2017/4/13.
 */

public class ChatTextLeftView extends ChatTextView{
    public ChatTextLeftView(Context context) {
        super(context);
    }

    public ChatTextLeftView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatTextLeftView(Context context, ChatAdapter adapter) {
        super(context, adapter);
    }

    @Override
    public void initViews(Context context, View root)
    {
        super.initViews(context, root);
        txtBody.setBackgroundResource(R.drawable.nim_chat_receive_bg);
    }

    @Override
    protected int getTemplateLayoutId()
    {
        return R.layout.nim_chat_receive_template;
    }


    @Override
    public int getLayoutId()
    {
        return R.layout.nim_chat_text_left;
    }
}
