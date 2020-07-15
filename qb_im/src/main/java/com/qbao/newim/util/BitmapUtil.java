package com.qbao.newim.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.TextUtils;

import com.qbao.newim.configure.Constants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 图片处理工具类
 * 
 * @author GalaxyBruce
 */
public class BitmapUtil {
	private static final String TAG = "---BitmapUtils---";

	/**
	 * 保存图片到sd卡中
	 * 
	 * @param filePath
	 *            保存的路径
	 * @param bitmap
	 *            源文件
	 * @return
	 */
	public static boolean saveBitmapToSD(String filePath, Bitmap bitmap) {
		FileOutputStream fos = null;
		boolean isSuccess = false;
		try {
			fos = new FileOutputStream(filePath);
			if (null != fos) {
				isSuccess = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (null != fos) {
				try {
					fos.flush();
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return isSuccess;
	}

	/**
	 * 读取图片属性：旋转的角度
	 *
	 * @param path
	 *            图片绝对路径
	 * @return degree旋转的角度
	 */
	public static int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/**
	 * 旋转图片
	 *
	 * @param angle
	 * @param bitmap
	 * @return Bitmap
	 */
	public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
		if (angle == 0 || bitmap == null) {
			return bitmap;
		}

		// 旋转图片 动作
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		// 创建新的图片
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
				bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		bitmap.recycle();
		return resizedBitmap;
	}

	/**
	 * byte数组转化成bitmap
	 *
	 * @param head
	 * @return
	 */
	public static Bitmap byte2Bitmap(byte[] head) {
		Bitmap bmp = null;
		if (head == null || head.length <= 0) {
			return bmp;
		}
		return BitmapFactory.decodeByteArray(head, 0, head.length);
	}

	/**
	 * bitmap转化成byte数组
	 *
	 * @param bm
	 * @return
	 */
	public static byte[] bitmap2Bytes(Bitmap bm) {
		if (bm == null) {
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	/**
	 * 扫描整个sdcard更新系统数据库
	 */
	public static void scanMedia(Context context) {
		context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri
				.parse("file://"
						+ Environment.getExternalStorageDirectory()
								.getAbsolutePath())));
	}

	/**
	 * 扫描指定文件
	 * 
	 * @param context
	 */
	public static void scanFile(Context context, String localPath) {
		Uri data = Uri.fromFile(new File(localPath));
		context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));
	}

	public static Bitmap mergeBitmap(Bitmap firstBitmap, Bitmap secondBitmap)
	{
		if(firstBitmap == null) return secondBitmap;
		if(secondBitmap == null) return firstBitmap;
		
		Bitmap bitmap = Bitmap.createBitmap(firstBitmap.getWidth(), firstBitmap.getHeight(), firstBitmap.getConfig());
		Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(firstBitmap, new Matrix(), null);
		
		int left = (firstBitmap.getWidth() - secondBitmap.getWidth()) / 2;
		int top = (firstBitmap.getHeight() - secondBitmap.getHeight()) / 2;
		canvas.drawBitmap(secondBitmap, left, top, null);
		
		return bitmap;
	}

	public static BitmapFactory.Options createOptions(String filePath, int maxWidth, int maxHeight) {
		File bitmapFile = new File(filePath);

		if (!bitmapFile.exists() || !bitmapFile.isFile()) {
			return null;
		}

		BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
		decodeOptions.inInputShareable = true;
		decodeOptions.inPurgeable = true;
		decodeOptions.inDither = false;
//		decodeOptions.inPreferredConfig = Config.RGB_565;
		if (maxWidth == 0 && maxHeight == 0) {
			
			decodeOptions.inJustDecodeBounds = false;
			decodeOptions.inSampleSize = 1;
		} else {
			// If we have to resize this image, first get the natural bounds.
			decodeOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(bitmapFile.getAbsolutePath(),
					decodeOptions);
			int actualWidth = decodeOptions.outWidth;
			int actualHeight = decodeOptions.outHeight;

			// Then compute the dimensions we would ideally like to decode to.
			int desiredWidth = getResizedDimension(maxWidth, maxHeight,
					actualWidth, actualHeight);
			int desiredHeight = getResizedDimension(maxHeight, maxWidth,
					actualHeight, actualWidth);

			// Decode to the nearest power of two scaling factor.
			decodeOptions.inJustDecodeBounds = false;
			decodeOptions.inSampleSize = findBestSampleSize(actualWidth,
					actualHeight, desiredWidth, desiredHeight);
		}

       return decodeOptions;
    }
	
	private static int getResizedDimension(int maxPrimary, int maxSecondary,
			int actualPrimary, int actualSecondary) {
		// If no dominant value at all, just return the actual.
		if (maxPrimary == 0 && maxSecondary == 0) {
			return actualPrimary;
		}

		// If primary is unspecified, scale primary to match secondary's scaling
		// ratio.
		if (maxPrimary == 0) {
			double ratio = (double) maxSecondary / (double) actualSecondary;
			return (int) (actualPrimary * ratio);
		}

		if (maxSecondary == 0) {
			return maxPrimary;
		}

		double ratio = (double) actualSecondary / (double) actualPrimary;
		
		if(ratio >= 3.0f)
		{
			return maxPrimary;
		}
		else if(ratio <= 0.3f)
		{
			return actualPrimary;
		}
		
		int resized = maxPrimary;
		if (resized * ratio > maxSecondary) {
			resized = (int) (maxSecondary / ratio);
		}
		return resized;
	}
	
	private static int findBestSampleSize(int actualWidth, int actualHeight,
			int desiredWidth, int desiredHeight) {
		double wr = (double) actualWidth / desiredWidth;
		double hr = (double) actualHeight / desiredHeight;
		double ratio = Math.min(wr, hr);
		float n = 1.0f;
		while ((n * 2) <= ratio) {
			n *= 2;
		}

		return (int) n;
	}
	
	public static String saveBitmap(Context context, Bitmap bitmap) {
		File dir = new File(Constants.ICON_PIC_DIR);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		String fileName = FileUtil.getUniqueName(".jpg", "qianbao");
		String filePath = dir + "/" + fileName;
		if (!BitmapUtil.saveBitmapToSD(filePath, bitmap)) {
			return "";
		}

		return filePath;
	}
	
	public static String saveBitmapForPath(Context context, Bitmap bitmap) {
		File dir = new File(Constants.ICON_PIC_DIR);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		String fileName = FileUtil.getUniqueName(".jpg", "qianbao");
		String filePath = dir + "/" + fileName;
		if (BitmapUtil.saveBitmapToSD(filePath, bitmap)) {
			return filePath;
		} else {
			return null;
		}
	}
	
	public static String createUploadBitmap(String filePath, int maxWidth, int maxHeight)
	{
		return createUploadBitmap(filePath, Constants.ICON_PIC_DIR, "_upload", maxWidth, maxHeight);
	}

	public static String createThumbnail(String filePath, int maxWidth, int maxHeight)
	{
		return createUploadBitmap(filePath, Constants.ICON_THUMB_DIR, "_thumb", maxWidth, maxHeight);
	}

	/**
	 * 上传图片如果不是原图上传，需要压缩
	 *
	 * @param filePath
	 * @param maxWidth
	 * @param maxHeight
	 * @return
	 */
	public static String createUploadBitmap(String filePath, String dir, String mark, int maxWidth, int maxHeight)
	{
		if (TextUtils.isEmpty(filePath))
		{
			return null;
		}

		int degree = readPictureDegree(filePath);
		//step1.计算最佳压缩比 inSampleSize=1 不用压缩
		BitmapFactory.Options decodeOptions = createOptions(filePath, maxWidth, maxHeight);
		if(decodeOptions == null)
		{
			return null;
		}

		if(decodeOptions.inSampleSize == 1 && degree == 0)
		{
			return filePath;
		}

		File bitmapFile = new File(filePath);
		String fileName = bitmapFile.getName();
		String fileExtension = ".jpg";
		String uploadFilePath = null;
		int lastDotIndex = fileName.lastIndexOf(".");
		if(lastDotIndex > 0)
		{
			uploadFilePath = dir + "/" + fileName.substring(0, lastDotIndex) + mark + fileExtension;
		}
		else
		{
			uploadFilePath = dir + "/" + fileName + mark;
		}

		//step2.判断否存在相同的图片，如果存在直接返回
		File uploadFile = new File(uploadFilePath);
		if(uploadFile.exists())
		{
			return uploadFilePath;
		}

		//step3.压缩图片
		Bitmap bitmap = BitmapFactory.decodeFile(filePath, decodeOptions);
		if(bitmap == null)
		{
			return filePath;
		}

		//step4.旋转角度
		if (degree != 0)
		{
			bitmap = rotaingImageView(degree, bitmap);
		}

		//step5.将压缩的图片存入sdcard
		FileUtil.ensureAppPath(dir);
		saveBitmapToSD(uploadFilePath, bitmap);
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
		}

		return uploadFilePath;
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		Bitmap bitmap;

		//如果本身就是BitmapDrawable类型 直接转换即可
		if (drawable instanceof BitmapDrawable) {
			BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
			if (bitmapDrawable.getBitmap() != null) {
				return bitmapDrawable.getBitmap();
			}
		}

		//取得Drawable固有宽高
		if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
			//创建一个1x1像素的单位色图
			bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
		} else {
			//直接设置一下宽高和ARGB
			bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		}

