package com.qbao.newim.adapter;

import android.graphics.Color;
import android.text.TextUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseViewHolder;
import com.qbao.newim.model.Contact;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chenjian on 2017/9/1.
 */

public class PhoneSearchAdapter extends BaseQuickAdapter<Contact, BaseViewHolder> implements Filterable {

    private List<Contact> mList;
    private List<Contact> show_list;
    private HashMap<Integer, String> keyword_map;

    public PhoneSearchAdapter(List<Contact> data, List<Contact> show_list) {
        super(R.layout.nim_contact_item, show_list);
        this.mList = data;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                String input = constraint.toString();

                if (!TextUtils.isEmpty(input)) {
                    List<Contact> searchData = new ArrayList<>();
                    if (keyword_map == null) {
                        keyword_map = new HashMap<>();
                    } else {
                        keyword_map.clear();
                    }

                    boolean is_letter = true;
                    char[] nameChar = input.toCharArray();
                    for (int i = 0; i < nameChar.length; i++) {
                        is_letter = Utils.isLetter(nameChar[i]);
                        if (!is_letter)
                            break;
                    }

                    for (Contact object : mList) {
                        // 纯字母
                        if (is_letter) {
                            String keyword;
                            keyword = Utils.containInput(input.toUpperCase(), object.contactName, object.pinyin_index);
                            if (!TextUtils.isEmpty(keyword)) {
                                searchData.add(object);
                                keyword_map.put(searchData.size() - 1, keyword);
                            }
                            // 非字母
                        } else {
                            String str = "";
                            if (!TextUtils.isEmpty(object.contactName))
                                str += object.contactName;
                            if (!TextUtils.isEmpty(object.contactNum))
                                str += object.contactNum;
                            if (!TextUtils.isEmpty(str)) {
                                if (str.contains(input)) {
                                    searchData.add(object);
                                    keyword_map.put(searchData.size() - 1, input);
                                }
                            }
                        }
                    }

                    filterResults.values = searchData;
                    filterResults.count = searchData.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.values != null) {
                    show_list = (ArrayList<Contact>) results.values;
                    setNewData(show_list);
                }
            }
        };
        return filter;
    }

    @Override
    protected void convert(BaseViewHolder helper, Contact item) {
        ImageView imageView = helper.getView(R.id.contact_img);
        TextView textView = helper.getView(R.id.cb_contact);
        if (item.user_id > 0) {
            Glide.with(imageView.getContext()).load(AppUtil.getHeadUrl(item.user_id))
                    .placeholder(R.mipmap.nim_head).into(imageView);
        }

        helper.setText(R.id.tv_contact_num, Utils.highlight(item.contactNum, keyword_map.get(helper.getAdapterPosition())));
        helper.setText(R.id.tv_contact_name, Utils.highlight(item.contactName, keyword_map.get(helper.getAdapterPosition())));

        if (item.is_add) {
            textView.setText("已添加");
            textView.setTextColor(Color.parseColor("#706E6E"));
            textView.setBackgroundColor(Color.TRANSPARENT);
        } else {
            textView.setText("添加");
            textView.setTextColor(Color.parseColor("#7BCF54"));
            textView.setBackgroundResource(R.drawable.nim_invite_selector);
        }

        helper.addOnClickListener(R.id.cb_contact);
    }
}
