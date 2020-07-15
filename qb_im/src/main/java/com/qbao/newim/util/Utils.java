package com.qbao.newim.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.EditText;

import com.qbao.newim.helper.HanziToPinyin;
import com.qbao.newim.qbim.BuildConfig;
import com.qbao.newim.views.ProgressDialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    /**
     * 给字符串md5加密
     *
     * @param myinfo 要加密的字符串
     * @return 加密好的字符串
     */
    public static String MD5(String myinfo) {
        byte[] digesta = null;
        try {
            MessageDigest alga = MessageDigest.getInstance("MD5");
            alga.update(myinfo.getBytes());
            digesta = alga.digest();

        } catch (NoSuchAlgorithmException ex) {
        }
        if (null != digesta) {
            return byte2hex(digesta);
        } else {
            return "";
        }
    }

    public static String byte2hex(byte[] b) {
        // 二行制转字符串
        String stmp = "";
        StringBuilder hs = new StringBuilder();
        for (int n = 0; n < b.length; n++) {
            stmp = (Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs.append("0");
            }
            hs.append(stmp);
        }
        return hs.toString();
    }

    public static String getImei(Context mContext) {
        TelephonyManager telephonyManager = ((TelephonyManager) mContext
                .getSystemService(Activity.TELEPHONY_SERVICE));
        if (telephonyManager == null) {
            return "";
        }
        String imei = telephonyManager.getDeviceId();
        if (TextUtils.isEmpty(imei)) {
            return "";
        }
        return imei;
    }

    public static int getVersionCode(Context mContext) {
        try {
            PackageManager manager = mContext.getPackageManager();
            PackageInfo info = manager.getPackageInfo(mContext.getPackageName(), 0);
            int versionCode = info.versionCode;
            return versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String getVersion(Context mContext) {
        try {
            PackageManager manager = mContext.getPackageManager();
            PackageInfo info = manager.getPackageInfo(mContext.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    public static String getDevId(Context mContext) {
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        String devId = tm.getDeviceId();
        return devId;
    }

    public static String getChannel(Context mContext) {
        try {
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return "qianbao_android";

    }

    public static String format(BigDecimal value) {
        try {
            if (BigDecimal.ZERO.compareTo(value) == 0) {
                return value.toString();
            }
            DecimalFormat balanceFormat = new DecimalFormat(",###");
            return balanceFormat.format(value);
        } catch (Exception e) {
            return value.toString();
        }
    }

    public static String format(long value) {
        return format(new BigDecimal(value));
    }

    /**
     * 用SHA-1算法加密字符串并返回16进制串
     *
     * @param strSrc
     * @return
     */
    public static String encrypt(String strSrc) {
        MessageDigest md = null;
        String strDes = null;
        byte[] bt = strSrc.getBytes();
        try {
            md = MessageDigest.getInstance("SHA-1");
            md.update(bt);
            strDes = bytes2Hex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        return strDes;
    }

    public static String bytes2Hex(byte[] bts) {
        String des = "";
        String tmp = null;
        for (int i = 0; i < bts.length; i++) {
            tmp = (Integer.toHexString(bts[i] & 0xFF));
            if (tmp.length() == 1) {
                des += "0";
            }
            des += tmp;
        }
        return des;
    }

    /**
     * 从左到右,按index,权重依次降低 例:备注名,昵称,电话号码.... null == ""
     */
    public static String getUserShowName(String[] names) {
        String showName = "";
        if (names != null && names.length > 0) {
            for (int i = 0; i < names.length; i++) {
                String tmpName = names[i];
                if (tmpName != null && !tmpName.trim().equals("")) {
                    showName = tmpName;
                    return showName;
                }
            }
        }
        return showName;
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static String format(String value) {
        if (TextUtils.isEmpty(value))
            return value;
        BigDecimal decimalValue = null;
        try {
            decimalValue = new BigDecimal(value);
        } catch (Exception e) {
            return value;
        }
        return format(decimalValue);
    }

    public static SpannableString highlight(String text, String keyword) {
        if (TextUtils.isEmpty(text)) {
            return new SpannableString("");
        }
        SpannableString result = new SpannableString(text);
        if (TextUtils.isEmpty(keyword)) {
            return result;
        }
        int start, end;

        Pattern p = Pattern.compile(keyword);
        Matcher matcher = p.matcher(text);
        while (matcher.find()) {
            start = matcher.start();//text.indexOf(word);
            if (start < 0) {
                return result;
            }
            end = matcher.end();//start + word.length();
            result.setSpan(new ForegroundColorSpan(Color.parseColor("#FF362C")), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); //设置前景色为洋红色
        }
        return result;
    }


    public static final Pattern ELLIPSIZE_PATTERN = Pattern.compile("(.{4})(.+)");

    public static String ellipsize(String value) {
        Matcher matcher = ELLIPSIZE_PATTERN.matcher(value);
        if (matcher.find() && matcher.groupCount() > 1) {
            return matcher.group(1) + "···";
        }
        return value;
    }

    /**
     * 汉字转换位汉语拼音，英文字符不变
     */
    public static String converterToSpell(String hanzi) {
        ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance().get(hanzi);
        StringBuilder sb = new StringBuilder();
        if (tokens != null && tokens.size() > 0) {
            for (HanziToPinyin.Token token : tokens) {
                if (HanziToPinyin.Token.PINYIN == token.type) {
                    sb.append(token.target);
                } else {
                    sb.append(token.source);
                }
            }
        } else {
            return "#";
        }

        return sb.toString().toUpperCase();
    }

    public static boolean isLetter(char c) {
        if (c >= 0x0391 && c <= 0xFFE5)  //中文字符
            return false;

        //英文字符
        if (c >= 0x0000 && c <= 0x00FF) {
            return true;
        }
        return false;
    }

    public static LinkedHashMap<Integer, String> converterToSpellMap(String chines) {
        LinkedHashMap<Integer, String> map = new LinkedHashMap<>();
        ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance().get(chines);
        StringBuilder sb = new StringBuilder();
        if (tokens != null && tokens.size() > 0) {
            int size = tokens.size();
            for (int i = 0; i < size; i++) {
                HanziToPinyin.Token token = tokens.get(i);
                if (HanziToPinyin.Token.PINYIN == token.type) {
                    sb.append(token.target);
                } else {
                    sb.append(token.source);
                }
                map.put(i, sb.toString().toUpperCase());
            }
        }
        return map;
    }

    public static String getEvnId(Context context) {
        String devId = getDevId(context);

        String androidId = android.provider.Settings.Secure.getString(
                context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        StringBuilder sb = new StringBuilder();
        sb.append(devId);
        sb.append("_");
        sb.append(androidId);
        String evnId = Utils.MD5(sb.toString());
        return "4" + evnId;
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

    /**
     * 随机指定范围内N个不重复的数
     * 在初始化的无重复待选数组中随机产生一个数放入结果中，
     * 将待选数组被随机到的数，用待选数组(len-1)下标对应的数替换
     * 然后从len-2里随机产生下一个随机数，如此类推
     *
     * @param max 指定范围最大值
     * @param min 指定范围最小值
     * @param n   随机数个数
     * @return int[] 随机数结果集
     */
    public static int[] randomArray(int min, int max, int n) {
        int len = max - min + 1;

        if (max < min || n > len) {
            return null;
        }

        //初始化给定范围的待选数组
        int[] source = new int[len];
        for (int i = min; i < min + len; i++) {
            source[i - min] = i;
        }

        int[] result = new int[n];
        Random rd = new Random();
        int index;
        for (int i = 0; i < result.length; i++) {
            //待选数组0到(len-2)随机一个下标
            index = Math.abs(rd.nextInt() % len--);
            //将随机到的数放入结果集
            result[i] = source[index];
            //将待选数组中被随机到的数，用待选数组(len-1)下标对应的数替换
            source[index] = source[len];
        }
        return result;
    }

    private static final String ROM_MIUI_V5 = "V5";
    private static final String ROM_MIUI_V6 = "V6";
    private static final String ROM_MIUI_V7 = "V7";
    private static final String ROM_MIUI_V8 = "V8";
    public static String getMiuiVersion() {
        String propName = "ro.miui.ui.version.name";
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(
                    new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return line;
    }


    public static String getPhoneModel() {
        return Build.MANUFACTURER;
    }

    public static boolean isSamsungPhone() {
        if (getPhoneModel().contains("samsung")) {
            return true;
        }
        return false;
    }

    public static boolean isHuaweiPhone() {
        if (getPhoneModel().contains("huawei") || getPhoneModel().contains("honour")) {
            return true;
        }
        return false;
    }

    public static boolean isXiaomiPhone() {
        if (getPhoneModel().contains("Xiaomi")) {
            return true;
        }
        return false;
    }

    public static boolean isMeizuPhone() {
        if (getPhoneModel().contains("Meizu")) {
            return true;
        }
        return false;
    }

    public static boolean isSonyPhone() {
        if (getPhoneModel().contains("Sony")) {
            return true;
        }
        return false;
    }

    public static boolean isOPPOPhone() {
        if (getPhoneModel().contains("OPPO")) {
            return true;
        }
        return false;
    }

    public static boolean isLGPhone() {
        if (getPhoneModel().contains("LG")) {
            return true;
        }
        return false;
    }

    public static boolean isvivoPhone() {
        if (getPhoneModel().contains("vivo")) {
            return true;
        }
        return false;
    }

    public static boolean isLetvPhone() {
        if (getPhoneModel().contains("Letv")) {
            return true;
        }
        return false;
    }

    public static boolean isZTEPhone() {
        if (getPhoneModel().contains("ZTE")) {
            return true;
        }
        return false;
    }

    //酷派
    public static boolean isYuLongPhone() {
        if (getPhoneModel().contains("YuLong")) {
            return true;
        }
        return false;
    }

    public static boolean isLENOVOPhone() {
        if (getPhoneModel().contains("LENOVO")) {
            return true;
        }
        return false;
    }

    public static boolean is360Phone() {
        if (getPhoneModel().contains("360")) {
            return true;
        }
        return false;
    }

    public static int type = -1;
    public static void startActivityToSetting(final Activity activity, final int requestCode) {
        String msg = "";
        if (isHuaweiPhone()) {
            msg += "该权限需要到华为权限中心获取";
            type = 0;
        } else if (isMeizuPhone()) {
            msg += "该权限需要到魅族权限中心获取";
            type = 1;
        } else if (isXiaomiPhone()) {
            msg += "该权限需要到小米安全中心获取";
            type = 2;
        } else if (isSonyPhone()) {
            msg += "该权限需要到索尼权限中心获取";
            type = 3;
        } else if (isOPPOPhone()) {
            msg += "该权限需要到OPPO权限中心获取";
            type = 4;
        } else if (isLGPhone()) {
            msg += "该权限需要到LG权限中心获取";
            type = 5;
        } else if (isLetvPhone()) {
            msg += "该权限需要到乐视权限中心获取";
            type = 6;
        } else if (is360Phone()) {
            msg += "该权限需要到360安全中心获取";
            type = 7;
        } else {
            msg += "该权限需要到设置中心获取";
        }

        ProgressDialog.showSingleDialog(activity, msg, "去设置", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                switch (type) {
                    case 0:
                        intent = new Intent();
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
                        ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");
                        intent.setComponent(comp);
                        break;
                    case 1:
                        intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
                        break;
                    case 2:
                        String rom = getMiuiVersion();
                        if (ROM_MIUI_V5.equals(rom)) {
                            Uri packageURI = Uri.parse("package:" + activity.getApplicationInfo().packageName);
                            intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                        } else if (ROM_MIUI_V6.equals(rom) || ROM_MIUI_V7.equals(rom)) {
                            intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                            intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                            intent.putExtra("extra_pkgname", activity.getPackageName());
                        } else if (ROM_MIUI_V8.equals(rom)) {
                            intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                            intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
                            intent.putExtra("extra_pkgname", activity.getPackageName());
                        } else {
                            intent = new Intent(Settings.ACTION_SETTINGS);
                        }
                        break;
                    case 3:
                        intent = new Intent();
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
                        ComponentName sony_comp = new ComponentName("com.sonymobile.cta", "com.sonymobile.cta.SomcCTAMainActivity");
                        intent.setComponent(sony_comp);
                        break;
                    case 4:
                        intent = new Intent();
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
                        ComponentName oppo_comp = new ComponentName("com.color.safecenter", "com.color.safecenter.permission.PermissionManagerActivity");
                        intent.setComponent(oppo_comp);
                        break;
                    case 5:
                        intent = new Intent("android.intent.action.MAIN");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
                        ComponentName lg_comp = new ComponentName("com.android.settings", "com.android.settings.Settings$AccessLockSummaryActivity");
                        intent.setComponent(lg_comp);
                        break;
                    case 6:
                        intent = new Intent();
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
                        ComponentName letv_comp = new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.PermissionAndApps");
                        intent.setComponent(letv_comp);
                        break;
                    case 7:
                        intent = new Intent("android.intent.action.MAIN");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
                        ComponentName comp_360 = new ComponentName("com.qihoo360.mobilesafe", "com.qihoo360.mobilesafe.ui.index.AppEnterActivity");
                        intent.setComponent(comp_360);
                        break;
                    default:
                        intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        if (Build.VERSION.SDK_INT >= 9) {
                            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                            intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
                        } else if (Build.VERSION.SDK_INT <= 8) {
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
                            intent.putExtra("com.android.settings.ApplicationPkgName", activity.getPackageName());
                        }
                        break;
                }

                if (intent != null)
                    activity.startActivityForResult(intent, requestCode);
            }
        });
    }

    /**
     * 这里只对字母进行匹配，并且都是大写字母
     * 将输入的和当前需要匹配的字符串做对比
     * @param input_case 当前输入字符串大写
     * @param matcher 当前需要匹配的字符串
     * @param map  当前需要匹配的字符串的全拼map
     * @return 是否匹配成功
     */
    public static String containInput(String input_case, String matcher, LinkedHashMap<Integer, String> map) {
        boolean is_contain = false;
        String keyword = "";
        int count = -1;
        if (map != null && map.size() > 0) {
            Iterator iterator_remark = map.entrySet().iterator();
            while (iterator_remark.hasNext()) {
                HashMap.Entry entry = (HashMap.Entry) iterator_remark.next();
                String value = (String) entry.getValue();
                Integer key = (Integer) entry.getKey();
                if (value.contains(input_case)) {
                    keyword += matcher.substring(key, key + 1);
                    input_case = input_case.replaceFirst(value, "" + value.charAt(0));
                    is_contain = true;
                    break;
                } else {
                    if (keyword.length() == input_case.length()) {
                        break;
                    }
                    boolean contain_letter = input_case.charAt(keyword.length()) == value.charAt(0);
                    if (!contain_letter) {
                        if (key - count >= 1 && count >= 0) {
                            is_contain = false;
                        }
                        continue;
                    } else {
                        if (key - count > 1) {
                            is_contain = false;
                        } else {
                            if (keyword.length() < input_case.length()) {
                                keyword += matcher.substring(key, key + 1);
                                is_contain = true;
                            }
                            count = key;
                        }
                    }

                    boolean contain_all = input_case.contains(value);
                    if (contain_all && value.length() > 1) {
                        input_case = input_case.replaceFirst(value, "" + value.charAt(0));
                        count = key;
                    }
                }
            }
        }

        if (is_contain && input_case.length() <= map.size()) {
            return keyword;
        } else {
            return "";
        }
    }

    public static <T> void swapSession(List<T> list, int oldPosition, int newPosition){
        if(null == list){
            throw new IllegalStateException("The list can not be empty...");
        }

        // 向前移动，前面的元素需要向后移动
        if(oldPosition < newPosition){
            for(int i = oldPosition; i < newPosition; i++){
                Collections.swap(list, i, i + 1);
            }
        }

        // 向后移动，后面的元素需要向前移动
        if(oldPosition > newPosition){
            for(int i = oldPosition; i > newPosition; i--){
                Collections.swap(list, i, i - 1);
            }
        }
    }
}
