package com.qbao.newim.views.imgpicker;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.qbao.newim.qbim.R;

import java.util.ArrayList;

/**
 * Created by chenjian on 2017/5/2.
 */

public class NIM_PhotoFolderPw extends NIM_PopupWindow implements NIM_OnRVItemClickListener {
    public static final int ANIM_DURATION = 300;
    private LinearLayout mRootLl;
    private RecyclerView mContentRv;
    private FolderAdapter mFolderAdapter;
    private Delegate mDelegate;
    private int mCurrentPosition;

    public NIM_PhotoFolderPw(Activity activity, View anchorView, Delegate delegate) {
        super(activity, R.layout.nim_pp_pw_photo_folder, anchorView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        mDelegate = delegate;
    }

    @Override
    protected void initView() {
        mRootLl = getViewById(R.id.ll_photo_folder_root);
        mContentRv = getViewById(R.id.rv_photo_folder_content);
    }

    @Override
    protected void setListener() {
        mRootLl.setOnClickListener(this);
        mFolderAdapter = new FolderAdapter(mContentRv);
        mFolderAdapter.setOnRVItemClickListener(this);
    }

    @Override
    protected void processLogic() {
        setAnimationStyle(android.R.style.Animation);
        setBackgroundDrawable(new ColorDrawable(0x90000000));

        mContentRv.setLayoutManager(new LinearLayoutManager(mActivity));
        mContentRv.setAdapter(mFolderAdapter);
    }

    /**
     * 设置目录数据集合
     *
     * @param datas
     */
    public void setData(ArrayList<NIM_ImageFolderModel> datas) {
        mFolderAdapter.setData(datas);
    }

    @Override
    public void show() {
        showAsDropDown(mAnchorView);
        ViewCompat.animate(mContentRv).translationY(-mWindowRootView.getHeight()).setDuration(0).start();
        ViewCompat.animate(mContentRv).translationY(0).setDuration(ANIM_DURATION).start();
        ViewCompat.animate(mRootLl).alpha(0).setDuration(0).start();
        ViewCompat.animate(mRootLl).alpha(1).setDuration(ANIM_DURATION).start();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ll_photo_folder_root) {
            dismiss();
        }
    }

    @Override
    public void dismiss() {
        ViewCompat.animate(mContentRv).translationY(-mWindowRootView.getHeight()).setDuration(ANIM_DURATION).start();
        ViewCompat.animate(mRootLl).alpha(1).setDuration(0).start();
        ViewCompat.animate(mRootLl).alpha(0).setDuration(ANIM_DURATION).start();

        if (mDelegate != null) {
            mDelegate.executeDismissAnim();
        }

        mContentRv.postDelayed(new Runnable() {
            @Override
            public void run() {
                NIM_PhotoFolderPw.super.dismiss();
            }
        }, ANIM_DURATION);
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    @Override
    public void onRVItemClick(ViewGroup viewGroup, View view, int position) {
        if (mDelegate != null && mCurrentPosition != position) {
            mDelegate.onSelectedFolder(position);
        }
        mCurrentPosition = position;
        dismiss();
    }

    private class FolderAdapter extends NIM_RecyclerViewAdapter<NIM_ImageFolderModel> {
        private int mImageSize;

        public FolderAdapter(RecyclerView recyclerView) {
            super(recyclerView, R.layout.nim_pp_item_photo_folder);

            mData = new ArrayList<>();
            mImageSize = NIM_PhotoPickerUtil.getScreenWidth() / 10;
        }

        @Override
        protected void fillData(NIM_ViewHolderHelper helper, int position, NIM_ImageFolderModel model) {
            helper.setText(R.id.tv_item_photo_folder_name, model.name);
            helper.setText(R.id.tv_item_photo_folder_count, String.valueOf(model.getCount()));
            NIM_Image.display(helper.getImageView(R.id.iv_item_photo_folder_photo), R.mipmap.nim_pp_ic_holder_light, model.coverPath, mImageSize);
        }
    }

    public interface Delegate {
        void onSelectedFolder(int position);

        void executeDismissAnim();
    }
}
