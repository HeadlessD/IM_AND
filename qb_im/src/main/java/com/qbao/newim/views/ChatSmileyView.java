package com.qbao.newim.views;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.qbao.newim.adapter.ChatAdapter;

import com.qbao.newim.model.message.BaseMessageModel;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.QbGifUtil;
import com.qbao.newim.util.ScreenUtils;

/**
 * Created by chenjian on 2017/5/4.
 */

public abstract class ChatSmileyView extends ChatView{

    private ImageView ivFaceView;

    public ChatSmileyView(Context context) {
        super(context);
    }

    public ChatSmileyView(Context context, ChatAdapter adapter) {
        super(context, adapter);
    }

    public ChatSmileyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void initViews(Context context, View root) {
        super.initViews(context, root);
        ivFaceView = (ImageView) findViewById(R.id.chat_gif_face_view);
        viewContent.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
    }

    @Override
    public int getLayoutId() {
        return R.layout.nim_chat_gif_face;
    }

    @Override
    protected int getTemplateLayoutId() {
        return R.layout.nim_chat_receive_template;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void setMessage(int position, BaseMessageModel chatMsg) {
        super.setMessage(position, chatMsg);
        int res = QbGifUtil.getInstance().getGifResCode(chatMsg.msg_content);
        int show_size = (int) ScreenUtils.dp2px(getContext(), getResources().getDimension(R.dimen.nim_qb_gif_show_size));
        Glide.with(getContext()).load(res).asGif().placeholder(R.mipmap.nim_pp_ic_holder_light).override(show_size, show_size).crossFade()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(ivFaceView);

        }
}
