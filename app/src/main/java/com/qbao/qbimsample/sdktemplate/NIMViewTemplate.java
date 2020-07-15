package com.qbao.qbimsample.sdktemplate;

import android.app.Activity;

import com.qbao.newim.niminterface.INIMViewDelegate;
import com.qbao.newim.niminterface.NIMCoreSDK;
import com.qbao.newim.util.ShowUtils;

/**
 * Created by shiyunjie on 2017/10/16.
 */

public class NIMViewTemplate implements INIMViewDelegate
{
    public NIMViewTemplate()
    {
        NIMCoreSDK.getInstance().SetViewDelegate(this);
    }
    @Override
    public void OnEnterTask(Activity activity)
    {
        ShowUtils.showToast("进入任务助手界面");
    }

    @Override
    public void OnEnterSubscribe(Activity activity)
    {
        ShowUtils.showToast("进入订阅助手界面");
    }

    @Override
    public void OnEnterOfficialContact(Activity activity)
    {
        ShowUtils.showToast("进入所有公众号界面");
    }

    @Override
    public void OnEnterOffcialChat(Activity activity, long official_id)
    {
        ShowUtils.showToast("进入所有公众号聊天界面");
    }
}
