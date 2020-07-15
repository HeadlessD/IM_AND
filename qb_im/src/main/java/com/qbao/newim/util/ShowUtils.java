/**
 * 提示工具类
 */
package com.qbao.newim.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;


public class ShowUtils
{

	private static final Context mContext;
	private static Toast mToast = null;
	static
	{
		mContext = AppUtil.GetContext();
	}

	/**
	 * Toast提示
	 * @param context
	 * @param stringId  文字资源id
	 */
	public static void showToast(Context context, int stringId)
	{
		showToast(stringId);
	}

	/**
	 * Toast提示
	 * @param stringId  文字资源id
	 */
	public static void showToast(int stringId)
	{
		String show_str = mContext.getString(stringId);
		showToast(show_str);
	}

	/**
	 * Toast提示
	 * @param context
	 * @param show_str
	 */
	public static void showToast(Context context, String show_str)
	{
		if (TextUtils.isEmpty(show_str) || "null".equals(show_str))
		{
			return;
		}
		if (mToast == null)
		{
			mToast = Toast.makeText(context, "", Toast.LENGTH_LONG);
		}
		mToast.setText(show_str);
		mToast.setDuration(Toast.LENGTH_LONG);
		mToast.show();
	}

	public static void showToast(String str)
	{
		if (TextUtils.isEmpty(str) || "null".equals(str))
		{
			return;
		}
		if (mToast == null)
		{
			mToast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);
		}
		mToast.setText(str);
		mToast.setDuration(Toast.LENGTH_SHORT);
		mToast.show();
	}
	
	

	/**
	 * 隐藏输入法
	 * @param activity
	 */
	public static void hideSoftInput(Activity activity)
	{
		// 隐藏输入法
		InputMethodManager imm = (InputMethodManager) activity.getApplicationContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		View view = activity.getCurrentFocus();
		if (view != null)
		{
			imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	/**
	 * 隐藏输入法
	 */
	public static void hideSoftInput(Dialog dialog)
	{
		InputMethodManager mInputMethodManager = (InputMethodManager) dialog.getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (dialog != null)
		{
			mInputMethodManager.hideSoftInputFromWindow(dialog.getCurrentFocus().getWindowToken(),
					0);
		}
	}
	
	public static void showSoftInput(final EditText pwd_et){
		pwd_et.requestFocus();
		new Handler().postDelayed(new Runnable()
		{

			@Override
			public void run()
			{
				InputMethodManager inputManager = (InputMethodManager) pwd_et.getContext()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(pwd_et, 0);

			}
		}, 250);
	}
}
