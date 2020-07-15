package com.qbao.newim.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseViewHolder;
import com.qbao.newim.helper.GlideCircleTransform;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.Utils;
import com.qbao.newim.views.quick_bar.StickyRecyclerHeadersAdapter;

import java.util.List;

/**
 * Created by chenjian on 2017/8/31.
 */

public class ContactAdapter extends BaseQuickAdapter <IMFriendInfo, BaseViewHolder> implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {

    public ContactAdapter(List<IMFriendInfo> data) {
        super(R.layout.nim_friend_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, IMFriendInfo item) {
        String name = Utils.getUserShowName(new String[]{item.remark_name, item.nickName, item.user_name});
        helper.setText(R.id.tv_friends_name, name);
        ImageView imageView = helper.getView(R.id.iv_friend_head);
        Context context = helper.getConvertView().getContext();
        Glide.with(context).load(AppUtil.getHeadUrl(item.userId)).
                error(R.mipmap.nim_head_circle).
                transform(new GlideCircleTransform(context))
                .into(imageView);
    }

    @Override
    public long getHeaderId(int position) {
        if (position <= getHeaderLayoutCount() - 1) {
            return -1;
        }
        if (position >= getItemCount() - getFooterLayoutCount()) {
            return -1;
        }

        IMFriendInfo friendInfo = getData().get(position - getHeaderLayoutCount());
        friendInfo.is_star = false;
        String pinyin = friendInfo.getInitial();
        if (TextUtils.isEmpty(pinyin)) {
            return -1;
        }
        return pinyin.charAt(0);
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.nim_quick_head, parent, false);
        return new RecyclerView.ViewHolder(view) {
        };
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView textView = (TextView) holder.itemView;
        String type = getItem(position - getHeaderLayoutCount()).getInitial();
        if (type.equals("|")) {
            type = "#";
        } else if (type.equals("~")) {
            type = "*";
        }
        textView.setText(type);
        holder.itemView.setBackgroundColor(Color.parseColor("#dcdcdc"));
    }
}
