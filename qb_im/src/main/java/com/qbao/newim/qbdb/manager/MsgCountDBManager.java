package com.qbao.newim.qbdb.manager;

import com.qbao.newim.model.MsgCountModel;

import org.greenrobot.greendao.AbstractDao;

import java.util.List;

/**
 * Created by shiyunjie on 2017/9/25.
 */

public class MsgCountDBManager extends BaseManager<MsgCountModel, String>
{
    private static MsgCountDBManager _instance;
    public static MsgCountDBManager getInstance()
    {
        if(_instance == null)
        {
            _instance = new MsgCountDBManager();
        }

        return _instance;
    }

    @Override
    public AbstractDao<MsgCountModel, String> getAbstractDao() {
        return daoSession.getMsgCountModelDao();
    }

    public List<MsgCountModel> GetMsgCountList()
    {
        return getQueryBuilder().build().list();
    }
    public void deleteAllUnreadInfoByType(int chat_type)
    {

    }
}
