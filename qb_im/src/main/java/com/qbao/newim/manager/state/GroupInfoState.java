package com.qbao.newim.manager.state;

import com.qbao.newim.constdef.StateConstDef;
import com.qbao.newim.manager.NIMGroupInfoManager;
import com.qbao.newim.processor.GlobalProcessor;
import com.qbao.newim.processor.GroupListProcessor;

import java.util.ArrayList;

/**
 * Created by shiyunjie on 2017/10/12.
 */

public class GroupInfoState extends State
{
    private final short cur_state = StateConstDef.GROUP_INFO;
    private ArrayList<Short> next_list = new ArrayList<>();

    public GroupInfoState()
    {
        next_list.add(StateConstDef.INFO_UPDATE_FINISHED);
    }

    @Override
    public short EnterState(short last_state)
    {
        GroupListProcessor processor = GlobalProcessor.getInstance().getGroupListProcessor();
        if (NIMGroupInfoManager.getInstance().getGroupCount() == 0)
        {
            //抛出完成消息
            processor.sendGroupListRQ();
        }
        else
        {
            processor.sendGroupIdRQ();
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
