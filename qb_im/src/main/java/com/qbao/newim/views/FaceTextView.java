package com.qbao.newim.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenjian on 2017/4/13.
 */

@SuppressLint("WrongCall")
public class FaceTextView extends android.support.v7.widget.AppCompatTextView {
    private static class FixingResult {
        public final boolean fixed;
        public final List<Object> spansWithSpacesBefore;
        public final List<Object> spansWithSpacesAfter;

        public static FixingResult fixed(List<Object> spansWithSpacesBefore,
                                         List<Object> spansWithSpacesAfter) {
            return new FixingResult(true, spansWithSpacesBefore,
                    spansWithSpacesAfter);
        }

        public static FixingResult notFixed() {
            return new FixingResult(false, null, null);
        }

        private FixingResult(boolean fixed, List<Object> spansWithSpacesBefore,
                             List<Object> spansWithSpacesAfter) {
            this.fixed = fixed;
            this.spansWithSpacesBefore = spansWithSpacesBefore;
            this.spansWithSpacesAfter = spansWithSpacesAfter;
        }
    }

    private static final String TAG = FaceTextView.class
            .getSimpleName();

    public FaceTextView(Context context, AttributeSet attrs,
                        int defStyle) {
        super(context, attrs, defStyle);
    }

    public FaceTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FaceTextView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } catch (IndexOutOfBoundsException e) {
            fixOnMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * If possible, fixes the Spanned text by adding spaces around spans when
     * needed.
     */
    private void fixOnMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        CharSequence text = getText();
        if (text instanceof Spanned) {
            SpannableStringBuilder builder = new SpannableStringBuilder(text);
            fixSpannedWithSpaces(builder, widthMeasureSpec, heightMeasureSpec);
        } else {
            fallbackToString(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * Add spaces around spans until the text is fixed, and then removes the
     * unneeded spaces
     */
    private void fixSpannedWithSpaces(SpannableStringBuilder builder,
                                      int widthMeasureSpec, int heightMeasureSpec) {
        long startFix = System.currentTimeMillis();

        FixingResult result = addSpacesAroundSpansUntilFixed(builder,
                widthMeasureSpec, heightMeasureSpec);

        if (result.fixed) {
            removeUnneededSpaces(widthMeasureSpec, heightMeasureSpec, builder,
                    result);
        } else {
            fallbackToString(widthMeasureSpec, heightMeasureSpec);
        }

        long fixDuration = System.currentTimeMillis() - startFix;
    }

    private FixingResult addSpacesAroundSpansUntilFixed(
            SpannableStringBuilder builder, int widthMeasureSpec,
            int heightMeasureSpec) {

        Object[] spans = builder.getSpans(0, builder.length(), Object.class);
        List<Object> spansWithSpacesBefore = new ArrayList<Object>(spans.length);
        List<Object> spansWithSpacesAfter = new ArrayList<Object>(spans.length);

        for (Object span : spans) {
            int spanStart = builder.getSpanStart(span);
            if (isNotSpace(builder, spanStart - 1)) {
                builder.insert(spanStart, " ");
                spansWithSpacesBefore.add(span);
            }

            int spanEnd = builder.getSpanEnd(span);
            if (isNotSpace(builder, spanEnd)) {
                builder.insert(spanEnd, " ");
                spansWithSpacesAfter.add(span);
            }

            try {
                setTextAndMeasure(builder, widthMeasureSpec, heightMeasureSpec);
                return FixingResult.fixed(spansWithSpacesBefore,
                        spansWithSpacesAfter);
            } catch (IndexOutOfBoundsException notFixed) {
            }
        }

        return FixingResult.notFixed();
    }

    private boolean isNotSpace(CharSequence text, int where) {
        if (where < 0 || where > text.length()) {
            return false;
        }

        return text.charAt(where) != ' ';
    }

    private void setTextAndMeasure(CharSequence text, int widthMeasureSpec,
                                   int heightMeasureSpec) {
        setText(text);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void removeUnneededSpaces(int widthMeasureSpec,
                                      int heightMeasureSpec, SpannableStringBuilder builder,
                                      FixingResult result) {

        for (Object span : result.spansWithSpacesAfter) {
            int spanEnd = builder.getSpanEnd(span);
            builder.delete(spanEnd, spanEnd + 1);
            try {
                setTextAndMeasure(builder, widthMeasureSpec, heightMeasureSpec);
            } catch (IndexOutOfBoundsException ignored) {
                builder.insert(spanEnd, " ");
            }
        }

        boolean needReset = true;
        for (Object span : result.spansWithSpacesBefore) {
            int spanStart = builder.getSpanStart(span);
            builder.delete(spanStart - 1, spanStart);
            try {
                setTextAndMeasure(builder, widthMeasureSpec, heightMeasureSpec);
                needReset = false;
            } catch (IndexOutOfBoundsException ignored) {
                needReset = true;
                int newSpanStart = spanStart - 1;
                builder.insert(newSpanStart, " ");
            }
        }

        if (needReset) {
            setText(builder);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void fallbackToString(int widthMeasureSpec, int heightMeasureSpec) {
        String fallbackText = getText().toString();
        setTextAndMeasure(fallbackText, widthMeasureSpec, heightMeasureSpec);
    }
}
