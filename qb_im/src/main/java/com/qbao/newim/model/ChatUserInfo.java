package com.qbao.newim.model;

import android.graphics.Bitmap;

import java.lang.ref.WeakReference;

/**
 * Created by chenjian on 2017/4/13.
 */

public class ChatUserInfo {
    public ChatUserInfo(Bitmap bitmap, String nickName, String jid) {
        bmpAvatar = new WeakReference<Bitmap>(bitmap);
        this.nickName = nickName;
        this.jid = jid;
    }

    /**
     * 头像
     */
    public WeakReference<Bitmap> bmpAvatar;

    /**
     * 昵称
     */
    public String nickName;

    public String jid;

    public int friendType;

    public Bitmap getAvatar() {
        return bmpAvatar.get();
    }
}
