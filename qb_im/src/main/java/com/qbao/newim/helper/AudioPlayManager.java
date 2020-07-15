package com.qbao.newim.helper;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

import com.qbao.newim.configure.Constants;
import com.qbao.newim.configure.GlobalVariable;

import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.model.message.BaseMessageModel;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.SharedPreferenceUtil;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by chenjian on 2017/3/25.
 */

public class AudioPlayManager implements Handler.Callback {
    private static final String PLAYER_THREAD_NAME = "playthread";

    /**
     * 外放模式
     */
    public static final int MODE_SPEAKER = 0;
    /**
     * 耳机模式
     */
    public static final int MODE_HEADSET = 1;
    /**
     * 听筒模式
     */
    public static final int MODE_EARPIECE = 2;


    private int currentMode = -1;
    //用户自定义模式
    private int customMode = -1;

    public static final int VOICE_PLAY_BEGIN = 1;
    public static final int VOICE_PLAY_END = 2;
    public static final int VOICE_RECORD = 3;
    public static final int MSG_SENT = 4;
    public static final int MSG_RECEIVE = 5;

    private final static int REQUEST_PLAY_RECORD = 997;
    private final static int REQUEST_PLAY_RECEIVE = 998;
    private final static int REQUEST_PLAY = 999;
    private final static int REQUEST_STOP = 1000;
    private final static int REQUEST_COMPLETE = 1001;
    private final static int PLAY_ERROR = 1002;

    private final Handler mMainThreadHandler = new Handler(this);
    private PlayThread mPlayThread;
    private MediaPlayer mMediaPlayer;

    private SoundPool soundPool;
    private HashMap<Integer, Integer> soundPoolMap;
    private Context mContext;

    private BaseMessageModel mChatAudioMsg;
    private int mPosition = -1;
    private boolean mAutoNext;

    private static AudioPlayManager playerManager;
    private AudioManager audioManager;

    public interface OnAudioPlayListener {
        void onPlayStart(BaseMessageModel Item);
        void onPlayStop(int position);
        void onPlayCompletion(BaseMessageModel Item, int position, boolean autoNext);
    }

    private OnAudioPlayListener mOnAudioPlayListener;

    public void setOnAudioPlayListener(OnAudioPlayListener onPlayCompletionListener) {
        mOnAudioPlayListener = onPlayCompletionListener;
    }

    public static AudioPlayManager getManager(){
        if (playerManager == null){
            synchronized (AudioPlayManager.class){
                playerManager = new AudioPlayManager();
            }
        }
        return playerManager;
    }

