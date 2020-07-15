package com.qbao.newim.model.message;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import offcialpack.T_OFFCIAL_MESSAGE;

/**
 * Created by chenjian on 2017/9/28.
 */

public class OcMessageModel extends BaseMessageModel{

    public long official_id = 0;                                       // 聊天对方的id
    public String official_name = "";                                 // 聊天对方名字

    @Override
    public void SerializeRQ(FlatBufferBuilder builder) {

    }

    @Override
    public void SerializeRS(FlatBufferBuilder builder) {

    }

    @Override
    public boolean UnSerializeRQ(Table data) {

        return false;
    }

    @Override
    public boolean UnSerializeRS(Table data) {
        T_OFFCIAL_MESSAGE msg_data = (T_OFFCIAL_MESSAGE)data;
        if (msg_data.sMsg() == null) {
            return false;
        }

        this.message_id = msg_data.messageId();
        this.official_name = msg_data.sMsg().sendUserName();
        this.official_id = msg_data.offcialId();
        this.is_self = false;
        this.app_id = msg_data.sMsg().appId();
        this.session_id = msg_data.sMsg().sessionId();
        return false;
    }
}
