package com.qbao.newim.views.imgpicker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qbao.newim.permission.AndPermission;
import com.qbao.newim.qbim.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by chenjian on 2017/5/2.
 */

public class NIM_PhotoPickerAct extends NIM_ToolbarAct implements NIM_OnItemChildClickListener, NIM_AsyncTask.Callback<ArrayList<NIM_ImageFolderModel>>{
    private static final String EXTRA_IMAGE_DIR = "EXTRA_IMAGE_DIR";
    private static final String EXTRA_SELECTED_IMAGES = "EXTRA_SELECTED_IMAGES";
    private static final String EXTRA_MAX_CHOOSE_COUNT = "EXTRA_MAX_CHOOSE_COUNT";
    private static final String EXTRA_PAUSE_ON_SCROLL = "EXTRA_PAUSE_ON_SCROLL";

    /**
     * 拍照的请求码
     */
    private static final int REQUEST_CODE_TAKE_PHOTO = 1;
    /**
     * 预览照片的请求码
     */
    private static final int REQUEST_CODE_PREVIEW = 2;

    private static final int SPAN_COUNT = 3;

    private TextView mTitleTv;
    private ImageView mArrowIv;
    private TextView mSubmitTv;
    private RecyclerView mContentRv;

    private NIM_ImageFolderModel mCurrentImageFolderModel;

    /**
     * 是否可以拍照
     */
    private boolean mTakePhotoEnabled;
    /**
     * 最多选择多少张图片，默认等于1，为单选
     */
    private int mMaxChooseCount = 1;
    /**
     * 右上角按钮文本
     */
    private String mTopRightBtnText;
    /**
     * 图片目录数据集合
     */
    private ArrayList<NIM_ImageFolderModel> mImageFolderModels;

    private NIM_PhotoPickerAdapter mPicAdapter;

    private NIM_ImageCaptureManager mImageCaptureManager;

    private NIM_PhotoFolderPw mPhotoFolderPw;

    private NIM_LoadPhotoTask mLoadPhotoTask;
    private AppCompatDialog mLoadingDialog;

    private NIM_OnNoDoubleClickListener mOnClickShowPhotoFolderListener = new NIM_OnNoDoubleClickListener() {
        @Override
        public void onNoDoubleClick(View v) {
            if (mImageFolderModels != null && mImageFolderModels.size() > 0) {
                showPhotoFolderPw();
            }
        }
    };

    /**
     * @param context        应用程序上下文
     * @param imageDir       拍照后图片保存的目录。如果传null表示没有拍照功能，如果不为null则具有拍照功能，
     * @param maxChooseCount 图片选择张数的最大值
     * @param selectedImages 当前已选中的图片路径集合，可以传null
     * @param pauseOnScroll  滚动列表时是否暂停加载图片
     * @return
     */
    public static Intent newIntent(Context context, File imageDir, int maxChooseCount, ArrayList<String> selectedImages, boolean pauseOnScroll) {
        Intent intent = new Intent(context, NIM_PhotoPickerAct.class);
        intent.putExtra(EXTRA_IMAGE_DIR, imageDir);
        intent.putExtra(EXTRA_MAX_CHOOSE_COUNT, maxChooseCount);
        intent.putStringArrayListExtra(EXTRA_SELECTED_IMAGES, selectedImages);
        intent.putExtra(EXTRA_PAUSE_ON_SCROLL, pauseOnScroll);
        return intent;
    }

