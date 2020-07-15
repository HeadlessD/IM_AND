package com.qbao.newim.adapter;

import android.view.View;

import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseViewHolder;
import com.qbao.newim.qbim.R;

import java.util.ArrayList;

/**
 * Created by chenjian on 2017/6/8.
 */

public class DialogListAdapter extends BaseQuickAdapter<String, BaseViewHolder>{
    public DialogListAdapter(ArrayList<String> data) {
        super(R.layout.nim_dialog_list_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        if (helper.getLayoutPosition() == 0) {
            helper.getView(R.id.dialog_list_divider).setVisibility(View.GONE);
        } else {
            helper.getView(R.id.dialog_list_divider).setVisibility(View.VISIBLE);
        }

        if (item.equals("取消")) {
            helper.getView(R.id.dialog_list_layout).setBackgroundResource(R.drawable.nim_dialog_list_cancel);
        } else {
            helper.getView(R.id.dialog_list_layout).setBackgroundResource(R.drawable.nim_dialog_item_selector);
        }

        helper.setText(R.id.dialog_list_text, item);
    }
}
