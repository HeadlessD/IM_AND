package com.qbao.newim.business;

import java.util.List;

/**
 * Created by chenjian on 2017/4/18.
 */

public class HttpMessage {
    public int code;
    public String message;
    public String data;
    public Object obj;			//对象
    public List<Object> list;	//数组

    public static HttpMessage getDefault(){
        HttpMessage msg= new HttpMessage();
        msg.code = -1;
        msg.message = "联网失败";
        return msg;
    }

    public boolean isSuccess(){
        if(code == 1000){
            return true;
        }

        return false;
    }

    public boolean isNull() {
        if (data == null) {
            return true;
        }
        return false;
    }
}
