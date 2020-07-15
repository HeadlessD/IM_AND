package com.qbao.newim.views.imgpicker;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.qbao.newim.qbim.R;
import com.qbao.newim.views.imgpicker.photoview.PhotoViewAttacher;

import java.util.ArrayList;

/**
 * Created by chenjian on 2017/5/2.
 */

public class NIM_PhotoPageAdapter extends PagerAdapter {
    private ArrayList<String> mPreviewImages;
    private PhotoViewAttacher.OnViewTapListener mOnViewTapListener;
    private Activity mActivity;

    public NIM_PhotoPageAdapter(Activity activity, PhotoViewAttacher.OnViewTapListener onViewTapListener, ArrayList<String> previewImages) {
        mOnViewTapListener = onViewTapListener;
        mPreviewImages = previewImages;
        mActivity = activity;
    }

    @Override
    public int getCount() {
        return mPreviewImages == null ? 0 : mPreviewImages.size();
    }

    @Override
    public View instantiateItem(ViewGroup container, int position) {
        final NIM_ImageView imageView = new NIM_ImageView(container.getContext());
        container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        final NIM_BrowserPhotoViewAttacher photoViewAttacher = new NIM_BrowserPhotoViewAttacher(imageView);
        photoViewAttacher.setOnViewTapListener(mOnViewTapListener);
        imageView.setDelegate(new NIM_ImageView.Delegate() {
            @Override
            public void onDrawableChanged(Drawable drawable) {
                if (drawable != null && drawable.getIntrinsicHeight() > drawable.getIntrinsicWidth() && drawable.getIntrinsicHeight() > NIM_PhotoPickerUtil.getScreenHeight()) {
                    photoViewAttacher.setIsSetTopCrop(true);
                    photoViewAttacher.setUpdateBaseMatrix();
                } else {
                    photoViewAttacher.update();
                }
            }
        });

        NIM_Image.display(imageView, R.mipmap.nim_pp_ic_holder_dark, mPreviewImages.get(position), NIM_PhotoPickerUtil.getScreenWidth(), NIM_PhotoPickerUtil.getScreenHeight());

        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public String getItem(int position) {
        return mPreviewImages == null ? "" : mPreviewImages.get(position);
    }
}