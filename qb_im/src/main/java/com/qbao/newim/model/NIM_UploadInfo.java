package com.qbao.newim.model;

/**
 * Created by chenjian on 2017/4/27.
 */

public class NIM_UploadInfo {
    String message_id;
    String url;
    String result;
    int file_type;
    long key2;
    String key1;
    boolean success;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getFile_type() {
        return file_type;
    }

    public void setFile_type(int file_type) {
        this.file_type = file_type;
    }

    public long getKey2() {
        return key2;
    }

    public void setKey2(long key2) {
        this.key2 = key2;
    }

    public String getKey1() {
        return key1;
    }

    public void setKey1(String key1) {
        this.key1 = key1;
    }
}
