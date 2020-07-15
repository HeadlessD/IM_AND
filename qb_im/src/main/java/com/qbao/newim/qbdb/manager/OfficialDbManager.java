package com.qbao.newim.qbdb.manager;

import com.qbao.newim.model.IMOfficialInfo;

import org.greenrobot.greendao.AbstractDao;

import java.util.List;

/**
 * Created by chenjian on 2017/8/23.
 */

public class OfficialDbManager extends BaseManager<IMOfficialInfo, Long>{
    @Override
    public AbstractDao<IMOfficialInfo, Long> getAbstractDao() {
        return daoSession.getIMOfficialInfoDao();
    }

    public List<IMOfficialInfo> getAllOfficial() {
        return getQueryBuilder().build().list();
    }
}
