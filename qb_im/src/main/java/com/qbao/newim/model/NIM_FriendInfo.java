package com.qbao.newim.model;

import friendpack.T_FREIND_INFO;

/**
 * Created by chenjian on 2017/4/26.
 */

public class NIM_FriendInfo {
    public long user_id;
    public byte source_type;    // 好友来源
    public String remark_name; // 备注名字
    public String op_msg;      // 添加好友验证信息
    public int opt_type;        // 消息类型
    public long opt_time;
    public int black_type;      // 黑名单状态  0：正常 1：被动黑名单 2：主动黑名单

    public boolean UnSerialize(T_FREIND_INFO data) {
        this.remark_name = data.remarkName();
        this.source_type = data.sourceType();
        this.user_id = data.userId();
        this.op_msg = data.opMsg();
        this.opt_type = data.optType();
        this.opt_time = data.opTime();
        this.black_type = data.blackType();
        return true;
    }
}
