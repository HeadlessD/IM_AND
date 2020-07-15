package com.qbao.newim.views;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.qbao.newim.adapter.ChatAdapter;
import com.qbao.newim.business.DownloadManager;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.helper.AudioPlayManager;
import com.qbao.newim.manager.NIMGroupMsgManager;
import com.qbao.newim.manager.NIMMsgManager;
import com.qbao.newim.model.message.BaseMessageModel;
import com.qbao.newim.model.message.GcMessageModel;
import com.qbao.newim.model.message.ScMessageModel;
import com.qbao.newim.qbim.R;

import java.io.File;

/**
 * Created by chenjian on 2017/3/25.
 */

public abstract class ChatAudioView extends ChatView {
    public static BaseMessageModel playingEntry;

    protected TextView txtDuration;
    protected ImageView imgPlay;
    protected ImageView imgProgress;
    protected ProgressBar progressBar;
    protected View playLayout;
    protected View spaceView;
    private int spaceStep;
    private int spaceMinLength;

    protected BaseMessageModel chatAudioMsg;

    public ChatAudioView(Context context) {
        super(context);
    }

    public ChatAudioView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatAudioView(Context context, ChatAdapter adapter) {
        super(context, adapter);
    }

    @Override
    public void initViews(Context context, View root) {
        super.initViews(context, root);
        txtDuration = (TextView) findViewById(R.id.chat_tv_duration);
        imgPlay = (ImageView) findViewById(R.id.chat_img_audio);
        imgProgress = (ImageView) findViewById(R.id.chat_img_download);
        progressBar = (ProgressBar) findViewById(R.id.chat_audio_progress);
        playLayout = findViewById(R.id.chat_layout_play);
        spaceView = findViewById(R.id.chat_tv_duration_length);
    }

    @Override
    public void onClick(View v) {
        final AnimationDrawable animationDrawable;
        // 当前点击正在播放的语音，停止
        if (playingEntry != null && playingEntry.message_id == chatAudioMsg.message_id) {
            //停止播放
            playingEntry = null;
            txtDuration.setText(String.valueOf(chatAudioMsg.ext_type));
            final Drawable drawable = imgPlay.getDrawable();
            if (drawable instanceof AnimationDrawable) {
                animationDrawable = (AnimationDrawable) drawable;
                if (animationDrawable != null) {
                    animationDrawable.stop();
                }
            }

            imgPlay.setImageResource(getDrawable());
            AudioPlayManager.getManager().requestStop(chatAudioMsg);
        } else {
            // 否则，语音地址为空的话，就去下载
            if (TextUtils.isEmpty(chatAudioMsg.audio_path) || !new File(chatAudioMsg.audio_path).exists()) {
                if (!TextUtils.isEmpty(chatAudioMsg.msg_content)) {
                    final DownloadManager downloadManager = DownloadManager.getInstance();
                    downloadManager.downloadAudio(chatAudioMsg);
                } else {
                    Toast.makeText(getContext(), R.string.nim_chat_audio_not_exists, Toast.LENGTH_SHORT).show();
                }
            }
            else {
                //开始播放
                playingEntry = chatAudioMsg;
                imgPlay.setImageResource(getAnimationDrawable());
                animationDrawable = (AnimationDrawable) imgPlay.getDrawable();
                if (animationDrawable != null) {
                    animationDrawable.start();
                }
                AudioPlayManager.getManager().requestPlay(chatAudioMsg, position);

                //更新是否播放字段
                if (chatAudioMsg.is_self == false) {
                    //// TODO: 2017/9/28 史云杰，加上群和公众号
                    switch (chatAudioMsg.chat_type)
                    {
                        case MsgConstDef.MSG_CHAT_TYPE.PRIVATE:
                            {
                                ScMessageModel sc_model = (ScMessageModel)chatAudioMsg;
                                NIMMsgManager.getInstance().SetMessageStatus(sc_model.opt_user_id, sc_model.message_id, MsgConstDef.MSG_STATUS.PLAYED);
                            }
                            break;
                        case MsgConstDef.MSG_CHAT_TYPE.GROUP:
                            {
                                GcMessageModel gc_model = (GcMessageModel)chatAudioMsg;
                                gc_model.msg_status = MsgConstDef.MSG_STATUS.PLAYED;
                                NIMGroupMsgManager.getInstance().updateGroupMessageInfoByMsgID(gc_model.group_id, gc_model);
                            }
                            break;
                        case MsgConstDef.MSG_CHAT_TYPE.PUBLIC:
                            {

                            }
                            break;
                        default:
                            break;
                    }
                }
            }

            adapter.updateItemView(position);
        }
    }

