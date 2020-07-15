package com.qbao.newim.model;

/**
 * Created by chenjian on 2017/4/26.
 */

public class NIM_Chat_ID {
    public long session_id;  // 会话id，私聊指opt_user_id，群聊指group_id
    public long message_id;  // 消息id
    public int index;
    public int chat_type;

    public NIM_Chat_ID() {
        session_id = -1;
        message_id = -1;
        index = -1;
		chat_type = -1;
    }

    // TODO: 2017/9/28 史云杰 这两个构造函数待删除
    public NIM_Chat_ID(long session_id, int index) {
        this.session_id = session_id;
        this.index = index;
		this.message_id = -1;
		this.chat_type = -1;
    }

    public NIM_Chat_ID(long session_id, long message_id) {
        this.session_id = session_id;
        this.message_id = message_id;
		this.chat_type = -1;
        this.index = -1;
    }

    public NIM_Chat_ID(long session_id, int index, int chat_type) {
        this.session_id = session_id;
        this.index = index;
        this.chat_type = chat_type;
		this.message_id = -1;
    }

    public NIM_Chat_ID(long session_id, long message_id, int chat_type) {
        this.session_id = session_id;
        this.message_id = message_id;
        this.chat_type = chat_type;
        this.index = -1;
    }
}
