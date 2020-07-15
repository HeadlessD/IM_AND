package com.qbao.newim.helper;

import com.qbao.newim.business.DownloadObject;
import com.qbao.newim.business.DownloadObserver;

/**
 * Created by chenjian on 2017/3/27.
 */

public interface IDownloadManager {
    public void deregisterDownloadObserver(DownloadObserver observer);

    public void registerDownloadObserver(DownloadObserver observer);

    public void notifyObservers(DownloadObject object);
}
