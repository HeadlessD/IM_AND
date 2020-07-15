package com.qbao.newim.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.qbao.newim.qbim.R;

import java.lang.reflect.Field;

/**
 * Created by chenjian on 2017/4/10.
 */

public class ChatFaceAdapter extends BaseAdapter {

    public static final int PAGER_SIZE = 20;
    private Context mContext;
    private LayoutInflater mInflater;
    private String[] faceNames;
    private String[] faceCodes;
    private int pageIndex = 0;
    int nHeight;


    public ChatFaceAdapter(Context context, String[] faceNames, String[] faceCodes) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        this.faceNames = faceNames;
        this.faceCodes = faceCodes;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.nim_chat_face_item, parent, false);
        }

        ImageView image = (ImageView) convertView.findViewById(R.id.image);

        Field f;
        Drawable drawable = null;
        try {
            String faceName;
            if (position == getCount() - 1) {
                faceName = "nim_chat_delete_face";
            } else {
                faceName = faceNames[pageIndex * PAGER_SIZE + position];
            }
            f = R.drawable.class.getDeclaredField(faceName.toLowerCase());
            final int id = f.getInt(R.drawable.class);
            drawable = ContextCompat.getDrawable(mContext, id);
//            Glide.with(mContext).load(id).into(image);
        } catch (Exception e) {
            e.printStackTrace();
        }


        image.setImageDrawable(drawable);
        if (position < getCount() - 1) {
            convertView.setTag(faceCodes[pageIndex * PAGER_SIZE + position]);
        }

        return convertView;
    }

    public final int getCount() {
        if (faceNames == null) {
            return 0;
        } else {
            if (faceNames.length - pageIndex * PAGER_SIZE > PAGER_SIZE) {
                return PAGER_SIZE + 1;
            } else {
                return faceNames.length - pageIndex * PAGER_SIZE + 1;
            }
        }
    }

    public final String getItem(int position) {
        if (faceNames == null || position >= (faceNames.length - pageIndex * PAGER_SIZE)) {
            return null;
        } else {
            return faceNames[pageIndex * PAGER_SIZE + position];
        }
    }

    public final long getItemId(int position) {
        return pageIndex * PAGER_SIZE + position;
    }
}
