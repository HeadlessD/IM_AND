package com.qbao.newim.adapter.adapterhelper;

import com.qbao.newim.qbim.R;

/**
 * Created by chenjian on 2017/3/22.
 * 提供一个简单的模板，这里可以修改加载样式
 */

public final class SimpleLoadMoreView extends LoadMoreView {

    @Override public int getLayoutId() {
        return R.layout.quick_view_load_more;
    }

    @Override protected int getLoadingViewId() {
        return R.id.load_more_loading_view;
    }

    @Override protected int getLoadFailViewId() {
        return R.id.load_more_load_fail_view;
    }

    @Override protected int getLoadEndViewId() {
        return R.id.load_more_load_end_view;
    }
}
