package com.qbao.newim.model;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.flatbuffers.FlatBufferBuilder;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Transient;

import java.util.LinkedHashMap;

import commonpack.USER_BASE_INFO;

/**
 * Created by chenjian on 2017/6/19.
 */
@Entity
public class IMGroupUserInfo implements Comparable<IMGroupUserInfo> {
    @Id
    private String primary_key;
    @Property(nameInDb = "user_id")
    public long user_id;
    @Property(nameInDb = "group_id")
    public long group_id;
    @Property(nameInDb = "user_nick_name")
    public String user_nick_name;       // 用户昵称
    @Property(nameInDb = "user_group_index")
    public int user_group_index;        // 用户在群中位置 群主根据位置排序
    @Property(nameInDb = "pinyin")
    public String pinyin;
    @Property(nameInDb = "need_agree")
    public boolean need_agree = false;
    @Transient
    public boolean sortByIndex;         // 是否按照索引排序
    @Transient
    public boolean is_select;           // 是否被选中
    @Transient
    public LinkedHashMap<Integer, String> nick_index;

    public void GenPrimaryKey()
    {
        this.primary_key = String.format("%d_%d", group_id, user_id);
    }

    @Generated(hash = 808060945)
    public IMGroupUserInfo(String primary_key, long user_id, long group_id,
            String user_nick_name, int user_group_index, String pinyin,
            boolean need_agree) {
        this.primary_key = primary_key;
        this.user_id = user_id;
        this.group_id = group_id;
        this.user_nick_name = user_nick_name;
        this.user_group_index = user_group_index;
        this.pinyin = pinyin;
        this.need_agree = need_agree;
    }

    @Generated(hash = 2049662506)
    public IMGroupUserInfo() {
    }

    public String getInitial(){
        String pinYin = pinyin;
        if (TextUtils.isEmpty(pinYin)) {
            return "|";
        }
        pinYin = pinYin.trim();
        if (TextUtils.isEmpty(pinYin)) {
            return "|";
        }
        String initial;
        char initialChar = pinYin.charAt(0);
        if (Character.isLetter(initialChar)) {
            initial = Character.toString(initialChar).toUpperCase();
        } else {
            initial = "|";
        }
        return initial;
    }

    public boolean UnSerialize(USER_BASE_INFO data) {
        if (data == null) {
            return  false;
        }

        user_id = data.userId();
        user_nick_name = data.userNickName();
        user_group_index = data.userGroupIndex();

        return true;
    }

    public void Serialize(FlatBufferBuilder builder) {
        int nick_name_offset = builder.createString(this.user_nick_name);
        USER_BASE_INFO.startUSER_BASE_INFO(builder);
        USER_BASE_INFO.addUserId(builder, this.user_id);
        USER_BASE_INFO.addUserGroupIndex(builder, this.user_group_index);
        USER_BASE_INFO.addUserNickName(builder, nick_name_offset);
    }

    public long getUser_id() {
        return this.user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public long getGroup_id() {
        return this.group_id;
    }

    public void setGroup_id(long group_id) {
        this.group_id = group_id;
    }

    public String getUser_nick_name() {
        return this.user_nick_name;
    }

    public void setUser_nick_name(String user_nick_name) {
        this.user_nick_name = user_nick_name;
    }

    public int getUser_group_index() {
        return this.user_group_index;
    }

    public void setUser_group_index(int user_group_index) {
        this.user_group_index = user_group_index;
    }

    public String getPinyin() {
        return this.pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    @Override
    public int compareTo(@NonNull IMGroupUserInfo o) {
        if (sortByIndex) {
            return this.user_group_index - o.user_group_index;
        }
        return this.getInitial().compareTo(o.getInitial());
    }

    public boolean getNeed_agree() {
        return this.need_agree;
    }

    public void setNeed_agree(boolean need_agree) {
        this.need_agree = need_agree;
    }

    public String getPrimary_key() {
        return this.primary_key;
    }

    public void setPrimary_key(String primary_key) {
        this.primary_key = primary_key;
    }
}
