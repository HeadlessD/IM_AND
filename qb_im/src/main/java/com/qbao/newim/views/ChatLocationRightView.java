package com.qbao.newim.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.qbao.newim.adapter.ChatAdapter;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.ShowUtils;

/**
 * Created by chenjian on 2017/7/18.
 */

public class ChatLocationRightView extends ChatLocationView {
    public ChatLocationRightView(Context context) {
        super(context);
    }

    public ChatLocationRightView(Context context, ChatAdapter adapter) {
        super(context, adapter);
    }

    public ChatLocationRightView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getTemplateLayoutId() {
        return R.layout.nim_chat_send_template;
    }

    @Override
    public int getLayoutId(){
        return R.layout.nim_chat_map_self_layout;
    }

    @Override
    public void onClick(View v) {
        if (lat > 0 || lon > 0) {
            mActivity.requestLocationPermission(lat, lon, address);
        } else {
            ShowUtils.showToast("无效位置");
        }
    }
}
