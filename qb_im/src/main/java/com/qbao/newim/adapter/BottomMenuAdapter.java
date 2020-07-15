package com.qbao.newim.adapter;

import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseViewHolder;
import com.qbao.newim.model.MenuMsgItem;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.SharedPreferenceUtil;

import java.util.List;

import static com.qbao.newim.qbim.R.id.chat_bottom_layout_item;

/**
 * Created by chenjian on 2017/3/28.
 */

public class BottomMenuAdapter extends BaseQuickAdapter<MenuMsgItem, BaseViewHolder> {

    public BottomMenuAdapter(List<MenuMsgItem> data) {
        super(R.layout.nim_chat_bottom_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, final MenuMsgItem item) {
        if(item.Item_Icon_Id != 0){
            helper.setImageResource(R.id.chat_bottom_layout_item_icon, item.Item_Icon_Id);
            helper.setVisible(chat_bottom_layout_item, true);
        }else{
            helper.setVisible(chat_bottom_layout_item, false);
        }

        if(item.Item_Name_Id != 0){
            helper.setText(R.id.chat_bottom_layout_item_text, item.Item_Name_Id);
        }

        int real_keyH = SharedPreferenceUtil.getKeyBoardHeight(helper.getConvertView().getContext().getResources().getDimensionPixelSize(R.dimen.chat_bottom_min_height));
        helper.getView(R.id.chat_bottom_layout_item).getLayoutParams().height = real_keyH / 2;
    }

}
