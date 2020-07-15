package com.qbao.newim.adapter;

import android.text.TextUtils;
import android.view.View;

import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseViewHolder;
import com.qbao.newim.model.NIMAddressInfo;
import com.qbao.newim.qbim.R;

import java.util.List;

/**
 * Created by chenjian on 2017/7/18.
 */

public class MapAddressAdapter extends BaseQuickAdapter <NIMAddressInfo, BaseViewHolder>{

    public MapAddressAdapter(List<NIMAddressInfo> data) {
        super(R.layout.nim_address_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, NIMAddressInfo item) {
        if (item.is_select) {
            helper.getView(R.id.map_address_item_radio).setVisibility(View.VISIBLE);
        } else {
            helper.getView(R.id.map_address_item_radio).setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(item.address)) {
            helper.getView(R.id.map_address_item_detail).setVisibility(View.GONE);
        } else {
            helper.getView(R.id.map_address_item_detail).setVisibility(View.VISIBLE);
            helper.setText(R.id.map_address_item_detail, item.address);
        }

        helper.setText(R.id.map_address_item_title, item.name);
    }
}
