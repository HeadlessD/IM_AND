package com.qbao.newim.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseViewHolder;
import com.qbao.newim.model.Contact;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.views.quick_bar.StickyRecyclerHeadersAdapter;

import java.util.List;

/**
 * Created by chenjian on 2017/8/2.
 */

public class ContactListAdapter extends BaseQuickAdapter<Contact, BaseViewHolder> implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {

    public ContactListAdapter(List<Contact> data) {
        super(R.layout.nim_contact_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Contact item) {
        ImageView imageView = helper.getView(R.id.contact_img);
        TextView textView = helper.getView(R.id.cb_contact);
        if (item.user_id > 0) {
            Glide.with(imageView.getContext()).load(AppUtil.getHeadUrl(item.user_id))
                    .placeholder(R.mipmap.nim_head).into(imageView);
        }

        helper.setText(R.id.tv_contact_num, item.contactNum);
        helper.setText(R.id.tv_contact_name, item.contactName);

        if (item.is_add) {
            textView.setEnabled(false);
            textView.setText("已添加");
            textView.setTextColor(Color.parseColor("#706E6E"));
            textView.setBackgroundColor(Color.TRANSPARENT);
        } else {
            textView.setEnabled(true);
            textView.setText("添加");
            textView.setTextColor(Color.parseColor("#7BCF54"));
            textView.setBackgroundResource(R.drawable.nim_invite_selector);
        }

        helper.addOnClickListener(R.id.cb_contact);
    }

    @Override
    public long getHeaderId(int position) {
        return getItem(position).pinyin.charAt(0);
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
