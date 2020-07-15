package com.qbao.newim.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseViewHolder;
import com.qbao.newim.helper.IGroupKickMember;
import com.qbao.newim.model.IMGroupUserInfo;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.Utils;
import com.qbao.newim.views.quick_bar.StickyRecyclerHeadersAdapter;

import java.util.HashMap;
import java.util.List;

/**
 * Created by chenjian on 2017/6/29.
 */

public class GroupMemberAdapter extends BaseQuickAdapter<IMGroupUserInfo, BaseViewHolder> implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {
    private HashMap<Integer, String> keyword_map;
    private long group_manager_id;
    private boolean is_edit_model;

    public void setKeyword_map(HashMap<Integer, String> keyword_map) {
        this.keyword_map = keyword_map;
    }

    private IGroupKickMember listener;

    public void setListener(IGroupKickMember listener) {
        this.listener = listener;
    }

    public void setGroup_manager_id(long group_manager_id) {
        this.group_manager_id = group_manager_id;
    }

    public void setIs_edit_model(boolean is_edit_model) {
        this.is_edit_model = is_edit_model;
    }

    public GroupMemberAdapter(List<IMGroupUserInfo> data) {
        super(R.layout.nim_group_invite_item, data);
        keyword_map = new HashMap<>();
    }

    @Override
    protected void convert(BaseViewHolder helper, final IMGroupUserInfo item) {
        ImageView imageView = helper.getView(R.id.iv_group_user_head);
        TextView textView = helper.getView(R.id.tv_group_user_name);
        CheckBox checkBox = helper.getView(R.id.tv_group_cb);
        TextView owner = helper.getView(R.id.tv_group_owner);

        Glide.with(helper.getConvertView().getContext()).
                load(AppUtil.getHeadUrl(item.user_id)).placeholder(R.mipmap.nim_head).into(imageView);
        textView.setText(Utils.highlight(item.user_nick_name, keyword_map.get(helper.getAdapterPosition())));
        owner.setVisibility(item.user_id == group_manager_id ? View.VISIBLE : View.GONE);

        if (is_edit_model && group_manager_id > 0) {
            if (item.group_id == group_manager_id) {
                checkBox.setVisibility(View.GONE);
            } else {
                checkBox.setVisibility(View.VISIBLE);
                checkBox.setChecked(item.is_select);
            }
        } else {
            checkBox.setVisibility(View.GONE);
        }

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onKick(item, !item.is_select);
                }
            }
        });
    }

    @Override
    public long getHeaderId(int position) {
        if (position <= getHeaderLayoutCount() - 1) {
            return -1;
        }
        if (position >= getItemCount() - getFooterLayoutCount()) {
            return -1;
        }
        return getData().get(position - getHeaderLayoutCount()).pinyin.charAt(0);
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
