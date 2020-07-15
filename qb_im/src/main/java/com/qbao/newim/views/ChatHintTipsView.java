package com.qbao.newim.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qbao.newim.activity.FriendVerifyActivity;
import com.qbao.newim.adapter.ChatAdapter;
import com.qbao.newim.constdef.FriendTypeDef;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.manager.NIMFriendInfoManager;
import com.qbao.newim.model.IMFriendInfo;

import com.qbao.newim.model.message.BaseMessageModel;
import com.qbao.newim.model.message.GcMessageModel;
import com.qbao.newim.model.message.ScMessageModel;
import com.qbao.newim.util.NIMStartActivityUtil;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.IconSpan;
import com.qbao.newim.util.ShowUtils;

/**
 * Created by chenjian on 2017/6/7.
 */

public class ChatHintTipsView extends ChatView {

    protected FullLineTextView txtBody;
    protected TextView txtLink;

    public ChatHintTipsView(Context context) {
        super(context);
    }

    public ChatHintTipsView(Context context, ChatAdapter adapter) {
        super(context, adapter);
    }

    public ChatHintTipsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void initViews(Context context, View root) {

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View rootView = layoutInflater.inflate(getLayoutId(), this);
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setGravity(Gravity.CENTER_HORIZONTAL);

        timeLineView = rootView.findViewById(R.id.chat_time_line_layout);
        txtTimeLine = (TextView) rootView.findViewById(R.id.chat_tv_timeline);
        txtBody = (FullLineTextView) rootView.findViewById(R.id.chat_tv_deny);
        txtLink = (TextView) rootView.findViewById(R.id.chat_tv_link);
    }


    @Override
    protected int getTemplateLayoutId() {
        return 0;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public int getLayoutId() {
        return R.layout.nim_chat_hint;
    }

    @Override
    public void setMessage(final int position, BaseMessageModel chatMsg) {
        super.setMessage(position, chatMsg);
        if (chatMsg.m_type == MsgConstDef.MSG_M_TYPE.TEXT ) {
            if (chatMsg.s_type == MsgConstDef.MSG_S_TYPE.TIP) {
                if (chatMsg.chat_type == MsgConstDef.MSG_CHAT_TYPE.PRIVATE) {
                    ScMessageModel scMessageModel = (ScMessageModel)chatMsg;
                    IMFriendInfo friendInfo = NIMFriendInfoManager.getInstance().getFriendUser(scMessageModel.opt_user_id);
                    if (friendInfo != null && friendInfo.delete_type == FriendTypeDef.ACTIVE_TYPE.PASSIVE) {
                        if (chatMsg.msg_content.contains(getContext().getResources().getString(R.string.msg_fail_tips))) {
                            setRequestVerify(chatMsg.msg_content, friendInfo);
                            return;
                        }
                    }
                }
                txtLink.setVisibility(GONE);
                txtBody.setVisibility(VISIBLE);
                txtBody.setText(chatMsg.msg_content);
            } else if (chatMsg.s_type == MsgConstDef.MSG_S_TYPE.GROUP_NEED_AGREE) {
                GcMessageModel gcMessageModel = (GcMessageModel)chatMsg;
                setRichText(gcMessageModel, true);
            } else if (chatMsg.s_type == MsgConstDef.MSG_S_TYPE.GROUP_ADD_AGREE) {
                GcMessageModel gcMessageModel = (GcMessageModel)chatMsg;
                setRichText(gcMessageModel, false);
            }
        }
    }

    private void setRequestVerify(String content, final IMFriendInfo friendInfo) {
        txtBody.setVisibility(GONE);
        txtLink.setVisibility(VISIBLE);
        txtLink.setText(content);
        String temp = "发送好友验证";
        SpannableString verify_txt = new SpannableString(content);
        verify_txt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                IMFriendInfo friend = NIMFriendInfoManager.getInstance().getFriendUser(friendInfo.userId);
                if (friend.delete_type == FriendTypeDef.ACTIVE_TYPE.INVALID) {
                    ShowUtils.showToast("已经是好友了");
                    return;
                }
                Intent intent = new Intent(getContext(), FriendVerifyActivity.class);
                intent.putExtra("source_type", friend.source_type);
                intent.putExtra("user_id", friend.userId);
                getContext().startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#75e7fb")); //设置颜色
                ds.setUnderlineText(true);
            }
        }, content.length() - temp.length(), content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        txtLink.setText(verify_txt);
        txtLink.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setRichText(final GcMessageModel msg, final boolean need_agree) {
        txtBody.setVisibility(GONE);
        txtLink.setVisibility(VISIBLE);
        Paint paint = new Paint();
        paint.setTextSize(txtLink.getTextSize());
        Paint.FontMetrics fm = paint.getFontMetrics();
        double font_size = Math.ceil(fm.bottom - fm.top);

        SpannableString msp = new SpannableString("  " + msg.msg_content);
        Drawable rightDrawable = ContextCompat.getDrawable(txtLink.getContext(), R.mipmap.nim_icon_qb_addfriend);
        rightDrawable.setBounds(0, 0, (int)font_size, (int)font_size);
        msp.setSpan(new IconSpan(rightDrawable, 1), 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        txtLink.setText(msp);
        SpannableString agree_txt;
        if (need_agree) {
            agree_txt = new SpannableString(" 去确认");
        } else {
            agree_txt = new SpannableString(" 已确认");
        }

        agree_txt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                if (need_agree) {
                    NIMStartActivityUtil.startToInviteDetailActivity(txtBody.getContext(), msg.user_id,
                            msg.group_id, msg.message_id);
                }
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                if (need_agree) {
                    ds.setColor(Color.parseColor("#15d95f")); //设置颜色
                } else {
                    ds.setColor(Color.parseColor("#6c6e6d")); //设置颜色
                }
                ds.setUnderlineText(false);
            }
        }, 0, agree_txt.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        txtLink.append(agree_txt);
        txtLink.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
