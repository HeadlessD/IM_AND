package com.qbao.newim.qbdb.manager;

import com.qbao.newim.model.IMGroupUserInfo;
import com.qbao.newim.qbdb.IMGroupUserInfoDao;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenjian on 2017/6/19.
 */

public class GroupUserDbManager extends BaseManager <IMGroupUserInfo, String>{

    public static GroupUserDbManager instance;

    public static GroupUserDbManager getInstance() {
        if (instance == null) {
            instance = new GroupUserDbManager();
        }
        return instance;
    }

    @Override
    public AbstractDao<IMGroupUserInfo, String> getAbstractDao() {
        return daoSession.getIMGroupUserInfoDao();
    }

    public IMGroupUserInfo getGroupUser(long group_id, long user_id) {
        QueryBuilder<IMGroupUserInfo> queryBuilder =  getQueryBuilder().where(IMGroupUserInfoDao.Properties.Group_id.eq(group_id)
        , IMGroupUserInfoDao.Properties.User_id.eq(user_id));
        return queryBuilder.unique();
    }

    public List<IMGroupUserInfo> getGroupAllUser(long group_id) {
        List<IMGroupUserInfo> query_list =  getQueryBuilder().where(IMGroupUserInfoDao.Properties.Group_id.eq(group_id)
        ).orderAsc(IMGroupUserInfoDao.Properties.User_group_index).build().list();
        return query_list;
    }

    // 获取所有待同意的群用户
    public ArrayList<IMGroupUserInfo> getGroupAgreeUser(long group_id) {
        List<IMGroupUserInfo> query_list =  getQueryBuilder().where(IMGroupUserInfoDao.Properties.Group_id.eq(group_id)
        , IMGroupUserInfoDao.Properties.Need_agree.eq(true)).build().list();
        ArrayList<IMGroupUserInfo> list = new ArrayList<>();
        list.addAll(query_list);
        return list;
    }

    // 获取所有已经同意的群用户
    public ArrayList<IMGroupUserInfo> getGroupNoAgreeUser(long group_id) {
        List<IMGroupUserInfo> query_list =  getQueryBuilder().where(IMGroupUserInfoDao.Properties.Group_id.eq(group_id)
                , IMGroupUserInfoDao.Properties.Need_agree.eq(false)).
                orderDesc(IMGroupUserInfoDao.Properties.User_group_index).build().list();
        ArrayList<IMGroupUserInfo> list = new ArrayList<>();
        list.addAll(query_list);
        return list;
    }

    public boolean deleteAllGroupUser(long group_id) {
        List<IMGroupUserInfo> delete_list = getGroupAllUser(group_id);
        return deleteList(delete_list);
    }
}
