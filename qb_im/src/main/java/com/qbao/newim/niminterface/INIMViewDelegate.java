package com.qbao.newim.niminterface;

import android.app.Activity;

/**
 * Created by shiyunjie on 2017/10/12.
 */

public interface INIMViewDelegate
{
    /**
     *进入任务助手通知
     *参数activity: 上下文
     */
    void OnEnterTask(Activity activity);
    /**
     *进入订阅助手通知
     *参数activity: 上下文
     */
    void OnEnterSubscribe(Activity activity);
    /**
     *进入公众号列表通知
     *参数activity: 上下文
     */
    void OnEnterOfficialContact(Activity activity);
    /**
     *进入公众号聊天界面
     *参数activity: 上下文
     *    official_id: 公众号ID
     */
    void OnEnterOffcialChat(Activity activity, long official_id);
}
