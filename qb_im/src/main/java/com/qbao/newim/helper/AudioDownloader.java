package com.qbao.newim.helper;

import com.qbao.newim.model.message.BaseMessageModel;

/**
 * Created by chenjian on 2017/3/27.
 */

public class AudioDownloader {
    private final DelayRemoveMap<String, BaseMessageModel> downloadObjects = new DelayRemoveMap<String, BaseMessageModel>();

    public AudioDownloader() {
    }

    public boolean addDownloadObject(BaseMessageModel messageItem)
    {
        if(messageItem == null)
        {
            return false;
        }

        String key = "-" + messageItem.message_id;
        if(downloadObjects.containsKey(key))
        {
            return false;
        }
        downloadObjects.put(key, messageItem);
        return true;
    }

    public BaseMessageModel getChatAudioMsg(String key)
    {
        return downloadObjects.get(key);
    }

    public void removeDownloadObject(BaseMessageModel messageItem)
    {
        if(messageItem != null)
        {
            String key = "-" + messageItem.message_id;
            downloadObjects.requestRemove(key);
        }

    }

    // TODO: 2017/9/28 史云杰，需要加上取消所有下载的功能
//    public void cancelAllDownload()
//    {
//        Map<String, BaseMessageModel> tChatAudioMsgs = downloadObjects.getDatas();
//
//        for(BaseMessageModel messageItem : tChatAudioMsgs.values())
//        {
//            ChatAudioDownloadJob job = messageItem.chatAudioDownloadJob;
//            if(job != null)
//            {
//                job.cancel();
//            }
//        }
//
//        downloadObjects.clear();
//    }
}
