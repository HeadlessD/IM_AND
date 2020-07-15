package com.qbao.newim.views.imgpicker;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.qbao.newim.qbim.R;

import java.util.ArrayList;

/**
 * Created by chenjian on 2017/5/2.
 */

public class NIM_PhotoPickerAdapter extends NIM_RecyclerViewAdapter<String> {
    private ArrayList<String> mSelectedImages = new ArrayList<>();
    private int mImageSize;
    private boolean mTakePhotoEnabled;
    private boolean bSingle;

    public NIM_PhotoPickerAdapter(RecyclerView recyclerView, boolean single) {
        super(recyclerView, R.layout.nim_pp_item_photo_picker);
        mImageSize = NIM_PhotoPickerUtil.getScreenWidth() / 6;
        this.bSingle = single;
    }

    @Override
    public int getItemViewType(int position) {
        if (mTakePhotoEnabled && position == 0) {
            return R.layout.nim_pp_item_photo_camera;
        } else {
            return R.layout.nim_pp_item_photo_picker;
        }
    }

    @Override
    public void setItemChildListener(NIM_ViewHolderHelper helper, int viewType) {
        if (viewType == R.layout.nim_pp_item_photo_camera) {
            helper.setItemChildClickListener(R.id.iv_item_photo_camera_camera);
        } else {
            helper.setItemChildClickListener(R.id.iv_item_photo_picker_flag);
            helper.setItemChildClickListener(R.id.iv_item_photo_picker_photo);
        }
    }

    @Override
    protected void fillData(NIM_ViewHolderHelper helper, int position, String model) {
        if (getItemViewType(position) == R.layout.nim_pp_item_photo_picker) {
            NIM_Image.display(helper.getImageView(R.id.iv_item_photo_picker_photo), R.mipmap.nim_pp_ic_holder_dark, model, mImageSize);

            if (bSingle) {
                helper.getView(R.id.iv_item_photo_picker_flag).setVisibility(View.GONE);
                helper.getImageView(R.id.iv_item_photo_picker_photo).setColorFilter(null);
            } else {
                helper.getView(R.id.iv_item_photo_picker_flag).setVisibility(View.VISIBLE);
                if (mSelectedImages.contains(model)) {
                    helper.setImageResource(R.id.iv_item_photo_picker_flag, R.mipmap.nim_pp_ic_cb_checked);
                    helper.getImageView(R.id.iv_item_photo_picker_photo).setColorFilter(ContextCompat.getColor(
                            helper.getConvertView().getContext(), R.color.nim_pp_photo_selected_mask));
                    helper.getView(R.id.v_picker).setVisibility(View.VISIBLE);
                } else {
                    helper.setImageResource(R.id.iv_item_photo_picker_flag, R.mipmap.nim_pp_ic_cb_normal);
                    helper.getImageView(R.id.iv_item_photo_picker_photo).setColorFilter(null);
                    helper.getView(R.id.v_picker).setVisibility(View.GONE);
                }
            }

        }
    }

    public void setSelectedImages(ArrayList<String> selectedImages) {
        if (selectedImages != null) {
            mSelectedImages = selectedImages;
        }
        notifyDataSetChanged();
    }

    public ArrayList<String> getSelectedImages() {
        return mSelectedImages;
    }

    public int getSelectedCount() {
        return mSelectedImages.size();
    }

    public void setImageFolderModel(NIM_ImageFolderModel imageFolderModel) {
        mTakePhotoEnabled = imageFolderModel.isTakePhotoEnabled();
        setData(imageFolderModel.getImages());
    }
}
