package com.qbao.newim.model;

import android.content.Context;
import android.view.View;

/**
 * Created by chenjian on 2017/4/12.
 */

public interface InitViews {
    /**
     * 初始化控件
     *
     * @param context
     */
    public void initViews(Context context, View root);

    /**
     * 给控件绑定事件
     */
    public void bindListener();

    /**
     * 给控件初始化数据
     */
    public void initData();

    /**
     * layout id
     * @return
     */
    public int getLayoutId();
}
