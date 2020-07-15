package com.qbao.newim.views.imgpicker;

import android.view.ViewGroup;
import android.widget.CompoundButton;

/**
 * Created by chenjian on 2017/5/2.
 */

public interface NIM_OnItemChildCheckedChangeListener {
    void onItemChildCheckedChanged(ViewGroup parent, CompoundButton childView, int position, boolean isChecked);
}
