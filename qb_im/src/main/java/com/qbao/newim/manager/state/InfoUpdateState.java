package com.qbao.newim.manager.state;

import android.os.Handler;
import android.os.Message;

import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.StateConstDef;
import com.qbao.newim.util.DataObserver;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by shiyunjie on 2017/10/13.
 */

public class InfoUpdateState extends State
{
    private final short cur_state = StateConstDef.INFO_UPDATE_FINISHED;
    private ArrayList<Short> next_state_list = new ArrayList<>();

    private HashMap<Short, Boolean> condition_state = new HashMap<>();

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

    public InfoUpdateState()
    {
        condition_state.put(StateConstDef.FRIEND_INFO, true);
        condition_state.put(StateConstDef.GROUP_INFO, true);
        condition_state.put(StateConstDef.SC_OFFLINE_MSG, true);
        condition_state.put(StateConstDef.GC_OFFLINE_MSG, true);
        condition_state.put(StateConstDef.OC_OFFLINE_MSG, true);

        next_state_list.add(StateConstDef.FINISHED);
    }

    @Override
    public short EnterState(short last_state)
    {
        condition_state.remove(last_state);
        if(condition_state.size() <= 0)
        {
            m_handler.sendEmptyMessage(DataConstDef.EVENT_STATE_MACHINE_FINISH);
        }
        return 0;
    }

    @Override
    public ArrayList<Short> FinishState()
    {
        return next_state_list;
    }

    @Override
    public void ResetState()
    {
        condition_state.put(StateConstDef.FRIEND_INFO, true);
        condition_state.put(StateConstDef.GROUP_INFO, true);
    }
}
