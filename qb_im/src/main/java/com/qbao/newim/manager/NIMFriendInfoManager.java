package com.qbao.newim.manager;

import android.text.TextUtils;

import com.qbao.newim.configure.GlobalVariable;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.FriendTypeDef;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.model.IMUserInfo;
import com.qbao.newim.qbdb.manager.FriendUserDbManager;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.Logger;
import com.qbao.newim.util.SharedPreferenceUtil;
import com.qbao.newim.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by shiyunjie on 17/3/2.
 */
public class NIMFriendInfoManager {
    private static final String TAG = "NIMFriendInfoManager";
    private HashMap<Long, IMFriendInfo> accept_map = new HashMap<>();    // 待接受的好友列表
    private HashMap<Long, IMFriendInfo> request_map = new HashMap<>();   // 请求对方为好友列表
    private HashMap<Long, IMFriendInfo> black_map = new HashMap<>();     // 黑名单好友
    private HashMap<Long, IMFriendInfo> active_map = new HashMap<>();    // 活跃好友，即非黑名单好友
    private HashMap<Long, IMFriendInfo> business_map = new HashMap<>();  // 商家用户，非好友
    private FriendUserDbManager dbManager;
    private int unread_count;
    private int friend_max_count = 0;

    private static class FriendInfoManagerHolder {
        private final static NIMFriendInfoManager instance = new NIMFriendInfoManager();
    }

    public static NIMFriendInfoManager getInstance(){
        return FriendInfoManagerHolder.instance;
    }

    public void init() {
        dbManager = FriendUserDbManager.getInstance();
        List<IMFriendInfo> friends = dbManager.getAllFriendList();
        for (IMFriendInfo friend : friends) {
            AddFriend(friend, false);
        }
    }

    public void AddFriend(IMFriendInfo info) {
        AddFriend(info, true);
    }

    // 添加好友用户,是否存进数据库
    public void AddFriend(IMFriendInfo info, boolean bWriteSQL) {
        if (info == null || info.userId <= 0) {
            Logger.warning(TAG, "invalid user");
            return;
        }
        if (info.is_business) {
            handBusiness(info);
        } else {
            switch (info.status) {
                case FriendTypeDef.FRIEND_ADD_TYPE.SEND_REQUEST:
                    handSendReq(info);
                    break;
                case FriendTypeDef.FRIEND_ADD_TYPE.ACCEPT_REQUEST:
                    handAcceptReq(info, bWriteSQL);
                    if(bWriteSQL)
                        DataObserver.Notify(DataConstDef.EVENT_FRIEND_ADD_REQUEST, info.userId, true);
                    break;
                case FriendTypeDef.FRIEND_ADD_TYPE.DELETE:
                    break;
                case FriendTypeDef.FRIEND_ADD_TYPE.TIME_OUT:
                    if(bWriteSQL)
                        DataObserver.Notify(DataConstDef.EVENT_FRIEND_REQ_TIMEOUT, info.userId, null);
                    break;
                case FriendTypeDef.FRIEND_ADD_TYPE.OWN_CONFIRM:
                    handConfirmReq(info);
                    if (bWriteSQL) {
                        NIMFriendInfoManager.getInstance().minusUnreadCount();
                        DataObserver.Notify(DataConstDef.EVENT_FRIEND_CONFIRM, info.userId, info.status);
                    }
                    break;
                case FriendTypeDef.FRIEND_ADD_TYPE.PEER_CONFIRM:
                case FriendTypeDef.FRIEND_ADD_TYPE.RESTART_ADD:
                    handConfirmReq(info);
                    if(bWriteSQL) {
                        DataObserver.Notify(DataConstDef.EVENT_FRIEND_CONFIRM, info.userId, info.status);
                    }
                    break;
                case FriendTypeDef.FRIEND_ADD_TYPE.FRIEND:
                    handExistFriend(info);
                    break;
            }

            switch (info.black_type) {
                case FriendTypeDef.ACTIVE_TYPE.ACTIVE:
                case FriendTypeDef.ACTIVE_TYPE.EACH:
                    handBlackFriend(info);
                    break;
            }
        }

        if (bWriteSQL) {
            if (dbManager == null) {
                dbManager = FriendUserDbManager.getInstance();
            }
            dbManager.insertOrReplace(info);
        }
    }

