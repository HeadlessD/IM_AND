package com.qbao.qbimsample.sdktemplate;

import android.app.Activity;
import android.widget.Toast;

import com.qbao.newim.model.message.GcMessageModel;
import com.qbao.newim.model.message.OcMessageModel;
import com.qbao.newim.model.message.ScMessageModel;
import com.qbao.newim.niminterface.INIMNetDelegate;
import com.qbao.newim.niminterface.NIMCoreSDK;

/**
 * Created by shiyunjie on 2017/10/16.
 */

public class NIMNetTemplate implements INIMNetDelegate
{
    private Activity m_activity = null;

    public NIMNetTemplate()
    {
        NIMCoreSDK.getInstance().SetNetDelegate(this);
    }

    public void SetActivity(Activity activity)
    {
        m_activity = activity;
    }

    @Override
    public int OnConnected()
    {
        if(m_activity != null)
        {
            Toast.makeText(m_activity, "连接成功", Toast.LENGTH_SHORT).show();
        }
        return 0;
    }

    @Override
    public int OnLogined()
    {
        if(m_activity != null)
        {
            Toast.makeText(m_activity, "登录成功", Toast.LENGTH_SHORT).show();
            m_activity.finish();
        }
        return 0;
    }

    @Override
    public int OnUpdateFinished()
    {
        if(m_activity != null)
        {
            Toast.makeText(m_activity, "数据加载完成", Toast.LENGTH_SHORT).show();
        }
        return 0;
    }

    @Override
    public void OnError()
    {
        if(m_activity != null)
        {
            Toast.makeText(m_activity, "网络异常", Toast.LENGTH_SHORT).show();
            m_activity.finish();
        }
    }

    @Override
    public void OnClose()
    {
        if(m_activity != null)
        {
            Toast.makeText(m_activity, "连接关闭", Toast.LENGTH_SHORT).show();
            m_activity.finish();
        }
    }

    @Override
    public void OnConnectFailure()
    {
        if(m_activity != null)
        {
            Toast.makeText(m_activity, "连接失败", Toast.LENGTH_SHORT).show();
            m_activity.finish();
        }
    }

    @Override
    public void OnKicked(String reason)
    {
        if(m_activity != null)
        {
            Toast.makeText(m_activity, reason, Toast.LENGTH_SHORT).show();
            m_activity.finish();
        }
    }

    @Override
    public void ProcessScMessage(ScMessageModel model)
    {
    }

    @Override
    public void ProcessGcMessage(GcMessageModel model)
    {
    }

    @Override
    public void ProcesscOcMessage(OcMessageModel model)
    {
    }

    @Override
    public void ProcesscSysMessage(OcMessageModel model)
    {
    }
}
