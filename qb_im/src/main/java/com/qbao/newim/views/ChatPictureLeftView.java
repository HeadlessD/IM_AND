package com.qbao.newim.views;

import android.content.Context;
import android.util.AttributeSet;

import com.qbao.newim.adapter.ChatAdapter;
import com.qbao.newim.qbim.R;

/**
 * 聊天图片接收布局
 *
 * @author zhangxiaolong
 * @since qianbao1.1
 */
public class ChatPictureLeftView extends ChatPictureView {
    public ChatPictureLeftView(Context context) {
        super(context);
    }

    public ChatPictureLeftView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatPictureLeftView(Context context, ChatAdapter adapter) {
        super(context, adapter);
    }

    @Override
    public int getLayoutId() {
        return R.layout.nim_chat_image_left;
    }

    @Override
    protected int getTemplateLayoutId() {
        return R.layout.nim_chat_receive_template;
    }
}