    public AudioPlayManager() {
        mContext = AppUtil.GetContext();
        initChatVoice();

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 100);// 初始化SoundPool
        soundPoolMap = new HashMap<>();
        soundPoolMap.put(MSG_RECEIVE, soundPool.load(mContext, R.raw.nim_msg_receive, 1));// 接收消息提示音

        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        } else {
            audioManager.setMode(AudioManager.MODE_IN_CALL);
        }

        customMode = SharedPreferenceUtil.getAudioMode();
        //初始化检测手机是否插上耳机
        if(checkHeadsetOn())
        {
            changeToHeadsetMode();
        }
        else
        {
            leaveHeadSet();
        }
    }

    private boolean checkHeadsetOn()
    {
        boolean isHeadsetOn=false;
        String HEADSET_STATE_PATH = "/sys/class/switch/h2w/state";//这就是其中一个接口
        int headsetState = 0;
        int len = 0;
        try {
            FileReader file = new FileReader(HEADSET_STATE_PATH);
            char[] buffer = new char[1024];
            try {
                len = file.read(buffer, 0, 1024);
            } catch (IOException e) {
            }
            headsetState = Integer.valueOf(new String(buffer, 0,len).trim());
        } catch (FileNotFoundException e) {
        }
        if (headsetState > 0) {//大多数手机这个值是1或者2，但是也有不少手机是100多，
            //分别表示耳机连接或者耳机和麦克风都连接
            isHeadsetOn=true;
        }else{//-1或者其他负数表示拔出
            isHeadsetOn=false;
        }

        return isHeadsetOn;
    }

    /**
     * 进入聊天界面初始化资源
     */
    public void initChatVoice() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public void requestPlay(BaseMessageModel entry, int position) {
        mAutoNext = entry.msg_status != MsgConstDef.MSG_STATUS.PLAYED && entry.is_self == false;
        if (mPosition >= 0) {
            if (mOnAudioPlayListener != null && mChatAudioMsg != null) {
                mOnAudioPlayListener.onPlayStop(mPosition);
            }
        }
        mPosition = position;
        mMainThreadHandler.sendMessage(mMainThreadHandler.obtainMessage(REQUEST_PLAY, entry));
    }

    public void requestStop(BaseMessageModel entry) {
        mMainThreadHandler.sendMessage(mMainThreadHandler.obtainMessage(REQUEST_STOP, entry));
    }


    public boolean isHeadHetMode() {
        return (currentMode == MODE_HEADSET);
    }

    //当前状态是否听筒模式
    public boolean isEarMode()
    {
        return (currentMode == MODE_EARPIECE);
    }

    //用户自定义为听筒模式
    public boolean isCustomEarMode()
    {
        return customMode == MODE_EARPIECE;
    }

    public void setCustomMode(int mode)
    {
        if(customMode == mode)
        {
            return;
        }

        customMode = mode;
        SharedPreferenceUtil.saveAudioMode(customMode);
        if(currentMode == MODE_HEADSET)
        {
            return;
        }

        leaveHeadSet();
    }

    //退出耳机模式
    public void leaveHeadSet()
    {
        if(customMode == MODE_SPEAKER)
        {
            changeToSpeakerMode();
        }
        else
        {
            changeToEarpieceMode();
        }
    }

    /**
     * 切换到听筒模式
     */
    public void changeToEarpieceMode(){
        if(currentMode == MODE_EARPIECE)
        {
            return;
        }
        currentMode = MODE_EARPIECE;
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(false);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
//            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
//                    audioManager.getStreamMaxVolume(AudioManager.MODE_IN_COMMUNICATION), AudioManager.FX_KEY_CLICK);
//        } else {
//            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
//                    audioManager.getStreamMaxVolume(AudioManager.MODE_IN_CALL), AudioManager.FX_KEY_CLICK);
//        }
    }
    /**
     * 切换到耳机模式
     */
    public void changeToHeadsetMode(){
        if(currentMode == MODE_HEADSET)
        {
            return;
        }
        currentMode = MODE_HEADSET;
        audioManager.setSpeakerphoneOn(false);
    }
    /**
     * 切换到外放模式
     */
    public void changeToSpeakerMode(){
        if(currentMode == MODE_SPEAKER)
        {
            return;
        }
        currentMode = MODE_SPEAKER;
        if (/*Utils.isHuaweiPhone() || Utils.isSamsungPhone()*/false) {
            requestStop(mChatAudioMsg);
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(true);
            requestPlay(mChatAudioMsg, mPosition);
        } else {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(true);
        }
    }

    /**
     * 开始录音播放提示音
     */
    public void playStartRecordSound() {
        if (canPlayVoice()) {
            mMainThreadHandler.sendMessage(mMainThreadHandler.obtainMessage(REQUEST_PLAY_RECORD, VOICE_RECORD, 0));
        }
    }

    public void ensurePlayThread() {
        if (mPlayThread == null) {
            mPlayThread = new PlayThread();
            mPlayThread.start();
        }
    }

    private boolean canPlayVoice() {
//        final boolean voice = UserShareedpreference.getSettingInfo_Boolean(mContext, UserShareedpreference.VOICE);
//        return voice;
        return true;
    }

    public int getLastPlayPosition() {
        return mPosition;
    }

    public void updatePosition(int position) {
        mPosition = position;
    }

    private class PlayThread extends HandlerThread implements Handler.Callback, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

        private final static int START_PLAY = 1002;

        private final static int PAUSE_PLAY = 1003;

        private final static int STOP_PLAY = 1004;

        private final static int COMPLETE = 1005;

        private final static int PLAY_TONE = 1006;

        private Handler mPlayThreadHandler;

        public PlayThread() {
            super(PLAYER_THREAD_NAME);
        }

        public void ensureHandler() {
            if (mPlayThreadHandler == null) {
                mPlayThreadHandler = new Handler(getLooper(), this);
            }
        }

        private void requestPlay(Object obj) {
            ensureHandler();
            mPlayThreadHandler.sendMessage(mPlayThreadHandler.obtainMessage(START_PLAY, obj));
        }

        private void requestStop(Object obj) {
            ensureHandler();
            mPlayThreadHandler.sendMessage(mPlayThreadHandler.obtainMessage(STOP_PLAY, obj));
        }

        private void playToneSound(int index) {
            ensureHandler();
            mPlayThreadHandler.sendMessage(mPlayThreadHandler.obtainMessage(PLAY_TONE, index, 0));
        }

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == PLAY_TONE) {
                playTone(msg.arg1);
                return true;
            }

            BaseMessageModel entry = null;

            if (msg.obj instanceof BaseMessageModel) {
                entry = (BaseMessageModel) msg.obj;
            }

            switch (msg.what) {
                case START_PLAY:
                    stopPlay(entry);

                    if (entry == null) {
                        return true;
                    }

                    mChatAudioMsg = entry;

                    play();
                    break;

                case PAUSE_PLAY:

                    break;

                case STOP_PLAY:
                    mChatAudioMsg = null;
                    stopPlay(entry);
                    break;

                case COMPLETE:
                    playTone(VOICE_PLAY_END);
                    mMainThreadHandler.sendEmptyMessage(REQUEST_COMPLETE);
                    break;

            }
            return true;
        }

        private void stopPlay(BaseMessageModel entry) {
            if (mMediaPlayer != null) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }

                mMediaPlayer.reset();
            }
        }

        private void play() {
            playTone(VOICE_PLAY_BEGIN);

            FileInputStream fis = null;
            try {
//				MultiMediaHelper.parseAudio(mChatAudioMsg);
                final String path = mChatAudioMsg.audio_path;

                if (path.startsWith(Constants.SYS_DATA_PATH)) {
                    fis = new FileInputStream(new File(path));
                    mMediaPlayer.setDataSource(fis.getFD());
                } else if (path.startsWith("content")) {
                    Uri uri = Uri.parse(path);
                    FileDescriptor fd = null;
                    try {
                        ParcelFileDescriptor pfd = mContext
                                .getContentResolver().openFileDescriptor(uri,
                                        "r");
                        fd = pfd.getFileDescriptor();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    mMediaPlayer.setDataSource(fd);
                } else {
                    mMediaPlayer.setDataSource(path);
                }

                try {
                    mMediaPlayer.prepare();
                } catch (IOException e) {
                    mMainThreadHandler.sendEmptyMessage(PLAY_ERROR);
                    Toast.makeText(
                            mContext,
                            mContext.getResources().getString(
                                    R.string.nim_chat_play_voice_exc),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                mMediaPlayer.setOnCompletionListener(this);
                mMediaPlayer.setOnErrorListener(this);

                if (GlobalVariable.isRecording) {
                    //如果正在录音则停止播放
                    stopPlay(mChatAudioMsg);
                    return;
                }

                mMediaPlayer.start();

            } catch (IllegalStateException e) {
                e.printStackTrace();
                mMainThreadHandler.sendEmptyMessage(PLAY_ERROR);
            } catch (IOException e) {
                e.printStackTrace();
                mMainThreadHandler.sendEmptyMessage(PLAY_ERROR);
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    mMainThreadHandler.sendEmptyMessage(PLAY_ERROR);
                }
            }
        }

        private void playTone(int index) {
            if (soundPoolMap.containsKey(index)) {
                int soundID = soundPoolMap.get(index);
                if (0 < soundID) // <= 0 提示音加载失败
                {
                    soundPool.play(soundID, 1, 1, 1, 0, 1);
                }
            }
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            mPlayThreadHandler.sendEmptyMessage(COMPLETE);
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            mMainThreadHandler.sendEmptyMessage(PLAY_ERROR);
            return false;
        }
    }

    /**
     * 在主线程处理
     */
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case REQUEST_PLAY: {
                ensurePlayThread();
                if (mOnAudioPlayListener != null) {
                    mOnAudioPlayListener.onPlayStart(mChatAudioMsg);
                }
                mPlayThread.requestPlay(msg.obj);
                return true;
            }

            case REQUEST_STOP: {
                ensurePlayThread();
                mPlayThread.requestStop(msg.obj);
                return true;
            }

            case REQUEST_COMPLETE: {
                if (mOnAudioPlayListener != null) {
                    mOnAudioPlayListener.onPlayCompletion(mChatAudioMsg, mPosition, mAutoNext);
                }
                mChatAudioMsg = null;
                return true;
            }

            case REQUEST_PLAY_RECEIVE:
            case REQUEST_PLAY_RECORD: {
                ensurePlayThread();
                mPlayThread.playToneSound(msg.arg1);
                return true;
            }

            case PLAY_ERROR:
                if (mOnAudioPlayListener != null) {
                    mOnAudioPlayListener.onPlayCompletion(mChatAudioMsg, mPosition, mAutoNext);
                }
                mChatAudioMsg = null;
                return true;
        }
        return false;
    }

    public void raiseVolume(){
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume < audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE, AudioManager.FX_FOCUS_NAVIGATION_UP);
        }
    }
    /**
     * 调小音量
     */
    public void lowerVolume(){
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume > 0) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER, AudioManager.FX_FOCUS_NAVIGATION_UP);
        }
    }
}
