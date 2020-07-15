package com.google.zxing.client.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.camera.CameraManager;
import com.qbao.newim.qbim.R;

public final class ViewfinderView extends View
{
	private CameraManager cameraManager;
	private final Paint paint;
	private Bitmap resultBitmap;
	private final int maskColor;
	private int scanLineY;

	private Bitmap scanFrame1;
	private Bitmap scanFrame2;
	private Bitmap scanFrame3;
	private Bitmap scanFrame4;
	private Bitmap scanLine;
	private BitmapDrawable scanLineDrawable;
	
	public ViewfinderView(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		maskColor = (ContextCompat.getColor(context, R.color.viewfinder_mask));
		scanLineY = 0;
		
		scanFrame1 = BitmapFactory.decodeResource(getResources(), R.mipmap.nim_scan_frame1);
		scanFrame2 = BitmapFactory.decodeResource(getResources(), R.mipmap.nim_scan_frame2);
		scanFrame3 = BitmapFactory.decodeResource(getResources(), R.mipmap.nim_scan_frame3);
		scanFrame4 = BitmapFactory.decodeResource(getResources(), R.mipmap.nim_scan_frame4);
		scanLine = BitmapFactory.decodeResource(getResources(), R.mipmap.nim_scan_line);
		scanLineDrawable = new BitmapDrawable(getResources(), scanLine);
	}

	public void setCameraManager(CameraManager cameraManager)
	{
		this.cameraManager = cameraManager;
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		if (cameraManager == null)
		{
			return; // not ready yet, early draw before done configuring
		}
		Rect frame = cameraManager.getFramingRect();
		if (frame == null)
		{
			return;
		}
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		// Draw the exterior (i.e. outside the framing rect) darkened
		paint.setColor(maskColor);
		canvas.drawRect(0, 0, width, frame.top, paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom, paint);
		canvas.drawRect(frame.right, frame.top, width, frame.bottom, paint);
		canvas.drawRect(0, frame.bottom, width, height, paint);
		
		//画窗口
//		canvas.drawBitmap(scanFrame, frame.left, frame.top, paint);
		canvas.drawBitmap(scanFrame1, frame.left - 1, frame.top - 1, paint);
		canvas.drawBitmap(scanFrame2, frame.right - scanFrame2.getWidth() + 1, frame.top - 1, paint);
		canvas.drawBitmap(scanFrame3, frame.left - 1, frame.bottom - scanFrame3.getHeight() + 1, paint);
		canvas.drawBitmap(scanFrame4, frame.right - scanFrame4.getWidth() + 1, frame.bottom - scanFrame4.getHeight() + 1, paint);

		if(scanLineY < frame.height() - scanLine.getHeight() / 2)
		{
//			int lineLeft = frame.left + (frame.width() - scanLine.getWidth()) / 2;
//			canvas.drawBitmap(scanLine, lineLeft, frame.top + scanLineY, paint);
			scanLineDrawable.setBounds(frame.left, frame.top + scanLineY, frame.right, frame.top + scanLineY + scanLine.getHeight());
			scanLineDrawable.draw(canvas);
		}
		
		scanLineY += 4;
		if(scanLineY >= frame.height() * 1.5)
		{
			scanLineY = 0;
		}
		
		invalidate(frame);
	}

	public void drawViewfinder()
	{
		Bitmap resultBitmap = this.resultBitmap;
		this.resultBitmap = null;
		if (resultBitmap != null)
		{
			resultBitmap.recycle();
		}
		invalidate();
	}

	/**
	 * Draw a bitmap with the result points highlighted instead of the live scanning display.
	 *
	 * @param barcode An image of the decoded barcode.
	 */
	public void drawResultBitmap(Bitmap barcode)
	{
		resultBitmap = barcode;
		invalidate();
	}

	public void addPossibleResultPoint(ResultPoint point)
	{

	}

	public int getBottomPosition()
	{
		if(cameraManager == null || cameraManager.getFramingRect() == null)
		{
			return 0;
		}
		return cameraManager.getFramingRect().bottom;
	}
}
