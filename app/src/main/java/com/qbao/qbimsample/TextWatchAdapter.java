package com.qbao.qbimsample;

/**
 * Created by chenjian on 2017/7/21.
 */

import android.text.Editable;
import android.text.TextWatcher;

/**
 * TextWatcher的实现抽象类
 */
public abstract class TextWatchAdapter implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}