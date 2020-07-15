package com.qbao.newim.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.qbao.newim.adapter.ChatAdapter;
import com.qbao.newim.constdef.MsgConstDef;

import com.qbao.newim.model.message.BaseMessageModel;
import com.qbao.newim.qbim.R;

/**
 * Created by chenjian on 2017/4/12.
 */

public class ChatAudioLeftView extends ChatAudioView {
    public ChatAudioLeftView(Context context) {
        super(context);
    }

    public ChatAudioLeftView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatAudioLeftView(Context context, ChatAdapter adapter) {
        super(context, adapter);
    }

    @Override
    public void initViews(Context context, View root) {
        super.initViews(context, root);
        viewContent.setBackgroundResource(R.drawable.nim_chat_receive_bg);
    }

    @Override
    public int getLayoutId() {
        return R.layout.nim_chat_audio_left;
    }

    @Override
    protected int getTemplateLayoutId() {
        return R.layout.nim_chat_receive_template;
    }

    @Override
    protected int getAnimationDrawable() {
        return R.drawable.nim_chat_audio_left_playing_anim;
    }

    @Override
    protected int getDrawable() {
        return R.mipmap.nim_chat_audio_left_playing03;
    }

    @Override
    public void setMessage(final int position, final BaseMessageModel chatMsg) {
        super.setMessage(position, chatMsg);
        if (MsgConstDef.MSG_STATUS.PLAYED == entry.msg_status) {
            //已经播放过
            statusLayout.setVisibility(View.GONE);
        } else {
            imgStatus.setVisibility(View.VISIBLE);
            statusLayout.setVisibility(View.VISIBLE);
        }
    }
}
