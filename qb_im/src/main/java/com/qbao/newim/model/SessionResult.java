package com.qbao.newim.model;

/**
 * Created by shiyunjie on 2017/9/22.
 */

public class SessionResult {
    public int add_index;
    public int remove_index;
    public SessionModel op_s_model;
    public SessionResult()
    {
        add_index = -1;
        remove_index = -1;
        op_s_model = null;
    }
}
