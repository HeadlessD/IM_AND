package com.qbao.newim.manager.state;

import com.qbao.newim.constdef.StateConstDef;

import java.util.ArrayList;

/**
 * Created by shiyunjie on 2017/10/13.
 */

public class ExceptionState extends State
{
    private final short cur_state = StateConstDef.EXCEPTION;
    private ArrayList<Short> next_state_list = new ArrayList<>();



    public ExceptionState()
    {
        next_state_list.add(StateConstDef.EXCEPTION);
    }

    @Override
    public short EnterState(short last_state)
    {
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
