package com.qbao.newim.helper;

import com.qbao.newim.model.IMGroupUserInfo;

/**
 * Created by chenjian on 2017/6/29.
 */

public interface IGroupKickMember {
    void onKick(IMGroupUserInfo info, boolean checked);
}
