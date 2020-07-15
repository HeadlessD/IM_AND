package com.qbao.newim.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.qbao.newim.adapter.ChatAdapter;
import com.qbao.newim.qbim.R;

/**
 * Created by chenjian on 2017/7/20.
 */

public class ChatRedRightView extends ChatRedView{

    public ChatRedRightView(Context context) {
        super(context);
    }

    public ChatRedRightView(Context context, ChatAdapter adapter) {
        super(context, adapter);
    }

    public ChatRedRightView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getTemplateLayoutId() {
        return R.layout.nim_chat_send_template;
    }

    @Override
    public int getLayoutId(){
        return R.layout.nim_chat_red_right_layout;
    }

    @Override
    public void onClick(View v) {

    }
}
