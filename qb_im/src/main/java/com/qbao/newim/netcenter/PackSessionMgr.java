package com.qbao.newim.netcenter;

import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.NetConstDef;
import com.qbao.newim.model.NIM_Chat_ID;
import com.qbao.newim.util.BaseUtil;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.Logger;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by shiyunjie on 17/3/2.
 */
public class PackSessionMgr {
    private static final String TAG = "PackSessionMgr";
    private static PackSessionMgr _instance = new PackSessionMgr();

    public static PackSessionMgr getInstance() {
        return _instance;
    }

    private PackSessionMgr() {
    }

    class PackInfo {
        int packet_id;
        byte[] buffer;
        int buf_len;
        long start_time;
        int resend_time;
    }

    private HashMap<Integer, PackInfo> time_out_map = new HashMap<>();
    private HashMap<Integer, NIM_Chat_ID> pack_msg_map = new HashMap<>();
    private int pack_session_id = 1;


    public int GetPackSessionID() {
        pack_session_id++;
        pack_session_id = pack_session_id % 2147483647;
        return pack_session_id;
    }

    public boolean AddPackSession(int packet_id, int pack_session_id, byte[] buffer, int buf_len) {
        if (pack_session_id <= 0) {
            return false;
        }

        PackInfo pack_info = new PackInfo();
        pack_info.packet_id = packet_id;
        pack_info.buffer = buffer;
        pack_info.buf_len = buf_len;
        pack_info.start_time = BaseUtil.GetSecondTime();
        pack_info.resend_time = 0;

        synchronized (time_out_map)
        {
            time_out_map.put(pack_session_id, pack_info);
        }

        return true;
    }

    public void DelPackSession(int pack_session_id) {
        synchronized (time_out_map)
        {
            if (time_out_map.size() <= 0 || !time_out_map.containsKey(pack_session_id)) {
                return;
            }

            time_out_map.remove(pack_session_id);
        }
    }

    public void CheckPackSession() {
        if (pack_session_id <= 0) {
            return;
        }

        long cur_time = BaseUtil.GetSecondTime();
        synchronized (time_out_map)
        {
            Iterator iterator = time_out_map.keySet().iterator();
            while (iterator.hasNext()) {
                Object pack_session_id = iterator.next();
                PackInfo pack_info = time_out_map.get(pack_session_id);
                if (pack_info == null) {
                    Logger.error(TAG, "pack_session_id = " + pack_session_id + " value  is null");
                    iterator.remove();
//                time_out_map.remove(pack_session_id);
                    return;
                }

                if ((cur_time - pack_info.start_time) <= NetConstDef.MAX_TIME_OUT) {
                    continue;
                }

                //到了超时时间，还有重发次数
                if (pack_info.resend_time < NetConstDef.MAX_RESEND_TIME) {
                    Logger.debug(TAG, "pack_info resend pack_session_id = " + pack_session_id +
                            "packet_id = " + pack_info.packet_id);
                    pack_info.resend_time++;
                    pack_info.start_time = cur_time;
                    NetCenter.getInstance().SendPack(pack_info.packet_id, pack_info.buffer, pack_info.buf_len);
                    continue;
                }

                pack_info.resend_time = 0;
                //断开连接 todo:重连
                Logger.debug(TAG, "pack_info resend failed pack_session_id = " + pack_session_id +
                        "packet_id = " + pack_info.packet_id);
                NetCenter.getInstance().DisConnect();
                Reset();
                return;
            }
        }
    }

    public void AddPackMsg(int pack_session_id, NIM_Chat_ID chat_id) {
        if (pack_session_id <= 0) {
            return;
        }

        if (pack_msg_map.containsKey(pack_session_id)) {
            Logger.error(TAG, "repeat add pack msg pack_session_id = " + pack_session_id +
                    "message_id = " + chat_id.message_id);
            pack_msg_map.remove(pack_session_id);
        }

        pack_msg_map.put(pack_session_id, chat_id);
    }

    public NIM_Chat_ID GetPackMsg(int pack_session_id) {
        NIM_Chat_ID chat_id = new NIM_Chat_ID();
        if (pack_session_id <= 0) {
            chat_id.session_id = 0;
            chat_id.message_id = -1;
            return chat_id;
        }

        if (!pack_msg_map.containsKey(pack_session_id)) {
            chat_id.session_id = 0;
            chat_id.message_id = -1;
            return chat_id;
        }

        chat_id = pack_msg_map.get(pack_session_id);
        return chat_id;
    }

    public void DelPackMsg(int pack_session_id) {
        if (pack_session_id <= 0) {
            return;
        }

        if (!pack_msg_map.containsKey(pack_session_id)) {
            return;
        }

        pack_msg_map.remove(pack_session_id);
    }

    public void ResetPackMsg() {
        if (pack_msg_map.size() <= 0) {
            return;
        }

        Iterator iter = pack_msg_map.keySet().iterator();
        while (iter.hasNext()) {
            NIM_Chat_ID chat_id = pack_msg_map.get(iter.next());
            //通知上层未发送成功的消息
            DataObserver.Notify(DataConstDef.EVENT_PROCESS_MESSAGE_TIME_OUT, chat_id, null);
        }
        pack_msg_map.clear();
    }

    public void Reset() {
        synchronized (time_out_map)
        {
            time_out_map.clear();
        }
        ResetPackMsg();
    }
}
