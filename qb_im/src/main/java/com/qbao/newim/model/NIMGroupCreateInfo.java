package com.qbao.newim.model;

import grouppack.T_GROUP_TYPE_INFO;

/**
 * Created by chenjian on 2017/7/19.
 */

public class NIMGroupCreateInfo {
    public int group_max_count;         // 最大人数
    public int group_type;              // 类型
    public byte group_is_show;          // 0 不显示 1显示
    public int group_add_max_count;     // 最大邀请人数

    public boolean UnSerialize(T_GROUP_TYPE_INFO data) {
        if (data == null) {
            return false;
        }
        this.group_add_max_count = data.groupAddMaxCount();
        this.group_is_show = data.groupIsShow();
        this.group_max_count = data.groupMaxCount();
        this.group_type = data.groupType();
        return true;
    }
}
