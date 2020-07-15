package com.qbao.newim.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.qbao.newim.qbim.R;

/**
 * Created by chenjian on 2017/4/20.
 */

public class BladeView extends View {
    private String[] b =
            {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
                    "W", "X", "Y", "Z", "#"};
    private String[] s =
            {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
                    "W", "X", "Y", "Z", "#", "*"};
    private final static int PADDING = 10;

    public interface OnItemClickListener {
        void onItemClick(String s);
    }

    private int choose = -1;
    private Paint paint = new Paint();
    private boolean showBkg = false;
    private PopupWindow mPopupWindow;
    private TextView mPopupText;
    private Handler handler = new Handler();

    private OnItemClickListener mOnItemClickListener;

    public BladeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BladeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BladeView(Context context) {
        super(context);
    }

    public void addStar() {
        b = s;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int height = getHeight() - PADDING * 2;
        int width = getWidth();
        int singleHeight = height / b.length;
        for (int i = 0; i < b.length; i++) {
            paint.setTextSize(24);
            if (!showBkg) {
                paint.setColor(Color.parseColor("#777777"));
            } else {
                paint.setColor(Color.WHITE);
            }
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setFakeBoldText(true);
            paint.setAntiAlias(true);
            float xPos = width / 2 - paint.measureText(b[i]) / 2;
            float yPos = singleHeight * i + singleHeight + PADDING;
            canvas.drawText(b[i], xPos, yPos, paint);
            paint.reset();
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();
        final int oldChoose = choose;
        final int c = (int) (y / getHeight() * (b.length));

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setBackgroundResource(R.drawable.nim_letterbac);
                showBkg = true;
                if (oldChoose != c) {
                    if (c >= 0 && c < b.length) {
                        performItemClicked(c);
                        choose = c;
                        invalidate();
                    }
                }

                break;
            case MotionEvent.ACTION_MOVE:
                if (oldChoose != c) {
                    if (c >= 0 && c < b.length) {
                        performItemClicked(c);
                        choose = c;
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_HOVER_EXIT:
            case MotionEvent.ACTION_CANCEL:
                setBackgroundResource(0);
                showBkg = false;
                choose = -1;
                dismissPopup();
                invalidate();
                break;
        }
        return true;
    }

    private void showPopup(int item) {
        if (mPopupWindow == null) {
            handler.removeCallbacks(dismissRunnable);
            View inflate = LayoutInflater.from(getContext()).inflate(R.layout.nim_letter_toast_view, null);
            mPopupText = (TextView) inflate.findViewById(R.id.tv_letter);
            mPopupWindow = new PopupWindow(inflate, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        String text;
        text = b[item];

        mPopupText.setText(text);
        if (mPopupWindow.isShowing()) {
            mPopupWindow.update();
        } else {
            mPopupWindow.showAtLocation(getRootView(), Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        }
    }

    private void dismissPopup() {
        handler.post(dismissRunnable);
    }

    Runnable dismissRunnable = new Runnable() {
        @Override
        public void run() {
            if (mPopupWindow != null) {
                mPopupWindow.dismiss();
            }
        }
    };

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    private void performItemClicked(int item) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(b[item]);
            showPopup(item);
        }
    }
}
