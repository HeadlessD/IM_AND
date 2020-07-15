package com.qbao.newim.manager.state;

import com.qbao.newim.constdef.StateConstDef;
import com.qbao.newim.processor.FriendListProcessor;
import com.qbao.newim.processor.GlobalProcessor;

import java.util.ArrayList;

/**
 * Created by shiyunjie on 2017/10/12.
 */

public class FriendInfoState extends State
{
    private final short cur_state = StateConstDef.FRIEND_INFO;
    private ArrayList<Short> next_list = new ArrayList<>();

    public FriendInfoState()
    {
        next_list.add(StateConstDef.INFO_UPDATE_FINISHED);
    }
    @Override
    public short EnterState(short last_state)
    {
        FriendListProcessor friendListProcessor = GlobalProcessor.getInstance().getFriend_processor();
        friendListProcessor.sendFriendsRQ(0);
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
