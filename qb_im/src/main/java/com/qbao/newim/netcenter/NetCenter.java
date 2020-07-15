package com.qbao.newim.netcenter;

import android.os.Handler;
import android.os.Message;

import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.NetConstDef;
import com.qbao.newim.constdef.StateConstDef;
import com.qbao.newim.network.IProcessInterface;
import com.qbao.newim.network.IProtocolCallBack;
import com.qbao.newim.network.JniNetCallBack;
import com.qbao.newim.network.JniNetWork;
import com.qbao.newim.niminterface.INIMNetDelegate;
import com.qbao.newim.util.BaseUtil;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.Logger;


/**
 * Created by shiyunjie on 17/3/1.
 */
public class NetCenter implements IProtocolCallBack {
    private static String TAG = "NetCenter";
    private static NetCenter _instance = null;
    private JniNetCallBack jni_call_back = new JniNetCallBack();
    private JniNetWork jni_network = null;
    private INIMNetDelegate m_net_delegate = null;

    //回调管理类
    private NetMsgMgr net_msg_mgr = new NetMsgMgr();
    private int start_random = 0;

    //连接地址
    private String last_host = "";
    private int last_port = 0;
    private boolean last_domain = false;
    //网络连接状态
    private NetConstDef.E_NET_STATUS e_net_status = NetConstDef.E_NET_STATUS.CLOSED;
    //重连次数
    private int recon_count = 0;
    //收到消息
    private static final int TRANS_RECV_MSG_NOTIFY = 0x1234;
    private static final int TRANS_DELEGATE_NOTIFY = 0x1235;
    //心跳
    private long last_heart_time = 0;

    public static NetCenter getInstance() {
        if (_instance == null) {
            _instance = new NetCenter();
        }

        return _instance;
    }

    private NetCenter() {
    }

    private class TRANS_PARAMS {
        private int packet_id;
        private int socket;
        private byte[] buffer;
        private int buf_len;
    }

