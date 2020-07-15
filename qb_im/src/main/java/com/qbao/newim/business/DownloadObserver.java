package com.qbao.newim.business;

import com.qbao.newim.helper.IDownloadManager;

/**
 * Created by chenjian on 2017/3/27.
 */

public interface DownloadObserver {
    void onDownloadChanged(IDownloadManager manager, DownloadObject downloadObject);
}
