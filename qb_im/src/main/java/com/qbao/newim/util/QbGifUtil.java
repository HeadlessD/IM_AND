package com.qbao.newim.util;

import android.content.res.Resources;

import com.qbao.newim.qbim.R;

import java.lang.reflect.Field;

/**
 * Created by chenjian on 2017/5/5.
 */

public class QbGifUtil {
    private static QbGifUtil instance;
    private Resources mResources = null;
    private String[] mFaceCodes;
    private String[] mFaceName;

    public synchronized static QbGifUtil getInstance() {
        if (null == instance) {
            instance = new QbGifUtil();
        }
        return instance;
    }

    private QbGifUtil() {
        mResources = AppUtil.GetContext().getApplicationContext().getResources();
        initFace();
    }

    private void initFace() {
        mFaceCodes = mResources.getStringArray(R.array.nim_qb_face_code);
        mFaceName = mResources.getStringArray(R.array.nim_qb_face_name);
    }

    public int getGifResCode(String text) {
        int size = mFaceCodes.length;
        for (int i = 0; i < size; i++) {
            if (text.equals(mFaceCodes[i])) {
               return getGifResName(mFaceName[i]);
            }
        }

        return 0;
    }

    public int getGifResName(String name) {
        Field f;
        try {
            f = R.drawable.class.getDeclaredField(name);
            return f.getInt(R.drawable.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
