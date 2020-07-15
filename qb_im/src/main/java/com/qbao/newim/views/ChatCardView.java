package com.qbao.newim.views;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.qbao.newim.adapter.ChatAdapter;
import com.qbao.newim.constdef.FriendTypeDef;

import com.qbao.newim.model.NIMCardInfo;
import com.qbao.newim.model.message.BaseMessageModel;
import com.qbao.newim.util.NIMStartActivityUtil;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.AppUtil;

/**
 * Created by chenjian on 2017/7/19.
 */

public abstract class ChatCardView extends ChatView {

    protected ImageView iv_card;
    protected TextView tv_nick_name;
    protected TextView tv_user_name;
    protected TextView tv_type;
    protected NIMCardInfo card_info;

    public ChatCardView(Context context) {
        super(context);
    }

    public ChatCardView(Context context, ChatAdapter adapter) {
        super(context, adapter);
    }

    public ChatCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void initViews(Context context, View root) {
        super.initViews(context, root);
        iv_card = (ImageView) findViewById(R.id.img_card_avatar);
        tv_nick_name = (TextView) findViewById(R.id.txt_card_nickname);
        tv_user_name = (TextView) findViewById(R.id.txt_card_username);
        tv_type = (TextView) findViewById(R.id.chat_card_type);
    }

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void onClick(View v) {
        if (card_info.type == 0) {
            NIMStartActivityUtil.startToNIMUserActivity(getContext(), card_info.id,
                    FriendTypeDef.FRIEND_SOURCE_TYPE.VCARD);
        }
    }

    @Override
    public void setMessage(int position, BaseMessageModel chatMsg) {
        String json = chatMsg.msg_content;
        if (TextUtils.isEmpty(json)) {
            return;
        } else {
            card_info = new Gson().fromJson(json, NIMCardInfo.class);
        }
        super.setMessage(position, chatMsg);

        String user_name = card_info.user_name;
        String nick_name = card_info.name;
        int type = card_info.type;
        if (TextUtils.isEmpty(user_name)) {
            tv_user_name.setVisibility(GONE);
            if (TextUtils.isEmpty(nick_name)) {
                tv_nick_name.setText("未知");
            } else {
                tv_nick_name.setText(nick_name);
            }
        } else {
            tv_user_name.setText(user_name);
            if (TextUtils.isEmpty(nick_name)) {
                tv_nick_name.setVisibility(GONE);
            } else {
                tv_nick_name.setText(nick_name);
            }
        }

        if (type == 0) {
            tv_type.setText("个人名片");
        } else {
            tv_type.setText("公众号名片");
        }

        Glide.with(getContext()).load(AppUtil.getHeadUrl(card_info.id)).placeholder(R.mipmap.nim_head)
                .into(iv_card);
    }
}
