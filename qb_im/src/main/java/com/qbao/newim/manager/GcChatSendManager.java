package com.qbao.newim.manager;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;

import com.qbao.newim.business.ApiRequest;
import com.qbao.newim.business.FileRequestBody;
import com.qbao.newim.business.RetrofitCallback;
import com.qbao.newim.configure.Constants;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.model.IMUserInfo;
import com.qbao.newim.model.NIM_Chat_ID;
import com.qbao.newim.model.NIM_UploadInfo;
import com.qbao.newim.model.message.GcMessageModel;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.processor.GlobalProcessor;
import com.qbao.newim.processor.GroupChatProcessor;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.BitmapUtil;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.FileUtil;
import com.qbao.newim.util.Logger;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import id.zelory.compressor.Compressor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

import static com.qbao.newim.configure.Constants.IM_SERVICE;

/**
 * Created by chenjian on 2017/9/26.
 */

public class GcChatSendManager {
    private static final int SEND_FILE_SUCCESS = 0x1234;
    private static final int SEND_FILE_FAILURE = 0x2345;
    private static final int SEND_TEXT = 0x1236;

    private static final int AUDIO_TYPE = 1;
    private static final int IMAGE_TYPE = 2;

    private static GcChatSendManager instance;
    private ChatThread mChatThread = null;
    private boolean bUploading;
    private ArrayList<GcMessageModel> queueMsg = new ArrayList<>();

    private GcChatSendManager() {
    }

    public static synchronized GcChatSendManager getInstance() {
        if (instance == null) {
            instance = new GcChatSendManager();
        }

        return instance;
    }

    public void send(GcMessageModel msg) {
        ensureChatThread();

        NIM_Chat_ID chat_id = new NIM_Chat_ID();
        chat_id.message_id = msg.message_id;
        chat_id.session_id = msg.group_id;

        NIMGroupMsgManager.getInstance().addGroupOneMsgInfo(msg.group_id, msg, true);

        if (!isGroup(msg)) {
            msg.msg_status = MsgConstDef.MSG_STATUS.SEND_FAILED;
            sendHandler(msg, SEND_FILE_FAILURE);
            return;
        }

        if (!NetCenter.getInstance().IsLogined()) {
            msg.msg_status = MsgConstDef.MSG_STATUS.SEND_FAILED;
            sendHandler(msg, SEND_FILE_FAILURE);
            return;
        }

        if (msg.m_type == MsgConstDef.MSG_M_TYPE.IMAGE) {
            // 发送图片之前压缩一下
            queueMsg.add(msg);
            msg.compress_path = compressImg(msg.pic_path);
            upLoadFile(msg, new File(msg.compress_path), IMAGE_TYPE);
        } else if (msg.m_type == MsgConstDef.MSG_M_TYPE.VOICE) {
            // 发送语音
            upLoadFile(msg, new File(msg.audio_path), AUDIO_TYPE);
        } else {
            sendHandler(msg, SEND_TEXT);
        }
    }

    private boolean isGroup(GcMessageModel msg) {
        IMGroupInfo groupInfo = NIMGroupInfoManager.getInstance().getGroupInfo(msg.group_id);
        if (groupInfo == null || groupInfo.isMember == 0) {
            return false;
        }
        return true;
    }

    private String compressImg(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }

        if (path.contains(Constants.ICON_PIC_DIR) && path.contains("_upload")) {
            return path;
        }
        int degree = BitmapUtil.readPictureDegree(path);

        File bitmapFile = new File(path);
        String fileName = bitmapFile.getName();
        String fileExtension = ".jpg";
        String uploadFilePath;
        int lastDotIndex = fileName.lastIndexOf(".");
        if(lastDotIndex > 0){
            uploadFilePath = Constants.ICON_PIC_DIR + "/" + fileName.substring(0, lastDotIndex)
                    + "_upload" + fileExtension;
        }
        else{
            uploadFilePath = Constants.ICON_PIC_DIR + "/" + fileName + "_upload";
        }

        File uploadFile = new File(uploadFilePath);
        if(uploadFile.exists()) {
            return uploadFilePath;
        }

        Logger.i(getReadableFileSize(bitmapFile.length()));
        Bitmap compress = Compressor.getDefault(AppUtil.GetContext()).compressToBitmap(bitmapFile);

        if (degree != 0){
            compress = BitmapUtil.rotaingImageView(degree, compress);
        }

