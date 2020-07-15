package com.qbao.newim.niminterface;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.baidu.mapapi.SDKInitializer;
import com.qbao.newim.configure.Constants;
import com.qbao.newim.configure.GlobalVariable;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.helper.AudioPlayManager;
import com.qbao.newim.helper.HttpClientFactory;
import com.qbao.newim.manager.GcChatSendManager;
import com.qbao.newim.manager.NIMChatNotifyManager;
import com.qbao.newim.manager.NIMObserverManager;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.manager.ScChatSendManager;
import com.qbao.newim.manager.state.NIMStateMachine;
import com.qbao.newim.model.message.BaseMessageModel;
import com.qbao.newim.model.message.GcMessageModel;
import com.qbao.newim.model.message.OcMessageModel;
import com.qbao.newim.model.message.ScMessageModel;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.permission.AndPermission;
import com.qbao.newim.permission.PermissionListener;
import com.qbao.newim.permission.Rationale;
import com.qbao.newim.permission.RationaleListener;
import com.qbao.newim.processor.GlobalProcessor;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.BaseUtil;
import com.qbao.newim.util.Logger;
import com.qbao.newim.util.NIMStartActivityUtil;
import com.qbao.newim.util.Utils;

import java.io.File;
import java.util.List;

import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by chenjian on 2017/4/24.
 */

public class NIMCoreSDK
{

    public abstract static class NIMPermissionDelegate
    {
        public abstract void OnSuccess();
        public abstract void OnCancel();
    }

    protected static String TAG = NIMCoreSDK.class.getSimpleName();
    protected static NIMCoreSDK instance = new NIMCoreSDK();

    public static NIMCoreSDK getInstance() {
        return instance;
    }
    private NIMObserverManager mObserverManager = null;
    private boolean m_start = false;
    private static final int REQUEST_CODE_PERMISSION_OTHER = 90;

    /**
    *初始化函数,只需要调用一次
     */
    public boolean Init(Application context, INIMNetDelegate net_delegate, INIMViewDelegate view_delegate)
    {
        AppUtil.SetContext(context);

        if(null == net_delegate || null == view_delegate)
        {
            Logger.warning(TAG, "call_back is null, are you sure you really don't need it?");
        }

        NIMStartActivityUtil.setViewDelegate(view_delegate);
        NetCenter.getInstance().SetNetDelegate(net_delegate);

        //初始化百度SDK
        SDKInitializer.initialize(context);
        //初始化文件路径
        InitDir();
        //初始化消息通知
        NIMChatNotifyManager.getInstance().init(context);
        //初始化声音
        AudioPlayManager.getManager();
        GlobalVariable.USER_AGENT += Utils.getVersion(context);
        //初始化http
        HttpClientFactory.init();
        //初始化tcp
        GlobalProcessor.getInstance().Init();
        //初始化管理器观察者
        mObserverManager = new NIMObserverManager();

        //预加载数据
        NIMStateMachine.getInstance().PreLoadState();

        return true;
    }

    /**
     * 启动函数每次设置用于信息时使用
     * user_id用户登录用户id
     * valid_key有效验证key，由用户自己定义[eg:tgt]
     */
    public boolean Start(long user_id, String valid_key)
    {
        if(NetCenter.getInstance().IsConnected() || NetCenter.getInstance().IsReConnecting())
        {
            DisConnect();
        }

        long cur_user_id = NIMUserInfoManager.getInstance().GetSelfUserId();
        if(user_id <= 0 || user_id == cur_user_id)
        {
            //用户和本地的一样直接进入连接状态
            NIMStateMachine.getInstance().StartConnect();
        }
        else
        {
            //用户和本地的不一样重新加载状态
            NIMUserInfoManager.getInstance().SetLoginModel(user_id, valid_key);
            NIMStateMachine.getInstance().ReLoadState();
        }

        m_start = true;
        return true;
    }


    /**
     *恢复app的时候用来检查登录状态
     */
    public void Resume()
    {
        //没有初始化
        if(!m_start || NIMUserInfoManager.getInstance().GetSelfUserId() <= 0)
            return;

        NIMStateMachine.getInstance().StartConnect();
    }

    /**
     *断开连接
     */
    public void DisConnect()
    {
        NetCenter.getInstance().DisConnect();
    }

    /**
     *设置网络代理
     */
    public void SetNetDelegate(INIMNetDelegate net_delegate)
    {
        NetCenter.getInstance().SetNetDelegate(net_delegate);
    }

    /**
     *设置界面代理
     */
    public void SetViewDelegate(INIMViewDelegate view_delegate)
    {
        NIMStartActivityUtil.setViewDelegate(view_delegate);
    }


