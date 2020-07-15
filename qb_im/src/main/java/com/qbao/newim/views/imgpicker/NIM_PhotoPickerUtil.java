package com.qbao.newim.views.imgpicker;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.Toast;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by chenjian on 2017/5/2.
 */

public class NIM_PhotoPickerUtil {
    public static final Application sApp;
    private static Handler sHandler = new Handler(Looper.getMainLooper());

    static {
        Application app = null;
        try {
            app = (Application) Class.forName("android.app.AppGlobals").getMethod("getInitialApplication").invoke(null);
            if (app == null)
                throw new IllegalStateException("Static initialization of Applications must be on main thread.");
        } catch (final Exception e) {
            Log.e(NIM_PhotoPickerUtil.class.getSimpleName(), "Failed to get current application from AppGlobals." + e.getMessage());
            try {
                app = (Application) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null);
            } catch (final Exception ex) {
                Log.e(NIM_PhotoPickerUtil.class.getSimpleName(), "Failed to get current application from ActivityThread." + e.getMessage());
            }
        } finally {
            sApp = app;
        }
    }

    private NIM_PhotoPickerUtil() {
    }

    public static void runInThread(Runnable task) {
        new Thread(task).start();
    }

    public static void runInUIThread(Runnable task) {
        sHandler.post(task);
    }

    public static void runInUIThread(Runnable task, long delayMillis) {
        sHandler.postDelayed(task, delayMillis);
    }

    /**
     * 获取取屏幕宽度
     *
     * @return
     */
    public static int getScreenWidth() {
        WindowManager windowManager = (WindowManager) sApp.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    /**
     * 获取屏幕高度
     *
     * @return
     */
    public static int getScreenHeight() {
        WindowManager windowManager = (WindowManager) sApp.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    public static int dp2px(float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, sApp.getResources().getDisplayMetrics());
    }

    public static String md5(String... strs) {
        if (strs == null || strs.length == 0) {
            throw new RuntimeException("请输入需要加密的字符串!");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            boolean isNeedThrowNotNullException = true;
            for (String str : strs) {
                if (!TextUtils.isEmpty(str)) {
                    isNeedThrowNotNullException = false;
                    md.update(str.getBytes());
                }
            }
            if (isNeedThrowNotNullException) {
                throw new RuntimeException("请输入需要加密的字符串!");
            }
            return new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 显示吐司
     *
     * @param text
     */
    public static void show(CharSequence text) {
        if (!TextUtils.isEmpty(text)) {
            if (text.length() < 10) {
                Toast.makeText(sApp, text, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(sApp, text, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 显示吐司
     *
     * @param resId
     */
    public static void show(@StringRes int resId) {
        show(sApp.getString(resId));
    }

    /**
     * 在子线程中显示吐司时使用该方法
     *
     * @param text
     */
    public static void showSafe(final CharSequence text) {
        runInUIThread(new Runnable() {
            @Override
            public void run() {
                show(text);
            }
        });
    }

    /**
     * 在子线程中显示吐司时使用该方法
     *
     * @param resId
     */
    public static void showSafe( @StringRes int resId) {
        showSafe(sApp.getString(resId));
    }
}
