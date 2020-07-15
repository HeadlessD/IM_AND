package com.qbao.newim.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.qbao.newim.adapter.ChatAdapter;
import com.qbao.newim.qbim.R;

/**
 * Created by chenjian on 2017/5/4.
 */

public class ChatSmileyLeftView extends ChatSmileyView {
    public ChatSmileyLeftView(Context context) {
        super(context);
    }

    public ChatSmileyLeftView(Context context, ChatAdapter adapter) {
        super(context, adapter);
    }

    public ChatSmileyLeftView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void initViews(Context context, View root) {
        super.initViews(context, root);
    }

    @Override
    protected int getTemplateLayoutId() {
        return R.layout.nim_chat_receive_template;
    }
}
