package com.qbao.newim.processor;

import com.google.flatbuffers.FlatBufferBuilder;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.ErrorDetail;
import com.qbao.newim.constdef.NetConstDef;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.netcenter.PackSessionMgr;
import com.qbao.newim.util.BaseUtil;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.Logger;

import commonpack.S_RQ_HEAD;
import commonpack.S_RS_HEAD;

/**
 * Created by shiyunjie on 17/3/1.
 */

public abstract class BaseProcessor {
    private static final String TAG = "BaseProcessor";

    protected void Init() {

    }

    protected void ProcessEvent() {

    }

    protected boolean BodyEmpty(Object obj) {
        if(obj == null) {
            NetCenter.getInstance().DisConnect();
            return true;
        }
        return  false;
    }

    protected boolean CheckHead(S_RS_HEAD s_rs_head) {
        if (null == s_rs_head) {
            Logger.error(TAG, "s_rs_head null");
            NetCenter.getInstance().DisConnect();
            return false;
        }

        if (s_rs_head.userId() <= 0) {
            Logger.error(TAG, "s_rs_head.userId() null");
            NetCenter.getInstance().DisConnect();
            return false;
        }

        if (s_rs_head.packSessionId() < 0) {
            Logger.error(TAG, "s_rs_head.packSessionId() null");
            NetCenter.getInstance().DisConnect();
            return false;
        }

        PackSessionMgr.getInstance().DelPackSession(s_rs_head.packSessionId());
        // 检查code是否是成功返回码，并且是否是特殊返回码
        if (!(BaseUtil.CheckResult(s_rs_head.result()) || NetConstDef.checkSpecialCode(s_rs_head.result()))) {
            //根据不同的错误码返回通知上层显示不同的消息
            int error_code = BaseUtil.MakeErrorResult(s_rs_head.result());
            String error_msg = ErrorDetail.GetErrorDetail(error_code);
            Logger.error(TAG, error_msg);
            DataObserver.Notify(DataConstDef.EVENT_NET_ERROR, s_rs_head.result(), null);
            return false;
        }

        return true;
    }

    protected int SendRQ(int packet_id, FlatBufferBuilder builder) {
        long self_user_id = NIMUserInfoManager.getInstance().GetSelfUserId();
        int pack_session_id = PackSessionMgr.getInstance().GetPackSessionID();
        byte platform = NetConstDef.PLATFORM.APP;
        int rq_head = S_RQ_HEAD.createS_RQ_HEAD(builder, self_user_id, pack_session_id, platform);
        //等同于addSRqHead
        builder.addStruct(0, rq_head, 0);

        //等同于endT_XXX_RQ
        int end_offset = builder.endObject();
        //等同于finishT_XXX_RQBuffer
        builder.finish(end_offset);
        byte[] rq_body = builder.sizedByteArray();
        int result = NetCenter.getInstance().SendPack(packet_id, rq_body, rq_body.length);
        if (result <= 0) {
            Logger.error(TAG, "packet_id = " + packet_id + "send failed");
            return -1;
        }

        PackSessionMgr.getInstance().AddPackSession(packet_id, pack_session_id, rq_body, rq_body.length);
        return pack_session_id;
    }

    protected int SendRS(int packet_id, int pack_session_id, FlatBufferBuilder builder) {
        long self_user_id = NIMUserInfoManager.getInstance().GetSelfUserId();
        byte platform = NetConstDef.PLATFORM.APP;
        int ret_result = BaseUtil.MakeSuccessResult();
        int rs_head = S_RS_HEAD.createS_RS_HEAD(builder, self_user_id, pack_session_id, ret_result, platform);
        //等同于addSRqHead
        builder.addStruct(0, rs_head, 0);

        //等同于endT_XXX_RS
        int end_offset = builder.endObject();
        //等同于finishT_XXX_RSBuffer
        builder.finish(end_offset);
        byte[] rq_body = builder.sizedByteArray();
        int result = NetCenter.getInstance().SendPack(packet_id, rq_body, rq_body.length);
        if (result <= 0) {
            Logger.error(TAG, "packet_id = " + packet_id + "send failed");
            return -1;
        }
        return 1;
    }
}
