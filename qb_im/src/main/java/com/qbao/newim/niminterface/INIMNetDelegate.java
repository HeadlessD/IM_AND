package com.qbao.newim.niminterface;

import android.app.Activity;

import com.qbao.newim.model.message.GcMessageModel;
import com.qbao.newim.model.message.OcMessageModel;
import com.qbao.newim.model.message.ScMessageModel;

/**
 * Created by chenjian on 2017/4/26.
 * nim public call back interface
 */

public interface INIMNetDelegate
{
     /**
       *当连接成功之后会调用此接口
      */
     int  OnConnected();
     /**
      *当登录成功之后会调用此接口
      */
     int  OnLogined();
     /**
      *当IM数据更新完成之后会调用此接口, 所有业务逻辑可以基于此回调之后
      */
     int  OnUpdateFinished();
     /**
      *当网络出现错误之后会调用此接口
      */
     void OnError();
     /**
      *当连接关闭之后会调用此接口
      */
     void OnClose();
     /**
      *当连接错误之后会调用此接口
      */
     void OnConnectFailure();
     /**
      *当被服务器踢了之后会调用此接口并告诉你原因
      */
     void OnKicked(String reason);
     /**
      *发送私聊消息回调接口
      */
     void ProcessScMessage(ScMessageModel model);
     /**
      *发送群聊消息回调接口
      */
     void ProcessGcMessage(GcMessageModel model);
     /**
      *发送公众号消息回调接口
      */
     void ProcesscOcMessage(OcMessageModel model);
     /**
      *发送系统消息回调接口
      */
     void ProcesscSysMessage(OcMessageModel model);
}
