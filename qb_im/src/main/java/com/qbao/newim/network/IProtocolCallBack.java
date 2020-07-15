package com.qbao.newim.network;

/**
 * Created by shiyunjie on 17/3/1.
 */

public interface IProtocolCallBack
{
    // $_FUNCTION_BEGIN ******************************
    // NAME:    OnRecvData
    // PARAM:   packet_id,socket,buffer,buf_len
    // RETURN:  int 1(success) -1(fail)
    // DETAIL:  recv buffer callback
    // $_FUNCTION_END ********************************
    int OnRecvData(int packet_id, int socket, byte[] buffer, int buf_len);
    // $_FUNCTION_BEGIN ******************************
    // NAME:    OnError
    // PARAM:   err_type
    // RETURN:  int 1(success) -1(fail)
    // DETAIL:  recv and send error callback
    // $_FUNCTION_END ********************************
    int OnError(int err_type);
    // $_FUNCTION_BEGIN ******************************
    // NAME:    OnClose
    // PARAM:   socket,client_closed
    // RETURN:  int 1(success) -1(fail)
    // DETAIL:  server closed connect if client_closed else client close socket
    // $_FUNCTION_END ********************************
    int OnClose(int socket, boolean client_closed);
    // $_FUNCTION_BEGIN ******************************
    // NAME:    OnConnected
    // PARAM:   socket
    // RETURN:  int 1(success) -1(fail)
    // DETAIL:  connect callback as soon as established
    // $_FUNCTION_END ********************************
    int  OnConnected(int socket);
    // $_FUNCTION_BEGIN ******************************
    // NAME:    OnConnectFailure
    // PARAM:   socket(0)
    // RETURN:  int 1(success) -1(fail)
    // DETAIL:  connect establish failure callback
    // $_FUNCTION_END ********************************
    int OnConnectFailure(int socket);
}
