package com.qbao.newim.manager;

import android.os.Handler;
import android.os.Message;

import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.manager.state.NIMStateMachine;
import com.qbao.newim.model.NIM_Chat_ID;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.IDataObserver;

/**
 * Created by shiyunjie on 2017/10/11.
 */

//负责处理逻辑层的消息队列通知
public class NIMObserverManager implements IDataObserver
{
    public static String TAG = NIMObserverManager.class.getSimpleName();

    public NIMObserverManager()
    {
        DataObserver.Register(this);
    }

    public Handler m_handler = new Handler(new Handler.Callback()
    {
        @Override
        public boolean handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case DataConstDef.EVENT_PROCESS_MESSAGE_TIME_OUT:
                    {
                        //通知界面层更新消息状态
                        DataObserver.Notify(DataConstDef.EVENT_MESSAGE_TIME_OUT, msg.obj, null);
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    @Override
    public void OnChange(int param1, Object param2, Object param3)
    {
        switch (param1)
        {
            // TODO: 2017/10/11 史云杰，补充公众号消息状态设置
            case DataConstDef.EVENT_PROCESS_MESSAGE_TIME_OUT:
                {
                    NIM_Chat_ID chat_id = (NIM_Chat_ID)param2;
                    if(null == chat_id)
                        break;
                    if(chat_id.chat_type == MsgConstDef.MSG_CHAT_TYPE.PRIVATE)
                    {
                        NIMMsgManager.getInstance().SetMessageStatus(chat_id.session_id,
                                chat_id.message_id, MsgConstDef.MSG_STATUS.SEND_FAILED);
                    }
                    else if(chat_id.chat_type == MsgConstDef.MSG_CHAT_TYPE.GROUP)
                    {
                        NIMGroupMsgManager.getInstance().SetMessageStatus(chat_id.session_id,
                                chat_id.message_id, MsgConstDef.MSG_STATUS.SEND_FAILED);
                    }
                    else if(chat_id.chat_type == MsgConstDef.MSG_CHAT_TYPE.PUBLIC)
                    {

                    }

                    m_handler.sendMessage(m_handler.obtainMessage(DataConstDef.EVENT_PROCESS_MESSAGE_TIME_OUT, param2));
                }
                break;
            case DataConstDef.EVENT_SC_CHAT_SESSION:
                {
                    // TODO: 2017/10/13 史云杰添加对应的铃音提示
                }
                break;
            case DataConstDef.EVENT_STATE_MACHINE_FINISH:
                {
                    NIMStateMachine.getInstance().Next((short)param2);
                }
                break;
            default:
                break;
        }
    }
}
