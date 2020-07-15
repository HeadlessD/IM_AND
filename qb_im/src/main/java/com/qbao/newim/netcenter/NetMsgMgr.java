package com.qbao.newim.netcenter;

import com.qbao.newim.network.IProcessInterface;
import com.qbao.newim.util.Logger;

/**
 * Created by shiyunjie on 17/3/1.
 */

//回调管理类
public class NetMsgMgr
{
    private static String TAG = "NetMsgMgr";
    private static int MAX_PACK_COUNT = 65535;
    private IProcessInterface[] process_interface_list = new IProcessInterface[MAX_PACK_COUNT];

    public NetMsgMgr()
    {
    }

    public boolean AttachNetMsg(int packet_id, IProcessInterface processor)
    {
        if(packet_id >= MAX_PACK_COUNT || packet_id <= 0)
        {
            Logger.error(TAG, "packet_id invalid");
            return false;
        }

        process_interface_list[packet_id] = processor;
        return true;
    }

    public boolean DetachNetMsg(int packet_id)
    {
        if(packet_id >= MAX_PACK_COUNT || packet_id <= 0)
        {
            Logger.error(TAG, "packet_id invalid");
            return false;
        }

        process_interface_list[packet_id] = null;
        return true;
    }

    public int ProcessNetMsg(int packet_id, int socket, byte[] buffer, int buf_len)
    {
        if(packet_id >= MAX_PACK_COUNT || packet_id <= 0)
        {
            Logger.error(TAG, "packet_id invalid");
            return 0;
        }

        if(process_interface_list[packet_id] == null)
        {
            Logger.error(TAG, "packet_id invalid = " + packet_id);
            return 0;
        }

        return process_interface_list[packet_id].OnProcess(packet_id, socket, buffer, buf_len);
    }
}
