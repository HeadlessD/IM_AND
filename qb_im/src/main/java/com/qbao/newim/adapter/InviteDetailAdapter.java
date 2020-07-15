package com.qbao.newim.adapter;

import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseViewHolder;
import com.qbao.newim.model.IMGroupUserInfo;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.AppUtil;

import java.util.ArrayList;

/**
 * Created by chenjian on 2017/7/6.
 */

public class InviteDetailAdapter extends BaseQuickAdapter <IMGroupUserInfo, BaseViewHolder> {

    public InviteDetailAdapter(ArrayList<IMGroupUserInfo> data) {
        super(R.layout.nim_invite_detail, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, IMGroupUserInfo item) {
        if (item.need_agree) {
            Glide.with(mContext).load(AppUtil.getHeadUrl(item.user_id))
                    .placeholder(R.mipmap.nim_head).into((ImageView)helper.getView(R.id.invite_item_head));
            helper.setText(R.id.invite_item_name, item.user_nick_name);
        } else {
            helper.getView(R.id.invite_item_head).setVisibility(View.INVISIBLE);
            helper.getView(R.id.invite_item_name).setVisibility(View.GONE);
        }
    }
}
