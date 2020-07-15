package com.qbao.newim.model;

import com.qbao.newim.constdef.MsgConstDef;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Transient;

import java.util.LinkedHashMap;

/**
 * Created by chenjian on 2017/5/8.
 */

@Entity
public class SessionModel {
    @Id
    public Long session_id;

    @Property(nameInDb = "chat_type")
    public int chat_type = MsgConstDef.MSG_CHAT_TYPE.INVALID;

    @Property(nameInDb = "is_top")
    public boolean is_top;
    @Property(nameInDb = "msg_time")
    public long msg_time;               // 消息时间

    @Generated(hash = 510979477)
    public SessionModel(Long session_id, int chat_type, boolean is_top,
            long msg_time) {
        this.session_id = session_id;
        this.chat_type = chat_type;
        this.is_top = is_top;
        this.msg_time = msg_time;
    }
    @Generated(hash = 1381345516)
    public SessionModel() {
    }
    public Long getSession_id() {
        return this.session_id;
    }
    public void setSession_id(Long session_id) {
        this.session_id = session_id;
    }
    public int getChat_type() {
        return this.chat_type;
    }
    public void setChat_type(int chat_type) {
        this.chat_type = chat_type;
    }
    public boolean getIs_top() {
        return this.is_top;
    }
    public void setIs_top(boolean is_top) {
        this.is_top = is_top;
    }
    public long getMsg_time() {
        return this.msg_time;
    }
    public void setMsg_time(long msg_time) {
        this.msg_time = msg_time;
    }
}
