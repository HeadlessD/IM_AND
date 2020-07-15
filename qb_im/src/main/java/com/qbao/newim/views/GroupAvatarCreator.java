package com.qbao.newim.views;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;

import java.util.ArrayList;

/**
 * Created by chenjian on 2017/6/22.
 * 群头像合并
 */

public class GroupAvatarCreator {
    public static final int MAX_COUNT = 9;
    private static final int BITMAP_SPAN = 4;
    private static final int BITMAP_PADDING = 2;

    public static Bitmap combineBitmap(ArrayList<Bitmap> bitmaps) {
        if (bitmaps == null || bitmaps.size() == 0) {
            return null;
        }

        int bgWidth = 100;
        int bgHeight = 100;

        Bitmap new_bitmap = Bitmap.createBitmap(bgWidth, bgHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(new_bitmap);
        canvas.drawColor(Color.parseColor("#e6e6e6"));
        drawBitmap(canvas, bitmaps, bgWidth, bgHeight);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return new_bitmap;
    }

    private static void drawBitmap(Canvas canvas, ArrayList<Bitmap> bitmaps, int bgWidth, int bgHeight) {
        final int count = bitmaps.size();

        if (count == 1) {
            drawBitmap1(canvas, bitmaps, bgWidth, bgHeight);
            return;
        }
        if (count == 2) {
            drawBitmap2(canvas, bitmaps, bgWidth, bgHeight);
            return;
        }
        if (count == 3) {
            drawBitmap3(canvas, bitmaps, bgWidth, bgHeight);
            return;
        }
        if (count == 4) {
            drawBitmap4(canvas, bitmaps, bgWidth, bgHeight);
            return;
        }
        if (count == 5) {
            drawBitmap5(canvas, bitmaps, bgWidth, bgHeight);
            return;
        }
        if (count == 6) {
            drawBitmap6(canvas, bitmaps, bgWidth, bgHeight);
            return;
        }
        if (count == 7) {
            drawBitmap7(canvas, bitmaps, bgWidth, bgHeight);
            return;
        }
        if (count == 8) {
            drawBitmap8(canvas, bitmaps, bgWidth, bgHeight);
            return;
        }
        if (count >= MAX_COUNT) {
            drawBitmap9(canvas, bitmaps, bgWidth, bgHeight);
            return;
        }
    }

    private static void drawBitmap1(Canvas canvas, ArrayList<Bitmap> bitmaps, int bgWidth, int bgHeight) {
        int width = bgWidth;
        int height = width;
        Bitmap bitmap = zoomBitmap(bitmaps.get(0), width, height);
        drawBitmap(canvas, bitmap, 0, 0);
    }

    private static void drawBitmap2(Canvas canvas, ArrayList<Bitmap> bitmaps, int bgWidth, int bgHeight) {
        int width = (bgWidth - BITMAP_PADDING * 2 - BITMAP_SPAN) / 2;
        int height = width;
        Bitmap bitmap = zoomBitmap(bitmaps.get(0), width, height);
        drawBitmap(canvas, bitmap, BITMAP_PADDING, (bgHeight - height) / 2);
        bitmap = zoomBitmap(bitmaps.get(1), width, height);
        drawBitmap(canvas, bitmap, BITMAP_PADDING + BITMAP_SPAN + width, (bgHeight - height) / 2);
    }

    private static void drawBitmap3(Canvas canvas, ArrayList<Bitmap> bitmaps, int bgWidth, int bgHeight) {
        int width = (bgWidth - BITMAP_PADDING * 2 - BITMAP_SPAN) / 2;
        int height = width;
        int top = (bgHeight - height * 2 - BITMAP_SPAN) / 2;
        Bitmap bitmap = zoomBitmap(bitmaps.get(0), width, height);
        drawBitmap(canvas, bitmap, (bgWidth - width) / 2, top);

        top = top + height + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(1), width, height);
        drawBitmap(canvas, bitmap, BITMAP_PADDING, top);

        bitmap = zoomBitmap(bitmaps.get(2), width, height);
        drawBitmap(canvas, bitmap, BITMAP_PADDING + BITMAP_SPAN + width, top);
    }

