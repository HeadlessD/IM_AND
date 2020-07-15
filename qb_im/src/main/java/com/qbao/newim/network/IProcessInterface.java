package com.qbao.newim.network;

/**
 * Created by shiyunjie on 17/3/1.
 */

public interface IProcessInterface
{
    int OnProcess(int packet_id, int socket, byte[] buffer, int len);
}
