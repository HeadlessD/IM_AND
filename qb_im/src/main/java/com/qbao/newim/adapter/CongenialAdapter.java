package com.qbao.newim.adapter;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseViewHolder;
import com.qbao.newim.model.CongenialItem;
import com.qbao.newim.qbim.R;

import java.util.ArrayList;

/**
 * Created by chenjian on 2017/5/31.
 */

public class CongenialAdapter extends BaseQuickAdapter<CongenialItem, BaseViewHolder> {

    public CongenialAdapter(ArrayList<CongenialItem> items) {
        super(R.layout.nim_congenial_item, items);
    }

    @Override
    protected void convert(BaseViewHolder helper, CongenialItem item) {
        helper.getView(R.id.msg_center_unread_count).setVisibility(View.GONE);
        Glide.with(mContext).load(item.getAvatar()).placeholder(R.mipmap.nim_head).into((ImageView)
                helper.getView(R.id.commont_avatar));
        helper.setText(R.id.msg_center_friend_name, item.getShowName());
        helper.setText(R.id.msg_center_last_msg, item.getDesc());
        if (item.getStatus().equals("0")) {
            helper.getView(R.id.bt_agree).setEnabled(true);
            helper.setText(R.id.bt_agree, "添加");
            helper.setTextColor(R.id.bt_agree, Color.parseColor("#FF362C"));
            helper.setBackgroundRes(R.id.bt_agree, R.drawable.nim_red_invite_selector);
            helper.addOnClickListener(R.id.bt_agree);
        } else {
            helper.setText(R.id.bt_agree, "待验证");
            helper.setTextColor(R.id.bt_agree, Color.parseColor("#FF362C"));
            helper.setBackgroundColor(R.id.bt_agree, Color.TRANSPARENT);
            helper.getView(R.id.bt_agree).setEnabled(false);
        }
    }
}
