package com.qbao.newim.views;

import android.content.Context;
import android.util.AttributeSet;

import com.qbao.newim.adapter.ChatAdapter;

/**
 * Created by chenjian on 2017/7/20.
 */

public abstract class ChatRedView extends ChatView{
    public ChatRedView(Context context) {
        super(context);
    }

    public ChatRedView(Context context, ChatAdapter adapter) {
        super(context, adapter);
    }

    public ChatRedView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
