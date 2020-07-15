package com.qbao.newim.adapter;

import android.text.TextUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseViewHolder;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenjian on 2017/9/1.
 */

public class SearchGroupAdapter extends BaseQuickAdapter<IMGroupInfo, BaseViewHolder> implements Filterable {

    private String keyword;
    private List<IMGroupInfo> mList;
    private List<IMGroupInfo> show_list;

    public SearchGroupAdapter(List<IMGroupInfo> data, List<IMGroupInfo> show_list) {
        super(R.layout.nim_group_item, show_list);
        this.mList = data;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (!TextUtils.isEmpty(constraint)) {
                    List<Object> searchData = new ArrayList<>();
                    keyword = constraint.toString();

                    for (IMGroupInfo object : mList) {
                        String str = "";
                        if (!TextUtils.isEmpty(object.group_name))
                            str += "" + object.group_name;
                        if (!TextUtils.isEmpty(str)) {
                            if (str.contains(constraint.toString().toUpperCase())) {
                                searchData.add(object);
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
                    show_list = (ArrayList<IMGroupInfo>) results.values;
                    setNewData(show_list);
                }
            }
        };
        return filter;
    }

    @Override
    protected void convert(BaseViewHolder helper, IMGroupInfo item) {
        Glide.with(helper.getConvertView().getContext()).load(item.group_img_url).placeholder(R.mipmap.nim_head)
                .into((ImageView) helper.getView(R.id.iv_group_head));

        helper.setText(R.id.tv_group_name, Utils.highlight(item.group_name, keyword));
        helper.setText(R.id.tv_group_member_count, item.group_count + "人");
        helper.addOnClickListener(R.id.iv_group_head);
    }
}
