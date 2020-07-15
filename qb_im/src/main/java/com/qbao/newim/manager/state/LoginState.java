package com.qbao.newim.manager.state;

import android.os.Handler;
import android.os.Message;

import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.StateConstDef;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.LoginModel;
import com.qbao.newim.processor.GlobalProcessor;
import com.qbao.newim.processor.SysProcessor;
import com.qbao.newim.util.DataObserver;

import java.util.ArrayList;

/**
 * Created by shiyunjie on 2017/10/12.
 */

public class LoginState extends State
{
    private final short cur_state = StateConstDef.LOGIN;
    private ArrayList<Short> next_state_list = new ArrayList<>();

    private Handler m_handler = new Handler(new Handler.Callback()
    {
        @Override
        public boolean handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case DataConstDef.EVENT_STATE_MACHINE_FINISH:
                {
                    //异常状态处理
                    DataObserver.Notify(DataConstDef.EVENT_STATE_MACHINE_FINISH, StateConstDef.EXCEPTION, null);
                }
                break;
            }
            return false;
        }
    });

    public LoginState()
    {
        next_state_list.add(StateConstDef.USER_INFO);
    }

    @Override
    public short EnterState(short last_state)
    {
        SysProcessor sys_processor = GlobalProcessor.getInstance().getSys_processor();
        LoginModel login_model = NIMUserInfoManager.getInstance().GetLoginModel();
        if (login_model == null)
        {
            m_handler.sendEmptyMessage(DataConstDef.EVENT_STATE_MACHINE_FINISH);
        }

        sys_processor.SendLoginRQ(login_model);
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