    @Override
    public void initData() {
        super.initData();
        spaceStep = getResources().getDimensionPixelSize(R.dimen.chat_audio_step_length);
        spaceMinLength = getResources().getDimensionPixelSize(R.dimen.chat_audio_min_length);
    }

    @Override
    public int getLayoutId() {
        return 0;
    }

    @Override
    public void setMessage(final int position, final BaseMessageModel chatMsg) {
        chatAudioMsg = chatMsg;
        super.setMessage(position, chatMsg);

        setDuration();

        if ((TextUtils.isEmpty(chatAudioMsg.audio_path) || !new File(chatAudioMsg.audio_path).exists())) {
            final DownloadManager downloadManager = DownloadManager.getInstance();
            downloadManager.downloadAudio(chatAudioMsg);
        }

        final AnimationDrawable animationDrawable;
        if (playingEntry != null && playingEntry.message_id == chatAudioMsg.message_id) {
            imgPlay.setImageResource(getAnimationDrawable());
            animationDrawable = (AnimationDrawable) imgPlay.getDrawable();
            if (animationDrawable != null) {
                animationDrawable.start();
            }
        } else {
            Drawable drawable = imgPlay.getDrawable();
            if (drawable instanceof AnimationDrawable) {
                animationDrawable = (AnimationDrawable) drawable;
                if (animationDrawable != null) {
                    animationDrawable.stop();
                }
            }
            imgPlay.setImageResource(getDrawable());
        }
    }

    @Override
    protected void delete() {
        super.delete();

        AudioPlayManager.getManager().requestStop(chatAudioMsg);
        playingEntry = null;

        final String path = chatAudioMsg.audio_path;
        final File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    private void setDuration() {
        // 如果当前是接受状态
        if (chatAudioMsg.is_self == false) {
            // 处于正在下载状态，显示下载进度圈圈
            if (chatAudioMsg.msg_status == MsgConstDef.MSG_STATUS.DOWNLOADING) {
                if (progressBar.getVisibility() != View.VISIBLE) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                if (txtDuration.getVisibility() != View.GONE) {
                    txtDuration.setVisibility(View.GONE);
                }
            } else if (chatAudioMsg.ext_type == 0) {
                if (progressBar.getVisibility() != View.GONE) {
                    progressBar.setVisibility(View.GONE);
                }
                if (txtDuration.getVisibility() != View.VISIBLE) {
                    txtDuration.setVisibility(View.VISIBLE);
                }
                setDurationText(chatAudioMsg.ext_type);
            } else {
                if (progressBar.getVisibility() != View.GONE) {
                    progressBar.setVisibility(View.GONE);
                }
                if (txtDuration.getVisibility() != View.VISIBLE) {
                    txtDuration.setVisibility(View.VISIBLE);
                }
                setDurationText(chatAudioMsg.ext_type);
            }
        } else
            setDurationText(chatAudioMsg.ext_type);
    }

    private void setDurationText(int duration) {
        txtDuration.setText(String.valueOf(duration) + "\"");

        try {
            int viewWidth = spaceMinLength;
            if (duration < 10) {
                viewWidth += duration * spaceStep;
            } else {
                int n = 10 + duration / 10;
                viewWidth += n * spaceStep;
            }
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) spaceView.getLayoutParams();
            params.width = viewWidth;
            spaceView.setLayoutParams(params);
        } catch (Exception e) {

        }
    }

    protected abstract int getAnimationDrawable();

    protected abstract int getDrawable();
}
