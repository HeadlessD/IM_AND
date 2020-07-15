package com.qbao.newim.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.qbao.newim.adapter.ChatAdapter;
import com.qbao.newim.qbim.R;

/**
 * Created by chenjian on 2017/5/4.
 */

public class ChatSmileyRightView extends ChatSmileyView {
    public ChatSmileyRightView(Context context) {
        super(context);
    }

    public ChatSmileyRightView(Context context, ChatAdapter adapter) {
        super(context, adapter);
    }

    public ChatSmileyRightView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void initViews(Context context, View root) {
        super.initViews(context, root);
    }

    @Override
    protected int getTemplateLayoutId() {
        return R.layout.nim_chat_send_template;
    }
}
