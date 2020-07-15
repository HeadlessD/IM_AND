package com.qbao.newim.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.qbao.newim.activity.NIMChatActivity;
import com.qbao.newim.configure.GlobalVariable;
import com.qbao.newim.manager.AudioRecordManager;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.ShowUtils;

/**
 * Created by chenjian on 2017/3/25.
 */

public class AudioRecordButton extends android.support.v7.widget.AppCompatButton {
    private NIMChatActivity mChatActivity;
    private long lastTime;

    public AudioRecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setChatActivity(NIMChatActivity chatActivity) {
        mChatActivity = chatActivity;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if ((System.currentTimeMillis() - lastTime) < 500) {
                    return false;
                }

                setText("松开 发送");
                setTextColor(Color.parseColor("#929292"));
                setBackgroundResource(R.drawable.nim_chat_voice_press);

                //停止正在播放的录音
                mChatActivity.stopPlayAudio();
                mChatActivity.startRecord();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                // 判断移动位置是否出界
                final boolean isMoveOut = isMoveOut(event.getRawX(), event.getRawY());
                getAudioRecordManager().updateDialogView(isMoveOut);
                setText(isMoveOut ? R.string.nim_chat_audio_up_cancel : R.string.nim_chat_audio_up_send);
                break;
            }
            case MotionEvent.ACTION_UP: {
                lastTime = System.currentTimeMillis();

                setText(R.string.nim_chat_press_speak);
                setTextColor(Color.parseColor("#888888"));
                setBackgroundResource(R.drawable.nim_chat_voice_normal);

                // 判断是否在按钮区域内，不是则取消
                if (isMoveOut(event.getRawX(), event.getRawY())) {
                    getAudioRecordManager().setIsCancelRecord(true);
                } else {
                    if (getAudioRecordManager().getVoiceSavePath() == null
                            && GlobalVariable.isRecording) {
                        ShowUtils.showToast(R.string.nim_chat_audio_too_short);
                    }
                }

                GlobalVariable.isRecording = false;
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                lastTime = System.currentTimeMillis();

                setText(R.string.nim_chat_press_speak);
                GlobalVariable.isRecording = false;
                getAudioRecordManager().setIsCancelRecord(true);
                break;
            }
            default:
                break;
        }
        return true;
    }

    private boolean isMoveOut(float x, float y) {
        int[] location = new int[2];
        getLocationOnScreen(location);
        int dialogViewX = location[0];
        int dialogViewY = location[1];

        float pointX = x;
        float pointY = y;

        final float span = getResources().getDimension(
                R.dimen.chat_audio_record_move_span);

        if ((pointX >= dialogViewX && pointX <= (dialogViewX + getWidth()))
                && (pointY >= (dialogViewY - span) && pointY <= (dialogViewY
                + getHeight() + span))) {
            return false;
        } else {
            return true;
        }
    }

    private AudioRecordManager getAudioRecordManager() {
        return mChatActivity.getAudioRecordManager();
    }
}