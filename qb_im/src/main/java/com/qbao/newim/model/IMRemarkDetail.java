package com.qbao.newim.model;

import grouppack.T_GROUP_REMARK_DETAIL_RS;

/**
 * Created by chenjian on 2017/7/6.
 */

public class IMRemarkDetail {
    public long group_id;     // 群组id
    public long op_user_id;   // 修改者的user_id
    public long op_ct;        // 修改时间
    public String op_remark;  // 公告

    public boolean UnSerialize(T_GROUP_REMARK_DETAIL_RS data) {
        if (data == null) {
            return false;
        }

        this.group_id = data.groupId();
        this.op_user_id = data.opUserId();
        this.op_ct = data.opCt();
        this.op_remark = data.opRemark();
        return true;
    }
}
