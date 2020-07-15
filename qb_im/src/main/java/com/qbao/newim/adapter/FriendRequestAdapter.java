package com.qbao.newim.adapter;

import android.graphics.Color;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseViewHolder;
import com.qbao.newim.constdef.FriendTypeDef;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chenjian on 2017/6/7.
 */

public class FriendRequestAdapter extends BaseQuickAdapter<IMFriendInfo, BaseViewHolder>{

    public HashMap<Long, IMFriendInfo> adapter_map = new HashMap();

    public FriendRequestAdapter(ArrayList<IMFriendInfo> data) {
        super(R.layout.nim_friend_request_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, IMFriendInfo item) {
        Glide.with(helper.getConvertView().getContext()).load(AppUtil.getHeadUrl(item.userId)).placeholder(R.mipmap.nim_head)
                .into((ImageView) helper.getView(R.id.friend_request_avatar));
        String show_name = Utils.getUserShowName(new String[]{item.nickName, item.user_name});
        helper.setText(R.id.friend_request_friend_name, show_name);

        helper.getView(R.id.friend_request_state_layout).setEnabled(true);
        helper.setText(R.id.friend_request_state_txt, "同意");
        helper.setBackgroundRes(R.id.friend_request_state_txt, R.drawable.nim_invite_selector);
        helper.setTextColor(R.id.friend_request_state_txt, Color.parseColor("#7bcf54"));
        helper.setText(R.id.friend_request_last_msg_content, item.opt_msg);
        helper.addOnClickListener(R.id.friend_request_state_layout);

        if (item.is_select) {
            helper.getView(R.id.friend_request_state_layout).setEnabled(true);
            helper.setText(R.id.friend_request_state_txt, "添加");
            helper.setBackgroundRes(R.id.friend_request_state_txt, R.drawable.nim_invite_selector);
            helper.setTextColor(R.id.friend_request_state_txt, Color.parseColor("#edb23f"));
            helper.setText(R.id.friend_request_last_msg_content, item.opt_msg);
            helper.addOnClickListener(R.id.friend_request_state_layout);
        } else {
            switch (item.status) {
                case FriendTypeDef.FRIEND_ADD_TYPE.ACCEPT_REQUEST:        // 有人加我为好友，是否同意
                    helper.getView(R.id.friend_request_state_layout).setEnabled(true);
                    helper.setText(R.id.friend_request_state_txt, "同意");
                    helper.setBackgroundRes(R.id.friend_request_state_txt, R.drawable.nim_invite_selector);
                    helper.setTextColor(R.id.friend_request_state_txt, Color.parseColor("#7bcf54"));
                    helper.setText(R.id.friend_request_last_msg_content, item.opt_msg);
                    helper.addOnClickListener(R.id.friend_request_state_layout);
                    break;
                case FriendTypeDef.FRIEND_ADD_TYPE.OWN_CONFIRM:           // 同意成为好友
                case FriendTypeDef.FRIEND_ADD_TYPE.PEER_CONFIRM:
                case FriendTypeDef.FRIEND_ADD_TYPE.RESTART_ADD:
                    helper.getView(R.id.friend_request_state_layout).setEnabled(false);
                    helper.setText(R.id.friend_request_state_txt, "已添加");
                    helper.setBackgroundRes(R.id.friend_request_state_txt, android.R.color.transparent);
                    helper.setTextColor(R.id.friend_request_state_txt, Color.parseColor("#bbbbbb"));
                    helper.setText(R.id.friend_request_last_msg_content, item.opt_msg);
                    break;
                case FriendTypeDef.FRIEND_ADD_TYPE.TIME_OUT:               // 好友请求已过期
                    helper.getView(R.id.friend_request_state_layout).setEnabled(false);
                    helper.setText(R.id.friend_request_state_txt, "已过期");
                    helper.setBackgroundRes(R.id.friend_request_state_txt, android.R.color.transparent);
                    helper.setTextColor(R.id.friend_request_state_txt, Color.parseColor("#bbbbbb"));
                    helper.setText(R.id.friend_request_last_msg_content, item.opt_msg);
                    break;
            }
        }

        if (!adapter_map.containsKey(item.userId)) {
            adapter_map.put(item.userId, item);
        }
    }

    public boolean contains(long user_id) {
        return adapter_map.containsKey(user_id);
    }

    public void removeSession(long user_id) {
        adapter_map.remove(user_id);
    }
}
