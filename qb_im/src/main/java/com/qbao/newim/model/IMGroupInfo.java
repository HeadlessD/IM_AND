package com.qbao.newim.model;

import com.qbao.newim.constdef.MsgConstDef;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Transient;

import java.util.LinkedHashMap;

import grouppack.T_GROUP_BASE_INFO;

/**
 * Created by chenjian on 2017/5/23.
 */

@Entity
public class IMGroupInfo {
    @Property(nameInDb = "isMember")
    public int isMember = 1;      // 1 表示是成员，0非成员
    @Id
    public long group_id;
    @Property(nameInDb = "group_name")
    public String group_name = "";
    @Property(nameInDb = "group_img_url")
    public String group_img_url = "";
    @Property(nameInDb = "group_count")
    public int group_count;
    @Property(nameInDb = "group_manager_user_id")
    public long group_manager_user_id;
    @Property(nameInDb = "group_add_is_agree")
    public int group_add_is_agree;
    @Property(nameInDb = "group_ct")
    public long group_ct;       // 创建时间
    @Property(nameInDb = "group_remark")
    public String group_remark = ""; // 群公告
    @Property(nameInDb = "group_max_count")
    public int group_max_count; // 群个数限制
    @Property(nameInDb = "is_save_contact")  // 是否保存到通讯录
    public boolean is_save_contact;
    @Property(nameInDb = "notify_type")      // 消息提醒类型
    public byte notify_type = MsgConstDef.GROUP_MESSAGE_STATUS.GROUP_MESSAGE_STATUS_NORMAL;
    @Property(nameInDb = "group_add_max_count")
    public int group_add_max_count;          // 群一次人数最大变动个数 建群、邀请、踢人
    @Property(nameInDb = "last_message_id")
    public long last_message_id = 0;
    @Property(nameInDb = "is_show_nick")
    public boolean is_show_nick = true;          // 群聊是否显示昵称

    @Transient
    public LinkedHashMap<Integer, String> name_pinyin;

    @Generated(hash = 1724867129)
    public IMGroupInfo(int isMember, long group_id, String group_name, String group_img_url,
            int group_count, long group_manager_user_id, int group_add_is_agree,
            long group_ct, String group_remark, int group_max_count, boolean is_save_contact,
            byte notify_type, int group_add_max_count, long last_message_id,
            boolean is_show_nick) {
        this.isMember = isMember;
        this.group_id = group_id;
        this.group_name = group_name;
        this.group_img_url = group_img_url;
        this.group_count = group_count;
        this.group_manager_user_id = group_manager_user_id;
        this.group_add_is_agree = group_add_is_agree;
        this.group_ct = group_ct;
        this.group_remark = group_remark;
        this.group_max_count = group_max_count;
        this.is_save_contact = is_save_contact;
        this.notify_type = notify_type;
        this.group_add_max_count = group_add_max_count;
        this.last_message_id = last_message_id;
        this.is_show_nick = is_show_nick;
    }

    @Generated(hash = 1073719233)
    public IMGroupInfo() {
    }

    public boolean UnSerialize(T_GROUP_BASE_INFO data) {
        if (data == null) {
            return false;
        }

        this.group_id = data.groupId();
        this.group_name = data.groupName();
        this.group_img_url = data.groupImgUrl();
        this.group_count = data.groupCount();
        this.group_manager_user_id = data.groupManagerUserId();
        this.group_add_is_agree = data.groupAddIsAgree();
        this.group_ct = data.groupCt();
        this.group_remark = data.groupRemark();
        this.group_max_count = data.groupMaxCount();
        this.group_add_max_count = data.groupAddMaxCount();
        this.notify_type = data.messageStatus();

        return true;
    }

//    public boolean UnSerializeDetail(T_GROUP_GET_INFO_RS data) {
//        if (data == null) {
//            return false;
//        }
//
//        this.isMember = data.isMember();
//        this.group_id = data.groupInfo().groupId();
//        this.group_name = data.groupInfo().groupName();
//        this.group_img_url = data.groupInfo().groupImgUrl();
//        this.group_count = data.groupInfo().groupCount();
//        this.group_manager_user_id = data.groupInfo().groupManagerUserId();
//        this.group_add_is_agree = data.groupInfo().groupAddIsAgree();
//        this.group_ct = data.groupInfo().groupCt();
//        this.group_remark = data.groupInfo().groupRemark();
//        this.group_max_count = data.groupInfo().groupMaxCount();
//
//        return true;
//    }

    public int getIsMember() {
        return this.isMember;
    }

    public void setIsMember(int isMember) {
        this.isMember = isMember;
    }

    public long getGroup_id() {
        return this.group_id;
    }

    public void setGroup_id(long group_id) {
        this.group_id = group_id;
    }

    public String getGroup_name() {
        return this.group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getGroup_img_url() {
        return this.group_img_url;
    }

    public void setGroup_img_url(String group_img_url) {
        this.group_img_url = group_img_url;
    }

    public int getGroup_count() {
        return this.group_count;
    }

    public void setGroup_count(int group_count) {
        this.group_count = group_count;
    }

    public long getGroup_manager_user_id() {
        return this.group_manager_user_id;
    }

    public void setGroup_manager_user_id(long group_manager_user_id) {
        this.group_manager_user_id = group_manager_user_id;
    }

    public int getGroup_add_is_agree() {
        return this.group_add_is_agree;
    }

    public void setGroup_add_is_agree(int group_add_is_agree) {
        this.group_add_is_agree = group_add_is_agree;
    }

    public long getGroup_ct() {
        return this.group_ct;
    }

    public void setGroup_ct(long group_ct) {
        this.group_ct = group_ct;
    }

    public String getGroup_remark() {
        return this.group_remark;
    }

    public void setGroup_remark(String group_remark) {
        this.group_remark = group_remark;
    }

    public int getGroup_max_count() {
        return this.group_max_count;
    }

    public void setGroup_max_count(int group_max_count) {
        this.group_max_count = group_max_count;
    }

    public boolean getIs_save_contact() {
        return this.is_save_contact;
    }

    public void setIs_save_contact(boolean is_save_contact) {
        this.is_save_contact = is_save_contact;
    }

    public byte getNotify_type() {
        return this.notify_type;
    }

    public void setNotify_type(byte notify_type) {
        this.notify_type = notify_type;
    }

    public int getGroup_add_max_count() {
        return this.group_add_max_count;
    }

    public void setGroup_add_max_count(int group_add_max_count) {
        this.group_add_max_count = group_add_max_count;
    }

    public long getLast_message_id() {
        return this.last_message_id;
    }

    public void setLast_message_id(long last_message_id) {
        this.last_message_id = last_message_id;
    }

    public boolean getIs_show_nick() {
        return this.is_show_nick;
    }

    public void setIs_show_nick(boolean is_show_nick) {
        this.is_show_nick = is_show_nick;
    }
}
