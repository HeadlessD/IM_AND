package com.qbao.newim.helper;

import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;

import com.qbao.newim.qbim.R;
import com.qbao.newim.util.ShowUtils;

/**
 * Created by chenjian on 2017/4/10.
 */

public class InputLengthFilter implements InputFilter {

    private int mMaxLength;

    Context mContext;

    /**
     * 默认构造函数，用于聊天输入框，默认值为聊天输入框输入字符的限制，中文1400，英文3200
     * <p>
     * 构造器
     *
     * @param context
     */
    public InputLengthFilter(Context context, int maxLength) {
        super();

        mContext = context;
        mMaxLength = maxLength;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        int keep = mMaxLength - (dest.length() - (dend - dstart));

        if (keep <= 0) {
//        	Toast.makeText(mContext, R.string.chat_content_exceed_max_length, Toast.LENGTH_SHORT).show();
            ShowUtils.showToast(R.string.nim_chat_content_exceed_max_length);
            return "";
        } else if (keep >= end - start) {
            return null; // keep original
        } else {
            return source.subSequence(start, start + keep);
        }
    }

    public int getMaxLength() {
        return mMaxLength;
    }

    private static final boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
            return true;
        }
        return false;
    }

    public static final boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }

    public static final boolean isChineseCharacter(String chineseStr) {
        char[] charArray = chineseStr.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            if ((charArray[i] >= 0x4e00) && (charArray[i] <= 0x9fbb)) {
                return true;
            }
        }
        return false;
    }
}
