package com.qbao.newim.qbdb.manager;

import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.model.SessionModel;
import com.qbao.newim.qbdb.SessionModelDao;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenjian on 2017/5/8.
 */

public class SessionDbManager extends BaseManager<SessionModel, Long> {

    private static SessionDbManager instance;
    public static SessionDbManager getInstance() {
        if (instance == null) {
            instance = new SessionDbManager();
        }
        return instance;
    }

    @Override
    public AbstractDao<SessionModel, Long> getAbstractDao() {
        return daoSession.getSessionModelDao();
    }

    // 获取所有置顶会话列表，不包含群助手
    public List<SessionModel> getTopSession() {
        return getQueryBuilder().where(SessionModelDao.Properties.Is_top.eq(true)).
                orderDesc(SessionModelDao.Properties.Msg_time).build().list();
    }

    // 获取所有非置顶会话列表，不包含群助手
    public List<SessionModel> getNormalSession() {
        return getQueryBuilder().where(SessionModelDao.Properties.Is_top.notEq(true)).
                orderDesc(SessionModelDao.Properties.Msg_time).build().list();
    }

    public SessionModel getSessionModel(long session_id) {
        SessionModel sessionModel;
        QueryBuilder<SessionModel> queryBuilder = getQueryBuilder().where(SessionModelDao.Properties.Session_id.eq(session_id));
        sessionModel = queryBuilder.unique();
        return sessionModel;
    }
}
