package com.qbao.newim.manager.state;

import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.NetConstDef;
import com.qbao.newim.constdef.StateConstDef;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by shiyunjie on 2017/10/13.
 */

public class NIMStateMachine
{
    private static final String TAG = NIMStateMachine.class.getSimpleName();
    private static NIMStateMachine instance = new NIMStateMachine();
    private HashMap<Short, State> m_state_map = new HashMap<>();
    private boolean m_finished = false;

    public static NIMStateMachine getInstance()
    {
        return instance;
    }

    public NIMStateMachine()
    {
        Init();
    }

    public void Init()
    {
        m_state_map.put(StateConstDef.EXCEPTION, new ExceptionState());
        m_state_map.put(StateConstDef.LOAD_DATA, new LoadState());
        m_state_map.put(StateConstDef.CONNECT, new ConnectState());
        m_state_map.put(StateConstDef.LOGIN, new LoginState());
        m_state_map.put(StateConstDef.USER_INFO, new UserInfoState());
        m_state_map.put(StateConstDef.FRIEND_INFO, new FriendInfoState());
        m_state_map.put(StateConstDef.GROUP_INFO, new GroupInfoState());
        m_state_map.put(StateConstDef.SC_OFFLINE_MSG, new ScOfflineMsgState());
        m_state_map.put(StateConstDef.GC_OFFLINE_MSG, new GcOfflineMsgState());
        m_state_map.put(StateConstDef.OC_OFFLINE_MSG, new OcOfflineMsgState());
        m_state_map.put(StateConstDef.INFO_UPDATE_FINISHED, new InfoUpdateState());
    }

    //从预加载数据状态启动
    public void PreLoadState()
    {
        LoadState state_obj = (LoadState)m_state_map.get(StateConstDef.LOAD_DATA);
        state_obj.PendingState(true);
        state_obj.EnterState(StateConstDef.LOAD_DATA);
        m_finished = false;
    }

    //从重新加载数据状态启动
    public void ReLoadState()
    {
        LoadState state_obj = (LoadState)m_state_map.get(StateConstDef.LOAD_DATA);
        state_obj.PendingState(false);
        state_obj.EnterState(StateConstDef.LOAD_DATA);
        m_finished = false;
    }

    //从连接状态启动
    public void StartConnect()
    {
        if(NetCenter.getInstance().IsConnected() ||
                NetCenter.getInstance().IsReConnecting())
            return;

        if(NetCenter.getInstance().IsLogined() ||
                NetCenter.getInstance().IsLogining())
        {
            return;
        }

        State state_obj = m_state_map.get(StateConstDef.CONNECT);
        state_obj.EnterState(StateConstDef.LOAD_DATA);
        m_finished = false;
    }

    public void Next(short state)
    {
        if(m_finished)
            return;

        State state_obj = m_state_map.get(state);
        if(null == state_obj)
        {
            Reset();
            Logger.error(TAG, "state not exsist: " + state);
            return;
        }

        ArrayList<Short> next_state_list = state_obj.FinishState();
        for(int index = 0; index  < next_state_list.size(); index++)
        {
            short next_state = next_state_list.get(index);
            if(next_state == StateConstDef.FINISHED)
            {
                //所有状态都完成了
                Finish();
                return;
            }
            else if(next_state == StateConstDef.EXCEPTION)
            {
                Exception();
                return;
            }

            State next_state_obj = m_state_map.get(next_state);
            if(null == next_state_obj)
            {
                Logger.error(TAG, "next_state_obj not exsist: " + state);
                return;
            }
            next_state_obj.EnterState(state);
        }
    }

    public void Finish()
    {
        //回调通知上层
        NetCenter.getInstance().NotifyStatusDelegate(NetConstDef.E_NET_STATUS.UPDATE_FINISHED);
        Reset();
        m_finished = true;
    }

    public void Exception()
    {
        Reset();
    }

    public void Reset()
    {
        //重置部分需要重置的状态即可
        m_state_map.get(StateConstDef.INFO_UPDATE_FINISHED).ResetState();
    }
}
