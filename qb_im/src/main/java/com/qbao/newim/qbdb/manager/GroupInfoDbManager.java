package com.qbao.newim.qbdb.manager;

import com.qbao.newim.model.IMGroupInfo;

import org.greenrobot.greendao.AbstractDao;

import java.util.ArrayList;

/**
 * Created by chenjian on 2017/5/23.
 */

public class GroupInfoDbManager extends BaseManager<IMGroupInfo, Long> {

//    public static GroupInfoDbManager instance;
//
//    public static GroupInfoDbManager getInstance() {
//        if (instance == null) {
//            instance = new GroupInfoDbManager();
//        }
//        return instance;
//    }

    @Override
    public AbstractDao<IMGroupInfo, Long> getAbstractDao() {
        return daoSession.getIMGroupInfoDao();
    }

    // 搜索所有群聊
    public ArrayList<IMGroupInfo> getAllGroupSession()
    {
        ArrayList<IMGroupInfo> group_list = new ArrayList<>();
        group_list.addAll(getQueryBuilder().build().list());
        return group_list;
    }
}
