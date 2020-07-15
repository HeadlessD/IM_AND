package com.qbao.newim.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseViewHolder;
import com.qbao.newim.model.IMGroupUserInfo;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.ScreenUtils;

import java.util.ArrayList;

/**
 * Created by chenjian on 2017/6/26.
 */

public class GroupAvatarAdapter extends BaseQuickAdapter <IMGroupUserInfo, BaseViewHolder> {
    private long owner_id;
    private Context mContext;

    public void setOwner_id(long owner_id) {
        this.owner_id = owner_id;
    }

    public GroupAvatarAdapter(Context context, ArrayList<IMGroupUserInfo> data) {
        super(R.layout.nim_group_avatar_item, data);
        this.mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, IMGroupUserInfo item) {
        ViewGroup.LayoutParams params = helper.getView(R.id.group_avatar_layout).getLayoutParams();
        int width = ScreenUtils.getSceenWidth(mContext);
        params.width = (width - 2 * ScreenUtils.dp2px(mContext, 16)) / 8 - ScreenUtils.dp2px(mContext, 4);
        params.height = params.width;
        helper.getView(R.id.group_avatar_layout).setLayoutParams(params);

        if (item.user_id == owner_id) {
            helper.getView(R.id.group_avatar_owner).setVisibility(View.VISIBLE);
        } else {
            helper.getView(R.id.group_avatar_owner).setVisibility(View.GONE);
        }

        Glide.with(mContext).load(AppUtil.getHeadUrl(item.user_id))
                .placeholder(R.mipmap.nim_head).into((ImageView)helper.getView(R.id.group_avatar_img));
    }
}
