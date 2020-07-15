package com.qbao.newim.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.qbao.newim.adapter.ChatAdapter;
import com.qbao.newim.qbim.R;

/**
 * Created by chenjian on 2017/7/20.
 */

public class ChatLinkLeftView extends ChatLinkView{
    public ChatLinkLeftView(Context context) {
        super(context);
    }

    public ChatLinkLeftView(Context context, ChatAdapter adapter) {
        super(context, adapter);
    }

    public ChatLinkLeftView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getTemplateLayoutId() {
        return R.layout.nim_chat_receive_template;
    }

    @Override
    public int getLayoutId() {
        return R.layout.nim_chat_link_left_layout;
    }

    @Override
    public void onClick(View v) {

    }
}
