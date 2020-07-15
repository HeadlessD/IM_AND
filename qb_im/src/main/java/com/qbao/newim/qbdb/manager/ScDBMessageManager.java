package com.qbao.newim.qbdb.manager;

import com.qbao.newim.model.message.ScDBMessageModel;
import com.qbao.newim.model.message.ScMessageModel;
import com.qbao.newim.qbdb.ScDBMessageModelDao;

import org.greenrobot.greendao.AbstractDao;

import java.util.List;

/**
 * Created by chenjian on 2017/5/8.
 */

public class ScDBMessageManager extends BaseManager<ScDBMessageModel, Long> {
    @Override
    public AbstractDao<ScDBMessageModel, Long> getAbstractDao() {
        return daoSession.getScDBMessageModelDao();
    }

    public static ScDBMessageManager instance;
    public static ScDBMessageManager getInstance() {
        if (instance == null) {
            instance = new ScDBMessageManager();
        }

        return instance;
    }

    public long getMessageCount(long session_id) {
        return getQueryBuilder().where(ScDBMessageModelDao.Properties.Opt_user_id.eq(session_id)).count();
    }

    public List<ScDBMessageModel> getMessageList(long session_id, int offset, int number) {
        return  getQueryBuilder().where(ScDBMessageModelDao.Properties.Opt_user_id.eq(session_id)).limit(number).offset(offset).orderDesc(ScDBMessageModelDao.Properties.Msg_time).list();
    }

    //根据opt_user_id分组，获取每个用户最近N条消息
    public List<ScDBMessageModel> getRecentMessageList(int number) {
        String sql =" inner join" +
                "(select a.opt_user_id,a.msg_time from "  + getAbstractDao().getTablename() + " a left join " +
                getAbstractDao().getTablename() + " b" +
                " on a.opt_user_id=b.opt_user_id and a.msg_time<=b.msg_time" +
                " group by a.opt_user_id,a.msg_time" +
                " having count(b.msg_time)<=" + String.valueOf(number) +
                ")b1" +
                " on T.opt_user_id=b1.opt_user_id and T.msg_time=b1.msg_time" +
                " order by T.opt_user_id,T.msg_time desc";
        return  getAbstractDao().queryRaw(sql);
    }

    public ScDBMessageModel getLastMessage(long session_id) {
        return  getQueryBuilder().where(ScDBMessageModelDao.Properties.Opt_user_id.eq(session_id)).
                orderDesc(ScDBMessageModelDao.Properties.Msg_time).limit(1).unique();
    }

    public List<ScDBMessageModel> getMessageList(long session_id) {
        return  getQueryBuilder().where(ScDBMessageModelDao.Properties.Opt_user_id.eq(session_id)).list();
    }

    public boolean deleteAllMessage(long session_id) {
        List<ScDBMessageModel> all_message = getQueryBuilder().where(ScDBMessageModelDao.Properties.Opt_user_id.eq(session_id)).list();
        return deleteList(all_message);
    }
}
