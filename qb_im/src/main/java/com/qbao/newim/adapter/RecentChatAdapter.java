package com.qbao.newim.adapter;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseViewHolder;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.manager.NIMComplexManager;
import com.qbao.newim.model.SessionModel;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.Utils;

import java.util.HashMap;
import java.util.List;


/**
 * Created by chenjian on 2017/7/19.
 */

public class RecentChatAdapter extends BaseQuickAdapter<SessionModel, BaseViewHolder> {

    private HashMap<Integer, String> keyword_map;

    public RecentChatAdapter(List<SessionModel> data) {
        super(R.layout.nim_recent_chat_item, data);
        keyword_map = new HashMap<>();
    }

    public void setKeyword_map(HashMap<Integer, String> keyword_map) {
        this.keyword_map = keyword_map;
    }

    @Override
    protected void convert(BaseViewHolder helper, SessionModel item) {
        if (item.chat_type == MsgConstDef.MSG_CHAT_TYPE.PRIVATE) {
            Glide.with(helper.getConvertView().getContext()).load(AppUtil.getHeadUrl(item.session_id)).placeholder(R.mipmap.nim_head)
                    .into((ImageView) helper.getView(R.id.item_recent_head));
        } else {
            Glide.with(helper.getConvertView().getContext()).load(AppUtil.getGroupUrl(item.session_id)).placeholder(R.mipmap.nim_head)
                    .into((ImageView) helper.getView(R.id.item_recent_head));
        }

        String session_name = NIMComplexManager.getInstance().GetSessionName(item.session_id, item.chat_type);
        helper.setText(R.id.item_recent_name, Utils.highlight(session_name,
                keyword_map.get(helper.getAdapterPosition() - getHeaderLayoutCount())));
    }
}