    /**
     * 获取已选择的图片集合
     *
     * @param intent
     * @return
     */
    public static ArrayList<String> getSelectedImages(Intent intent) {
        return intent.getStringArrayListExtra(EXTRA_SELECTED_IMAGES);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.nim_pp_activity_photo_picker);
        mContentRv = getViewById(R.id.rv_photo_picker_content);
        mMaxChooseCount = getIntent().getIntExtra(EXTRA_MAX_CHOOSE_COUNT, 1);
    }

    @Override
    protected void setListener() {
        mPicAdapter = new NIM_PhotoPickerAdapter(mContentRv, mMaxChooseCount > 1 ? false : true);
        mPicAdapter.setOnItemChildClickListener(this);

        if (getIntent().getBooleanExtra(EXTRA_PAUSE_ON_SCROLL, false)) {
            mContentRv.addOnScrollListener(new NIM_RVOnScrollListener(this));
        }
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        // 获取拍照图片保存目录
        File imageDir = (File) getIntent().getSerializableExtra(EXTRA_IMAGE_DIR);
        if (imageDir != null) {
            mTakePhotoEnabled = true;
            mImageCaptureManager = new NIM_ImageCaptureManager(imageDir);
            takePhoto();
        }
        // 获取图片选择的最大张数
        if (mMaxChooseCount < 1) {
            mMaxChooseCount = 1;
        }

        // 获取右上角按钮文本
        mTopRightBtnText = getString(R.string.nim_pp_confirm);

        GridLayoutManager layoutManager = new GridLayoutManager(this, SPAN_COUNT, LinearLayoutManager.VERTICAL, false);
        mContentRv.setLayoutManager(layoutManager);
        mContentRv.addItemDecoration(new NIM_SpaceItemDecoration(getResources().getDimensionPixelSize(R.dimen.nim_pp_size_photo_divider)));

        ArrayList<String> selectedImages = getIntent().getStringArrayListExtra(EXTRA_SELECTED_IMAGES);
        if (selectedImages != null && selectedImages.size() > mMaxChooseCount) {
            String selectedPhoto = selectedImages.get(0);
            selectedImages.clear();
            selectedImages.add(selectedPhoto);
        }

        mContentRv.setAdapter(mPicAdapter);
        mPicAdapter.setSelectedImages(selectedImages);
    }

    @Override
    protected void onStart() {
        super.onStart();
        showLoadingDialog();
        mLoadPhotoTask = new NIM_LoadPhotoTask(this, this, mTakePhotoEnabled).perform();
    }

    private void showLoadingDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = new AppCompatDialog(this);
            mLoadingDialog.setContentView(R.layout.nim_pp_dialog_loading);
            mLoadingDialog.setCancelable(false);
        }
        mLoadingDialog.show();
    }

    private void dismissLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nim_pp_menu_photo_picker, menu);
        MenuItem menuItem = menu.findItem(R.id.item_photo_picker_title);
        View actionView = menuItem.getActionView();

        mTitleTv = (TextView) actionView.findViewById(R.id.tv_photo_picker_title);
        mArrowIv = (ImageView) actionView.findViewById(R.id.iv_photo_picker_arrow);
        mSubmitTv = (TextView) actionView.findViewById(R.id.tv_photo_picker_submit);

        mTitleTv.setOnClickListener(mOnClickShowPhotoFolderListener);
        mArrowIv.setOnClickListener(mOnClickShowPhotoFolderListener);

        if (mMaxChooseCount == 1) {
            mSubmitTv.setVisibility(View.GONE);
        }

        mSubmitTv.setOnClickListener(new NIM_OnNoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                returnSelectedImages(mPicAdapter.getSelectedImages());
            }
        });

        actionView.findViewById(R.id.title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mTitleTv.setText(R.string.nim_pp_all_image);
        if (mCurrentImageFolderModel != null) {
            mTitleTv.setText(mCurrentImageFolderModel.name);
        }

        renderTopRightBtn();

        return true;
    }

    /**
     * 返回已选中的图片集合
     *
     * @param selectedImages
     */
    private void returnSelectedImages(ArrayList<String> selectedImages) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(EXTRA_SELECTED_IMAGES, selectedImages);
        setResult(RESULT_OK, intent);
        if (mTakePhotoEnabled) {
            finish();
        } else {
            onBackPressed();
        }
    }

    private void showPhotoFolderPw() {
        if (mPhotoFolderPw == null) {
            mPhotoFolderPw = new NIM_PhotoFolderPw(this, mToolbar, new NIM_PhotoFolderPw.Delegate() {
                @Override
                public void onSelectedFolder(int position) {
                    reloadPhotos(position);
                }

                @Override
                public void executeDismissAnim() {
                    ViewCompat.animate(mArrowIv).setDuration(NIM_PhotoFolderPw.ANIM_DURATION).rotation(0).start();
                }
            });
        }
        mPhotoFolderPw.setData(mImageFolderModels);
        mPhotoFolderPw.show();

        ViewCompat.animate(mArrowIv).setDuration(NIM_PhotoFolderPw.ANIM_DURATION).rotation(-180).start();
    }

    /**
     * 显示只能选择 mMaxChooseCount 张图的提示
     */
    private void toastMaxCountTip() {
        NIM_PhotoPickerUtil.show(getString(R.string.nim_pp_toast_photo_picker_max, mMaxChooseCount));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_TAKE_PHOTO) {
                ArrayList<String> photos = new ArrayList<>();
                photos.add(mImageCaptureManager.getCurrentPhotoPath());
                returnSelectedImages(photos);
//                startActivityForResult(NIM_PhotoPreviewAct.newIntent(this, 1, photos, photos, 0, true), REQUEST_CODE_PREVIEW);
            } else if (requestCode == REQUEST_CODE_PREVIEW) {
                if (NIM_PhotoPreviewAct.getIsFromTakePhoto(data)) {
                    // 从拍照预览界面返回，刷新图库
                    mImageCaptureManager.refreshGallery();
                }

                mSubmitTv.setText("确定");
                mSubmitTv.setEnabled(false);
                returnSelectedImages(NIM_PhotoPreviewAct.getSelectedImages(data));
            }
        } else if (resultCode == RESULT_CANCELED && requestCode == REQUEST_CODE_PREVIEW) {
            if (NIM_PhotoPreviewAct.getIsFromTakePhoto(data)) {
                // 从拍照预览界面返回，删除之前拍的照片
                mImageCaptureManager.deletePhotoFile();
            } else {
                mPicAdapter.setSelectedImages(NIM_PhotoPreviewAct.getSelectedImages(data));
                renderTopRightBtn();

            }
        } else if (resultCode == RESULT_CANCELED && requestCode == REQUEST_CODE_TAKE_PHOTO){
            finish();
        }
    }

    /**
     * 渲染右上角按钮
     */
    private void renderTopRightBtn() {
        if (mPicAdapter.getSelectedCount() == 0) {
            mSubmitTv.setEnabled(false);
            mSubmitTv.setText(mTopRightBtnText);
            mSubmitTv.setTextColor(Color.parseColor("#b5b2b2"));
        } else {
            mSubmitTv.setTextColor(Color.parseColor("#ffffff"));
            mSubmitTv.setEnabled(true);
            mSubmitTv.setText(mTopRightBtnText + "(" + mPicAdapter.getSelectedCount() + "/" + mMaxChooseCount + ")");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mTakePhotoEnabled) {
            mImageCaptureManager.onSaveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (mTakePhotoEnabled) {
            mImageCaptureManager.onRestoreInstanceState(savedInstanceState);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onItemChildClick(ViewGroup viewGroup, View view, int position) {
        if (mMaxChooseCount == 1) {
            String currentImage = mPicAdapter.getItem(position);
            mPicAdapter.getSelectedImages().add(currentImage);
            returnSelectedImages(mPicAdapter.getSelectedImages());
            return;
        }
        if (view.getId() == R.id.iv_item_photo_camera_camera) {
            handleTakePhoto();
        } else if (view.getId() == R.id.iv_item_photo_picker_photo) {
            changeToPreview(position);
        } else if (view.getId() == R.id.iv_item_photo_picker_flag) {
            handleClickSelectFlagIv(position);
        }
    }

    /**
     * 处理拍照
     */
    private void handleTakePhoto() {
        if (mMaxChooseCount == 1) {
            // 单选
            takePhoto();
        } else if (mPicAdapter.getSelectedCount() == mMaxChooseCount) {
            toastMaxCountTip();
        } else {
            takePhoto();
        }
    }

    /**
     * 拍照
     */
    private void takePhoto() {
        try {
                startActivityForResultNoAnim(mImageCaptureManager.getTakePictureIntent(), REQUEST_CODE_TAKE_PHOTO);
        } catch (Exception e) {
            if (!AndPermission.hasPermission(this, Manifest.permission.CAMERA)) {
                showToastStr("当前没有拍摄权限权限");
            } else {
                NIM_PhotoPickerUtil.show(R.string.nim_pp_camera_not_support);
            }
            onBackPressed();
        }
    }

    /**
     * 跳转到图片选择预览界面
     *
     * @param position 当前点击的item的索引位置
     */
    private void changeToPreview(int position) {
        int currentPosition = position;
        if (mCurrentImageFolderModel.isTakePhotoEnabled()) {
            currentPosition--;
        }
        startActivityForResult(NIM_PhotoPreviewAct.newIntent(this, mMaxChooseCount, mPicAdapter
                        .getSelectedImages(), (ArrayList<String>) mPicAdapter.getData(), currentPosition, false),
                REQUEST_CODE_PREVIEW);
    }

    /**
     * 处理点击选择按钮事件
     *
     * @param position 当前点击的item的索引位置
     */
    private void handleClickSelectFlagIv(int position) {
        String currentImage = mPicAdapter.getItem(position);
        if (mMaxChooseCount == 1) {
            // 单选

            if (mPicAdapter.getSelectedCount() > 0) {
                String selectedImage = mPicAdapter.getSelectedImages().remove(0);
                if (TextUtils.equals(selectedImage, currentImage)) {
                    mPicAdapter.notifyItemChanged(position);
                } else {
                    int preSelectedImagePosition = mPicAdapter.getData().indexOf(selectedImage);
                    mPicAdapter.notifyItemChanged(preSelectedImagePosition);
                    mPicAdapter.getSelectedImages().add(currentImage);
                    mPicAdapter.notifyItemChanged(position);
                }
            } else {
                mPicAdapter.getSelectedImages().add(currentImage);
                mPicAdapter.notifyItemChanged(position);
            }
            renderTopRightBtn();
        } else {
            // 多选

            if (!mPicAdapter.getSelectedImages().contains(currentImage) && mPicAdapter.getSelectedCount() == mMaxChooseCount) {
                toastMaxCountTip();
            } else {
                if (mPicAdapter.getSelectedImages().contains(currentImage)) {
                    mPicAdapter.getSelectedImages().remove(currentImage);
                } else {
                    mPicAdapter.getSelectedImages().add(currentImage);
                }
                mPicAdapter.notifyItemChanged(position);

                renderTopRightBtn();
            }
        }
    }

    private void reloadPhotos(int position) {
        if (position < mImageFolderModels.size()) {
            mCurrentImageFolderModel = mImageFolderModels.get(position);
            if (mTitleTv != null) {
                mTitleTv.setText(mCurrentImageFolderModel.name);
            }

            mPicAdapter.setImageFolderModel(mCurrentImageFolderModel);
            if (mPicAdapter.getSelectedCount() > 0) {
                for (int i = mPicAdapter.getSelectedImages().size() - 1; i >= 0; i--) {
                    if (!mCurrentImageFolderModel.getImages().contains(mPicAdapter.getSelectedImages().get(i))) {
                        mPicAdapter.getSelectedImages().remove(mPicAdapter.getSelectedImages().get(i));
                    }
                }
                renderTopRightBtn();
            }
        }
    }

    @Override
    public void onPostExecute(ArrayList<NIM_ImageFolderModel> imageFolderModels) {
        dismissLoadingDialog();
        mLoadPhotoTask = null;
        mImageFolderModels = imageFolderModels;
        reloadPhotos(mPhotoFolderPw == null ? 0 : mPhotoFolderPw.getCurrentPosition());
    }

    @Override
    public void onTaskCancelled() {
        dismissLoadingDialog();
        mLoadPhotoTask = null;
    }

    private void cancelLoadPhotoTask() {
        if (mLoadPhotoTask != null) {
            mLoadPhotoTask.cancelTask();
            mLoadPhotoTask = null;
        }
    }

    @Override
    protected void onDestroy() {
        dismissLoadingDialog();
        cancelLoadPhotoTask();

        super.onDestroy();
    }
}
