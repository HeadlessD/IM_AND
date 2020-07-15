package com.qbao.newim.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.qbim.R;
import com.qbao.newim.views.dialog.Effectstype;
import com.qbao.newim.views.dialog.NiftyDialogBuilder;

import java.util.ArrayList;

/**
 * Created by chenjian on 2015/9/28.
 */
public class ProgressDialog {

    /**
     * 请求对话框，进度对话框
     *
     * @param context    当前上下文
     * @param strTip     进度提示文字
     * @param bTouchMiss 触摸对话框外是否消失
     * @return
     */
    public static Dialog createRequestDialog(final Context context, String strTip, boolean bTouchMiss) {

        final Dialog requestDialog = new Dialog(context, R.style.dialog);
        requestDialog.setContentView(R.layout.progressbar_style);
        requestDialog.setCanceledOnTouchOutside(bTouchMiss);
        Window window = requestDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        lp.width = (int) (0.5 * width);

        TextView tvLoad = (TextView) requestDialog.findViewById(R.id.tv_load);
        if (strTip == null || strTip.length() == 0) {
            tvLoad.setText("");
        } else {
            tvLoad.setText(strTip);
        }
        return requestDialog;
    }

    /**
     * 系统自带提示对话框
     * @param title 提示标题
     * @param message 提示内容
     * @param icon 提示图标
     * @param positiveText 左边按钮文字
     * @param onPositiveClickListener
     * @param negativeText 右边按钮文字
     * @param onNegativeClickListener
     * @return
     */
    protected AlertDialog showSystemDialog(Activity activity, String title, String message, int icon, String positiveText,
                                           DialogInterface.OnClickListener onPositiveClickListener, String negativeText, DialogInterface.OnClickListener onNegativeClickListener){
        AlertDialog alertDialog = new AlertDialog.Builder(activity).setTitle(title)
                .setMessage(message).setIcon(icon)
                .setPositiveButton(positiveText, onPositiveClickListener)
                .setNegativeButton(negativeText, onNegativeClickListener)
                .show();
        return alertDialog;
    }

    /**
     * 系统自带提示对话框
     * @param msg 提示内容 只有一个确定按钮
     * @param onPositiveClickListener 抛出点击事件
     */
    protected void showSystemDialog(Activity activity, String msg, DialogInterface.OnClickListener onPositiveClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(msg);
        builder.setTitle("提示");
        builder.setPositiveButton("确定", onPositiveClickListener);
        builder.create().show();
    }

    private static NiftyDialogBuilder showCustomDialog(Activity activity, String title, String message,String first_btn,
                  String second_btn, ArrayList<String> data, BaseQuickAdapter.OnItemClickListener listener, View.OnClickListener clickListener1
                  , View.OnClickListener clickListener2) {
        NiftyDialogBuilder dialogBuilder = new NiftyDialogBuilder(activity);
        dialogBuilder
                .setCustomView(null)
                .withTitle(title)
                .withTitleColor("#333333")
                .withDividerColor(ContextCompat.getColor(activity, R.color.colorAccent))
                .withDialogColor("#FFFFFFFF")
                .withMessage(message)
                .withMessageColor("#333333")
                .setCustomListView(activity, data, listener)
                .withButton1Text(first_btn)
                .withButton2Text(second_btn)
                .setButton1Click(clickListener1)
                .setButton2Click(clickListener2)
                .isCancelableOnTouchOutside(false)
                .withDuration(400)
                .withEffect(Effectstype.SlideBottom)
                .show();
        return dialogBuilder;
    }

    // 简单的确定取消按钮，加信息提示
    public static NiftyDialogBuilder showCustomDialog(Activity activity, String title, String message, View.OnClickListener clickListener2) {
        return showCustomDialog(activity, title, message, "取消", "确定", null, null, null, clickListener2);
    }

    // 确定取消按钮，无提示
    public static NiftyDialogBuilder showCustomDialog(Activity activity, String message, View.OnClickListener clickListener2) {
        return showCustomDialog(activity, null, message, "取消", "确定", null, null, null, clickListener2);
    }

    // 只有一个确定按钮
    public static NiftyDialogBuilder showCustomDialog(Activity activity, String message) {
        return showCustomDialog(activity, "提示", message, "", "确定", null, null, null, null);
    }

    // 只有一个确定按钮
    public static NiftyDialogBuilder showSingleDialog(Activity activity, String message, View.OnClickListener clickListener2) {
        return showCustomDialog(activity, "提示", message, "", "确定", null, null, null, clickListener2);
    }

    // 只有一个确定按钮
    public static NiftyDialogBuilder showSingleDialog(Activity activity, String message, String btn, View.OnClickListener clickListener2) {
        return showCustomDialog(activity, "提示", message, "", btn, null, null, null, clickListener2);
    }

    // 有listview的对话框
    public static NiftyDialogBuilder showCustomDialog(Activity activity, String title, ArrayList<String> data,
                                                      BaseQuickAdapter.OnItemClickListener listener) {
        return showCustomDialog(activity, title, "", "", "", data, listener, null, null);
    }

    // 显示对话框
    public static void showDialog(Context context, String title, final DialogCallBack callBack) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(title);
        builder.setTitle("提示");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callBack.clickCall();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.create().show();
    }

    public interface DialogCallBack {
        void clickCall();
    }

    public interface DialogClick{
        void OkClick();
        void CancelClick();
    }
}
