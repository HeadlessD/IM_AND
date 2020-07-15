package com.qbao.newim.model;

import com.google.flatbuffers.FlatBufferBuilder;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

import userpack.T_GET_ME_INFO_RS;
import userpack.T_GET_USER_INFO_RS;
import userpack.T_KEYINFO;
import userpack.T_UPDATE_USER_INFO_RQ;

/**
 * Created by chenjian on 2017/5/10.
 */

@Entity
public class IMUserInfo {
    @Id
    public long userId;
    @Property(nameInDb = "user_name")
    public String user_name = "";
    @Property(nameInDb = "mobile")
    public long mobile;
    @Property(nameInDb = "birthday")
    public long birthday;
    @Property(nameInDb = "locationCity")
    public String locationCity = "";
    @Property(nameInDb = "nickName")
    public String nickName = "";
    @Property(nameInDb = "locationPro")
    public String locationPro = "";
    @Property(nameInDb = "sex")
    public int sex = 0;
    @Property(nameInDb = "mail")
    public String mail = "";
    @Property(nameInDb = "head_url")
    public String head_url;
    @Property(nameInDb = "token")
    public String token = "";
    @Property(nameInDb = "signature")
    public String signature = "";
    @Property(nameInDb = "verification")
    public String verification = "";

    @Generated(hash = 924968848)
    public IMUserInfo(long userId, String user_name, long mobile, long birthday,
            String locationCity, String nickName, String locationPro, int sex, String mail,
            String head_url, String token, String signature, String verification) {
        this.userId = userId;
        this.user_name = user_name;
        this.mobile = mobile;
        this.birthday = birthday;
        this.locationCity = locationCity;
        this.nickName = nickName;
        this.locationPro = locationPro;
        this.sex = sex;
        this.mail = mail;
        this.head_url = head_url;
        this.token = token;
        this.signature = signature;
        this.verification = verification;
    }

    @Generated(hash = 699328686)
    public IMUserInfo() {
    }

    /**
     * 上传用户序列化信息
     * @param builder
     * @param isAddToTcp 如果当前是添加新用户，所有信息需要上传，如果是update，手机和用户名不可更改
     */
    public void SerializeUpload(FlatBufferBuilder builder, boolean isAddToTcp) {
        int offset[];

        int birthdayOffset = builder.createString(String.valueOf(this.birthday));
        int cityOffset = builder.createString(this.locationCity);
        int signatureOffset = builder.createString(this.signature);
        int nickOffset = builder.createString(this.nickName);
        int sexOffset = builder.createString(String.valueOf(this.sex));
        int mailOffset = builder.createString(this.mail);
        int proOffset = builder.createString(this.locationPro);
        if (isAddToTcp) {
            int nameOffset = builder.createString(this.user_name);
            int mobileOffset = builder.createString(String.valueOf(this.mobile));
            offset = new int[9];
            offset[0] = T_KEYINFO.createT_KEYINFO(builder, 1, nameOffset);
            offset[1] = T_KEYINFO.createT_KEYINFO(builder, 2, birthdayOffset);
            offset[2] = T_KEYINFO.createT_KEYINFO(builder, 3, cityOffset);
            offset[3] = T_KEYINFO.createT_KEYINFO(builder, 4, signatureOffset);
            offset[4] = T_KEYINFO.createT_KEYINFO(builder, 5, mobileOffset);
            offset[5] = T_KEYINFO.createT_KEYINFO(builder, 6, nickOffset);
            offset[6] = T_KEYINFO.createT_KEYINFO(builder, 7, sexOffset);
            offset[7] = T_KEYINFO.createT_KEYINFO(builder, 8, mailOffset);
            offset[8] = T_KEYINFO.createT_KEYINFO(builder, 9, proOffset);
        } else {
            offset = new int[7];
            offset[0] = T_KEYINFO.createT_KEYINFO(builder, 2, birthdayOffset);
            offset[1] = T_KEYINFO.createT_KEYINFO(builder, 3, cityOffset);
            offset[2] = T_KEYINFO.createT_KEYINFO(builder, 4, signatureOffset);
            offset[3] = T_KEYINFO.createT_KEYINFO(builder, 6, nickOffset);
            offset[4] = T_KEYINFO.createT_KEYINFO(builder, 7, sexOffset);
            offset[5] = T_KEYINFO.createT_KEYINFO(builder, 8, mailOffset);
            offset[6] = T_KEYINFO.createT_KEYINFO(builder, 9, proOffset);
        }

        int list_info_offset = T_UPDATE_USER_INFO_RQ.createKeyLstInfoVector(builder, offset);
        T_UPDATE_USER_INFO_RQ.startT_UPDATE_USER_INFO_RQ(builder);
        T_UPDATE_USER_INFO_RQ.addKeyLstInfo(builder, list_info_offset);
    }

    public boolean UnUserSerialize(T_GET_USER_INFO_RS data) {
        if (data == null) {
            return false;
        }

        this.userId = data.userId();
        this.user_name = data.userName();
        this.mobile = data.mobile();
        this.birthday = data.birthday();
        this.locationCity = data.city();
        this.nickName = data.nickName();
        this.sex = data.sex();
        this.mail = data.mail();
        this.signature = data.signature();
        this.locationPro = data.province();
        return true;
    }

    public boolean UnSelfSerialize(T_GET_ME_INFO_RS data) {
        if (data == null) {
            return false;
        }

        this.userId = data.userId();
        this.user_name = data.userName();
        this.mobile = data.mobile();
        this.birthday = data.birthday();
        this.locationCity = data.city();
        this.nickName = data.nickName();
        this.sex = data.sex();
        this.mail = data.mail();
        this.token = data.token();
        this.verification = data.verification();
        this.signature = data.signature();
        this.locationPro = data.province();
        return true;
    }

    public boolean UnSerializeKey(T_KEYINFO data) {
        if (data == null) {
            return false;
        }

        switch (data.keyName()) {
            case 1:
                this.user_name = data.keyValue();
                break;
            case 2:
                this.birthday = Long.parseLong(data.keyValue());
                break;
            case 3:
                this.locationCity = data.keyValue();
                break;
            case 4:
                this.signature = data.keyValue();
                break;
            case 5:
                this.mobile = Long.parseLong(data.keyValue());
                break;
            case 6:
                this.nickName = data.keyValue();
                break;
            case 7:
                this.sex = Integer.parseInt(data.keyValue());
                break;
            case 8:
                this.mail = data.keyValue();
                break;
            case 9:
                this.locationPro = data.keyValue();
                break;
        }

        return true;
    }

    public long getUserId() {
        return this.userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUser_name() {
        return this.user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public long getBirthday() {
        return this.birthday;
    }

    public void setBirthday(long birthday) {
        this.birthday = birthday;
    }

    public String getLocationCity() {
        return this.locationCity;
    }

    public void setLocationCity(String locationCity) {
        this.locationCity = locationCity;
    }

    public String getNickName() {
        return this.nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getLocationPro() {
        return this.locationPro;
    }

    public void setLocationPro(String locationPro) {
        this.locationPro = locationPro;
    }

    public int getSex() {
        return this.sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getMail() {
        return this.mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getHead_url() {
        return this.head_url;
    }

    public void setHead_url(String head_url) {
        this.head_url = head_url;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSignature() {
        return this.signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getVerification() {
        return this.verification;
    }

    public void setVerification(String verification) {
        this.verification = verification;
    }

    public long getMobile() {
        return this.mobile;
    }

    public void setMobile(long mobile) {
        this.mobile = mobile;
    }
}
