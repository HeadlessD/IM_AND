package com.qbao.newim.network;

/**
 * Created by shiyunjie on 17/3/1.
 */

public class JniNetCallBack implements IProtocolCallBack
{
    private IProtocolCallBack net_center = null;
    public void SetCallBack(IProtocolCallBack callback)
    {
        net_center = callback;
    }

    @Override
    public int OnRecvData(int packet_id, int socket, byte[] buffer, int buf_len)
    {
        net_center.OnRecvData(packet_id, socket, buffer, buf_len);
        return 0;
    }

    @Override
    public int OnError(int err_type)
    {
        net_center.OnError(err_type);
        return 0;
    }

    @Override
    public int OnClose(int socket, boolean client_closed)
    {
        net_center.OnClose(socket, client_closed);
        return 0;
    }

    @Override
    public int OnConnected(int socket)
    {
        net_center.OnConnected(socket);
        return 0;
    }

    @Override
    public int OnConnectFailure(int socket)
    {
        net_center.OnConnectFailure(socket);
        return 0;
    }
}
