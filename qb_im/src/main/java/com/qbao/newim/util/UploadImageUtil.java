package com.qbao.newim.util;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.qbao.newim.business.ApiRequest;
import com.qbao.newim.business.FileRequestBody;
import com.qbao.newim.business.RetrofitCallback;
import com.qbao.newim.manager.NIMGroupInfoManager;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.model.IMUserInfo;
import com.qbao.newim.model.NIM_UploadInfo;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

import static com.qbao.newim.configure.Constants.IM_SERVICE;

/**
 * Created by chenjian on 2017/6/22.
 */

public class UploadImageUtil {
    public static void upLoadFile(Bitmap bitmap, final long group_id) {
        if (bitmap == null) {
            return;
        }
        String file_path = BitmapUtil.saveBitmap(AppUtil.GetContext(), bitmap);
        if (TextUtils.isEmpty(file_path)) {
            return;
        }
        final File file = new File(file_path);
        RetrofitCallback<NIM_UploadInfo> callback = new RetrofitCallback<NIM_UploadInfo>() {

            @Override
            public void onSuccess(Call<NIM_UploadInfo> call, Response<NIM_UploadInfo> response) {
                NIM_UploadInfo info = response.body();
                if (info.getKey1().equals(String.valueOf(group_id))) {
                    IMGroupInfo groupInfo = NIMGroupInfoManager.getInstance().getGroupInfo(group_id);
                    groupInfo.group_img_url = AppUtil.getGroupUrl(group_id);
                    NIMGroupInfoManager.getInstance().AddGroup(groupInfo);
                    file.delete();
                }
            }

            @Override
            public void onLoading(long total, long progress) {
            }

            @Override
            public void onFailure(Call<NIM_UploadInfo> call, Throwable t) {
                file.delete();
                Logger.e("");
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
                String url = IM_SERVICE + "uploadicon?key1=" + group_id + "&verification=" + verification;
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                FileRequestBody file_body = new FileRequestBody(requestFile, callback);
                MultipartBody.Part body = MultipartBody.Part.createFormData("", file.getName(), file_body);
                Call<NIM_UploadInfo> call = apiRequest.uploadFile(url, body);
                call.enqueue(callback);
            } else {
                Logger.error("NIMChatSendManager", "user_info verification null");
            }
        } else {
            Logger.error("NIMChatSendManager", "user_info is null");
        }
    }
}
