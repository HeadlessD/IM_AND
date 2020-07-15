package com.qbao.newim.util;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateFormat;

import com.qbao.newim.qbim.R;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 时间帮助类
 * 
 * @author zhangxiaolong
 * @since qianbao1.1
 */
public class DateUtil
{
	public static final long MINUTE_UNITE = 60 * 1000;
	public static final long HOUR_UNITE = 60 * MINUTE_UNITE;
	public static final long DAY_UNITE = 24 * HOUR_UNITE;
	public static String numeric_date_format_default = "yyyy-MM-dd EE";
	public static final String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};

	private static final Context mContext;
	static
	{
		mContext = AppUtil.GetContext();
	}

	/**
	 * 根据手机本地设置的语言和时间格式，显示：年月日 星期 （适合消息内容页面分割线）
	 * 
	 * @param context
	 *            上下文环境变量
	 * @param when
	 *            时间参数
	 * @return String 返回本地设置的时间格式格式化后的时间字串
	 */
	public static String formatTimeLocalString(Context context, long when)
	{
		String text = "";
		ContentResolver cv = context.getContentResolver();
		String format = android.provider.Settings.System.getString(cv,
				android.provider.Settings.System.DATE_FORMAT);
		if (!TextUtils.isEmpty(format))
		{
			text = (String) DateFormat.format(format, when);
			// 更新默认的时间设置为最近获取的值。
			numeric_date_format_default = format;
		}
		else
		{
			// 如果获取不到时间格式，则用系统默认格式来格式化
			text = DateFormat.getDateFormat(context).format(when);

			if (TextUtils.isEmpty(text))
			{
				// 如果还是获取不是时间格式，则用天天聊系统设置的默认格式来格式化时间。
				text = (String) DateFormat.format(numeric_date_format_default, when);
			}
		}

		return text;
	}

	public static String formatData(long data)
	{
		return formatData(data, true);
	}

	/**
	 * 计算时间，并以一定的格式返回 今天 16:00 昨天 16:00 2013.03.04 16:00
	 * 
	 * @param data
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public static String formatData(long data, boolean showThisYear)
	{
		if (data <= 0)
		{
			data = System.currentTimeMillis();
		}
		Date date = new Date(data);
		String format = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);

		StringBuffer sb = new StringBuffer();

		AppUtil.GetContext().getResources().getString(R.string.nim_yesterday);
		AppUtil.GetContext().getResources().getString(R.string.nim_today);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		final int bYear = c.get(Calendar.YEAR);
		final int bMonth = c.get(Calendar.MONTH);
		final int bDay = c.get(Calendar.DATE);

		c.setTime(new Date());
		final int cYear = c.get(Calendar.YEAR);
		final int cMonth = c.get(Calendar.MONTH);
		final int cDay = c.get(Calendar.DATE);
		if (cMonth == bMonth && cYear == bYear)
		{
			if (cDay == bDay)
			{
				sb.append(AppUtil.GetContext().getResources().getString(R.string.nim_today))
						.append(" ").append(format);
				return sb.toString();
			}
			if (cDay - bDay == 1)
			{
				sb.append(
						AppUtil.GetContext().getResources()
								.getString(R.string.nim_yesterday)).append(" ").append(format);
				return sb.toString();
			}
		}

		if (!showThisYear && cYear == bYear)
		{
			format = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(date);
		}
		else
		{
			format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date);
		}

		return format;
	}

	/**
	 * 计算时间，并以一定的格式返回
	 * 今天
	 * 昨天
	 * 日期
	 * 
	 * @param data
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public static String formatDate(long data)
	{
		if (data <= 0)
		{
			data = System.currentTimeMillis();
		}
		Date date = new Date(data);
		String format = new SimpleDateFormat("MM月dd日", Locale.getDefault()).format(date);

		StringBuffer sb = new StringBuffer();

		AppUtil.GetContext().getResources().getString(R.string.nim_yesterday);
		AppUtil.GetContext().getResources().getString(R.string.nim_today);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		final int bYear = c.get(Calendar.YEAR);
		final int bMonth = c.get(Calendar.MONTH);
		final int bDay = c.get(Calendar.DATE);

		c.setTime(new Date());
		final int cYear = c.get(Calendar.YEAR);
		final int cMonth = c.get(Calendar.MONTH);
		final int cDay = c.get(Calendar.DATE);
		if (cMonth == bMonth && cYear == bYear)
		{
			if (cDay == bDay)
			{
				sb.append(AppUtil.GetContext().getResources().getString(R.string.nim_today));
				return sb.toString();
			}
			if (cDay - bDay == 1)
			{
				sb.append(AppUtil.GetContext().getResources()
						.getString(R.string.nim_yesterday));
				return sb.toString();
			}
		}
		return format;
	}

	/**
	 * 计算时间，并以一定的格式返回 今天 / 昨天 /2013.03.04
	 * 
	 * @param data
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public static String formatData(String data)
	{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		if (data == null || data.length() <= 0)
		{
			return format.format(new Date());
		}
		Calendar today = Calendar.getInstance();
		today.add(Calendar.DATE, 0);
		today.getTime();
		String todayString = format.format(today.getTime());
		Calendar yestoday = Calendar.getInstance();
		yestoday.add(Calendar.DATE, -1);
		String yestodayString = format.format(yestoday.getTime());
		if (data.equals(todayString))
		{
			return AppUtil.GetContext().getResources().getString(R.string.nim_today);
		}
		if (data.equals(yestodayString))
		{
			return AppUtil.GetContext().getResources().getString(R.string.nim_yesterday);
		}
		return data;
	}

	public static String formatVisitorDate(long date)
	{
		long second = 1000;
		long minutes = 60 * second;
		long hour = minutes * 60;
		long day = hour * 24;
		SimpleDateFormat format = null;
		String str = null;
		long currentDate = System.currentTimeMillis();
		if (date > currentDate)
		{
			format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
			str = format.format(new Date(date));
			return str;
		}
		long interval = currentDate - date;
		if (interval < minutes)
		{
			return AppUtil.GetContext().getString(R.string.nim_just_now);
		}
		if (interval >= minutes && interval < hour)
		{

			return AppUtil.GetContext().getString(R.string.nim_many_minutes_ago,
					String.valueOf(interval / minutes));
		}
		if (interval >= hour && interval < day)
		{
			return AppUtil.GetContext().getString(R.string.nim_many_hour_ago,
					String.valueOf(interval / hour));
		}
		Date oldDate = new Date(date);
		Date newDate = new Date(currentDate);
		Calendar c = Calendar.getInstance();
		c.setTime(oldDate);
		int oldDay = c.get(Calendar.DATE);
		c.setTime(newDate);
		int newDay = c.get(Calendar.DATE);
		int mDay = newDay - oldDay;
		if (mDay >= 1 && mDay <= 5)
		{
			return AppUtil.GetContext().getString(R.string.nim_many_day_ago, String.valueOf(mDay));
		}
		int oldYear = c.get(Calendar.YEAR);
		int newYear = c.get(Calendar.YEAR);
		int mYear = newYear - oldYear;
		if (mDay > 5 && mYear == 0)
		{
			format = new SimpleDateFormat("MM-dd", Locale.getDefault());
			str = format.format(oldDate);
			return str;
		}
		if (mYear >= 1)
		{
			format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
			str = format.format(oldDate);
			return str;
		}
		return str;
	}

	/**
	 * 格式化聊天时间
	 * 
	 * @param data 需要格式化时间
	 * @param showThisYear 如果是当前年 是否显示年
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public static String formatChatDate(long data, boolean showThisYear)
	{
		final long cDate = System.currentTimeMillis();
		if (data <= 0 || data > cDate)
		{
			data = System.currentTimeMillis();
		}

		final Date date = new Date(data);
		final SimpleDateFormat sdf = new SimpleDateFormat();

		final Calendar c = Calendar.getInstance();

		//需格式化时间
		c.setTime(date);
		final int bYear = c.get(Calendar.YEAR);
		final int bMonth = c.get(Calendar.MONTH);
		final int bDay = c.get(Calendar.DATE);

		//当前时间
		c.setTime(new Date());
		final int cYear = c.get(Calendar.YEAR);
		final int cMonth = c.get(Calendar.MONTH);
		final int cDay = c.get(Calendar.DATE);

		if (cYear != bYear || showThisYear)
		{
			sdf.applyPattern("yyyy-MM-dd HH:mm");
			return sdf.format(date);
		}

		if (cMonth == bMonth)
		{
			final StringBuffer sb = new StringBuffer();
			if (cDay == bDay)
			{
				sdf.applyPattern("HH:mm");
				return sdf.format(date);
			}

			if (cDay - bDay == 1)
			{
				sdf.applyPattern("HH:mm");
				final String yesterday = mContext.getResources().getString(R.string.nim_yesterday);
				final String formatDate = sdf.format(date);
				sb.append(yesterday).append(" ").append(formatDate);
				return sb.toString();
			}
		}

		sdf.applyPattern("MM-dd HH:mm");
		return sdf.format(date);
	}

	public static String formatSnsDate(long data)
	{
		final long cDate = System.currentTimeMillis();
		if (data <= 0 || data > cDate)
		{
			data = cDate;
		}

		final Date date = new Date(data);
		final SimpleDateFormat sdf = new SimpleDateFormat();

		final Calendar c = Calendar.getInstance();

		//需格式化时间
		c.setTime(date);
		final int year = c.get(Calendar.YEAR);
		final int day = c.get(Calendar.DATE);

		//当前时间
		c.setTime(new Date());
		final int cYear = c.get(Calendar.YEAR);
		final int cDay = c.get(Calendar.DATE);
		final int cHour = c.get(Calendar.HOUR_OF_DAY);
		final int cMinute = c.get(Calendar.MINUTE);
		final int cSecond = c.get(Calendar.SECOND);
		// 当前星期几
		int week = c.get(Calendar.DAY_OF_WEEK) - 1;

		if (week < 0) {
			week = 0;
		}

		long tDate = cDate - data;
		if (day == cDay) {
			sdf.applyPattern("HH:mm");
			return sdf.format(date);
		}
		else if (tDate <= (DAY_UNITE + cHour * HOUR_UNITE + cMinute * MINUTE_UNITE + cSecond * 1000)) {
			sdf.applyPattern("昨天 HH:mm");
			return sdf.format(date);
		} else if (cDay - day > 1 && cDay - day <= 7) {
			sdf.applyPattern("HH:mm");
			String time = sdf.format(date);
			int day_dif = cDay - day;
			int week_dif = week - day_dif;
			if (week_dif < 0) {
				week_dif = weekDays.length + week_dif;
			}
			String week_now = weekDays[week_dif];
			return week_now + " " + time;
		} else if (cDay - day > 7 && cYear == year){
			sdf.applyPattern("MM-dd HH:mm");
			return sdf.format(date);
		}

		sdf.applyPattern("yyyy-MM-dd HH:mm");
		return sdf.format(date);
	}

	/**
	 * 格式化聊天时间
	 * 
	 * @param data 需要格式化时间
	 * @param showThisYear 如果是当前年 是否显示年
	 * @return
	 */
	public static String formatChatDateWidthoutHM(long data, boolean showThisYear)
	{
		final long cDate = System.currentTimeMillis();
		if (data <= 0 || data > cDate)
		{
			data = System.currentTimeMillis();
		}

		final Date date = new Date(data);
		final SimpleDateFormat sdf = new SimpleDateFormat();

		final Calendar c = Calendar.getInstance();

		//需格式化时间
		c.setTime(date);
		final int bYear = c.get(Calendar.YEAR);

		//当前时间
		c.setTime(new Date());
		final int cYear = c.get(Calendar.YEAR);

		if (cYear != bYear || showThisYear)
		{
			sdf.applyPattern("yyyy年MM月dd日");
			return sdf.format(date);
		}

		sdf.applyPattern("MM月dd日");
		return sdf.format(date);
	}

	public static String formatTaskDate(String dateStr)
	{
		if (TextUtils.isEmpty(dateStr))
		{
			return dateStr;
		}
		try
		{
			SimpleDateFormat sdf_old = null;
			if (dateStr.contains(":"))
			{
				sdf_old = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
			}
			else
			{
				sdf_old = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
			}
			final Date date = sdf_old.parse(dateStr);
			final SimpleDateFormat sdf = new SimpleDateFormat();
			final Calendar c = Calendar.getInstance();
			// 需格式化时间
			c.setTime(date);
			final int bYear = c.get(Calendar.YEAR);
			// 当前时间
			c.setTime(new Date());
			final int cYear = c.get(Calendar.YEAR);

			if (cYear == bYear)
			{
				sdf.applyPattern("MM-dd");
				dateStr = sdf.format(date);
			}
			else
			{
				sdf.applyPattern("yyyy-MM-dd");
				dateStr = sdf.format(date);
			}
		}
		catch (ParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dateStr;
	}

	public static boolean isTimeLater(long beforeTime, long time)
	{
		long currentTime = System.currentTimeMillis();
		long elapseTime = currentTime - beforeTime;
		return elapseTime > time;
	}

	public static String surplusTime(long beforeTime, long time)
	{
		long HOUR = 60 * 60 * 1000;
		long MINUTES = 60 * 1000;
		long currentTime = System.currentTimeMillis();
		long surplusTime = time - (currentTime - beforeTime);
		if (surplusTime < 0)
		{
			return "";
		}
		int hour = (int) (surplusTime / HOUR);
		int minutes = (int) ((surplusTime % HOUR) / MINUTES);
		StringBuffer sb = new StringBuffer();
		if (hour != 0)
		{
			sb.append(hour);
			sb.append(AppUtil.GetContext().getString(R.string.nim_hour));
		}
		if (minutes != 0)
		{
			sb.append(minutes);
			sb.append(AppUtil.GetContext().getString(R.string.nim_minutes));
		}
		return sb.toString();
	}

	public static String formatDate(String formatDate)
	{
		return formatDate(formatDate, false);
	}

	public static String formatDate(String formatDate, boolean isShowYear)
	{
		if (TextUtils.isEmpty(formatDate))
		{
			return formatDate;
		}
		SimpleDateFormat resultFormat = null;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		if (isShowYear)
		{
			resultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		}
		else
		{
			resultFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
		}
		try
		{
			Date date = format.parse(formatDate);
			return resultFormat.format(date);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		return formatDate;
	}

	@SuppressLint("SimpleDateFormat")
	public static String formatDataForRefundRecord(long data, boolean showThisYear)
	{
		if (data <= 0)
		{
			data = System.currentTimeMillis();
		}
		Date date = new Date(data);

		Calendar c = Calendar.getInstance();
		c.setTime(date);
		final int bYear = c.get(Calendar.YEAR);

		c.setTime(new Date());
		final int cYear = c.get(Calendar.YEAR);

		if (!showThisYear && cYear == bYear)
		{
			return new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(date);
		}
		else
		{
			return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date);
		}

	}
	
	@SuppressLint("SimpleDateFormat")
	public static String formatData(long data, boolean showThisYear, boolean showThisDay)
	{
		if (data <= 0)
		{
			data = System.currentTimeMillis();
		}
		Date date = new Date(data);

		Calendar c = Calendar.getInstance();
		c.setTime(date);
		final int bYear = c.get(Calendar.YEAR);
		final int bDay = c.get(Calendar.DATE);

		c.setTime(new Date());
		final int cYear = c.get(Calendar.YEAR);
		final int cDay = c.get(Calendar.DATE);

		if (cYear == bYear)
		{
			if (!showThisYear)
			{
				if (bDay == cDay && !showThisDay)
				{
					return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
				}
				return new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(date);
			}
		}
		
		return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date);
	}

	public static String formatBasicTime(long data)
	{
		final long cDate = System.currentTimeMillis();
		if (data <= 0 || data > cDate)
		{
			data = System.currentTimeMillis();
		}

		final Date date = new Date(data);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		return format.format(date);
	}
	
	public static String getCurrentYearMonthDay()
	{
		 Calendar c = Calendar.getInstance();
		 int year = c.get(Calendar.YEAR);
		 int month = c.get(Calendar.MONTH);
		 int day = c.get(Calendar.DAY_OF_MONTH);
		 StringBuilder sb = new StringBuilder();
		 sb.append(year);
		 sb.append(month);
		 sb.append(day);
		 return sb.toString();
	}

	public static String formatDateText(String date) {
		return date.replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
	}

	/**
	 * 2017-5-17转long毫秒
	 * @param data
	 * @return
	 */
	public static long formatDateToLong(String data) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		try {
			return simpleDateFormat.parse(data).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return 0;
	}

	/**
	 * long毫秒转2019-05-16 15:23
	 * @param time
	 * @return
	 */
	public static String  formatLongToString(long time) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
		Date date = new Date(time);
		String dateStr = simpleDateFormat.format(date);
		return dateStr;
	}

	public static int longToAge(long time) {
		if (time < 0) {
			return 99;
		}
		Date now_date = new Date();
		long day = (now_date.getTime() - time) / (24 * 60 * 60 * 1000) + 1;
		String age = new DecimalFormat("#.00").format(day / 365f);
		float nAge = Float.parseFloat(age);
		return (int)nAge;
	}

}
