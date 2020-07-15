package com.qbao.newim.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.qbao.newim.adapter.ChatAdapter;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.model.message.BaseMessageModel;
import com.qbao.newim.qbim.R;

/**
 * 聊天文本布局基类
 *
 * @author zhangxiaolong
 * @since qianbao1.1
 */
public class ChatPictureRightView extends ChatPictureView {

    public ChatPictureRightView(Context context) {
        super(context);
    }

    public ChatPictureRightView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatPictureRightView(Context context, ChatAdapter adapter) {
        super(context, adapter);
    }

    @Override
    public int getLayoutId() {
        return R.layout.nim_chat_image_right;
    }

    @Override
    protected int getTemplateLayoutId() {
        return R.layout.nim_chat_send_template;
    }

    @Override
    public void setMessage(final int position, final BaseMessageModel chatMsg) {
        super.setMessage(position, chatMsg);

        final int progress = chatPicMsg.progress;
        if (chatPicMsg.is_self && chatPicMsg.msg_status == MsgConstDef.MSG_STATUS.SENDING && progress < 100) {

            if (txtProgress.getVisibility() == View.GONE) {
                txtProgress.setVisibility(View.VISIBLE);
            }
            txtProgress.setText(progress + "%");
        } else {
            txtProgress.setVisibility(View.GONE);
        }
    }
}
