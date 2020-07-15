package com.qbao.newim.model.message;

import android.support.annotation.NonNull;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;
import com.qbao.newim.constdef.MsgConstDef;

import java.io.Serializable;

/**
 * Created by shiyunjie on 17/3/2.
 */

public abstract class BaseMessageModel implements Comparable<BaseMessageModel>, Serializable{

    private static final long serialVersionUID = 1L;
    /**
     * 服务器基础字段
     */
    public long message_id = 0;                                        // 消息唯一ID
    public boolean is_self = false;                                    // 是否自己发送
    public long b_id = 0;                                              // 只是一个id类型，可以代表：商家id或者其他的id
    public long w_id = 0;                                              // 只是一个id类型，可以代表：小二id或者其他的id
    public long c_id = 0;                                              // 只是一个id类型，可以代表：客户id或者其他的id
    public short app_id = 0;                                           // 子公司id
    public int session_id = 0;                                         // 保留字段 会话id
    public short chat_type = MsgConstDef.MSG_CHAT_TYPE.INVALID;        // 聊天类型（单聊、群聊等
    public int m_type = MsgConstDef.MSG_M_TYPE.INVALID;                // 消息类型（文本、图片、地图等
    public int s_type = MsgConstDef.MSG_S_TYPE.INVALID;                // 消息类型扩展（红包、订单、名片等
    public int ext_type = MsgConstDef.MSG_EXT_TYPE.INVALID;            // 保留字段 以后扩展
    public String msg_content = "";                                    // 消息内容
    public long msg_time = 0;                                          // 消息发送的时间
    public short msg_status = MsgConstDef.MSG_STATUS.INVALID;          // 消息发送状态

    public String audio_path;                                          // 语音本地地址
    public String pic_path;                                            // 图片原图本地地址
    public String compress_path;                                       // 图片压缩后地址

    public int progress;                                               // 上传进度

    // 序列化消息结构体[client->server]
    public abstract void SerializeRQ(FlatBufferBuilder builder);
    public abstract void SerializeRS(FlatBufferBuilder builder);
    // 反序列化结构体[server->client]
    public abstract boolean UnSerializeRQ(Table data);
    public abstract boolean UnSerializeRS(Table data);

    @Override
    public int compareTo(@NonNull BaseMessageModel o)
    {
        if (this.msg_time > o.msg_time)
        {
            return 1;
        }
        else if (this.msg_time == o.msg_time)
        {
            return 0;
        }
        else
        {
            return -1;
        }
    }

    public void CopyFrom(BaseMessageModel src)
    {
        this.message_id = src.message_id;
        this.is_self = src.is_self;
        this.b_id = src.b_id;
        this.w_id = src.w_id;
        this.c_id = src.c_id;
        this.app_id = src.app_id;
        this.session_id = src.session_id;
        this.chat_type = src.chat_type;
        this.m_type = src.m_type;
        this.s_type = src.s_type;
        this.ext_type = src.ext_type;
        this.msg_content = src.msg_content;
        this.msg_time = src.msg_time;
        this.msg_status = src.msg_status;
        this.audio_path = src.audio_path;
        this.pic_path = src.pic_path;
        this.compress_path = src.compress_path;
        this.progress = src.progress;
    }
}
