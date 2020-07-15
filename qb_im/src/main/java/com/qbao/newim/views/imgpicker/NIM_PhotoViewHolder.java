package com.qbao.newim.views.imgpicker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by chenjian on 2017/5/2.
 */

public class NIM_PhotoViewHolder {
    protected View mConvertView;
    protected NIM_ViewHolderHelper mViewHolderHelper;

    private NIM_PhotoViewHolder(ViewGroup parent, int layoutId) {
        mConvertView = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        mConvertView.setTag(this);
        mViewHolderHelper = new NIM_ViewHolderHelper(parent, mConvertView);
    }

    /**
     * 拿到一个可重用的ViewHolder对象
     *
     * @param convertView
     * @param parent
     * @param layoutId
     * @return
     */
    public static NIM_PhotoViewHolder dequeueReusableAdapterViewHolder(View convertView, ViewGroup parent, int layoutId) {
        if (convertView == null) {
            return new NIM_PhotoViewHolder(parent, layoutId);
        }
        return (NIM_PhotoViewHolder) convertView.getTag();
    }

    public NIM_ViewHolderHelper getViewHolderHelper() {
        return mViewHolderHelper;
    }

    public View getConvertView() {
        return mConvertView;
    }
}
