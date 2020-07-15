package com.qbao.newim.business;

import android.content.Context;

import com.qbao.newim.model.TotalInfo;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.Logger;
import com.qbao.newim.util.NetUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by chenjian on 2017/4/18.
 */

public class RetrofitRequest<T> {

    private Call<TotalInfo<T>> mCall;

    public RetrofitRequest(Call call) {
        mCall = call;
    }

    // 将返回的结构体再次进行解析封装
    private void parseCallBack(TotalInfo result, MsgCallBack callBack) {
        if (result == null) {
            Context mContext = AppUtil.GetContext();
            HttpMessage msg = new HttpMessage();

            if (!NetUtils.NetAvailable(mContext)) {
                msg.code = 1001;
                msg.obj = "当前没有连接网络,请检查网络设置" + "(" + msg.code + ")";
            } else {
                msg.code = 4001;
                msg.obj = "网络未知错误连接" + "(" + msg.code + ")";
            }

            if (callBack != null) {
                callBack.onResult(msg);
                return;
            }
            // 有数据显示判断
        } else {
            HttpMessage msg = new HttpMessage();
            if (result.getResponseCode() == 1005) {
                msg.code = 1005;
                msg.message = "账号信息已过期，请重新登陆！" + "(" + msg.code + ")";
            } else {
                msg.code = result.getResponseCode();
                msg.message = result.getErrorMsg();
                msg.obj = result.getData();
            }
            if (callBack != null) {
                callBack.onResult(msg);
            }
        }
    }

    // 核心代码，请求服务器，并返回数据
    public void handRequest(final MsgCallBack msgCallBack) {
        mCall.enqueue(new Callback<TotalInfo<T>>() {
            @Override
            public void onResponse(Call<TotalInfo<T>> call, Response<TotalInfo<T>> response) {
                parseCallBack(response.body(), msgCallBack);
            }

            @Override
            public void onFailure(Call<TotalInfo<T>> call, Throwable t) {
                Logger.error("sss", t.toString());
            }
        });
    }
}
