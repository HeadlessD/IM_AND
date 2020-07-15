package com.qbao.newim.manager.state;

import android.os.Handler;
import android.os.Message;

import com.qbao.newim.configure.GlobalVariable;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.StateConstDef;
import com.qbao.newim.manager.NIMGroupMsgManager;
import com.qbao.newim.model.IMGetOfflineInfo;
import com.qbao.newim.processor.GlobalProcessor;
import com.qbao.newim.processor.GroupChatProcessor;
import com.qbao.newim.util.DataObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shiyunjie on 2017/10/13.
 */

public class GcOfflineMsgState extends State
{
    private final short cur_state = StateConstDef.GC_OFFLINE_MSG;
    private ArrayList<Short> next_list = new ArrayList<>();

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

    public GcOfflineMsgState()
    {
        next_list.add(StateConstDef.INFO_UPDATE_FINISHED);
    }

    @Override
    public short EnterState(short last_state)
    {
        ArrayList<IMGetOfflineInfo> list = NIMGroupMsgManager.getInstance().getGroupAllNextMessageID();
        GroupChatProcessor processor = GlobalProcessor.getInstance().getGc_processor();
        List<IMGetOfflineInfo> temp_list = new ArrayList<>();
        if(list.isEmpty())
        {
            m_handler.sendEmptyMessage(DataConstDef.EVENT_STATE_MACHINE_FINISH);
            return cur_state;
        }

        for (int i = 1; i <= list.size(); i++)
        {
            temp_list.add(list.get(i - 1));
            if ((i % GlobalVariable.GROUP_OFFLINE_COUNT == 0) || i == list.size())
            {
                IMGetOfflineInfo[] arr = temp_list.toArray(new IMGetOfflineInfo[temp_list.size()]);
                processor.sendGroupOfflineMsg(arr);
                temp_list.clear();
            }
        }
        return cur_state;
    }

    @Override
    public ArrayList<Short> FinishState()
    {
        return next_list;
    }

    @Override
    public void ResetState()
    {
    }
}
