package com.qbao.newim.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseViewHolder;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.Utils;
import com.qbao.newim.views.quick_bar.StickyRecyclerHeadersAdapter;

import java.util.HashMap;
import java.util.List;

/**
 * Created by chenjian on 2017/9/14.
 */

public class GroupCreateAdapter extends BaseQuickAdapter<IMFriendInfo, BaseViewHolder> implements
        StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {

    private boolean bCreate;
    private HashMap<Integer, String> keyword_map;

    public GroupCreateAdapter(List<IMFriendInfo> data) {
        super(R.layout.nim_friend_item, data);
        keyword_map = new HashMap<>();
    }

    public GroupCreateAdapter(List<IMFriendInfo> data, boolean create) {
        super(R.layout.nim_friend_item, data);
        bCreate = create;
        keyword_map = new HashMap<>();
    }

    @Override
    protected void convert(BaseViewHolder helper, IMFriendInfo item) {
        String name = Utils.getUserShowName(new String[]{item.remark_name, item.nickName, item.user_name});
        helper.setText(R.id.tv_friends_name, Utils.highlight(name,
                keyword_map.get(helper.getAdapterPosition() - getHeaderLayoutCount())));
        ImageView imageView = helper.getView(R.id.iv_friend_head);
        Glide.with(helper.getConvertView().getContext()).load(AppUtil.getHeadUrl(item.userId)).
                placeholder(R.mipmap.nim_head).into(imageView);
        CheckBox checkBox = helper.getView(R.id.cb_checked);
        if (bCreate) {
            checkBox.setVisibility(View.VISIBLE);
            checkBox.setChecked(item.is_select);
        }

    }

    public void setKeyword_map(HashMap<Integer, String> keyword_map) {
        this.keyword_map = keyword_map;
    }

    @Override
    public long getHeaderId(int position) {
        if (position <= getHeaderLayoutCount() - 1) {
            return -1;
        }
        if (position >= getItemCount() - getFooterLayoutCount()) {
            return -1;
        }

        String pinyin = getData().get(position - getHeaderLayoutCount()).getInitial();
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