        FileUtil.ensureAppPath(Constants.ICON_PIC_DIR);
        BitmapUtil.saveBitmapToSD(uploadFilePath, compress);
        if (compress != null && !compress.isRecycled()) {
            compress.recycle();
        }
        return uploadFilePath;
    }

    private void upLoadFile(final GcMessageModel msg, File file, int type) {
        // 如果当前有文件正在上传，则等待
        if (bUploading) return;
        RetrofitCallback<NIM_UploadInfo> callback = new RetrofitCallback<NIM_UploadInfo>() {

            @Override
            public void onSuccess(Call<NIM_UploadInfo> call, Response<NIM_UploadInfo> response) {
                NIM_UploadInfo info = response.body();
                msg.msg_status = MsgConstDef.MSG_STATUS.UPLOAD_SUCCESS;
                msg.msg_content = info.getUrl();
                sendHandler(msg, SEND_FILE_SUCCESS);
                bUploading = false;
                checkMsg(msg);
                msg.progress = 100;
                DataObserver.Notify(DataConstDef.EVENT_FiLE_PROGRESS, msg, null);
            }

            @Override
            public void onLoading(long total, long progress) {
                bUploading = true;
                if (total == 0) {
                    return;
                }

                msg.progress = (int)(100 * progress / total);
                msg.msg_status = MsgConstDef.MSG_STATUS.UPLOADING;
                DataObserver.Notify(DataConstDef.EVENT_FiLE_PROGRESS, msg, null);
            }

            @Override
            public void onFailure(Call<NIM_UploadInfo> call, Throwable t) {
                bUploading = false;
                msg.msg_status = MsgConstDef.MSG_STATUS.UPLOAD_FAILED;
                Logger.error("file fail", t.getMessage().toString());
                sendHandler(msg, SEND_FILE_FAILURE);
                checkMsg(msg);
            }
        };
        ApiRequest.ApiQbao apiRequest = ApiRequest.getApiQbao();
        IMUserInfo userInfo = NIMUserInfoManager.getInstance().getSelfUser();
        if (userInfo != null) {
            String verification = userInfo.verification;
            if (verification == null) {
                verification = "test";
            }
            if (!TextUtils.isEmpty(verification)) {
                String url = IM_SERVICE + "uploadfile?key1=" + NIMUserInfoManager.getInstance().GetSelfUserId() +
                        "&key2=" + msg.group_id + "&message_id=" + msg.message_id + "&file_type=" + type + "&verification=" + verification;
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                FileRequestBody file_body = new FileRequestBody(requestFile, callback);
                MultipartBody.Part body = MultipartBody.Part.createFormData("", file.getName(), file_body);
                Call<NIM_UploadInfo> call = apiRequest.uploadFile(url, body);
                msg.msg_status = MsgConstDef.MSG_STATUS.UPLOADING;
                call.enqueue(callback);
            } else {
                Logger.error("NIMChatSendManager", "user_info verification null");
            }
        } else {
            Logger.error("NIMChatSendManager", "user_info is null");
        }
    }

    private void checkMsg(GcMessageModel msg) {
        int size = queueMsg.size();
        if (size == 0 || bUploading) {
            return;
        }

        int pos = queueMsg.indexOf(msg);
        // 如果当前上传的图片消息所在队列位置小于待发送队列的size
        if (pos < size - 1) {
            // 当前图片消息已经上传成功了，继续发送队列中下一条消息
            if (msg.msg_status == MsgConstDef.MSG_STATUS.UPLOAD_SUCCESS) {
                GcMessageModel nextMsg = queueMsg.get(pos + 1);
                upLoadFile(nextMsg, new File(nextMsg.compress_path), IMAGE_TYPE);
            } else { // 如果该条图片消息上传失败了，剩下全部通知失败，并删除待请求队列中
                for (int i = pos + 1; i < size - 1; i++) {
                    GcMessageModel failMsg = queueMsg.get(i);
                    failMsg.msg_status = MsgConstDef.MSG_STATUS.UPLOAD_FAILED;
                    sendHandler(msg, SEND_FILE_FAILURE);
                }
                queueMsg.clear();
            }
        } else { // 如果当前上传的图片消息所在队列已经是最后一条了，则清除消息队列
            queueMsg.clear();
        }
    }

    private void sendHandler(GcMessageModel msg, int status) {
        Handler handler = mChatThread.getChatHandler();
        Message msgMessage = handler.obtainMessage();
        msgMessage.obj = msg;
        msgMessage.what = status;
        handler.sendMessage(msgMessage);
    }

    private void ensureChatThread() {
        if (mChatThread == null || mChatThread.getState() == Thread.State.TERMINATED) {
            mChatThread = new ChatThread();
            mChatThread.start();
        }
    }

    public void close() {
        destroyHandlerThread();
        instance = null;
    }

    public void destroyHandlerThread() {
        if (null != mChatThread) {
            Looper looper = mChatThread.getLooper();
            if (null != looper) {
                looper.quit();
                looper = null;
            }
            mChatThread = null;
        }
    }

    private class ChatThread extends HandlerThread implements Handler.Callback {
        private Handler mChatHandler;

        public ChatThread() {
            super("send_chat_thread");
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        }

        public Handler getChatHandler() {
            ensureHandler();
            return mChatHandler;
        }

        private void ensureHandler() {
            if (mChatHandler == null) {
                mChatHandler = new Handler(getLooper(), this);
            }
        }

        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case SEND_TEXT:
                    GcMessageModel msg_success = (GcMessageModel) msg.obj;
                    sendMsgProcessor(msg_success);
                    break;
                case SEND_FILE_SUCCESS:
                    GcMessageModel file_success = (GcMessageModel) msg.obj;
                    sendMsgProcessor(file_success);
                    DataObserver.Notify(DataConstDef.EVENT_UPLOAD_STATUS, file_success, true);
                    break;
                case SEND_FILE_FAILURE:
                    GcMessageModel msg_failed = (GcMessageModel) msg.obj;
                    DataObserver.Notify(DataConstDef.EVENT_UPLOAD_STATUS, msg_failed, false);

                    Logger.e("消息发送失败");
                    break;
            }
            return true;
        }
    }

    private void sendMsgProcessor(GcMessageModel msg) {
        GroupChatProcessor gc_processor = GlobalProcessor.getInstance().getGc_processor();
        gc_processor.GroupChatSendMsgRQ(msg);
    }

    public String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
