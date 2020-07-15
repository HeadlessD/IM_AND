package com.qbao.newim.adapter;

import android.text.TextUtils;
import android.view.View;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseViewHolder;
import com.qbao.newim.helper.GlideCircleTransform;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.model.IMOfficialInfo;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chenjian on 2017/8/31.
 */

public class SearchAdapter extends BaseQuickAdapter <Object, BaseViewHolder> implements Filterable {

    private List<Object> mList;
    private List<Object> show_list;
    private HashMap<Integer, Integer> pos_map;
    private HashMap<Integer, String> keyword_map;
    private static final int FRIEND_TYPE = 1;
    private static final int GROUP_TYPE = 2;
    private static final int OFFICIAL_TYPE = 3;

    public SearchAdapter(List<Object> data, List<Object> show_list) {
        super(R.layout.nim_friend_item, show_list);
        this.mList = data;
        pos_map = new HashMap<>();
    }

    @Override
    protected void convert(BaseViewHolder helper, Object item) {
        if (pos_map.size() == 0) {
            return;
        }
        ImageView img = helper.getView(R.id.iv_friend_head);
        TextView tv_title = helper.getView(R.id.tv_friends_name);
        TextView tv_subTitle = helper.getView(R.id.tv_friends_nick);
        if (item instanceof IMFriendInfo) {
            if (pos_map.get(FRIEND_TYPE) == null) {
                return;
            }
            if (helper.getLayoutPosition() == pos_map.get(FRIEND_TYPE)) {
                helper.getView(R.id.tv_friend_type).setVisibility(View.VISIBLE);
                helper.setText(R.id.tv_friend_type, "联系人");
            } else {
                helper.getView(R.id.tv_friend_type).setVisibility(View.GONE);
            }
            IMFriendInfo friend = (IMFriendInfo) item;
            Glide.with(mContext).load(AppUtil.getHeadUrl(friend.userId)).fitCenter().error(R.mipmap.nim_head_circle).
                    transform(new GlideCircleTransform(mContext)).into(img);
            if (!TextUtils.isEmpty(friend.remark_name)) {
                tv_title.setText(Utils.highlight(friend.remark_name, keyword_map.get(helper.getAdapterPosition())));
                if (!TextUtils.isEmpty(friend.nickName)) {
                    if (friend.nickName.contains(keyword_map.get(helper.getAdapterPosition()))) {
                        helper.getView(R.id.tv_friend_layout).setVisibility(View.VISIBLE);
                        tv_subTitle.setText(Utils.highlight(friend.nickName, keyword_map.get(helper.getAdapterPosition())));
                    } else {
                        helper.getView(R.id.tv_friend_layout).setVisibility(View.GONE);
                    }
                }
            } else {
                tv_title.setText(Utils.highlight(friend.nickName, keyword_map.get(helper.getAdapterPosition())));
                helper.getView(R.id.tv_friend_layout).setVisibility(View.GONE);
            }
        } else if (item instanceof IMGroupInfo) {
            if (pos_map.get(GROUP_TYPE) == null) {
                return;
            }
            if (helper.getLayoutPosition() == pos_map.get(GROUP_TYPE)) {
                helper.getView(R.id.tv_friend_type).setVisibility(View.VISIBLE);
                helper.setText(R.id.tv_friend_type, "群聊");
            } else {
                helper.getView(R.id.tv_friend_type).setVisibility(View.GONE);
            }
            IMGroupInfo group = (IMGroupInfo) item;
            Glide.with(mContext).load(AppUtil.getGroupUrl(group.group_id)).fitCenter().error(R.mipmap.nim_head_circle).
                    transform(new GlideCircleTransform(mContext)).into(img);
            tv_title.setText(Utils.highlight(group.group_name, keyword_map.get(helper.getAdapterPosition())));
            helper.getView(R.id.tv_friend_layout).setVisibility(View.GONE);
        } else if (item instanceof IMOfficialInfo) {
            if (pos_map.get(OFFICIAL_TYPE) == null) {
                return;
            }
            if (helper.getLayoutPosition() == pos_map.get(OFFICIAL_TYPE)) {
                helper.getView(R.id.tv_friend_type).setVisibility(View.VISIBLE);
                helper.setText(R.id.tv_friend_type, "公众号");
            } else {
                helper.getView(R.id.tv_friend_type).setVisibility(View.GONE);
            }
            IMOfficialInfo official = (IMOfficialInfo) item;
            Glide.with(mContext).load(official.official_url).fitCenter().error(R.mipmap.nim_head_circle).
                    transform(new GlideCircleTransform(mContext)).into(img);
            tv_title.setText(Utils.highlight(official.official_name, keyword_map.get(helper.getAdapterPosition())));
            helper.getView(R.id.tv_friend_layout).setVisibility(View.GONE);
        }
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                pos_map.clear();
                String input = constraint.toString();
                if (!TextUtils.isEmpty(input)) {
                    List<Object> searchData = new ArrayList<>();
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

                    for (Object object : mList) {
                        if (object instanceof IMFriendInfo) {
                            IMFriendInfo user_info = (IMFriendInfo)object;
                            if (!pos_map.containsKey(FRIEND_TYPE)) {
                                pos_map.put(FRIEND_TYPE, 0);
                            }
                            // 纯字母
                            if (is_letter) {
                                String keyword;
                                keyword = Utils.containInput(input.toUpperCase(), user_info.remark_name, user_info.remark_index);
                                if (TextUtils.isEmpty(keyword)) {
                                    keyword = Utils.containInput(input.toUpperCase(), user_info.nickName, user_info.nick_index);
                                }
                                if (!TextUtils.isEmpty(keyword)) {
                                    searchData.add(user_info);
                                    keyword_map.put(searchData.size() - 1, keyword);
                                }
                            // 非字母
                            } else {
                                String str = "";
                                if (!TextUtils.isEmpty(user_info.remark_name))
                                    str += user_info.remark_name;
                                if (!TextUtils.isEmpty(user_info.nickName))
                                    str += user_info.nickName;
                                if (!TextUtils.isEmpty(str)) {
                                    if (str.contains(input)) {
                                        searchData.add(user_info);
                                        keyword_map.put(searchData.size() - 1, input);
                                    }
                                }
                            }

                        } else if (object instanceof IMGroupInfo) {
                            IMGroupInfo group_info = (IMGroupInfo)object;
                            if (!pos_map.containsKey(GROUP_TYPE)) {
                                pos_map.put(GROUP_TYPE, searchData.size());
                            }

                            String keyword = Utils.containInput(input.toUpperCase(), group_info.group_name, group_info.name_pinyin);
                            if (!TextUtils.isEmpty(keyword)) {
                                searchData.add(group_info);
                                keyword_map.put(searchData.size() - 1, keyword);
                            } else {
                                if (!TextUtils.isEmpty(group_info.group_name)) {
                                    if (group_info.group_name.contains(input)) {
                                        searchData.add(group_info);
                                        keyword_map.put(searchData.size() - 1, input);
                                    }
                                }
                            }

                        } else if (object instanceof IMOfficialInfo) {
                            IMOfficialInfo official_info = (IMOfficialInfo)object;
                            if (!pos_map.containsKey(OFFICIAL_TYPE)) {
                                pos_map.put(OFFICIAL_TYPE, searchData.size());
                            }

                            String keyword = Utils.containInput(input.toUpperCase(), official_info.official_name, official_info.name_pinyin);
                            if (!TextUtils.isEmpty(keyword)) {
                                searchData.add(official_info);
                                keyword_map.put(searchData.size() - 1, keyword);
                            } else {
                                if (!TextUtils.isEmpty(official_info.official_name)) {
                                    if (official_info.official_name.contains(input)) {
                                        searchData.add(official_info);
                                        keyword_map.put(searchData.size() - 1, input);
                                    }
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
                    show_list = (ArrayList<Object>) results.values;
                    setNewData(show_list);
                }
            }
        };
        return filter;
    }
}
