package com.qbao.newim.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.qbao.newim.adapter.ChatAdapter;
import com.qbao.newim.qbim.R;

/**
 * Created by chenjian on 2017/4/12.
 */

public class ChatAudioRightView extends ChatAudioView{
    public ChatAudioRightView(Context context) {
        super(context);
    }

    public ChatAudioRightView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatAudioRightView(Context context, ChatAdapter adapter) {
        super(context, adapter);
    }

    @Override
    public void initViews(Context context, View root)
    {
        super.initViews(context, root);
        viewContent.setBackgroundResource(R.drawable.nim_chat_send_bg);
    }

    @Override
    public int getLayoutId()
    {
        return R.layout.nim_chat_audio_right;
    }

    @Override
    protected int getTemplateLayoutId()
    {
        return R.layout.nim_chat_send_template;
    }

    @Override
    protected int getAnimationDrawable()
    {
        return R.drawable.nim_chat_audio_right_playing_anim;
    }

    @Override
    protected int getDrawable()
    {
        return R.mipmap.nim_chat_audio_right_playing03;
    }
}
