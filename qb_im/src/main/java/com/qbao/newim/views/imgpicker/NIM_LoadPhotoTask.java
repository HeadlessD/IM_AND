package com.qbao.newim.views.imgpicker;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.qbao.newim.qbim.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by chenjian on 2017/5/2.
 */

public class NIM_LoadPhotoTask extends NIM_AsyncTask<Void, ArrayList<NIM_ImageFolderModel>> {
    private Context mContext;
    private boolean mTakePhotoEnabled;


    public NIM_LoadPhotoTask(Callback<ArrayList<NIM_ImageFolderModel>> callback, Context context, boolean takePhotoEnabled) {
        super(callback);
        mContext = context.getApplicationContext();
        mTakePhotoEnabled = takePhotoEnabled;
    }

    private static boolean isNotImageFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return true;
        }

        File file = new File(path);
        return !file.exists() || file.length() == 0;

        // 获取图片的宽和高，但不把图片加载到内存中
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(path, options);
//        return options.outMimeType == null;
    }

    @Override
    protected ArrayList<NIM_ImageFolderModel> doInBackground(Void... voids) {
        ArrayList<NIM_ImageFolderModel> imageFolderModels = new ArrayList<>();

        NIM_ImageFolderModel allImageFolderModel = new NIM_ImageFolderModel(mTakePhotoEnabled);
        allImageFolderModel.name = mContext.getString(R.string.nim_pp_all_image);
        imageFolderModels.add(allImageFolderModel);

        HashMap<String, NIM_ImageFolderModel> imageFolderModelMap = new HashMap<>();

        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media.DATA},
                    MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                    new String[]{"image/jpeg", "image/png", "image/jpg"},
                    MediaStore.Images.Media.DATE_ADDED + " DESC"
            );

            NIM_ImageFolderModel otherImageFolderModel;
            if (cursor != null && cursor.getCount() > 0) {
                boolean firstInto = true;
                while (cursor.moveToNext()) {
                    String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

                    if (isNotImageFile(imagePath)) {
                        continue;
                    }

                    if (firstInto) {
                        allImageFolderModel.coverPath = imagePath;
                        firstInto = false;
                    }
                    // 所有图片目录每次都添加
                    allImageFolderModel.addLastImage(imagePath);

                    String folderPath = null;
                    // 其他图片目录
                    File folder = new File(imagePath).getParentFile();
                    if (folder != null) {
                        folderPath = folder.getAbsolutePath();
                    }

                    if (TextUtils.isEmpty(folderPath)) {
                        int end = imagePath.lastIndexOf(File.separator);
                        if (end != -1) {
                            folderPath = imagePath.substring(0, end);
                        }
                    }

                    if (!TextUtils.isEmpty(folderPath)) {
                        if (imageFolderModelMap.containsKey(folderPath)) {
                            otherImageFolderModel = imageFolderModelMap.get(folderPath);
                        } else {
                            String folderName = folderPath.substring(folderPath.lastIndexOf(File.separator) + 1);
                            if (TextUtils.isEmpty(folderName)) {
                                folderName = "/";
                            }
                            otherImageFolderModel = new NIM_ImageFolderModel(folderName, imagePath);
                            imageFolderModelMap.put(folderPath, otherImageFolderModel);
                        }
                        otherImageFolderModel.addLastImage(imagePath);
                    }
                }

                // 添加其他图片目录
                imageFolderModels.addAll(imageFolderModelMap.values());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return imageFolderModels;
    }

    public NIM_LoadPhotoTask perform() {
        if (Build.VERSION.SDK_INT >= 11) {
            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            execute();
        }
        return this;
    }
}
