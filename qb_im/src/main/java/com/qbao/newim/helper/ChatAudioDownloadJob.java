package com.qbao.newim.helper;

import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Log;

import com.qbao.newim.business.ApiRequest;
import com.qbao.newim.business.DownloadManager;
import com.qbao.newim.business.DownloadObject;
import com.qbao.newim.configure.Constants;

import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.model.message.BaseMessageModel;
import com.qbao.newim.util.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

import static com.qbao.newim.util.Utils.bytesToHexString;


/**
 * Created by chenjian on 2017/3/25.
 */

public class ChatAudioDownloadJob implements DownloadObject {

    private static final String TAG = ChatAudioDownloadJob.class.getSimpleName();

    private BaseMessageModel mChatAudioMsg;
    private DownloadManager mDownloadManager;

    Call<ResponseBody> call;

    public ChatAudioDownloadJob(BaseMessageModel messageItem, DownloadManager downloadManager) {
        mDownloadManager = downloadManager;
        mChatAudioMsg = messageItem;
        mChatAudioMsg.msg_status = MsgConstDef.MSG_STATUS.INVALID;
    }

    public BaseMessageModel getChatAudioMsg() {
        return mChatAudioMsg;
    }


    public void cancel() {
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
    }

    public void start() {

        mDownloadManager.notifyObservers(this);
//        final String url = "98194876_1/5a5ef2a7ad3fe8a48543f02dc062a3ff.au";
        final String url = mChatAudioMsg.msg_content;
        ApiRequest.ApiQbao apiQbao = ApiRequest.getApiQbao();

        if ((call == null || call.isCanceled()) && mChatAudioMsg.msg_status != MsgConstDef.MSG_STATUS.DOWNLOADING) {
            call = apiQbao.downloadFile(Constants.IM_SERVICE + url);
            mChatAudioMsg.msg_status = MsgConstDef.MSG_STATUS.DOWNLOADING;
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> request, retrofit2.Response<ResponseBody> response) {
                    Log.d(TAG, "server contacted and has file");

                    String path = writeResponseBodyToDisk(response.body(), url);

                    if (!TextUtils.isEmpty(path)) {
                        downloadSuccess(path);
                    }

                    Log.d(TAG, "file download was a success? " + path);
                }

                @Override
                public void onFailure(Call<ResponseBody> request, Throwable t) {
                    downloadFail();
                }

            });
        }

    }

    private void downloadFail() {
        if (call != null && !call.isCanceled()) {
            mChatAudioMsg.msg_status = MsgConstDef.MSG_STATUS.DOWNLOAD_FAILED;
            mDownloadManager.notifyObservers(this);
            call = null;

        }
        mDownloadManager.getAudioDownloader().removeDownloadObject(mChatAudioMsg);
    }

    private void downloadSuccess(String path) {
        if (call != null && !call.isCanceled()) {
            mChatAudioMsg.msg_status = MsgConstDef.MSG_STATUS.DOWNLOAD_SUCCESS;
            mChatAudioMsg.audio_path = path;

            if (mChatAudioMsg.ext_type == 0) {
                if (!TextUtils.isEmpty(mChatAudioMsg.audio_path)) {
                    // 获取时长
                    MediaPlayer mp = new MediaPlayer();
                    try {
                        mp.setDataSource(mChatAudioMsg.audio_path);
                        mp.prepare();
                        int duration = mp.getDuration() / 1000;
                        if (duration < 0) {
                            duration = 0;
                        } else if (duration > 60) {
                            duration = 60;
                        }
                        mChatAudioMsg.ext_type = duration;
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        mp.release();
                        mp = null;
                    }
                }
            }

            mDownloadManager.notifyObservers(this);
            call = null;
        }
    }

    private String writeResponseBodyToDisk(ResponseBody body, String url) {
        String path = "";
        try {

            InputStream inputStream = null;
            OutputStream outputStream = null;
            File tempFile = null;
            String tempSavePath = url;
            String filePath;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                String temp = tempSavePath.substring(tempSavePath.lastIndexOf("/") + 1);
                temp = hashKeyForDisk(temp);

                filePath = IMFileHelper.generateFilePath(tempSavePath, temp);
                path = filePath;


                if (FileUtil.hasFreeSpace(filePath, fileSize)) {
                    tempFile = creatLocalFile(filePath);
                }

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(tempFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();

                return path;
            } catch (IOException e) {

                return path;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }

                if (tempFile == null) {
                    throw new FileNotFoundException("sdcard has no enough space");
                }
            }
        } catch (IOException e) {
            return path;
        }
    }

    private File creatLocalFile(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            } else if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    @Override
    public String getUrl() {
        return mChatAudioMsg.msg_content;
    }

    @Override
    public String getFilePath() {
        return mChatAudioMsg.audio_path;
    }

    @Override
    public int getProgress() {
        return mChatAudioMsg.progress;
    }

    @Override
    public int getDownloadStatus() {
        return mChatAudioMsg.msg_status;
    }
}
