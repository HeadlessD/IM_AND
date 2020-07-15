package com.qbao.newim.adapter;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseViewHolder;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.Utils;

import java.util.List;

/**
 * Created by chenjian on 2017/6/23.
 */

public class GroupInfoAdapter extends BaseQuickAdapter<IMGroupInfo, BaseViewHolder>{

    private String input_word = "";

    public GroupInfoAdapter(List<IMGroupInfo> data) {
        super(R.layout.nim_group_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, IMGroupInfo item) {
        Glide.with(helper.getConvertView().getContext()).load(item.group_img_url).placeholder(R.mipmap.nim_head)
                .into((ImageView)helper.getView(R.id.iv_group_head));

        helper.setText(R.id.tv_group_name, Utils.highlight(item.group_name, input_word));
        helper.setText(R.id.tv_group_member_count, item.group_count + "人");
        helper.addOnClickListener(R.id.iv_group_head);
    }
}