    private void handBusiness(IMFriendInfo info) {
        if (business_map.containsKey(info.userId)) {
            business_map.remove(info.userId);
        }

        business_map.put(info.userId, info);
    }

    private void handSendReq(IMFriendInfo info) {
        if (request_map.containsKey(info.userId)) {
            request_map.remove(info.userId);
        }

        request_map.put(info.userId, info);
    }

    private void handAcceptReq(IMFriendInfo info, boolean bSql) {

        if (accept_map.containsKey(info.userId)) {
            accept_map.remove(info.userId);
        } else {
            if (bSql) {
                unread_count++;
                SharedPreferenceUtil.saveFriendUnread(unread_count);
                NIMChatNotifyManager.getInstance().notify(info);
            }
        }

        accept_map.put(info.userId, info);
    }

    private void handConfirmReq(IMFriendInfo info) {
        info = handPinyin(info);
        if (accept_map.containsKey(info.userId)) {
            accept_map.remove(info.userId);
        } else {
            friend_max_count++;
        }

        accept_map.put(info.userId, info);
        handExistFriend(info);
    }

    private void handExistFriend(IMFriendInfo info) {
        if (active_map.containsKey(info.userId)) {
            active_map.remove(info.userId);
        }

        active_map.put(info.userId, info);
    }

    private void handBlackFriend(IMFriendInfo info){
        if (black_map.containsKey(info.userId)) {
            black_map.remove(info.userId);
        }

        if (active_map.containsKey(info.userId)) {
            active_map.remove(info.userId);
        }

        black_map.put(info.userId, info);
    }

    private IMFriendInfo handPinyin(IMFriendInfo info) {
        if (!TextUtils.isEmpty(info.user_name)) {
            String show_name;
            if (TextUtils.isEmpty(info.remark_name)) {
                if (TextUtils.isEmpty(info.nickName)) {
                    show_name = info.user_name;
                } else {
                    show_name = info.nickName;
                }
            } else {
                show_name = info.remark_name;
            }
            info.pinyin = Utils.converterToSpell(show_name);
        }

        return info;
    }

    /**
     * 删除好友申请
     * @param user_id
     */
    public void delFriendAdd(long user_id) {
        if (user_id < 0) {
            return;
        }
        if (accept_map.containsKey(user_id)) {
            IMFriendInfo friendInfo = accept_map.get(user_id);
            accept_map.remove(user_id);
            dbManager.delete(friendInfo);
        }
    }

    // 通过用户id获取新增朋友关系
    public IMFriendInfo getFriendReqInfo(long user_id) {

        if (user_id < 0) {
            return null;
        }

        if (!accept_map.containsKey(user_id)) {
            return null;
        }

        return accept_map.get(user_id);

    }

    public void delFriend(long user_id) {
        if (user_id < 0) {
            return;
        }

        IMFriendInfo info = getFriendUser(user_id);
        if (info == null) {
            return;
        }

        if (active_map.containsKey(user_id)) {
            active_map.remove(user_id);
        }

        if (request_map.containsKey(user_id)) {
            request_map.remove(user_id);
        }

        if (black_map.containsKey(user_id)) {
            black_map.remove(user_id);
        }

        if (accept_map.containsKey(user_id)) {
            accept_map.remove(user_id);
            delFriendAdd(user_id);
        }

        dbManager.delete(info);
        NIMMsgManager.getInstance().ClearMessage(user_id, false);
        friend_max_count--;
    }

    public boolean is_friend(long user_id) {
        if (user_id < 0) {
            return false;
        }
        return active_map.containsKey(user_id) || accept_map.containsKey(user_id);
    }

    // 获取好友信息
    public IMFriendInfo getFriendUser(long user_id) {
        if (user_id <= 0) {
            Logger.warning(TAG, "user not exist");
            return null;
        }

        if (accept_map.containsKey(user_id)) {
            return accept_map.get(user_id);
        }

        if (request_map.containsKey(user_id)) {
            return request_map.get(user_id);
        }

        if (black_map.containsKey(user_id)) {
            return black_map.get(user_id);
        }

        if (active_map.containsKey(user_id)) {
            return active_map.get(user_id);
        }

        if (business_map.containsKey(user_id)) {
            return business_map.get(user_id);
        }

        if (dbManager == null) {
            dbManager = FriendUserDbManager.getInstance();
        }
        return dbManager.getSingleFriend(user_id);
    }

