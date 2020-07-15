package com.qbao.newim.manager.state;

import com.qbao.newim.constdef.StateConstDef;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.IMUserInfo;
import com.qbao.newim.processor.GlobalProcessor;
import com.qbao.newim.processor.UserInfoGetProcessor;
import com.qbao.newim.qbdb.manager.UserInfoDbManager;

import java.util.ArrayList;

/**
 * Created by shiyunjie on 2017/10/12.
 */

public class UserInfoState extends State
{
    private final short cur_state = StateConstDef.USER_INFO;
    private ArrayList<Short> next_state_list = new ArrayList<>();

    public UserInfoState()
    {
        next_state_list.add(StateConstDef.FRIEND_INFO);
        next_state_list.add(StateConstDef.GROUP_INFO);
        next_state_list.add(StateConstDef.SC_OFFLINE_MSG);
        next_state_list.add(StateConstDef.GC_OFFLINE_MSG);
        next_state_list.add(StateConstDef.OC_OFFLINE_MSG);
    }

    @Override
    public short EnterState(short last_state)
    {
        UserInfoGetProcessor userProcessor = GlobalProcessor.getInstance().getUser_processor();
        long cur_user_id = NIMUserInfoManager.getInstance().GetSelfUserId();
        IMUserInfo self_info = UserInfoDbManager.getInstance().getSingleIMUser(cur_user_id);
        String token = "";
        if (self_info != null) {
            token = self_info.token;
        }
        userProcessor.SendSelfInfoRQ(token);
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
