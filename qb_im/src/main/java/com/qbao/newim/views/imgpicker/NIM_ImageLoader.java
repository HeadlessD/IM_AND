package com.qbao.newim.views.imgpicker;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by chenjian on 2017/5/2.
 */

public abstract class NIM_ImageLoader {

    protected String getPath(String path) {
        if (path == null) {
            path = "";
        }

        if (!path.startsWith("http") && !path.startsWith("file")) {
            path = "file://" + path;
        }
        return path;
    }

    protected Activity getActivity(View view) {
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    public abstract void display(ImageView imageView, String path, @DrawableRes int loadingResId, @DrawableRes int failResId, int width, int height, DisplayDelegate delegate);

    public abstract void download(String path, DownloadDelegate delegate);

    public abstract void pause(Activity activity);

    public abstract void resume(Activity activity);

    public interface DisplayDelegate {
        void onSuccess(View view, String path);
    }

    public interface DownloadDelegate {
        void onSuccess(String path, Bitmap bitmap);

        void onFailed(String path);
    }
}
