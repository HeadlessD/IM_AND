package com.qbao.newim.manager.state;

import com.qbao.newim.constdef.NetConstDef;
import com.qbao.newim.constdef.StateConstDef;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.LoginModel;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.util.BaseUtil;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by shiyunjie on 2017/10/12.
 */

public class ConnectState extends State
{
    private final short cur_state = StateConstDef.CONNECT;
    private ArrayList<Short> next_list = new ArrayList<>();

    public ConnectState()
    {
        next_list.add(StateConstDef.LOGIN);
    }

    @Override
    public short EnterState(short last_state)
    {
        LoginModel login_model = NIMUserInfoManager.getInstance().GetLoginModel();
        if(login_model.host.isEmpty())
        {

            Random rd = new Random();
            int index = Math.abs(rd.nextInt() % NetConstDef.port_list.size());
            NetCenter.getInstance().Connect(NetConstDef.host, NetConstDef.port_list.get(index), NetConstDef.domain);
        }
        else
        {
            NetCenter.getInstance().Connect(login_model.host,login_model.port, login_model.domain);
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
