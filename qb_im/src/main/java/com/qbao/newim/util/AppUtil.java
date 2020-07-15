package com.qbao.newim.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.qbao.newim.configure.Constants;
import com.qbao.newim.constdef.NetConstDef;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by shiyunjie on 17/3/2.
 */

public class AppUtil {
    private static String device_code = null;
    private static Application main_context = null;
    private static List<Activity> mList = new LinkedList<>();
    private static int screenWidth = 0;

    private static int screenHeight = 0;

    private static final int TITLE_HEIGHT = 0;

    public static void SetContext(Application context) {
        main_context = context;
    }
    public static Context GetContext() {
        return main_context;
    }

    public static String GetDeviceCode() {
        if (device_code == null || device_code == "") {
            String deviceID = null;
            String Mac = null;
            try {
                TelephonyManager tm = (TelephonyManager) main_context.getSystemService(Context.TELEPHONY_SERVICE);
                if (tm != null) {
                    deviceID = tm.getDeviceId();
                }

                WifiManager wifi = (WifiManager) main_context.getSystemService(Context.WIFI_SERVICE);


                if (wifi != null) {
                    WifiInfo info = wifi.getConnectionInfo();

                    if (null != info) {
                        Mac = info.getMacAddress();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }


            if (deviceID == null) {
                deviceID = "NULL";
            }

            if (Mac == null) {
                Mac = "NULL";
            }

            device_code = Mac + "+" + deviceID;
        }

        return device_code;
    }

    public static String GetDeviceOSType() {
        String os_type = android.os.Build.VERSION.RELEASE;
        os_type += "|" + android.os.Build.DISPLAY;
        return os_type;
    }

    public static byte GetNetworkType() {
        try {
            ConnectivityManager manager = (ConnectivityManager) main_context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo network_info = manager.getActiveNetworkInfo();
            if (network_info == null || !network_info.isConnected()) {
                return NetConstDef.NET_TYPE.NETWORK_TYPE_NONE;
            }

            String type = network_info.getTypeName();
            if (network_info.getType() == ConnectivityManager.TYPE_WIFI) {
                return NetConstDef.NET_TYPE.NETWORK_TYPE_WIFI;
            } else if (type.equalsIgnoreCase("MOBILE")) {
                TelephonyManager tel_manager = (TelephonyManager) main_context.getSystemService(Context.TELEPHONY_SERVICE);
                return GetNetworkClassByType(tel_manager.getNetworkType());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return NetConstDef.NET_TYPE.NETWORK_TYPE_NONE;
        }

        return NetConstDef.NET_TYPE.NETWORK_TYPE_NONE;
    }

    private static byte GetNetworkClassByType(int network_type) {
        switch (network_type) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_GSM:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return NetConstDef.NET_TYPE.NETWORK_TYPE_2G;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                return NetConstDef.NET_TYPE.NETWORK_TYPE_3G;
            case TelephonyManager.NETWORK_TYPE_LTE:
            case TelephonyManager.NETWORK_TYPE_IWLAN:
                return NetConstDef.NET_TYPE.NETWORK_TYPE_4G;
            default:
                return NetConstDef.NET_TYPE.NETWORK_TYPE_NONE;
        }
    }

    public static String GetClientVersion() {
        String version = null;
        try {
            PackageManager manager = main_context.getPackageManager();
            PackageInfo pack_info = manager.getPackageInfo(main_context.getPackageName(), 0);
            version = pack_info.versionName;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return (version == null || version == "") ? "1.1.100" : version;
    }

    /**
     * 获取 虚拟按键的高度
     *
     * @param context
     * @return
     */
    public static int getBottomStatusHeight(Context context) {
        int totalHeight = getDpi(context);

        int contentHeight = getScreenHeight(context);

        return totalHeight - contentHeight;
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        return screenWidth;
    }

    public static int getScreenHeight(Context context) {
        int top = 0;
        if (context instanceof Activity) {
            top = ((Activity) context).getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
            if (top == 0) {
                top = (int) (TITLE_HEIGHT * getScreenDensity(context));
            }
        }
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        screenHeight = dm.heightPixels - top;
        return screenHeight;
    }

    public static float getScreenDensity(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metric = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metric);
        return metric.density;
    }

    //获取屏幕原始尺寸高度，包括虚拟功能键高度
    public static int getDpi(Context context) {
        int dpi = 0;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        @SuppressWarnings("rawtypes")
        Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, displayMetrics);
            dpi = displayMetrics.heightPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dpi;
    }

    public static boolean isFullScreen(final Activity activity) {
        return (activity.getWindow().getAttributes().flags &
                WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isTranslucentStatus(final Activity activity) {
        //noinspection SimplifiableIfStatement
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return (activity.getWindow().getAttributes().flags &
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) != 0;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    static boolean isFitsSystemWindows(final Activity activity) {
        //noinspection SimplifiableIfStatement
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0).
                    getFitsSystemWindows();
        }

        return false;
    }

    public static boolean refreshHeight(final View view, final int aimHeight) {
        if (view.isInEditMode()) {
            return false;
        }

        if (view.getHeight() == aimHeight) {
            return false;
        }

        if (Math.abs(view.getHeight() - aimHeight) ==
                StatusbarUtils.getStatusBarHeight(view.getContext())) {
            return false;
        }

        final int validPanelHeight = KeyboardUtil.getValidPanelHeight(view.getContext());
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    validPanelHeight);
            view.setLayoutParams(layoutParams);
        } else {
            layoutParams.height = validPanelHeight;
            view.requestLayout();
        }

        return true;
    }

