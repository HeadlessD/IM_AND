package com.qbao.newim.views.imgpicker;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by chenjian on 2017/5/2.
 */

public class NIM_RecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener{
    protected Context mContext;
    protected NIM_OnRVItemClickListener mOnRVItemClickListener;
    protected NIM_OnRVItemLongClickListener mOnRVItemLongClickListener;
    protected NIM_ViewHolderHelper mViewHolderHelper;
    protected RecyclerView mRecyclerView;
    protected NIM_RecyclerViewAdapter mRecyclerViewAdapter;

    public NIM_RecyclerViewHolder(NIM_RecyclerViewAdapter recyclerViewAdapter, RecyclerView recyclerView, View itemView, NIM_OnRVItemClickListener onRVItemClickListener, NIM_OnRVItemLongClickListener onRVItemLongClickListener) {
        super(itemView);
        mRecyclerViewAdapter = recyclerViewAdapter;
        mRecyclerView = recyclerView;
        mContext = mRecyclerView.getContext();
        mOnRVItemClickListener = onRVItemClickListener;
        mOnRVItemLongClickListener = onRVItemLongClickListener;
        itemView.setOnClickListener(new NIM_OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                if (v.getId() == NIM_RecyclerViewHolder.this.itemView.getId() && null != mOnRVItemClickListener) {
                    mOnRVItemClickListener.onRVItemClick(mRecyclerView, v, getAdapterPositionWrapper());
                }
            }
        });
        itemView.setOnLongClickListener(this);

        mViewHolderHelper = new NIM_ViewHolderHelper(mRecyclerView, this);
    }

    public NIM_ViewHolderHelper getViewHolderHelper() {
        return mViewHolderHelper;
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == this.itemView.getId() && null != mOnRVItemLongClickListener) {
            return mOnRVItemLongClickListener.onRVItemLongClick(mRecyclerView, v, getAdapterPositionWrapper());
        }
        return false;
    }

    public int getAdapterPositionWrapper() {
        if (mRecyclerViewAdapter.getHeadersCount() > 0) {
            return getAdapterPosition() - mRecyclerViewAdapter.getHeadersCount();
        } else {
            return getAdapterPosition();
        }
    }
}
