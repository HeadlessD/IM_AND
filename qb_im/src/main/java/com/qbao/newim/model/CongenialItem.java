package com.qbao.newim.model;

import com.qbao.newim.manager.NIMFriendInfoManager;

/**
 * Created by chenjian on 2017/5/31.
 * 趣味相投的人
 */

public class CongenialItem {
    private String desc;
    private String userId;
    private String showName;
    private String avatar;
    private String status;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getShowName() {
        return showName;
    }

    public void setShowName(String showName) {
        this.showName = showName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getStatus() {
        IMFriendInfo ship = NIMFriendInfoManager.getInstance().getFriendReqInfo(Long.parseLong(userId));
        if (ship == null) {
            return "0";
        } else {
            return String.valueOf(ship.status);
        }
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
