package com.qbao.newim.model;

import ecpack.T_EC_GETFREEWAITER_RS;

/**
 * Created by chenjian on 2017/8/25.
 */

public class IMBusinessInfo {
    public long bid;      //商家ID
    public long wid;      //小二ID
    public int session_id; //会话ID
    public String name;

    public boolean UnSerialize(T_EC_GETFREEWAITER_RS data) {
        if (data == null) {
            return false;
        }

        bid = data.bId();
        wid = data.wId();
        session_id = data.sessionId();
        return true;
    }
}
