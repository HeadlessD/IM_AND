package com.qbao.newim.model;

import com.qbao.newim.constdef.MsgConstDef;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by shiyunjie on 2017/9/25.
 */

@Entity
public class MsgCountModel {
    @Id
    public String primary_key;

    @Property(nameInDb = "session_id")
    public long session_id;                                   // 会话ID

    @Property(nameInDb = "chat_type")
    public int chat_type = MsgConstDef.MSG_CHAT_TYPE.INVALID;

    @Property(nameInDb = "unread_count")
    public int unread_count = 0;


    @Generated(hash = 1124779598)
    public MsgCountModel(String primary_key, long session_id, int chat_type,
            int unread_count) {
        this.primary_key = primary_key;
        this.session_id = session_id;
        this.chat_type = chat_type;
        this.unread_count = unread_count;
    }



    @Generated(hash = 1095628636)
    public MsgCountModel() {
    }



    public void GenPrimaryKey()
    {
        this.primary_key = String.format("%d_%d", session_id, chat_type);
    }



    public String getPrimary_key() {
        return this.primary_key;
    }



    public void setPrimary_key(String primary_key) {
        this.primary_key = primary_key;
    }



    public long getSession_id() {
        return this.session_id;
    }



    public void setSession_id(long session_id) {
        this.session_id = session_id;
    }



    public int getChat_type() {
        return this.chat_type;
    }



    public void setChat_type(int chat_type) {
        this.chat_type = chat_type;
    }



    public int getUnread_count() {
        return this.unread_count;
    }



    public void setUnread_count(int unread_count) {
        this.unread_count = unread_count;
    }
}
