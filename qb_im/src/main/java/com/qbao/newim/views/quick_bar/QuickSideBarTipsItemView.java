package com.qbao.newim.views.quick_bar;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.qbao.newim.qbim.R;

/**
 * Created by chenjian on 2017/8/31.
 */

public class QuickSideBarTipsItemView extends View {
    private int mCornerRadius;

    private Path mBackgroundPath = new Path();
    private RectF mBackgroundRect = new RectF();
    private Paint mBackgroundPaint;

    private String mText ="";

    private Paint mTextPaint;
    private int mWidth;
    private int mItemHeight;
    private float mTextSize;
    private int mTextColor;
    private int mBackgroundColor;
    private int mCenterTextStartX;
    private int mCenterTextStartY;


    public QuickSideBarTipsItemView(Context context) {
        this(context, null);
    }

    public QuickSideBarTipsItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QuickSideBarTipsItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        mTextColor = ContextCompat.getColor(context, android.R.color.black);
        mBackgroundColor = ContextCompat.getColor(context, android.R.color.darker_gray);
        mTextSize = context.getResources().getDimension(R.dimen.nim_textSize_quick_sidebar_tips);
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.QuickSideBarView);

            mTextColor = a.getColor(R.styleable.QuickSideBarView_sidebarTextColor, mTextColor);
            mBackgroundColor = a.getColor(R.styleable.QuickSideBarView_sidebarBackgroundColor, mBackgroundColor);
            mTextSize = a.getDimension(R.styleable.QuickSideBarView_sidebarTextSize, mTextSize);
            a.recycle();
        }

        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setColor(mBackgroundColor);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getWidth();
        mItemHeight = mWidth;
        mCornerRadius = (int) (mWidth * 0.5);

    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (TextUtils.isEmpty(mText) || mWidth == 0)return;
        if (mCenterTextStartX <= 0) {
            setText(mText);
        }
        canvas.drawColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
        float[] radii;

        mBackgroundRect.set(0, 0, mWidth, mItemHeight);
        if (isRtl()) {
            radii = new float[]{mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, 0, 0};
        } else {

            radii = new float[]{mCornerRadius, mCornerRadius, mCornerRadius, mCornerRadius, 0, 0, mCornerRadius, mCornerRadius};
        }

        mBackgroundPath.addRoundRect(mBackgroundRect, radii, Path.Direction.CW);

        canvas.drawPath(mBackgroundPath, mBackgroundPaint);
        canvas.drawText(mText, mCenterTextStartX, mCenterTextStartY, mTextPaint);

    }

    public void setText(String text) {
        mText = text;

        Rect rect = new Rect();
        mTextPaint.getTextBounds(mText, 0, mText.length(), rect);
        mCenterTextStartX = (int)((mWidth - rect.width()) * 0.5);
        mCenterTextStartY = mItemHeight - rect.height();
        invalidate();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public boolean isRtl() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) &&
                (getContext().getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
    }
}
