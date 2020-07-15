package com.qbao.newim.model;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.flatbuffers.FlatBufferBuilder;
import com.qbao.newim.constdef.FriendTypeDef;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.util.SharedPreferenceUtil;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Transient;

import java.util.LinkedHashMap;

import friendpack.T_FRIEND_CLIENT_ADD_RQ;
import friendpack.T_FRIEND_CLIENT_ADD_RS;
import friendpack.T_FRIEND_CLIENT_CONFIRM_RQ;
import friendpack.T_FRIEND_SERVER_ADD_RQ;
import friendpack.T_FRIEND_SERVER_CONFIRM_RQ;
import userpack.T_GET_USER_INFO_RS;

/**
 * Created by chenjian on 2017/6/19.
 */
@Entity
public class IMFriendInfo implements Comparable<IMFriendInfo>{
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
    public int sex;
    @Property(nameInDb = "mail")
    public String mail = "";
    @Property(nameInDb = "head_url")
    public String head_url;
    @Property(nameInDb = "signature")
    public String signature = "";
    @Property(nameInDb = "pinyin")
    public String pinyin = "";
    @Property(nameInDb = "remark_name")
    public String remark_name = "";
    @Property(nameInDb = "black_type")
    public int black_type = FriendTypeDef.ACTIVE_TYPE.INVALID;
    @Property(nameInDb = "delete_type")
    public int delete_type = FriendTypeDef.ACTIVE_TYPE.INVALID;
    @Property(nameInDb = "status")           // 当前好友状态
    public int status = FriendTypeDef.FRIEND_ADD_TYPE.INVALID;
    @Property(nameInDb = "source_type")           // 当前好友状态
    public byte source_type = 0;
    @Property(nameInDb = "friend_token")           // 当前好友操作时间
    public long friend_token = 0;
    @Property(nameInDb = "opt_msg")               // 当前好友附加消息
    public String opt_msg = "";
    @Property(nameInDb = "notify")
    public boolean notify = true;
    @Property(nameInDb = "is_business")
    public boolean is_business = false;

    @Transient
    public boolean is_select;
    @Transient
    public boolean is_star;
    @Transient
    public LinkedHashMap<Integer, String> nick_index;
    @Transient
    public LinkedHashMap<Integer, String> remark_index;

    @Generated(hash = 515805145)
    public IMFriendInfo(long userId, String user_name, long mobile, long birthday, String locationCity,
            String nickName, String locationPro, int sex, String mail, String head_url,
            String signature, String pinyin, String remark_name, int black_type, int delete_type,
            int status, byte source_type, long friend_token, String opt_msg, boolean notify,
            boolean is_business) {
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
        this.signature = signature;
        this.pinyin = pinyin;
        this.remark_name = remark_name;
        this.black_type = black_type;
        this.delete_type = delete_type;
        this.status = status;
        this.source_type = source_type;
        this.friend_token = friend_token;
        this.opt_msg = opt_msg;
        this.notify = notify;
        this.is_business = is_business;
    }

    @Generated(hash = 72506506)
    public IMFriendInfo() {
    }

    // 好友拼音首字母
    public String getInitial(){
        if (is_star) {
            return "~";
        }
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

    // 好友信息序列化
    public boolean UnSerialize(T_GET_USER_INFO_RS data) {
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

    public void SerializeADD(FlatBufferBuilder builder) {
        int op_msg_offset = builder.createString(this.opt_msg);
        int own_name_offset = builder.createString(NIMUserInfoManager.getInstance().getSelfName());

        T_FRIEND_CLIENT_ADD_RQ.startT_FRIEND_CLIENT_ADD_RQ(builder);
        T_FRIEND_CLIENT_ADD_RQ.addPeerUserId(builder, userId);
        T_FRIEND_CLIENT_ADD_RQ.addOwnNickname(builder, own_name_offset);
        T_FRIEND_CLIENT_ADD_RQ.addSourceType(builder, this.source_type);
        T_FRIEND_CLIENT_ADD_RQ.addOpMsg(builder, op_msg_offset);
    }

    public void SerializeAccept(FlatBufferBuilder builder) {
        int remark_offset = builder.createString(this.remark_name);
        int own_name_offset = builder.createString(NIMUserInfoManager.getInstance().getSelfName());

        T_FRIEND_CLIENT_CONFIRM_RQ.startT_FRIEND_CLIENT_CONFIRM_RQ(builder);
        T_FRIEND_CLIENT_CONFIRM_RQ.addPeerUserId(builder, userId);
        T_FRIEND_CLIENT_CONFIRM_RQ.addSourceType(builder, source_type);
        T_FRIEND_CLIENT_CONFIRM_RQ.addPeerRemark(builder, remark_offset);
        T_FRIEND_CLIENT_CONFIRM_RQ.addToken(builder, SharedPreferenceUtil.getFriendsToken());
        T_FRIEND_CLIENT_CONFIRM_RQ.addOwnNickname(builder, own_name_offset);
        T_FRIEND_CLIENT_CONFIRM_RQ.addResult(builder, 0);
    }

    public boolean UnSerializeADDRS(T_FRIEND_CLIENT_ADD_RS data) {
        if (data == null) {
            return false;
        }

        this.userId = data.peerUserId();
        this.opt_msg = data.opMsg();
        this.friend_token = data.token();
        return true;
    }

    public boolean UnSerializeADDRQ(T_FRIEND_SERVER_ADD_RQ data) {
        if (data == null) {
            return false;
        }
        this.userId = data.peerUserId();
        this.opt_msg = data.opMsg();
        this.source_type = data.sourceType();
        this.friend_token = data.token();
        this.nickName = data.peerUserName();
        return true;
    }

    public boolean UnSerializeAcceptRQ(T_FRIEND_SERVER_CONFIRM_RQ data) {
        if (data == null) {
            return false;
        }

        this.userId = data.peerUserId();
        this.friend_token = data.token();
        this.nickName = data.peerNickname();
        return true;
    }

    @Override
    public int compareTo(@NonNull IMFriendInfo o) {
        if (this.getInitial().equals("#")) {
            return 1;
        }else if (o.getInitial().equals("#")) {
            return -1;
        } else
            return this.getInitial().compareTo(o.getInitial());
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

    public String getSignature() {
        return this.signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getPinyin() {
        return this.pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getRemark_name() {
        return this.remark_name;
    }

    public void setRemark_name(String remark_name) {
        this.remark_name = remark_name;
    }

    public int getBlack_type() {
        return this.black_type;
    }

    public void setBlack_type(int black_type) {
        this.black_type = black_type;
    }

    public int getDelete_type() {
        return this.delete_type;
    }

    public void setDelete_type(int delete_type) {
        this.delete_type = delete_type;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public byte getSource_type() {
        return this.source_type;
    }

    public void setSource_type(byte source_type) {
        this.source_type = source_type;
    }

    public long getFriend_token() {
        return this.friend_token;
    }

    public void setFriend_token(long friend_token) {
        this.friend_token = friend_token;
    }

    public String getOpt_msg() {
        return this.opt_msg;
    }

    public void setOpt_msg(String opt_msg) {
        this.opt_msg = opt_msg;
    }

    public boolean getNotify() {
        return this.notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    public long getMobile() {
        return this.mobile;
    }

    public void setMobile(long mobile) {
        this.mobile = mobile;
    }

    public boolean getIs_business() {
        return this.is_business;
    }

    public void setIs_business(boolean is_business) {
        this.is_business = is_business;
    }

    public boolean getIs_star() {
        return this.is_star;
    }

    public void setIs_star(boolean is_star) {
        this.is_star = is_star;
    }
}
