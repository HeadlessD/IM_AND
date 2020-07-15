package com.qbao.newim.manager.state;

import android.os.Handler;
import android.os.Message;

import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.StateConstDef;
import com.qbao.newim.manager.NIMContactManager;
import com.qbao.newim.manager.NIMFriendInfoManager;
import com.qbao.newim.manager.NIMGroupInfoManager;
import com.qbao.newim.manager.NIMGroupMsgManager;
import com.qbao.newim.manager.NIMGroupUserManager;
import com.qbao.newim.manager.NIMMsgCountManager;
import com.qbao.newim.manager.NIMMsgManager;
import com.qbao.newim.manager.NIMOfficialManager;
import com.qbao.newim.manager.NIMSessionManager;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.qbdb.manager.BaseManager;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.SharedPreferenceUtil;

import java.util.ArrayList;

/**
 * Created by shiyunjie on 2017/10/12.
 */

public class LoadState extends State
{
    private final short cur_state = StateConstDef.LOAD_DATA;
    private ArrayList<Short> next_state_list = new ArrayList<>();
    private boolean m_pending = false;

    private Handler m_handler = new Handler(new Handler.Callback()
    {
        @Override
        public boolean handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case DataConstDef.EVENT_STATE_MACHINE_FINISH:
                {
                    DataObserver.Notify(DataConstDef.EVENT_STATE_MACHINE_FINISH, cur_state, null);
                }
                break;
            }
            return false;
        }
    });

    public LoadState()
    {
        next_state_list.add(StateConstDef.CONNECT);
    }

    public void PendingState(boolean flag)
    {
        m_pending = flag;
    }

    @Override
    public short EnterState(short last_state)
    {
        /******清除所有数据*********/
        NIMContactManager.getInstance().clear();
        NIMFriendInfoManager.getInstance().clear();
        NIMGroupInfoManager.getInstance().clear();
        NIMGroupUserManager.getInstance().clear();
        NIMMsgManager.getInstance().clear();
        NIMSessionManager.getInstance().clear();
        NIMUserInfoManager.getInstance().clear();
        // TODO: 2017/10/18 添加公众号清除函数
        SharedPreferenceUtil.clear();
        BaseManager.closeDbConnections();
        /******清除所有数据*********/

        /******初始化所有数据*********/
        long user_id = NIMUserInfoManager.getInstance().GetSelfUserId();
        if(user_id > 0)
        {
            BaseManager.initOpenHelper(AppUtil.GetContext(), String.valueOf(user_id));
            NIMFriendInfoManager.getInstance().init();
            //初始化私聊消息
            NIMMsgManager.getInstance().init();
            //初始化群信息
            NIMGroupInfoManager.getInstance().init();
            //初始化群聊消息
            NIMGroupMsgManager.getInstance().init();
            //初始化会话框
            NIMSessionManager.getInstance().init();
            //初始化未读消息数
            NIMMsgCountManager.getInstance().init();
            //公众号
            NIMOfficialManager.getInstance().init();
        }
        /******初始化所有数据*********/

        if(!m_pending)
            m_handler.sendEmptyMessage(DataConstDef.EVENT_STATE_MACHINE_FINISH);
        return cur_state;
    }

    @Override
    public ArrayList<Short> FinishState()
    {
        return next_state_list;
    }

    @Override
    public void ResetState()
    {
    }
}
