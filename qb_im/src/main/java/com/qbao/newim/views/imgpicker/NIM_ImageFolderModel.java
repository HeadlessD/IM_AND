package com.qbao.newim.views.imgpicker;

import java.util.ArrayList;

/**
 * Created by chenjian on 2017/5/2.
 */

public class NIM_ImageFolderModel {
    public String name;
    public String coverPath;
    private ArrayList<String> mImages = new ArrayList<>();
    private boolean mTakePhotoEnabled;

    public NIM_ImageFolderModel(boolean takePhotoEnabled) {
        mTakePhotoEnabled = takePhotoEnabled;
        if (takePhotoEnabled) {
            // 拍照
            mImages.add("");
        }
    }

    public NIM_ImageFolderModel(String name, String coverPath) {
        this.name = name;
        this.coverPath = coverPath;
    }

    public boolean isTakePhotoEnabled() {
        return mTakePhotoEnabled;
    }

    public void addLastImage(String imagePath) {
        mImages.add(imagePath);
    }

    public ArrayList<String> getImages() {
        return mImages;
    }

    public int getCount() {
        return mTakePhotoEnabled ? mImages.size() - 1 : mImages.size();
    }
}
