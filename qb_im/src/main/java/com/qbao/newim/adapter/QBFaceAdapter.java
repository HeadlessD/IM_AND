package com.qbao.newim.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.QbGifUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chenjian on 2017/4/10.
 */

public class QBFaceAdapter extends BaseAdapter {
    public static final int PAGER_SIZE = 8;
    private Context mContext;
    private LayoutInflater mInflater;
    private String[] faceNames;
    private String[] faceCodes;
    private int pageIndex = 0;

    public QBFaceAdapter(Context context, String[] faceNames, String[] faceCodes) {
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
            convertView = mInflater.inflate(R.layout.nim_chat_qbface_item, parent, false);
        }

        ImageView image = (ImageView) convertView.findViewById(R.id.image);
        TextView text = (TextView) convertView.findViewById(R.id.face_name);

        String faceName = faceNames[pageIndex * PAGER_SIZE + position];
        int res = QbGifUtil.getInstance().getGifResName(faceName);
        Glide.with(mContext).load(res).asBitmap().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(image);

        String faceCode = faceCodes[pageIndex * PAGER_SIZE + position];
        Pattern p = Pattern.compile("\\[(.+)\\]");
        Matcher m = p.matcher(faceCode);
        if (m.find()) {
            text.setText(m.group(1));
        } else {
            text.setText(faceCode);
        }

        convertView.setTag(faceCode);
        return convertView;
    }

    public final int getCount() {
        if (faceNames == null) {
            return 0;
        } else {
            if (faceNames.length - pageIndex * PAGER_SIZE > PAGER_SIZE) {
                return PAGER_SIZE;
            } else {
                return faceNames.length - pageIndex * PAGER_SIZE;
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
