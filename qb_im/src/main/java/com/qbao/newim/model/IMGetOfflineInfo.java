package com.qbao.newim.model;

/**
 * Created by chenjian on 2017/7/11.
 */

public class IMGetOfflineInfo {
    public long group_id;
    public long last_message_id;

    public IMGetOfflineInfo(long group_id, long last_message_id) {
        this.group_id = group_id;
        this.last_message_id = last_message_id;
    }
}
