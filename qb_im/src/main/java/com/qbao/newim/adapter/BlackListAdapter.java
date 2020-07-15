package com.qbao.newim.adapter;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseViewHolder;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.Utils;

import java.util.List;

/**
 * Created by chenjian on 2017/7/26.
 */

public class BlackListAdapter extends BaseQuickAdapter <IMFriendInfo, BaseViewHolder>{

    public BlackListAdapter(List<IMFriendInfo> data) {
        super(R.layout.nim_black_list_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, IMFriendInfo item) {
        Glide.with(helper.getConvertView().getContext()).load(AppUtil.getHeadUrl(item.userId)).placeholder(R.mipmap.nim_head)
                .into((ImageView)helper.getView(R.id.iv_black_head));
        String show_name = Utils.getUserShowName(new String[]{item.remark_name, item.nickName, item.user_name});
        helper.setText(R.id.tv_black_name, show_name);
    }
}
