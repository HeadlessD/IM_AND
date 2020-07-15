package com.qbao.newim.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.qbao.newim.qbim.R;

/**
 * Created by chenjian on 2017/5/5.
 */

public class BubbleImageView extends android.support.v7.widget.AppCompatImageView {
    private Context context;
    private Bitmap iconBitmap;
    private int res;

    private static final int OK_INT = 0x0001;
    private static final int ERROR_INT = 0x0000;

    private static final int DEFAULT_WIDTH_SCALE = 3;
    private static final int DEFAULT_HEIGHT_SCALE = 4;

    private Handler bitmapHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case OK_INT:
                    Bitmap bitmap_bg = BitmapFactory.decodeResource(getResources(),
                            res);
                    final Bitmap bp = getRoundCornerImage(bitmap_bg, iconBitmap);
                    setImageBitmap(bp);
                    break;
                case ERROR_INT:
                    break;
            }
            return false;
        }
    });

    public BubbleImageView(Context context) {
        super(context);
        this.context = context;
    }

    public BubbleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public BubbleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    public void load(String url, int res) {
        this.res = res;
        if (TextUtils.isEmpty(url)) {
            return;
        }

        if (url.startsWith("http")) {
            Glide.with(context).load(url).asBitmap().placeholder(R.drawable.nim_chat_default_img)
                    .error(R.drawable.nim_chat_default_img)
                    .into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    if (resource != null) {
                        setImageBitmap(resource);
                        iconBitmap = resource;
                        bitmapHandler.sendEmptyMessage(OK_INT);
                    }
                }
            });
        } else {
            setLocalImageBitmap(url);
        }

    }

    public void setLocalImageBitmap(String url) {
        Bitmap origin = BitmapFactory.decodeFile(url);
        Bitmap bitmap_bg = BitmapFactory.decodeResource(getContext().getResources(), res);
        final Bitmap bp = getRoundCornerImage(bitmap_bg, origin);
        setImageBitmap(bp);
    }

    public void setLocalImageBitmap(int bg_res, int res) {
        Bitmap origin = BitmapFactory.decodeResource(getContext().getResources(), bg_res);
        Bitmap bitmap_bg = BitmapFactory.decodeResource(getContext().getResources(), res);
        final Bitmap bp = getRoundCornerImage(bitmap_bg, origin);
        setImageBitmap(bp);
    }

    // 圆角处理并叠加三角形形状
    public Bitmap getRoundCornerImage(Bitmap bitmap_bg, Bitmap bitmap_in) {
        int width = bitmap_in.getWidth();
        int height = bitmap_in.getHeight();
        if(height != 0){
            double scale = (width * 1.00) / height;
            if (width >= height) {
                width = getBitmapWidth();
                height = (int) (width / scale);
            } else {
                height = getBitmapHeight();
                width = (int) (height * scale);
            }
        }else{
            width = 400;
            height = 400;
        }
        Bitmap roundCornerImage = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(roundCornerImage);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, width, height);
        Rect rectF = new Rect(0, 0, bitmap_in.getWidth(), bitmap_in.getHeight());
        paint.setAntiAlias(true);
        NinePatch patch = new NinePatch(bitmap_bg,
                bitmap_bg.getNinePatchChunk(), null);
        patch.draw(canvas, rect);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap_in, rectF, rect, paint);
        return roundCornerImage;
    }

    // 获取屏幕的宽度
    public int getScreenWidth(Context context) {
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.widthPixels;
    }

    // 获取屏幕的高度
    public int getScreenHeight(Context context) {
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.heightPixels;
    }

    public int getBitmapWidth() {
        return getScreenWidth(context) / DEFAULT_WIDTH_SCALE;
    }

    public int getBitmapHeight() {
        return getScreenHeight(context) / DEFAULT_HEIGHT_SCALE;
    }
}
