package com.qbao.newim.qbdb.manager;

import com.qbao.newim.model.message.GcDBMessageModel;
import com.qbao.newim.qbdb.GcDBMessageModelDao;

import org.greenrobot.greendao.AbstractDao;

import java.util.List;
/**
 * Created by qlguoze on 17/9/26.
 */

public class GroupMsgDbManager extends BaseManager<GcDBMessageModel, String> {
    @Override
    public AbstractDao<GcDBMessageModel, String> getAbstractDao() {
        return daoSession.getGcDBMessageModelDao();
    }

    public static GroupMsgDbManager instance;
    public static GroupMsgDbManager getInstance()
    {
        if (instance == null)
        {
            instance = new GroupMsgDbManager();
        }

        return instance;
    }

    //根据opt_user_id分组，获取每个用户最近N条消息
    public List<GcDBMessageModel> getRecentGroupMessageList(int number)
    {
        String sql =" inner join" +
                "(select a.group_id,a.msg_time from "  + getAbstractDao().getTablename() + " a left join " +
                getAbstractDao().getTablename() + " b" +
                " on a.group_id=b.group_id and a.msg_time<=b.msg_time" +
                " group by a.group_id,a.msg_time" +
                " having count(b.msg_time)<=" + String.valueOf(number) +
                ")b1" +
                " on T.group_id=b1.group_id and T.msg_time=b1.msg_time" +
                " order by T.group_id,T.msg_time desc";
        return  getAbstractDao().queryRaw(sql);
    }

    public List<GcDBMessageModel> getMessageList(long group_id, int offset, int number)
    {
        return getQueryBuilder().where(GcDBMessageModelDao.Properties.Group_id.eq(group_id))
                .offset(offset).limit(number).orderDesc(GcDBMessageModelDao.Properties.Msg_time).list();
    }

    // 这个函数需要修改掉
    public boolean deleteAllMessage(long group_id)
    {
        List<GcDBMessageModel> all_message = getQueryBuilder().where(GcDBMessageModelDao.Properties.Group_id.eq(group_id)).list();
        return deleteList(all_message);
    }
}