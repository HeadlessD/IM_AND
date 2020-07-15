package com.qbao.newim.helper;

/**
 * Created by chenjian on 2017/3/27.
 */

public class NetException extends Exception {
    private static final long serialVersionUID = 8718479860032388691L;
    protected String message;

    public NetException() {
        message = "网络异常，无法连接钱宝网";
    }

    public NetException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
