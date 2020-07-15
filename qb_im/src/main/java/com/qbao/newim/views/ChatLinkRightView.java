package com.qbao.newim.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.qbao.newim.adapter.ChatAdapter;
import com.qbao.newim.qbim.R;

/**
 * Created by chenjian on 2017/7/20.
 */

public class ChatLinkRightView extends ChatLinkView{
    public ChatLinkRightView(Context context) {
        super(context);
    }

    public ChatLinkRightView(Context context, ChatAdapter adapter) {
        super(context, adapter);
    }

    public ChatLinkRightView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getTemplateLayoutId() {
        return R.layout.nim_chat_send_template;
    }

    @Override
    public int getLayoutId() {
        return R.layout.nim_chat_link_right_laytout;
    }

    @Override
    public void onClick(View v) {

    }
}
