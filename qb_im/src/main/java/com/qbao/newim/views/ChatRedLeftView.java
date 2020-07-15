package com.qbao.newim.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.qbao.newim.adapter.ChatAdapter;
import com.qbao.newim.qbim.R;

/**
 * Created by chenjian on 2017/7/20.
 */

public class ChatRedLeftView extends ChatRedView{

    public ChatRedLeftView(Context context) {
        super(context);
    }

    public ChatRedLeftView(Context context, ChatAdapter adapter) {
        super(context, adapter);
    }

    public ChatRedLeftView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getTemplateLayoutId() {
        return R.layout.nim_chat_receive_template;
    }

    @Override
    public int getLayoutId(){
        return R.layout.nim_chat_red_left_layout;
    }

    @Override
    public void onClick(View v) {

    }
}
