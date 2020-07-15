package com.qbao.newim.views;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.qbao.newim.adapter.ChatAdapter;
import com.qbao.newim.constdef.MsgConstDef;

import com.qbao.newim.model.NIMLocationInfo;
import com.qbao.newim.model.message.BaseMessageModel;
import com.qbao.newim.qbim.R;

/**
 * Created by chenjian on 2017/7/18.
 */

public abstract class ChatLocationView extends ChatView {

    protected TextView txtPosition;
    protected double lat;
    protected double lon;
    protected String address;
    protected BubbleImageView imageView;

    public ChatLocationView(Context context) {
        super(context);
    }

    public ChatLocationView(Context context, ChatAdapter adapter) {
        super(context, adapter);
    }

    public ChatLocationView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void initViews(Context context, View root) {
        super.initViews(context, root);
        txtPosition = (TextView) findViewById(R.id.chat_tv_position);
        imageView = (BubbleImageView) findViewById(R.id.chat_position_img);
    }

    @Override
    public void setMessage(int position, BaseMessageModel chatMsg) {
        super.setMessage(position, chatMsg);
        if (chatMsg.m_type == MsgConstDef.MSG_M_TYPE.MAP) {
            if (TextUtils.isEmpty(chatMsg.msg_content)) {
                return;
            }
            NIMLocationInfo info = new Gson().fromJson(chatMsg.msg_content, NIMLocationInfo.class);
            if (info == null) {
                return;
            }

            lat = info.getLat();
            lon = info.getLng();
            address = info.getAddress();
            txtPosition.setText(info.getAddress());
        }

        if (chatMsg.is_self) {
            imageView.setLocalImageBitmap(R.mipmap.nim_chat_default_position, R.drawable.nim_chat_send_normal);
        } else {
            imageView.setLocalImageBitmap(R.mipmap.nim_chat_default_position, R.drawable.nim_chat_receive_normal);
        }
    }
}
