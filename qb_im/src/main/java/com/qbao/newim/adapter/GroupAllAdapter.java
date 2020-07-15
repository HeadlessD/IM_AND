package com.qbao.newim.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

/**
 * Created by chenjian on 2017/6/16.
 */

public class GroupAllAdapter extends ResourceCursorAdapter {

    private String keyword;

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public GroupAllAdapter(Context context, int layout) {
        super(context, layout, null, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }

    final static class GroupAllViewHolder {
        public ImageView avatar;
        public TextView name;
        public TextView memberCount;

        OnAvatarClickListener onAvatarClickListener;
    }

    private final class OnAvatarClickListener implements View.OnClickListener {
        private String groupId = "";

        public OnAvatarClickListener() {
        }

        public void setGropuId(String groupId) {
            this.groupId = groupId;
        }

        @Override
        public void onClick(View v) {
        }
    }
}
