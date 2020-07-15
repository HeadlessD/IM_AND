package com.qbao.newim.views;

import android.content.Context;
import android.util.AttributeSet;

import com.qbao.newim.adapter.ChatAdapter;

/**
 * Created by chenjian on 2017/7/20.
 */

public abstract class ChatLinkView extends ChatView{
    public ChatLinkView(Context context) {
        super(context);
    }

    public ChatLinkView(Context context, ChatAdapter adapter) {
        super(context, adapter);
    }

    public ChatLinkView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
