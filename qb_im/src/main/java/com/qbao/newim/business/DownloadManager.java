package com.qbao.newim.business;

import com.qbao.newim.helper.AudioDownloader;
import com.qbao.newim.helper.ChatAudioDownloadJob;
import com.qbao.newim.helper.IDownloadManager;
import com.qbao.newim.model.message.BaseMessageModel;

import java.util.ArrayList;

/**
 * Created by chenjian on 2017/3/27.
 */

public class DownloadManager implements IDownloadManager {
    private final ArrayList<DownloadObserver> mObservers = new ArrayList<DownloadObserver>();
    private AudioDownloader mAudioDownloader = null;
    private static DownloadManager mDownloadManager = new DownloadManager();

    public DownloadManager() {
        mAudioDownloader = new AudioDownloader();
    }

    public static DownloadManager getInstance()
    {
        return mDownloadManager;
    }

    @Override
    public synchronized void deregisterDownloadObserver(
            DownloadObserver observer) {
        mObservers.remove(observer);
    }

    @Override
    public synchronized void registerDownloadObserver(DownloadObserver observer) {
        mObservers.add(observer);
    }

    @Override
    public synchronized void notifyObservers(DownloadObject object) {
        for (DownloadObserver observer : mObservers) {
            observer.onDownloadChanged(this, object);
        }
    }

    public void downloadAudio(BaseMessageModel item)
    {
        if(item != null)
        {
            boolean success = mAudioDownloader.addDownloadObject(item);
            if(success)
            {
                ChatAudioDownloadJob chatAudioDownloadJob = new ChatAudioDownloadJob(item, this);
                chatAudioDownloadJob.start();
            }
        }
    }
    public AudioDownloader getAudioDownloader()
    {
        return mAudioDownloader;
    }
}
