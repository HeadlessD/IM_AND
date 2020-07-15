package com.qbao.newim.adapter.adapterhelper;

import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;

/**
 * Created by chenjian on 2017/3/22.
 * 加载更多数据
 */

public abstract class LoadMoreView {

    public static final int STATUS_DEFAULT = 1;  // 默认状态
    public static final int STATUS_LOADING = 2;  // 正在加载
    public static final int STATUS_FAIL = 3;     // 加载失败
    public static final int STATUS_END = 4;      // 加载完成

    private int mLoadMoreStatus = STATUS_DEFAULT;
    private boolean mLoadMoreEndGone = false;

    public void setLoadMoreStatus(int loadMoreStatus) {
        this.mLoadMoreStatus = loadMoreStatus;
    }

    public int getLoadMoreStatus() {
        return mLoadMoreStatus;
    }

    public void convert(BaseViewHolder holder) {
        switch (mLoadMoreStatus) {
            case STATUS_LOADING:                      // 加载中：进度条可见，失败和完成不可见
                visibleLoading(holder, true);
                visibleLoadFail(holder, false);
                visibleLoadEnd(holder, false);
                break;
            case STATUS_FAIL:
                visibleLoading(holder, false);
                visibleLoadFail(holder, true);
                visibleLoadEnd(holder, false);
                break;
            case STATUS_END:
                visibleLoading(holder, false);
                visibleLoadFail(holder, false);
                visibleLoadEnd(holder, true);
                break;
            case STATUS_DEFAULT:
                visibleLoading(holder, false);
                visibleLoadFail(holder, false);
                visibleLoadEnd(holder, false);
                break;
        }
    }

    private void visibleLoading(BaseViewHolder holder, boolean visible) {
        holder.setVisible(getLoadingViewId(), visible);
    }

    private void visibleLoadFail(BaseViewHolder holder, boolean visible) {
        holder.setVisible(getLoadFailViewId(), visible);
    }

    private void visibleLoadEnd(BaseViewHolder holder, boolean visible) {
        final int loadEndViewId=getLoadEndViewId();
        if (loadEndViewId != 0) {
            holder.setVisible(loadEndViewId, visible);
        }
    }

    public final void setLoadMoreEndGone(boolean loadMoreEndGone) {
        this.mLoadMoreEndGone = loadMoreEndGone;
    }

    public final boolean isLoadEndMoreGone(){
        if(getLoadEndViewId()==0){
            return true;
        }
        return mLoadMoreEndGone;}

    /**
     * 是否加载到底了
     */
    @Deprecated
    public boolean isLoadEndGone(){return mLoadMoreEndGone;}

    /**
     * 获取加载进度布局
     *
     * @return
     */
    public abstract @LayoutRes
    int getLayoutId();

    /**
     * 获取加载View
     */
    protected abstract @IdRes
    int getLoadingViewId();

    /**
     * 加载失败的View
     */
    protected abstract @IdRes int getLoadFailViewId();

    /**
     * 加载完成后的View，可以直接返回0
     *
     * @return
     */
    protected abstract @IdRes int getLoadEndViewId();
}
