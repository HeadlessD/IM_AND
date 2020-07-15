package com.qbao.newim.processor;

import com.qbao.newim.constdef.ErrorDetail;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.netcenter.PackSessionMgr;
import com.qbao.newim.util.Logger;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by shiyunjie on 17/3/2.
 */

public class GlobalProcessor {
    private static String TAG = "GlobalProcessor";
    private static GlobalProcessor _instance = null;
    //全局定时器
    private Timer global_timer = null;
    private TimerTask global_timer_task = null;

    private SysProcessor sys_processor = null;
    private UserChatProcessor sc_processor = null;
    private GroupChatProcessor gc_processor = null;
    private FriendListProcessor friend_processor = null;
    private UserInfoGetProcessor user_processor = null;
    private UserUpdateProcessor upload_user_processor = null;
    private UserListProcessor user_list_processor = null;
    private FriendAddProcessor friendAddProcessor = null;
    private FriendDelProcessor friendDelProcessor = null;
    private GroupOperateProcessor groupOperateProcessor = null;
    private GroupListProcessor groupListProcessor = null;
    private GroupGetProcessor groupGetProcessor = null;
    private OfficialProcessor officialProcessor = null;

    public GroupListProcessor getGroupListProcessor() {
        return groupListProcessor;
    }

    public GroupGetProcessor getGroupGetProcessor() {
        return groupGetProcessor;
    }

    public UserUpdateProcessor getUpload_user_processor() {
        return upload_user_processor;
    }

    public SysProcessor getSys_processor() {
        return sys_processor;
    }

    public UserChatProcessor GetScProcessor() {
        return sc_processor;
    }

    public GroupChatProcessor getGc_processor() {
        return gc_processor;
    }

    public FriendListProcessor getFriend_processor() {
        return friend_processor;
    }

    public UserInfoGetProcessor getUser_processor() {
        return user_processor;
    }

    public UserListProcessor getUser_list_processor() {
        return user_list_processor;
    }

    public FriendAddProcessor getFriendAddProcessor() {
        return friendAddProcessor;
    }

    public FriendDelProcessor getFriendDelProcessor() {
        return friendDelProcessor;
    }

    public GroupOperateProcessor getGroupOperateProcessor() {
        return groupOperateProcessor;
    }

    public OfficialProcessor getOfficialProcessor() {
        return officialProcessor;
    }

    public static GlobalProcessor getInstance() {
        if (_instance == null) {
            _instance = new GlobalProcessor();
        }

        return _instance;
    }

    private GlobalProcessor() {
    }

    public void Init() {
        NetCenter.getInstance().Init();
        ErrorDetail.Init();
        sys_processor = new SysProcessor();
        sc_processor = new UserChatProcessor();
        friend_processor = new FriendListProcessor();
        user_processor = new UserInfoGetProcessor();
        gc_processor = new GroupChatProcessor();
        upload_user_processor = new UserUpdateProcessor();
        user_list_processor = new UserListProcessor();
        friendAddProcessor = new FriendAddProcessor();
        friendDelProcessor = new FriendDelProcessor();
        groupOperateProcessor = new GroupOperateProcessor();
        groupListProcessor = new GroupListProcessor();
        groupGetProcessor = new GroupGetProcessor();
        officialProcessor = new OfficialProcessor();
        StartGlobalTimer();
    }

    private void StartGlobalTimer() {
        if (global_timer != null || global_timer_task != null) {
            StopGlobalTimer();
        }

        global_timer = new Timer();
        global_timer_task = new TimerTask() {
            @Override
            public void run() {
                ProcessEvent();
            }
        };

        //每秒执行一次
        global_timer.schedule(global_timer_task, 1000, 1000);
    }

    private void StopGlobalTimer() {
        if (global_timer != null) {
            global_timer.cancel();
            global_timer = null;
        }

        if (global_timer_task != null) {
            global_timer_task.cancel();
            global_timer_task = null;
        }
    }

    private void ProcessEvent() {
        if (!CheckNetStatus()) {
            return;
        }

        PackSessionMgr.getInstance().CheckPackSession();
        NetCenter.getInstance().ProcessEvent();
        sys_processor.ProcessEvent();
    }

    private boolean CheckNetStatus() {
        boolean result = false;
        switch (NetCenter.getInstance().GetNetStatus()) {
            case CLOSED: {
                //这里需要主动去连接
                PackSessionMgr.getInstance().Reset();
                break;
            }
            case CONNECTING: {
                //连接中
                Logger.debug(TAG, "CONNECTING");
                break;
            }
            case CONNECTED: {
                //连接上了需要发登录包
                Logger.debug(TAG, "CONNECTED");
                break;
            }
            case LOGINING: {
                //登录中
                Logger.debug(TAG, "LOGINING");
                break;
            }
            case LOGINED: {
                //已登录之后可以做逻辑处理了
                result = true;
                break;
            }
            case DISCONNECT: {
                //开始重新连接
                //todo 检查网络，最大连接次数限制，弹框提示，CLOSED
                Logger.warning(TAG, "DISCONNECT");
                NetCenter.getInstance().CacheConnect();
                break;
            }
            case BEKICKED: {
                //被踢了
                Logger.warning(TAG, "BEKICKED");
                PackSessionMgr.getInstance().Reset();
                break;
            }
            default:
                break;

        }

        return result;
    }
}
