package com.qbao.newim.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.qbao.newim.qbim.R;

import java.lang.reflect.Field;

/**
 * Created by chenjian on 2017/4/20.
 */

public class MultiEditText extends android.support.v7.widget.AppCompatEditText {
    /**
     * edittext右边图片
     */
    private Drawable rightDrawable;
    /**
     * 默认右边图片type
     */
    private int rightDrawableType = 0;
    /**
     * 右边图片的bounds
     */
    private Rect bounds;
    /**
     * 是否设置自定义右边图片
     */
    boolean isSetCustomIcon = false;
    /**
     * 是否跟随焦点的变化显示右边图片
     */
    boolean changeWithFocusHiddenRes = true;
    /**
     * 右边图片资源
     */

    private int[] res = new int[]{R.mipmap.nim_green_dui, R.mipmap.nim_red_cha, R.mipmap.nim_icon_dialog_clear};
    private Drawable[] drawable;
    /**
     * 右边图片类型
     */
    public static final int DEFALUT = 0;
    public static final int GREEN_DUI = 1;
    public static final int RED_CHA = 2;
    public static final int GREY_CHA = 3;
    public static final int CUSTOM_ICON = 4;

    private boolean is_show_img = true;

    OnTextChangeListener listener;
    RightDrawableClickListener rightListener;
    private View leftView;

    public MultiEditText(Context paramContext) {
        super(paramContext);
        initEditText();
    }

    public void setCustomIcon(int resId) {
        if (resId <= 0) {
            setCustomIcon(null);
            return;
        }
        setCustomIcon(getResources().getDrawable(resId));
    }

    public void setCustomIcon(Drawable drawable) {
        isSetCustomIcon = true;
        this.drawable[CUSTOM_ICON] = drawable;
        setDrawableType(CUSTOM_ICON);
    }

    public void setIs_show_img(boolean is_show_img) {
        this.is_show_img = is_show_img;
    }

    public MultiEditText(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        initEditText();
    }

    public MultiEditText(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        initEditText();
    }

    private void initEditText() {
        setEditCursor(this, R.drawable.nim_cursor_green);
        drawable = new Drawable[]{null, ContextCompat.getDrawable(getContext(), res[0]),
                ContextCompat.getDrawable(getContext(), res[1]), ContextCompat.getDrawable(getContext(), res[2])};
        addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (listener != null) {
                    listener.onTextChange(MultiEditText.this);
                }
                setEditTextDrawable(MultiEditText.this.hasFocus());

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                ViewGroup parent = ((ViewGroup) getParent());
                if (parent != null) {
                    if (hasFocus) {
                        parent.setSelected(true);
                    } else {
                        parent.setSelected(false);
                    }
                }
                if (leftView != null) {
                    if (hasFocus) {
                        leftView.setSelected(true);
                    } else {
                        leftView.setSelected(false);
                    }
                }
                setDrawableStateWithFocus(hasFocus);
            }
        });
    }

    private void setDrawableStateWithFocus(boolean hasFocus) {
        if (!changeWithFocusHiddenRes) {
            return;
        }
        setEditTextDrawable(hasFocus);
    }

    private void setEditTextDrawable(boolean hasFocus) {
        if (!is_show_img) {
            return;
        }
        if (!hasFocus || TextUtils.isEmpty(getText())) {
            setDrawableType(DEFALUT);
        } else {
            if (isSetCustomIcon) {
                setDrawableType(CUSTOM_ICON);
            } else {
                setDrawableType(GREY_CHA);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        if ((this.rightDrawable != null) && (event.getAction() == MotionEvent.ACTION_UP)) {
            Rect bounds = this.rightDrawable.getBounds();
            int i = (int) event.getX();
            if ((i > getWidth() - 2 * bounds.width() - getPaddingRight() && rightDrawableType == GREY_CHA)
                    || (i > getWidth() - bounds.width() - getPaddingRight() && rightDrawableType == CUSTOM_ICON)) {
                if (rightListener != null) {
                    rightListener.onClick(this);
                } else {
                    setText("");
                }
                event.setAction(MotionEvent.ACTION_CANCEL);
                requestFocus();
            }
        }
        return super.onTouchEvent(event);
    }

    public void setDrawableType(int drawableType) {
        rightDrawableType = drawableType;
        if (drawableType < 0 || drawableType >= drawable.length) {
            drawableType = 0;
        }
        rightDrawable = drawable[drawableType];
        if (rightDrawable != null) {
            if (bounds == null) {
                rightDrawable.setBounds(0, 0, rightDrawable.getIntrinsicWidth(),
                        rightDrawable.getIntrinsicWidth());
            } else {
                rightDrawable.setBounds(bounds);
            }

        }
        setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1], rightDrawable,
                getCompoundDrawables()[3]);
    }

    protected void finalize() throws Throwable {
        super.finalize();
        this.rightDrawable = null;
        this.bounds = null;
    }

    public interface OnTextChangeListener {
        void onTextChange(MultiEditText view);
    }


    public interface RightDrawableClickListener {
        void onClick(View view);
    }

    private void setEditCursor(EditText edittext, int drawableId) {
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
}