    /**
     * 发送私聊消息
     * return 唯一消息ID
     */
    public long SendScMessage(int opt_user_id, BaseMessageModel model)
    {
        if(opt_user_id <= 0)
        {
            return -1;
        }
        ScMessageModel sc_model = new ScMessageModel();
        sc_model.CopyFrom(model);
        sc_model.opt_user_id = opt_user_id;
        sc_model.message_id = NetCenter.getInstance().CreateMsgID();
        sc_model.chat_type = MsgConstDef.MSG_CHAT_TYPE.PRIVATE;
        sc_model.send_user_name = NIMUserInfoManager.getInstance().GetSelfUserName();
        sc_model.msg_time = BaseUtil.GetServerTime();
        sc_model.msg_status = MsgConstDef.MSG_STATUS.SENDING;
        sc_model.is_self = true;
        ScChatSendManager.getInstance().send(sc_model);
        return sc_model.message_id;
    }

    /**
     * 发送群聊消息
     * return 唯一消息ID
     */
    public long SendGcMessage(int group_id, BaseMessageModel model)
    {
        if(group_id <= 0)
        {
            return -1;
        }
        GcMessageModel gc_model = new GcMessageModel();
        gc_model.CopyFrom(model);
        gc_model.group_id = group_id;
        gc_model.message_id = NetCenter.getInstance().CreateGroupMsgId();
        gc_model.chat_type = MsgConstDef.MSG_CHAT_TYPE.GROUP;
        gc_model.user_id = NIMUserInfoManager.getInstance().GetSelfUserId();
        gc_model.send_user_name = NIMUserInfoManager.getInstance().getSelfName();
        GcChatSendManager.getInstance().send(gc_model);
        return gc_model.message_id;
    }

    /**
     * 发送公众号消息
     * return 唯一消息ID
     */
    public long SendOcMessage(int official_id, BaseMessageModel model)
    {
        if(official_id <= 0)
        {
            return -1;
        }
        OcMessageModel oc_model = new OcMessageModel();
        oc_model.CopyFrom(model);
        oc_model.official_id = official_id;
        //// TODO: 2017/10/11 史云杰 从公众号管理器里面获取,message_id生成方式是否和群一样？
        oc_model.official_name = "";
        oc_model.message_id = NetCenter.getInstance().CreateGroupMsgId();
        oc_model.chat_type = MsgConstDef.MSG_CHAT_TYPE.PUBLIC;
        return oc_model.message_id;
    }

    /**
     * 发送系统消息
     * param: is_local表示需要发送到服务器[true:是, false:否]
     * return 唯一消息ID
     */
    public long SendSysMessae(boolean is_local, BaseMessageModel model)
    {
        // TODO: 2017/10/12 完善接口
        long message_id = NetCenter.getInstance().CreateGroupMsgId();
        //保存本地
        if(is_local)
        {
            return message_id;
        }

        return message_id;
    }

    /**
     * 自定义连接信息
     * just for test
     */
    public void SetConnectInfo(String host, int port, boolean domain)
    {
        NIMUserInfoManager.getInstance().SetConnectInfo(host, port, domain);
    }


    public void CheckPermission(final Activity context, final NIMPermissionDelegate l_delegate)
    {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                switch (requestCode) {
                    case REQUEST_CODE_PERMISSION_OTHER:
                        if(l_delegate != null)
                        {
                            l_delegate.OnSuccess();
                        }
                        break;
                }
            }

            @Override
            public void onCancel(int requestCode, Context context) {
                if(l_delegate != null)
                {
                    l_delegate.OnCancel();
                }
            }

        };
        final String tip = context.getString(R.string.nim_permission_network_fail);
        AndPermission.with(context)
                .requestCode(REQUEST_CODE_PERMISSION_OTHER)
                .callback(permissionListener)
                .failTips(tip)
                .permission(WRITE_EXTERNAL_STORAGE, READ_PHONE_STATE)
                .rationale(new RationaleListener() {
                    @Override
                    public void showRequestPermissionRationale(int requestCode, Rationale rationale)
                    {
                        AndPermission.rationaleDialog(context, tip, rationale).show();
                    }
                }).start();
    }

    private void InitDir()
    {
        try
        {
            File file = new File(Constants.ICON_CACHE_DIR);
            if (!file.exists())
            {
                file.mkdirs();
            }

            file = new File(Constants.ICON_CACHE_DIR + "/.nomedia");
            if (!file.exists())
            {
                file.createNewFile();
            }

            file = new File(Constants.BASE_PATH + "/photo");
            if (!file.exists())
            {
                file.createNewFile();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
