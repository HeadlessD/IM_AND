package com.qbao.newim.views.imgpicker;

import android.app.Activity;
import android.support.annotation.DrawableRes;
import android.widget.ImageView;

/**
 * Created by chenjian on 2017/5/2.
 */

public class NIM_Image {
    private static NIM_ImageLoader sImageLoader;

    private NIM_Image() {
    }

    private static final NIM_ImageLoader getImageLoader() {
        if (sImageLoader == null) {
            synchronized (NIM_Image.class) {
                if (sImageLoader == null) {
                    if (isClassExists("com.bumptech.glide.Glide")) {
                        sImageLoader = new NIM_GlideImageLoader();
                    } else if (isClassExists("com.squareup.picasso.Picasso")) {
//                        sImageLoader = new BGAPicassoImageLoader();
                    } else if (isClassExists("com.nostra13.universalimageloader.core.ImageLoader")) {
//                        sImageLoader = new BGAUILImageLoader();
                    } else if (isClassExists("org.xutils.x")) {
//                        sImageLoader = new BGAXUtilsImageLoader();
                    } else {
                        throw new RuntimeException("必须在你的build.gradle文件中配置「Glide、Picasso、universal-image-loader、XUtils3」中的某一个图片加载库的依赖");
                    }
                }
            }
        }
        return sImageLoader;
    }

    private static final boolean isClassExists(String classFullName) {
        try {
            Class.forName(classFullName);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void display(ImageView imageView, @DrawableRes int loadingResId, @DrawableRes int failResId, String path, int width, int height, final NIM_ImageLoader.DisplayDelegate delegate) {
        getImageLoader().display(imageView, path, loadingResId, failResId, width, height, delegate);
    }

    public static void display(ImageView imageView, @DrawableRes int placeholderResId, String path, int width, int height, final NIM_ImageLoader.DisplayDelegate delegate) {
        display(imageView, placeholderResId, placeholderResId, path, width, height, delegate);
    }

    public static void display(ImageView imageView, @DrawableRes int placeholderResId, String path, int width, int height) {
        display(imageView, placeholderResId, path, width, height, null);
    }

    public static void display(ImageView imageView, @DrawableRes int placeholderResId, String path, int size) {
        display(imageView, placeholderResId, path, size, size);
    }

    public static void download(String path, final NIM_ImageLoader.DownloadDelegate delegate) {
        getImageLoader().download(path, delegate);
    }

    /**
     * 暂停加载
     *
     * @param activity
     */
    public static void pause(Activity activity) {
        getImageLoader().pause(activity);
    }

    /**
     * @param activity
     */
    public static void resume(Activity activity) {
        getImageLoader().resume(activity);
    }
}
