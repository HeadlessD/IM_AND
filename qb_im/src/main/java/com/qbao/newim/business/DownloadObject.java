package com.qbao.newim.business;


/**
 * Created by chenjian on 2017/3/25.
 */

public interface DownloadObject
{
    public abstract String getUrl();

    public abstract String getFilePath();

    public abstract int getProgress();

    public abstract int getDownloadStatus();
}
