package com.qbao.newim.manager.state;

import com.qbao.newim.constdef.StateConstDef;
import com.qbao.newim.processor.GlobalProcessor;
import com.qbao.newim.processor.UserChatProcessor;

import java.util.ArrayList;

/**
 * Created by shiyunjie on 2017/10/12.
 */

public class ScOfflineMsgState extends State
{
    private final short cur_state = StateConstDef.SC_OFFLINE_MSG;
    private ArrayList<Short> next_state_list = new ArrayList<>();

    public ScOfflineMsgState()
    {
        next_state_list.add(StateConstDef.INFO_UPDATE_FINISHED);
    }

    @Override
    public short EnterState(short last_status)
    {
        UserChatProcessor processor = GlobalProcessor.getInstance().GetScProcessor();
        processor.SendOfflineMsgRQ();
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
