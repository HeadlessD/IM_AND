package com.qbao.newim.qbdb.manager;

import com.qbao.newim.model.IMUserInfo;
import com.qbao.newim.qbdb.IMUserInfoDao;

import org.greenrobot.greendao.AbstractDao;

/**
 * Created by chenjian on 2017/5/10.
 */

public class UserInfoDbManager extends BaseManager<IMUserInfo, Long> {

    public static UserInfoDbManager instance;

    public static UserInfoDbManager getInstance() {
        if (instance == null) {
            instance = new UserInfoDbManager();
        }
        return instance;
    }

    @Override
    public AbstractDao<IMUserInfo, Long> getAbstractDao() {
        return daoSession.getIMUserInfoDao();
    }


    public IMUserInfo getSingleIMUser(long user_id) {
        return getQueryBuilder().where(IMUserInfoDao.Properties.UserId.eq(user_id)).unique();
    }
}