    public void userToFriend(IMUserInfo userInfo, IMFriendInfo friendInfo) {
        userToFriend(userInfo, friendInfo, true);
    }

    public void userToFriend(IMUserInfo userInfo, IMFriendInfo friendInfo, boolean update) {
        friendInfo.userId = userInfo.userId;
        friendInfo.birthday = userInfo.birthday;
        friendInfo.nickName = userInfo.nickName;
        friendInfo.user_name = userInfo.user_name;
        friendInfo.mobile = userInfo.mobile;
        friendInfo.locationCity = userInfo.locationCity;
        friendInfo.locationPro = userInfo.locationPro;
        friendInfo.sex = userInfo.sex;
        friendInfo.signature = userInfo.signature;
        friendInfo.is_business = false;
        if (update)
            updateFriend(friendInfo);
    }

    public void updateFriend(IMFriendInfo friendInfo) {
        if (friendInfo == null || friendInfo.userId <= 0) {
            Logger.warning(TAG, "invalid user");
            return;
        }

        if (accept_map.containsKey(friendInfo.userId)) {
            accept_map.put(friendInfo.userId, friendInfo);
            handPinyin(friendInfo);
        }

        if (request_map.containsKey(friendInfo.userId)) {
            request_map.put(friendInfo.userId, friendInfo);
        }

        if (black_map.containsKey(friendInfo.userId)) {
            black_map.put(friendInfo.userId, friendInfo);
        }

        if (active_map.containsKey(friendInfo.userId)) {
            active_map.put(friendInfo.userId, friendInfo);
            handPinyin(friendInfo);
        }

        if (business_map.containsKey(friendInfo.userId)) {
            business_map.put(friendInfo.userId, friendInfo);
        }

        if (dbManager == null) {
            dbManager = FriendUserDbManager.getInstance();
        }
        dbManager.update(friendInfo);
    }

    public void setBlackStatus(IMFriendInfo friendInfo) {
        if (friendInfo == null || friendInfo.userId <= 0) {
            Logger.warning(TAG, "invalid user");
            return;
        }

        switch (friendInfo.black_type) {
            case FriendTypeDef.ACTIVE_TYPE.ACTIVE:
            case FriendTypeDef.ACTIVE_TYPE.EACH:
                removeFriend(friendInfo.userId);
                black_map.put(friendInfo.userId, friendInfo);
                break;
            case FriendTypeDef.ACTIVE_TYPE.INVALID:
            case FriendTypeDef.ACTIVE_TYPE.PASSIVE:
                removeFriend(friendInfo.userId);
                active_map.put(friendInfo.userId, friendInfo);
                break;
        }
        if (dbManager == null) {
            dbManager = FriendUserDbManager.getInstance();
        }
        dbManager.update(friendInfo);
    }

    private void removeFriend(long user_id) {
        if (accept_map.containsKey(user_id)) {
            accept_map.remove(user_id);
        }

        if (request_map.containsKey(user_id)) {
            request_map.remove(user_id);
        }

        if (black_map.containsKey(user_id)) {
            black_map.remove(user_id);
        }

        if (active_map.containsKey(user_id)) {
            active_map.remove(user_id);
        }
    }

    // 被好友移除
    public void removeFriend(long userId, long token) {
        if (userId <= 0) {
            return;
        }
        IMFriendInfo friendInfo = getFriendUser(userId);
        if (friendInfo == null) {
            return;
        }

        boolean black_status_change = false;
        friendInfo.delete_type = FriendTypeDef.ACTIVE_TYPE.PASSIVE;
        if (friendInfo.black_type == FriendTypeDef.ACTIVE_TYPE.PASSIVE) {
            friendInfo.black_type = FriendTypeDef.ACTIVE_TYPE.INVALID;
            black_status_change = true;
        } else if (friendInfo.black_type == FriendTypeDef.ACTIVE_TYPE.EACH) {
            friendInfo.black_type = FriendTypeDef.ACTIVE_TYPE.ACTIVE;
            black_status_change = true;
        }
        friendInfo.friend_token = token;
        if (black_status_change) {
            setBlackStatus(friendInfo);
        } else {
            updateFriend(friendInfo);
        }
    }

