package com.qbao.newim.qbdb.manager;

import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.qbdb.IMFriendInfoDao;

import org.greenrobot.greendao.AbstractDao;

import java.util.List;

/**
 * Created by chenjian on 2017/6/19.
 */

public class FriendUserDbManager extends BaseManager <IMFriendInfo, Long>{

    @Override
    public AbstractDao<IMFriendInfo, Long> getAbstractDao() {
        return daoSession.getIMFriendInfoDao();
    }

    private static class FriendDbManagerHolder {
        private final static FriendUserDbManager instance = new FriendUserDbManager();
    }

    public static FriendUserDbManager getInstance(){
        return FriendDbManagerHolder.instance;
    }

    public IMFriendInfo getSingleFriend(long user_id) {
        return getQueryBuilder().where(IMFriendInfoDao.Properties.UserId.eq(user_id)).unique();
    }

    public List<IMFriendInfo> getAllFriendList() {
        return getQueryBuilder().build().list();
    }

    // 通过手机号码查询好友
    public IMFriendInfo queryPhone(String phone) {
        return getQueryBuilder().where(IMFriendInfoDao.Properties.Mobile.eq(phone)).unique();
    }
}
