package com.qbao.newim.manager.state;

import java.util.ArrayList;

/**
 * Created by shiyunjie on 2017/10/12.
 */

//父类状态机
public abstract class State
{
    /**
        param: last_state
        return: cur_state
     */
    public abstract short EnterState(short last_state);
    /**
        return: next_state
     */
    public abstract ArrayList<Short> FinishState();
    public abstract void ResetState();
}
