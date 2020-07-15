package com.qbao.newim.processor;

import com.google.flatbuffers.FlatBufferBuilder;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.NetConstDef;
import com.qbao.newim.constdef.PackTypeDef;
import com.qbao.newim.constdef.StateConstDef;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.LoginModel;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.netcenter.PackSessionMgr;
import com.qbao.newim.network.IProcessInterface;
import com.qbao.newim.util.BaseUtil;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.Logger;

import java.nio.ByteBuffer;

import commonpack.S_RQ_HEAD;
import syspack.T_LOGIN_RS;
import syspack.T_SERVER_DISCON_ID;
import syspack.T_TIME_SYNC_RQ;
import syspack.T_TIME_SYNC_RS;

/**
 * Created by shiyunjie on 17/3/2.
 */

public class SysProcessor extends BaseProcessor
{
    private static final String TAG = "SysProcessor";
    private ProcessLoginRS p_login_rs = new ProcessLoginRS();
    private ProcessHeartRS p_heart_rs = new ProcessHeartRS();
    private ProcessKickPackID p_kick_pack_id = new ProcessKickPackID();
    private ProcessTimeSync p_time_sync_rs = new ProcessTimeSync();

    public SysProcessor()
    {
        Init();
    }

    @Override
    protected void Init()
    {
        super.Init();
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_LOGIN_RS, p_login_rs);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_HEART_RS, p_heart_rs);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_SERVER_DISCON_ID, p_kick_pack_id);
        NetCenter.getInstance().AttachNetMsg(PackTypeDef.NEW_DEF_TIME_SYNC_RS, p_time_sync_rs);
    }

    @Override
    protected void ProcessEvent()
    {
        super.ProcessEvent();
        long last_send_time = NetCenter.getInstance().GetLastHeartTime();
        long cur_time = BaseUtil.GetSecondTime();
        if((cur_time - last_send_time) >= NetConstDef.HEART_BEAT_TIME)
        {
            SendHeartRQ();
            NetCenter.getInstance().SetLastHeartTime(cur_time);
        }
    }

    public boolean sendTimeSyncRQ() {
        FlatBufferBuilder builder = new FlatBufferBuilder();
        T_TIME_SYNC_RQ.startT_TIME_SYNC_RQ(builder);
        super.SendRQ(PackTypeDef.NEW_DEF_TIME_SYNC_RQ, builder);
        return true;
    }

    //登录
    public boolean SendLoginRQ(LoginModel user_model)
    {
        NetCenter.getInstance().SetNetStatus(NetConstDef.E_NET_STATUS.LOGINING, 0);
        FlatBufferBuilder builder = new FlatBufferBuilder();
        user_model.Serialize(builder);

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
        // 加密
        rq_body = NetCenter.getInstance().EncryptBody(rq_body);
        int result = NetCenter.getInstance().SendPack(PackTypeDef.NEW_DEF_LOGIN_RQ, rq_body, rq_body.length);
        if (result <= 0) {
            Logger.error(TAG, "packet_id = " + PackTypeDef.NEW_DEF_LOGIN_RQ + "send failed");
        }

        PackSessionMgr.getInstance().AddPackSession(PackTypeDef.NEW_DEF_LOGIN_RQ, pack_session_id, rq_body, rq_body.length);

        Logger.info(TAG, "send user_id = " +  NIMUserInfoManager.getInstance().GetSelfUserId());
        return true;
    }
    class ProcessLoginRS implements IProcessInterface
    {
        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len)
        {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_LOGIN_RS login_rs = T_LOGIN_RS.getRootAsT_LOGIN_RS(byte_buffer);
            if(BodyEmpty(login_rs))
            {
                Logger.error(TAG, "login rs is null");
                return -1;
            }

            if(false == CheckHead(login_rs.sRsHead()))
            {
                Logger.error(TAG, "login rs head is null");
                return -1;
            }

            NetCenter.getInstance().SetNetStatus(NetConstDef.E_NET_STATUS.LOGINED, 0);
            //设置服务器时间
            BaseUtil.SetServerTime(login_rs.serverTime());
            //通知状态机状态完成
            DataObserver.Notify(DataConstDef.EVENT_STATE_MACHINE_FINISH, StateConstDef.LOGIN, null);
            //外部回调通知
            NetCenter.getInstance().NotifyStatusDelegate(NetConstDef.E_NET_STATUS.LOGINED);
            return 1;
        }
    }


    //心跳包
    public boolean SendHeartRQ()
    {
        NetCenter.getInstance().SendPack(PackTypeDef.NEW_DEF_HEART_RQ, null, 0);
        return true;
    }

    class ProcessHeartRS implements IProcessInterface
    {
        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len)
        {
            Logger.debug(TAG, "ProcessHeartRS  recv");
            return 1;
        }
    }

    //踢人包
    class ProcessKickPackID implements IProcessInterface
    {
        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len)
        {

            Logger.info("SysProcessor", "ProcessKickPackID");
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_SERVER_DISCON_ID kick_rs = T_SERVER_DISCON_ID.getRootAsT_SERVER_DISCON_ID(byte_buffer);
            if(null == kick_rs)
            {
                return -1;
            }
            NetCenter.getInstance().SetNetStatus(NetConstDef.E_NET_STATUS.BEKICKED, kick_rs.result());
            return 1;
        }
    }

    // 时间同步包
    class ProcessTimeSync implements IProcessInterface {

        @Override
        public int OnProcess(int packet_id, int socket, byte[] buffer, int len) {
            ByteBuffer byte_buffer = ByteBuffer.wrap(buffer);
            T_TIME_SYNC_RS time_sync_rs = T_TIME_SYNC_RS.getRootAsT_TIME_SYNC_RS(byte_buffer);
            if (time_sync_rs == null) {
                return -1;
            }

            if(false == CheckHead(time_sync_rs.sRsHead()))
            {
                Logger.error(TAG, "time_sync_rs rs head is null");
                return -1;
            }

            Logger.error("ProcessTimeSync", "" + time_sync_rs.serverTime());
            BaseUtil.SetServerTime(time_sync_rs.serverTime());
            return 1;
        }
    }
}
