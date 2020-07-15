package com.qbao.newim.views;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.qbao.newim.configure.GlobalVariable;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.FaceUtil;
import com.qbao.newim.util.ShowUtils;

/**
 * Created by chenjian on 2017/4/6.
 */

public class FaceEditText extends android.support.v7.widget.AppCompatEditText {
    private SoftInputListener listener;
    private Editable editable;
    private Handler mHandler;

    public FaceEditText(Context context) {
        super(context);
        init();
    }

    public FaceEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public FaceEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setImeOptions(/*EditorInfo.IME_FLAG_NO_EXTRACT_UI |*/ EditorInfo.IME_ACTION_SEND);
        AppUtil.setEditCursor(this, R.drawable.nim_cursor_green);
        mHandler = new Handler();
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return super.onCreateInputConnection(outAttrs);
    }

    public void setSoftInputListener(SoftInputListener l) {
        this.listener = l;
    }

    public interface SoftInputListener {
        public void onSoftInputHide();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        try {
            super.setText(text, type);
        } catch (IndexOutOfBoundsException e) {
            if (text != null) {
                setText(text.toString());
            }
        }
    }

    /**
     * 重写粘贴方法，为解决显示表情图片的问题
     */
    @Override
    public boolean onTextContextMenuItem(int id) {
        int selStart = getSelectionStart();
        int selEnd = getSelectionEnd();

        editable = getText();

        if (!isFocused()) {
            selStart = 0;
            selEnd = editable.length();
        }

        int min = Math.min(selStart, selEnd);
        int max = Math.max(selStart, selEnd);

        if (min < 0) {
            min = 0;
        }
        if (max < 0) {
            max = 0;
        }

        switch (id) {
            case android.R.id.paste:
                ClipboardManager clip = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = clip.getPrimaryClip();

                int count = clipData.getItemCount();
                String text = "";
                for (int i = 0; i < count; ++i) {
                    ClipData.Item item = clipData.getItemAt(i);
                    CharSequence str = item.coerceToText(getContext());
                    text += str;
                }
                if (text.length() > GlobalVariable.SEND_MESSAGE_LENGTH) {
                    ShowUtils.showToast(getContext().getString(R.string.nim_message_txt_count, GlobalVariable.SEND_MESSAGE_LENGTH));
                    text = text.substring(0, GlobalVariable.SEND_MESSAGE_LENGTH);
                }
                if (!TextUtils.isEmpty(text)) {
                    new MyAsyncTask().execute(text, min, max);

                }
                return true;
        }
        return super.onTextContextMenuItem(id);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }


    private class MyAsyncTask extends AsyncTask<Object, Void, CharSequence> {
        private CharSequence mText;
        private int mMin;
        private int mMax;

        @Override
        protected CharSequence doInBackground(Object... params) {
            mText = (CharSequence) (params[0]);
            mMin = (Integer) (params[1]);
            mMax = (Integer) (params[2]);

            CharSequence text1 = FaceUtil.getInstance().formatTextToFace(mText, FaceUtil.FACE_TYPE.CHAT_EDITTEXT);
            return text1;
        }

        @Override
        protected void onPostExecute(final CharSequence result) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    editable.replace(mMin, mMax, result);
                    setText(editable);

                    int index = mMin + mText.length();
                    if (editable.length() >= index) {
                        setSelection(index);
                    } else {
                        setSelection(editable.length());
                    }
                }
            });
        }
    }
}