    public int getUnread_count() {
        return unread_count;
    }

    public void addUnreadCount() {
        unread_count++;
        SharedPreferenceUtil.saveFriendUnread(unread_count);
    }

    public void minusUnreadCount() {
        if (unread_count > 0)
            unread_count--;
        SharedPreferenceUtil.saveFriendUnread(unread_count);
    }

    public void clearUnread_count() {
        unread_count = 0;
        SharedPreferenceUtil.saveFriendUnread(unread_count);
    }

    public IMFriendInfo queryPhone(String phone) {
        return dbManager.queryPhone(phone);
    }

    public List<IMFriendInfo> getBlackList() {
        List<IMFriendInfo> list = new ArrayList<>();
        Iterator iterator = black_map.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry entry = (HashMap.Entry) iterator.next();
            IMFriendInfo value = (IMFriendInfo) entry.getValue();
            list.add(value);
        }
        return list;
    }

    public List<IMFriendInfo> getAllFriendList() {
        List<IMFriendInfo> list = new ArrayList<>();
        Iterator iterator = active_map.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry entry = (HashMap.Entry) iterator.next();
            IMFriendInfo value = (IMFriendInfo) entry.getValue();
            if (!TextUtils.isEmpty(value.remark_name)) {
                value.remark_index = Utils.converterToSpellMap(value.remark_name);
            }
            if (!TextUtils.isEmpty(value.nickName)) {
                value.nick_index = Utils.converterToSpellMap(value.nickName);
            }
            list.add(value);
        }
        return list;
    }

    public List<IMFriendInfo> getAllFriendSortList() {
        List<IMFriendInfo> list = new ArrayList<>();
        List<IMFriendInfo> first_list = new ArrayList<>();
        List<IMFriendInfo> other_list = new ArrayList<>();
        Iterator iterator = active_map.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry entry = (HashMap.Entry) iterator.next();
            IMFriendInfo value = (IMFriendInfo) entry.getValue();
            value.is_select = false;
            if (!TextUtils.isEmpty(value.remark_name)) {
                value.remark_index = Utils.converterToSpellMap(value.remark_name);
            }
            if (!TextUtils.isEmpty(value.nickName)) {
                value.nick_index = Utils.converterToSpellMap(value.nickName);
            }

            if (value.delete_type == FriendTypeDef.ACTIVE_TYPE.PASSIVE) {
                other_list.add(0, value);
            } else if (value.black_type == FriendTypeDef.ACTIVE_TYPE.PASSIVE) {
                other_list.add(value);
            } else {
                first_list.add(value);
            }
        }

        Collections.sort(first_list);
        list.addAll(first_list);
        list.addAll(other_list);
        return list;
    }

    public List<IMFriendInfo> getEachFriendList() {
        List<IMFriendInfo> list = new ArrayList<>();
        Iterator iterator = active_map.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry entry = (HashMap.Entry) iterator.next();
            IMFriendInfo value = (IMFriendInfo) entry.getValue();
            if (value.delete_type == FriendTypeDef.ACTIVE_TYPE.INVALID)
                list.add(value);
        }
        return list;
    }



    public List<IMFriendInfo> getAllFriendReqList() {
        List<IMFriendInfo> list = new ArrayList<>();
        Iterator iterator = accept_map.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry entry = (HashMap.Entry) iterator.next();
            IMFriendInfo value = (IMFriendInfo) entry.getValue();
            list.add(value);
        }
        return list;
    }

    public boolean OutFriendMaxCount() {
        return friend_max_count >= GlobalVariable.FRIEND_MAX_COUNT;
    }

    public void clear() {
        accept_map.clear();
        active_map.clear();
        black_map.clear();
        request_map.clear();
        business_map.clear();
        unread_count = 0;
        friend_max_count = 0;
    }
}
