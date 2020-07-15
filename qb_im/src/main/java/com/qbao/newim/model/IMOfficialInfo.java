package com.qbao.newim.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.util.LinkedHashMap;

/**
 * Created by chenjian on 2017/8/22.
 */

@Entity
public class IMOfficialInfo {
    @Id
    public long official_id;
    public String official_name;
    public String official_url;
    public long last_msg_id;
    @Transient
    public LinkedHashMap<Integer, String> name_pinyin;

    @Generated(hash = 384036696)
    public IMOfficialInfo(long official_id, String official_name,
            String official_url, long last_msg_id) {
        this.official_id = official_id;
        this.official_name = official_name;
        this.official_url = official_url;
        this.last_msg_id = last_msg_id;
    }
    @Generated(hash = 524189313)
    public IMOfficialInfo() {
    }
    public long getOfficial_id() {
        return this.official_id;
    }
    public void setOfficial_id(long official_id) {
        this.official_id = official_id;
    }
    public long getLast_msg_id() {
        return this.last_msg_id;
    }
    public void setLast_msg_id(long last_msg_id) {
        this.last_msg_id = last_msg_id;
    }
    public String getOfficial_name() {
        return this.official_name;
    }
    public void setOfficial_name(String official_name) {
        this.official_name = official_name;
    }
    public String getOfficial_url() {
        return this.official_url;
    }
    public void setOfficial_url(String official_url) {
        this.official_url = official_url;
    }
}