		//重新绘制Bitmap
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	private static final float BITMAP_SCALE = 0.4f;

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public static Bitmap blurBitmap(Context context, Bitmap image, float blurRadius) {
		// 计算图片缩小后的长宽
		int width = Math.round(image.getWidth() * BITMAP_SCALE);
		int height = Math.round(image.getHeight() * BITMAP_SCALE);

		// 将缩小后的图片做为预渲染的图片。
		Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
		// 创建一张渲染后的输出图片。
		Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

		// 创建RenderScript内核对象
		RenderScript rs = RenderScript.create(context);
		// 创建一个模糊效果的RenderScript的工具对象
		ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

		// 由于RenderScript并没有使用VM来分配内存,所以需要使用Allocation类来创建和分配内存空间。
		// 创建Allocation对象的时候其实内存是空的,需要使用copyTo()将数据填充进去。
		Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
		Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);

		// 设置渲染的模糊程度, 25f是最大模糊度
		blurScript.setRadius(blurRadius);
		// 设置blurScript对象的输入内存
		blurScript.setInput(tmpIn);
		// 将输出数据保存到输出内存中
		blurScript.forEach(tmpOut);

		// 将数据填充到Allocation中
		tmpOut.copyTo(outputBitmap);

		return outputBitmap;
	}

	public static Bitmap clipBitmap(Context context, Bitmap origin_res) {
		int width = origin_res.getWidth();
		int height = origin_res.getHeight();

		int window_width = ScreenUtils.getSceenWidth(context);
		float scale = (float) window_width / width;

		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);

		int real_height = ScreenUtils.dp2px(context, 240);
		int clip_height;
		if (real_height > height) {
			clip_height = height;
		} else {
			clip_height = real_height;
		}

		Bitmap showPic = Bitmap.createBitmap(origin_res, 0, height - clip_height, width, clip_height);
		return showPic;
	}

	@SuppressLint("NewApi")
	public static Bitmap fastblur(Context context, Bitmap sentBitmap, int radius) throws OutOfMemoryError,Exception {

		if (sentBitmap == null) return null;

		if (Build.VERSION.SDK_INT > 16) {// api16以上的处理高斯模糊,速度快
			Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

			if (bitmap == null) return null;

			final RenderScript rs = RenderScript.create(context);
			final Allocation input = Allocation.createFromBitmap(rs, sentBitmap, Allocation.MipmapControl.MIPMAP_NONE,
					Allocation.USAGE_SCRIPT);
			final Allocation output = Allocation.createTyped(rs, input.getType());
			final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
			script.setRadius(radius /* e.g. 3.f */);
			script.setInput(input);
			script.forEach(output);
			output.copyTo(bitmap);
			return bitmap;
		}

		Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

		if (bitmap == null) return null;

		if (radius < 1) {
			return (null);
		}


		int w = bitmap.getWidth();
		int h = bitmap.getHeight();


		int[] pix = new int[w * h];
//        Log.e("pix", w + " " + h + " " + pix.length);
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);


		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;


		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int vmin[] = new int[Math.max(w, h)];


		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int temp = 256 * divsum;
		int dv[] = new int[temp];
		for (i = 0; i < temp; i++) {
			dv[i] = (i / divsum);
		}


		yw = yi = 0;


		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;


		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}
			}
			stackpointer = radius;


			for (x = 0; x < w; x++) {


				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];


				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;


				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];


				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];


				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);
				}
				p = pix[yw + vmin[x]];


				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);


				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];


				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;


				stackpointer = (stackpointer + 1) % div;
				sir = stack[(stackpointer) % div];


				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];


				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];


				yi++;
			}
			yw += w;
		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;


				sir = stack[i + radius];


				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];


				rbs = r1 - Math.abs(i);


				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;


				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}


				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				// Preserve alpha channel: ( 0xff000000 & pix[yi] )
				pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];


				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;


				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];


				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];


				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;
				}
				p = x + vmin[y];


				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];


				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];


				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;


				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];


				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];


				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];


				yi += w;
			}
		}

//        Log.e("pix", w + " " + h + " " + pix.length);
		bitmap.setPixels(pix, 0, w, 0, 0, w, h);
		return (bitmap);
	}

}