    private static void drawBitmap4(Canvas canvas, ArrayList<Bitmap> bitmaps, int bgWidth, int bgHeight) {
        int width = (bgWidth - BITMAP_PADDING * 2 - BITMAP_SPAN) / 2;
        int height = width;
        int left = BITMAP_PADDING;
        int top = (bgHeight - height * 2 - BITMAP_SPAN) / 2;

        //z型路线
        Bitmap bitmap = zoomBitmap(bitmaps.get(0), width, height);
        drawBitmap(canvas, bitmap, left, top);

        left = left + width + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(1), width, height);
        drawBitmap(canvas, bitmap, left, top);

        top = top + height + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(2), width, height);
        drawBitmap(canvas, bitmap, left, top);

        left = left - width - BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(3), width, height);
        drawBitmap(canvas, bitmap, left, top);
    }

    private static void drawBitmap5(Canvas canvas, ArrayList<Bitmap> bitmaps, int bgWidth, int bgHeight) {
        int width = (bgWidth - BITMAP_PADDING * 2 - BITMAP_SPAN * 2) / 3;
        int height = width;
        int left = (bgWidth - width * 2 - BITMAP_SPAN) / 2;
        int top = (bgHeight - height * 2 - BITMAP_SPAN) / 2;

        //第一行
        Bitmap bitmap = zoomBitmap(bitmaps.get(0), width, height);
        drawBitmap(canvas, bitmap, left, top);

        left = left + width + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(1), width, height);
        drawBitmap(canvas, bitmap, left, top);

        //第二行
        left = BITMAP_PADDING;
        top = top + height + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(2), width, height);
        drawBitmap(canvas, bitmap, left, top);

        left = left + width + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(3), width, height);
        drawBitmap(canvas, bitmap, left, top);

        left = left + width + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(4), width, height);
        drawBitmap(canvas, bitmap, left, top);
    }

    private static void drawBitmap6(Canvas canvas, ArrayList<Bitmap> bitmaps, int bgWidth, int bgHeight) {
        int width = (bgWidth - BITMAP_PADDING * 2 - BITMAP_SPAN * 2) / 3;
        int height = width;
        int left = BITMAP_PADDING;
        int top = (bgHeight - height * 2 - BITMAP_SPAN) / 2;

        //第一行
        Bitmap bitmap = zoomBitmap(bitmaps.get(0), width, height);
        drawBitmap(canvas, bitmap, left, top);

        left = left + width + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(1), width, height);
        drawBitmap(canvas, bitmap, left, top);


        left = left + width + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(2), width, height);
        drawBitmap(canvas, bitmap, left, top);

        //第二行
        left = BITMAP_PADDING;
        top = top + height + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(3), width, height);
        drawBitmap(canvas, bitmap, left, top);

        left = left + width + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(4), width, height);
        drawBitmap(canvas, bitmap, left, top);

        left = left + width + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(5), width, height);
        drawBitmap(canvas, bitmap, left, top);
    }

    private static void drawBitmap7(Canvas canvas, ArrayList<Bitmap> bitmaps, int bgWidth, int bgHeight) {
        int width = (bgWidth - BITMAP_PADDING * 2 - BITMAP_SPAN * 2) / 3;
        int height = width;
        int left = (bgWidth - width) / 2;
        int top = (bgHeight - height * 3 - BITMAP_SPAN * 2) / 2;

        //第一行
        Bitmap bitmap = zoomBitmap(bitmaps.get(0), width, height);
        drawBitmap(canvas, bitmap, left, top);

        //第二行
        left = BITMAP_PADDING;
        top = top + height + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(1), width, height);
        drawBitmap(canvas, bitmap, left, top);

        left = left + width + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(2), width, height);
        drawBitmap(canvas, bitmap, left, top);

        left = left + width + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(3), width, height);
        drawBitmap(canvas, bitmap, left, top);

        //第三行
        left = BITMAP_PADDING;
        top = top + height + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(4), width, height);
        drawBitmap(canvas, bitmap, left, top);

        left = left + width + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(5), width, height);
        drawBitmap(canvas, bitmap, left, top);

        left = left + width + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(6), width, height);
        drawBitmap(canvas, bitmap, left, top);
    }

    private static void drawBitmap8(Canvas canvas, ArrayList<Bitmap> bitmaps, int bgWidth, int bgHeight) {
        int width = (bgWidth - BITMAP_PADDING * 2 - BITMAP_SPAN * 2) / 3;
        int height = width;
        int left = (bgWidth - width * 2 - BITMAP_SPAN) / 2;
        int top = (bgHeight - height * 3 - BITMAP_SPAN * 2) / 2;

        //第一行
        Bitmap bitmap = zoomBitmap(bitmaps.get(0), width, height);
        drawBitmap(canvas, bitmap, left, top);

        left = left + width + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(1), width, height);
        drawBitmap(canvas, bitmap, left, top);

        //第二行
        left = BITMAP_PADDING;
        top = top + height + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(2), width, height);
        drawBitmap(canvas, bitmap, left, top);

        left = left + width + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(3), width, height);
        drawBitmap(canvas, bitmap, left, top);

        left = left + width + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(4), width, height);
        drawBitmap(canvas, bitmap, left, top);

        //第三行
        left = BITMAP_PADDING;
        top = top + height + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(5), width, height);
        drawBitmap(canvas, bitmap, left, top);

        left = left + width + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(6), width, height);
        drawBitmap(canvas, bitmap, left, top);

        left = left + width + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(7), width, height);
        drawBitmap(canvas, bitmap, left, top);
    }

    private static void drawBitmap9(Canvas canvas, ArrayList<Bitmap> bitmaps, int bgWidth, int bgHeight) {
        int width = (bgWidth - BITMAP_PADDING * 2 - BITMAP_SPAN * 2) / 3;
        int height = width;
        int left = BITMAP_PADDING;
        int top = (bgHeight - height * 3 - BITMAP_SPAN * 2) / 2;

        //第一行
        Bitmap bitmap = zoomBitmap(bitmaps.get(0), width, height);
        drawBitmap(canvas, bitmap, left, top);

        left = left + width + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(1), width, height);
        drawBitmap(canvas, bitmap, left, top);

        left = left + width + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(2), width, height);
        drawBitmap(canvas, bitmap, left, top);

        //第二行
        left = BITMAP_PADDING;
        top = top + height + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(3), width, height);
        drawBitmap(canvas, bitmap, left, top);

        left = left + width + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(4), width, height);
        drawBitmap(canvas, bitmap, left, top);

        left = left + width + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(5), width, height);
        drawBitmap(canvas, bitmap, left, top);

        //第三行
        left = BITMAP_PADDING;
        top = top + height + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(6), width, height);
        drawBitmap(canvas, bitmap, left, top);

        left = left + width + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(7), width, height);
        drawBitmap(canvas, bitmap, left, top);

        left = left + width + BITMAP_SPAN;
        bitmap = zoomBitmap(bitmaps.get(8), width, height);
        drawBitmap(canvas, bitmap, left, top);
    }

    private static void drawBitmap(Canvas canvas, Bitmap bitmap, int left, int top) {
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, left, top, null);
            bitmap.recycle();
        }
    }

    private static Bitmap zoomBitmap(Bitmap src, int destWidth, int destHeigth) {
        if (src == null) {
            return null;
        }

        int w = src.getWidth();
        int h = src.getHeight();

        float scaleWidth = ((float) destWidth) / w;
        float scaleHeight = ((float) destHeigth) / h;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(src, 0, 0, w, h, matrix, true);

        return resizedBitmap;
    }
}
