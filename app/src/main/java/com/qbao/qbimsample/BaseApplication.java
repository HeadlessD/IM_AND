package com.qbao.qbimsample;

import android.app.Application;

import com.qbao.newim.niminterface.NIMCoreSDK;
import com.qbao.newim.util.NIMExceptionHandler;

/**
 * Created by chenjian on 2017/5/9.
 */

public class BaseApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        //也可以一次性初始化设置回调
        NIMCoreSDK.getInstance().Init(this, null, null);
        //出包的时候打开
        NIMExceptionHandler.getInstance().init(this);
    }
}
