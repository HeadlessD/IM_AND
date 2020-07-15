package com.qbao.newim.views;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qbao.newim.adapter.ChatAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.configure.Constants;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.helper.GlideImageLoader;
import com.qbao.newim.helper.NumberIndexIndicator;
import com.qbao.newim.helper.ProgressPieIndicator;
import com.qbao.newim.helper.TransferConfig;
import com.qbao.newim.helper.Transferee;
import com.qbao.newim.manager.ChatMsgBuildManager;
import com.qbao.newim.manager.NIMGroupMsgManager;
import com.qbao.newim.manager.NIMMsgManager;
import com.qbao.newim.model.message.BaseMessageModel;
import com.qbao.newim.model.message.GcMessageModel;
import com.qbao.newim.model.message.ScMessageModel;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.FileUtil;
import com.qbao.newim.util.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenjian on 2017/5/2.
 */

public abstract class ChatPictureView extends ChatView {
    protected BubbleImageView imgPicture;
    protected TextView txtProgress;
    protected BaseMessageModel chatPicMsg;

    public ChatPictureView(Context context) {
        super(context);
    }

    public ChatPictureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatPictureView(Context context, ChatAdapter adapter) {
        super(context, adapter);
    }

    @Override
    public void initViews(Context context, View root) {
        super.initViews(context, root);
        imgPicture = (BubbleImageView) findViewById(R.id.chat_img);
        txtProgress = (TextView) findViewById(R.id.chat_tv_progress);
    }

    @Override
    public void initData() {
        super.initData();
    }

    @Override
    public void onClick(View v) {

        List<String> url_list = new ArrayList<>();
        int pic_pos = -1;
        if (chatPicMsg.chat_type == MsgConstDef.MSG_CHAT_TYPE.GROUP) {
            GcMessageModel gcMessageModel = (GcMessageModel)chatPicMsg;
            url_list = NIMGroupMsgManager.getInstance().GetPicPathList(gcMessageModel.group_id);
            pic_pos = NIMGroupMsgManager.getInstance().getImgPosition().get(position);
        } else if (chatPicMsg.chat_type == MsgConstDef.MSG_CHAT_TYPE.PRIVATE){
            ScMessageModel scMessageModel = (ScMessageModel)chatPicMsg;
            url_list = NIMMsgManager.getInstance().GetPicPathList(scMessageModel.opt_user_id);
            pic_pos = NIMMsgManager.getInstance().getImgPosition().get(position);
        } else {
        }

        if (url_list.isEmpty()) {
            return;
        }

        TransferConfig config = TransferConfig.build()
                .setNowThumbnailIndex(pic_pos)
                .setSourceImageList(url_list)
                .setMissPlaceHolder(R.mipmap.nim_pp_ic_holder_dark)
                .setOriginImageList(imgPicture)
                .setProgressIndicator(new ProgressPieIndicator())
                .setJustLoadHitImage(true)
                .setIndexIndicator(new NumberIndexIndicator())
                .setImageLoader(GlideImageLoader.with(getContext().getApplicationContext()))
                .setOnLongClickListener(new Transferee.OnTransfereeLongClickListener() {
                    @Override
                    public void onLongClick(ImageView imageView, int pos) {
                        showLongMenu(imageView);
                    }
                })
                .create();
        mActivity.transferee.apply(config).show();
    }

    @Override
    public void setMessage(final int position, final BaseMessageModel chatMsg) {
        chatPicMsg = chatMsg;
        super.setMessage(position, chatMsg);

        String url;
        int res;
        if (chatMsg.is_self) {
            url = chatPicMsg.compress_path;
            res = R.drawable.nim_chat_send_normal;
        } else {
            url = Constants.IM_SERVICE + chatPicMsg.msg_content;
            res = R.drawable.nim_chat_receive_normal;
        }

        Logger.error("img_receive", url);
        boolean long_pic = chatMsg.ext_type == ChatMsgBuildManager.LONG_PICTURE;
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)imgPicture.getLayoutParams();
        if (long_pic) {
            params.height = imgPicture.getBitmapHeight();
        } else {
            params.height = 320;
        }
        imgPicture.setLayoutParams(params);
        imgPicture.load(url, res);
    }

    @Override
    protected void delete() {
        if (!TextUtils.isEmpty(chatPicMsg.compress_path)) {
            String path = chatPicMsg.compress_path;
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        }

        super.delete();
    }

    @Override
    protected void setStatus() {
        // 当前发送方
        if (entry.is_self && barSending != null && imgStatus != null) {
            // 发送中，图片上传状态
            if (entry.msg_status == MsgConstDef.MSG_STATUS.SENDING) {
                statusLayout.setVisibility(View.VISIBLE);
                imgStatus.setVisibility(View.GONE);
                txtStatus.setVisibility(View.GONE);
                switch (entry.msg_status) {
                    case MsgConstDef.MSG_STATUS.UPLOADING:
                        barSending.setVisibility(View.VISIBLE);
                        break;
                    case MsgConstDef.MSG_STATUS.UPLOAD_SUCCESS:
                        break;
                    case MsgConstDef.MSG_STATUS.UPLOAD_FAILED:
                        break;
                }
            } else if (entry.msg_status == MsgConstDef.MSG_STATUS.SEND_SUCCESS){
                statusLayout.setVisibility(View.INVISIBLE);
            } else if (entry.msg_status == MsgConstDef.MSG_STATUS.SEND_FAILED){
                statusLayout.setVisibility(View.VISIBLE);
                barSending.setVisibility(View.GONE);
                txtStatus.setVisibility(View.GONE);
                imgStatus.setVisibility(View.VISIBLE);
            } else {
                statusLayout.setVisibility(View.INVISIBLE);
            }

        } else if (statusLayout != null) {
            statusLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void reSend() {
        chatPicMsg.progress = 0;
        super.reSend();
    }

    protected void showLongMenu(final ImageView imageView) {
        final ArrayList<String> op_aar = new ArrayList<>();
        op_aar.add(getContext().getString(R.string.nim_image_save_txt));
        op_aar.add(getContext().getString(R.string.nim_image_repeat_txt));
        op_aar.add(getContext().getString(R.string.nim_permission_cancel));
        ProgressDialog.showCustomDialog(mActivity, getContext().getString(R.string.operater), op_aar, new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                String click_str = op_aar.get(position);
                if (click_str.equals(getContext().getString(R.string.nim_image_save_txt))) {
                    FileUtil.saveImageToSdcard(imageView);
                } else if (click_str.equals(getContext().getString(R.string.nim_image_repeat_txt))) {
                    repeat(entry);
                }
            }
        });
    }
}