    public static void setEditCursor(EditText edittext, int drawableId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            try {
                Field textcursorDrawable = EditText.class.getSuperclass().getDeclaredField(
                        "mCursorDrawableRes");
                if (textcursorDrawable != null) {
                    textcursorDrawable.setAccessible(true);
                    textcursorDrawable.setInt(edittext, drawableId);
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

        }
    }

    public static int getCharacterWidth(String text, float size) {
        if (null == text || "".equals(text)) {
            return 0;

        }

        Paint paint = new Paint();
        paint.setTextSize(size);
        int text_width = (int) paint.measureText(text);// 得到总体长度
        return text_width;
    }

    /**
     * 此方法仅仅用于测试:获取一个可测试用户数据
     */
    public static final long getTestUserId() {
        boolean xm = Utils.isXiaomiPhone();
        boolean sx = Utils.isSamsungPhone();
        boolean vv = Utils.isvivoPhone();
        long id1 = 5504182;
        long id2 = 5504185;
        long id3 = 5504183;
        long id4 = 5504180;
        if (xm) {
            return id1;
        } else if (sx){
            return id2;
        } else if (vv) {
            return id3;
        }

        return id4;
    }

    public static String getHeadUrl(long user_id) {
        return "http://user.qbcdn.com/user/avatar/queryAvata65/" + user_id + "/nosrc/1";
    }

    public static String getGroupUrl(long group_id) {
        return Constants.IM_SERVICE + "group/" + group_id + "_1.icon";
    }

    // 判断App是否处于后台
    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                Logger.error(context.getPackageName(), "appimportace ="
                        + appProcess.importance
                        + ",context.getClass().getName()="
                        + context.getClass().getName());
                if (appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    Logger.error(context.getPackageName(), "background"
                            + appProcess.processName);
                    return true;
                } else {
                    Logger.error(context.getPackageName(), "not background"
                            + appProcess.processName);
                    return false;
                }
            }
        }
        return false;
    }

    public static void addActivity(Activity activity) {
        mList.add(activity);
    }

    public static void exit() {
        try {
            for (Activity activity : mList) {
                if (activity != null)
                    activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mList.clear();
    }
}
