package com.qbao.newim.views;

import android.content.Context;
import android.util.AttributeSet;

import com.qbao.newim.adapter.ChatAdapter;
import com.qbao.newim.qbim.R;

/**
 * Created by chenjian on 2017/7/19.
 */

public class ChatCardRightView extends ChatCardView {
    public ChatCardRightView(Context context) {
        super(context);
    }

    public ChatCardRightView(Context context, ChatAdapter adapter) {
        super(context, adapter);
    }

    public ChatCardRightView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int getLayoutId() {
        return R.layout.nim_chat_right_card;
    }

    @Override
    protected int getTemplateLayoutId() {
        return R.layout.nim_chat_send_template;
    }
}