    //主线程消息
    private final Handler m_handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TRANS_RECV_MSG_NOTIFY:
                    TRANS_PARAMS trans_params = (TRANS_PARAMS) msg.obj;
                    OnRecvData_impl(trans_params.packet_id, trans_params.socket, trans_params.buffer, trans_params.buf_len);
                    break;
                case TRANS_DELEGATE_NOTIFY:
                    NetConstDef.E_NET_STATUS e_net_status = (NetConstDef.E_NET_STATUS)msg.obj;
                    NotifyStatusDelegate(e_net_status);
                    break;
            }
        }
    };

    //网络线程
    public class NetWorkThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                if (null == jni_network) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    continue;
                }

                jni_network.Update();
            }
        }
    }

    public boolean Init() {
        //初始化jni
        jni_network = new JniNetWork();
        jni_network.create();
        //这里设置callback回调
        jni_network.InitCallBack(jni_call_back);
        jni_call_back.SetCallBack(this);
        //初始化随机数
        InitRandom();

        //开启网络线程
        try {
            (new Thread(new NetWorkThread())).start();
        } catch (UnsupportedOperationException e) {
            return false;
        }

        return true;
    }

    public byte[] EncryptBody(byte[] input) {
        return jni_network.EncryptBody(input, input.length);
    }

    public byte[] EncryptCookie(byte[] input) {
        return jni_network.EncryptCookie(input, input.length);
    }

    private void InitRandom() {
        long value = BaseUtil.GetMsTime();
        int liTempRand = (int) ((Math.random() + value) % 8 + 4);
        for (int i = 0; i < liTempRand; i++)
            Math.random();
        start_random = (int) (Math.random() * 1000000);
    }

    public long CreateMsgID() {
        long msg_id = 0;
        long server_time = BaseUtil.GetServerTime();
        start_random++;
        start_random = start_random & 0x3FFFFF;
        msg_id = (server_time << 22) & 0xFFFFFFFFFFC00000L;
        msg_id = msg_id + start_random;

        return msg_id;
    }

    public long getJniTime() {
        return jni_network.GetMicrosecond();
    }

    public long CreateGroupMsgId() {
        return BaseUtil.GetServerMicroTime();
    }

    public void SetNetStatus(NetConstDef.E_NET_STATUS e_status, int reason) {
        if (e_net_status == e_status) {
            if (e_status != NetConstDef.E_NET_STATUS.DISCONNECT) {
                return;
            }
            //超过最大重连次数CLOSED
            if (recon_count++ < NetConstDef.MAX_RECON_TIME) {
                return;
            }

            e_status = NetConstDef.E_NET_STATUS.CLOSED;
        }

        //如果已经是关闭状态了
//        if(e_status == NetConstDef.E_NET_STATUS.DISCONNECT &&
//                e_net_status == NetConstDef.E_NET_STATUS.CLOSED)
//        {
//            return;
//        }

        //如果已经被踢状态了
        if (e_net_status == NetConstDef.E_NET_STATUS.BEKICKED &&
                (e_status == NetConstDef.E_NET_STATUS.CLOSED ||
                        e_status == NetConstDef.E_NET_STATUS.DISCONNECT)) {
            return;
        }

        e_net_status = e_status;

        //登陆成功
        if (e_net_status == NetConstDef.E_NET_STATUS.LOGINED) {
            recon_count = 0;
        }

        //网络状态改变抛出消息通知上层
        DataObserver.Notify(DataConstDef.EVENT_LOGIN_STATUS, e_status, reason);
    }

    public NetConstDef.E_NET_STATUS GetNetStatus() {
        return e_net_status;
    }

    public boolean IsLogined()
    {
        return e_net_status == NetConstDef.E_NET_STATUS.LOGINED;
    }

    public boolean IsLogining()
    {
        return e_net_status == NetConstDef.E_NET_STATUS.LOGINING;
    }

    public boolean IsConnected()
    {
        return e_net_status == NetConstDef.E_NET_STATUS.CONNECTED;
    }

    public boolean IsReConnecting()
    {
        return (e_net_status == NetConstDef.E_NET_STATUS.CONNECTING ||
                e_net_status == NetConstDef.E_NET_STATUS.DISCONNECT);
    }

    public void AttachNetMsg(int packet_id, IProcessInterface processor) {
        net_msg_mgr.AttachNetMsg(packet_id, processor);
    }

    public long GetLastHeartTime() {
        return last_heart_time;
    }

    public void SetLastHeartTime(long time) {
        last_heart_time = time;
    }

    public void ProcessEvent() {
        //todo 处理网络类型
    }

    public boolean Connect(String host, int port, boolean domain) {
        last_host = host;
        last_port = port;
        last_domain = domain;

        return CacheConnect();
    }

    public boolean CacheConnect() {
        if (last_host.isEmpty() || last_port <= 0) {
            Logger.error(TAG, "Connect host port is empty");
            return false;
        }

        SetNetStatus(NetConstDef.E_NET_STATUS.CONNECTING, 0);
        boolean ret = jni_network.Connect(last_host, last_port, last_domain);
        return ret;
    }

    public void DisConnect() {
        if (jni_network == null) {
            return;
        }

        jni_network.Close();
        SetNetStatus(NetConstDef.E_NET_STATUS.CLOSING, 0);
    }

    public int SendPack(int package_id, byte[] buffer, int buf_len) {
        if (null == jni_network) {
            return -1;
        }

        jni_network.SendPack(package_id, buffer, buf_len);
        return 1;
    }

    @Override
    public int OnRecvData(int packet_id, int socket, byte[] buffer, int buf_len) {
        TRANS_PARAMS trans_params = new TRANS_PARAMS();
        trans_params.packet_id = packet_id;
        trans_params.socket = socket;
        trans_params.buf_len = buf_len;
        trans_params.buffer = new byte[buf_len];
        System.arraycopy(buffer, 0, trans_params.buffer, 0, buf_len);
        Message msg = new Message();
        msg.what = TRANS_RECV_MSG_NOTIFY;
        msg.obj = trans_params;
        m_handler.sendMessage(msg);
        return 0;
    }

    public int OnRecvData_impl(int packet_id, int socket, byte[] buffer, int buf_len) {
        //收到包了延迟心跳时间
        last_heart_time = BaseUtil.GetSecondTime();
        net_msg_mgr.ProcessNetMsg(packet_id, socket, buffer, buf_len);
        return 0;
    }

    @Override
    public int OnError(int err_type) {
        Logger.error(TAG, "err_type = " + err_type);
        SetNetStatus(NetConstDef.E_NET_STATUS.CLOSED, 0);
        m_handler.sendMessage(m_handler.obtainMessage(TRANS_DELEGATE_NOTIFY, NetConstDef.E_NET_STATUS.ERROR));
        //通知状态机
        DataObserver.Notify(DataConstDef.EVENT_STATE_MACHINE_FINISH, StateConstDef.EXCEPTION, null);
        return 0;
    }

    @Override
    public int OnClose(int socket, boolean client_closed) {
        Logger.error(TAG, "server close client_closed = " + client_closed);
        if (client_closed) {
            SetNetStatus(NetConstDef.E_NET_STATUS.CLOSED, 0);
        } else {
            SetNetStatus(NetConstDef.E_NET_STATUS.DISCONNECT, 0);
        }
        m_handler.sendMessage(m_handler.obtainMessage(TRANS_DELEGATE_NOTIFY, NetConstDef.E_NET_STATUS.CLOSED));
        //通知状态机
        DataObserver.Notify(DataConstDef.EVENT_STATE_MACHINE_FINISH, StateConstDef.EXCEPTION, null);
        return 0;
    }

    @Override
    public int OnConnected(int socket) {
        SetNetStatus(NetConstDef.E_NET_STATUS.CONNECTED, 0);
        Logger.debug(TAG, "connect success");
        m_handler.sendMessage(m_handler.obtainMessage(TRANS_DELEGATE_NOTIFY, NetConstDef.E_NET_STATUS.CONNECTED));
        //通知状态机
        DataObserver.Notify(DataConstDef.EVENT_STATE_MACHINE_FINISH, StateConstDef.CONNECT, null);
        return 0;
    }

    @Override
    public int OnConnectFailure(int socket) {
        SetNetStatus(NetConstDef.E_NET_STATUS.CLOSED, 0);
        m_handler.sendMessage(m_handler.obtainMessage(TRANS_DELEGATE_NOTIFY, NetConstDef.E_NET_STATUS.CONNECT_FAIL));
        //通知状态机
        DataObserver.Notify(DataConstDef.EVENT_STATE_MACHINE_FINISH, StateConstDef.EXCEPTION, null);
        return 0;
    }

    public void SetNetDelegate(INIMNetDelegate net_delegate) {
        m_net_delegate = net_delegate;
    }
    public INIMNetDelegate GetNetDelegate() {
        return m_net_delegate;
    }

    public void NotifyStatusDelegate(NetConstDef.E_NET_STATUS e_status) {
        if(m_net_delegate == null)
            return;
        switch (e_status)
        {
            case CLOSED:
            {
                m_net_delegate.OnClose();
            }
            break;
            case CONNECTED:
            {
                m_net_delegate.OnConnected();
            }
            break;
            case LOGINED:
            {
                m_net_delegate.OnLogined();
            }
            break;
            case ERROR:
            {
                m_net_delegate.OnError();
            }
            break;
            case CONNECT_FAIL:
            {
                m_net_delegate.OnConnectFailure();
            }
            break;
            case UPDATE_FINISHED:
            {
                m_net_delegate.OnUpdateFinished();
            }
            break;
        }
    }
}
