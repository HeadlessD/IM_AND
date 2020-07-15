package com.qbao.newim.manager.state;

import com.qbao.newim.constdef.StateConstDef;
import com.qbao.newim.manager.NIMOfficialManager;
import com.qbao.newim.model.IMOfficialInfo;
import com.qbao.newim.processor.GlobalProcessor;
import com.qbao.newim.processor.OfficialProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shiyunjie on 2017/10/13.
 */

public class OcOfflineMsgState extends State
{
    private final short cur_state = StateConstDef.OC_OFFLINE_MSG;
    private ArrayList<Short> next_state_list = new ArrayList<>();

    public OcOfflineMsgState()
    {
        next_state_list.add(StateConstDef.INFO_UPDATE_FINISHED);
    }

    @Override
    public short EnterState(short last_state)
    {
        List<IMOfficialInfo> list = NIMOfficialManager.getInstance().getOfficialList();
        IMOfficialInfo[] array = list.toArray(new IMOfficialInfo[list.size()]);
        OfficialProcessor processor = GlobalProcessor.getInstance().getOfficialProcessor();
        processor.GetOfficialMsg(array);
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
