package com.qbao.newim.manager;

import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qbao.newim.configure.Constants;
import com.qbao.newim.configure.GlobalVariable;
import com.qbao.newim.helper.AudioPlayManager;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.FileUtil;
import com.qbao.newim.util.ShowUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by chenjian on 2017/3/25.
 */

public class AudioRecordManager {
    static
    {
        System.loadLibrary("mp3lame");
    }

    /**
     * 初始化录制参数
     */
    public void init(int inSamplerate, int outChannel, int outSamplerate, int outBitrate) {
        init(inSamplerate, outChannel, outSamplerate, outBitrate, 7);
    }

    /**
     * 初始化录制参数
     * quality:0=很好很慢 9=很差很快
     */
    public native void init(int inSamplerate, int outChannel, int outSamplerate,
                            int outBitrate, int quality);

    /**
     * 音频数据编码(PCM左进,PCM右进,MP3输出)
     */
    public native int encode(short[] buffer_l, short[] buffer_r, int samples, byte[] mp3buf);

    /**
     * 据说录完之后要刷干净缓冲区,就是冲出来的那些东西要擦干净啦
     */
    public native int flush(byte[] mp3buf);

    /**
     * 结束编码
     */
    public native static void close();

    public static final int NUM_CHANNELS = 1;
    public static final int SAMPLE_RATE = 8000;
    public static final int BITRATE = 128;
    public static final int MODE = 1;
    public static final int QUALITY = 2;


    private final String TAG = AudioRecordManager.class.getSimpleName();

    public interface OnAudioRecordCompletionListener
    {
        void onAudioRecordCompletion(String audioPath, int recordDuration);
    }

    OnAudioRecordCompletionListener mOnAudioRecordCompletionListener;

    public void setOnAudioRecordCompletionListener(OnAudioRecordCompletionListener onAudioRecordCompletionListener)
    {
        mOnAudioRecordCompletionListener = onAudioRecordCompletionListener;
    }

    /**
     * 动画弹出对话框
     */
    private RelativeLayout mAudioRecordViewContainer = null;
    private ImageView imgRecordAnim;
    private ImageView imgRecordCancel;
    private TextView txtDuration;
    private TextView txtHint;

    private Timer timer;

    /**
     * 录音最长时间60秒
     */
    public final static int RECORD_MAX_TIME = 60 * 1000;

    /**
     * 显示“正在录音”面板
     */
    private final static int SHOWRECORDANIM = 116;

    /**
     * 去掉dialog
     */
    private final static int DISMISSDIALOG = 120;

    /**
     * 录音根据音量变换背景图片
     */
    private final static int VOLUME_CHANGE = 136;

    /**
     * 录音倒计时
     */
    private final static int COUNT_DOWN = 135;

    /**
     * 录音错误
     */
    private final static int CHAT_RECORDER_ERROR = 10029;

    /**
     * 发送音频
     */
    private final static int SEND_VOICE = 153;

    /**
     * 取消录音
     */
    private boolean isCancelRecord = false;

    /**
     *
     */
    private boolean isShowCancelView = false;

    /**
     * 锁屏
     */
    private boolean isLockScreen = false;

    /**
     * 录音的存放路径
     */
    private String voiceSavePath = "";

    /**
     * 上下文
     */
    private Context mContext = null;

    /**
     * 窗口管理类
     */
    private WindowManager mWindowManager = null;

    /**
     * 录音开始时间
     */
    private long beginTime = 0;

    private RecordThread recordThread = null;

    /**
     * 设置播放音频时长期亮屏对象
     */
    private PowerManager.WakeLock wakeLock;

    /**
     * 释放长期亮屏定时器
     */
    private TimerTask timerTask;

    private AudioRecord mAudioRecord;
    private int bufferSize;

    /**
     * 构造器
     */
    public AudioRecordManager(Context context)
    {
        mContext = context;
        mWindowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);

        timer = new Timer();

        initAudioRecorder();
    }

    private void initAudioRecorder()
    {
        if(mAudioRecord == null)
        {
            bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            try
            {
                mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, bufferSize);
            }
            catch (IllegalArgumentException e)
            {

            }
        }
    }

    /**
     * 释放编解码资源
     */
    public void destoryEncoder()
    {
        if(mAudioRecord != null)
        {
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }

    public void init()
    {
        GlobalVariable.isRecording = true;
        setVoiceSavePath(null);
        setIsLockScreen(false);
        setIsCancelRecord(false);
    }

    /**
     * 设置锁屏
     *
     * @param value
     * @author
     */
    public void setIsLockScreen(boolean value)
    {
        isLockScreen = value;
    }

    /**
     * 获取
     *
     * @return boolean
     * @author
     */
    public boolean getIsLockScreen()
    {
        return isLockScreen;
    }

    /**
     * 设置语音存储路径
     *
     * @param path
     * @author
     */
    public void setVoiceSavePath(String path)
    {
        voiceSavePath = path;
    }

    /**
     * 获取语音存储路径
     *
     * @return String
     * @author
     */
    public String getVoiceSavePath()
    {
        return voiceSavePath;
    }

    /**
     * 开始录音 void
     *
     * @author
     */
    public void startRecord()
    {
        if(checkDir())
        {
            init();

            // 开启录音任务
            recordThread = new RecordThread();
            recordThread.start();
        }
    }

    /**
     * Handler异步处理
     *
     * @author
     */
    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if (null == msg)
            {
                return;
            }
            super.handleMessage(msg);

            switch (msg.what)
            {
                case SHOWRECORDANIM:// 录音开始，弹出麦克风界面
                {
                    showRecordDialog();
                    break;
                }
                case DISMISSDIALOG:// 录音结束或者异常时，取消麦克风界面
                {
                    dismissAnimationDialog();
                    break;
                }
                case VOLUME_CHANGE:// 根据音量大小更新UI的透明度
                {
                    change((Integer)msg.obj);
                    break;
                }
                case COUNT_DOWN:// 录音倒计时
                {
                    countDown((Integer)msg.obj);
                    break;
                }
                case CHAT_RECORDER_ERROR:// 录音异常显示
                {
                    Toast.makeText(mContext, R.string.nim_chat_audio_recorder_error, Toast.LENGTH_SHORT).show();
                    break;
                }
                case SEND_VOICE:// 录音结束时，发送语音消息
                {
                    dismissDialog();
                    sendVoice((Integer)msg.obj);
                    break;
                }

                default:
            }
        }

    };

    /**
     * 录音倒计时
     *
     * @param second 倒计时显示的时间
     * @author
     */
    private void countDown(int second)
    {
        if (mAudioRecordViewContainer != null)
        {
            if(imgRecordAnim.getVisibility() != View.GONE)
            {
                imgRecordAnim.setVisibility(View.GONE);
            }
            if(imgRecordCancel.getVisibility() != View.GONE)
            {
                imgRecordCancel.setVisibility(View.GONE);
            }
            if(txtDuration.getVisibility() != View.VISIBLE)
            {
                txtDuration.setVisibility(View.VISIBLE);
            }
//        	txtDuration.setText(second + "\"");
            txtDuration.setText(Integer.toString(second));
            mAudioRecordViewContainer.invalidate();
        }
    }

    /**
     * 根据音量的振幅大小更新UI界面麦克风的透明度
     *
     * @param volume
     * @author
     */
    private void change(int volume)
    {
        if (mAudioRecordViewContainer != null)
        {
            volume = volume / 1000;

            int resId;

            if (volume == 0)
            {
                resId = R.mipmap.nim_chat_audio_record0;
            }
            else if (volume < 2)
            {
                resId = R.mipmap.nim_chat_audio_record1;
            }
            else if (volume < 4)
            {
                resId = R.mipmap.nim_chat_audio_record2;
            }
            else if (volume < 6)
            {
                resId = R.mipmap.nim_chat_audio_record3;
            }
            else if (volume < 8)
            {
                resId = R.mipmap.nim_chat_audio_record4;
            }
            else if (volume < 9)
            {
                resId = R.mipmap.nim_chat_audio_record5;
            }
            else
            {
                resId = R.mipmap.nim_chat_audio_record6;
            }

            imgRecordAnim.setImageResource(resId);
            mAudioRecordViewContainer.invalidate();
        }
    }

    /**
     * 录音线程
     */
    public class RecordThread extends Thread implements MediaRecorder.OnErrorListener
    {
        @Override
        public void run()
        {
            super.run();

            if(mAudioRecord == null) return;

            android.os.Process
                    .setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            if (GlobalVariable.isRecording)
            {
                acquireWakeLock();

                record();
            }
        }

        public synchronized void record()
        {
            try
            {
                // 1.显示正在录音的面板
                handler.sendEmptyMessage(SHOWRECORDANIM);

                // 2.生成录音文件的名字和存放位置
                final File file = createFile(".mp3");

                // 3.播放开始录制音频消息的提示音
                AudioPlayManager.getManager().playStartRecordSound();

                // 4.默认的透明度设置
                handler.sendMessage(handler.obtainMessage(VOLUME_CHANGE, 0));

                // 5.录制过程中，根据音频的强度，更新UI显示
                recording(file);

                // 6.录音结束，处理时间
                int duration = processRecordLength();

                // 7.录音结束，发送语音
                sendVoiceRecord(duration);
            }
            catch (IllegalStateException e)
            {
                disposeError(e);
            }
            catch (RuntimeException e)
            {
                disposeError(e);
            }
            catch (Exception e)
            {
                disposeError(e);
            }
            finally
            {
            }
        }

        /**
         * 生成录音文件的名字和存放位置
         *
         */
        private File createFile(final String suffix)
        {
            String voiName = "audio_" + System.currentTimeMillis() + suffix;
            File parent = new File(Constants.AUDIO_CACHE_DIR);
            if (!parent.exists()) {
                parent.mkdir();
            }
            File audioFile = new File(Constants.AUDIO_CACHE_DIR + voiName);
            setVoiceSavePath(audioFile.getAbsolutePath());

            return audioFile;
        }

        /**
         * 录制过程中，根据音频的强度，更新UI显示
         *
         */
        private void recording(final File file)
        {
            // 剩余的可录音时间长度
            long remainTime = 0;
            long lastShowTime = 0;
            long lastShowVolume = 0;

            FileOutputStream output = null;
            try
            {
                // 5秒的缓冲
//				short[] buffer = new short[SAMPLE_RATE * (16 / 8) * 1 * 5];
                final short[] buffer = new short[SAMPLE_RATE * (16 / 8) * 1 * 1];
                byte[] mp3buffer = new byte[(int)(7200 + buffer.length * 2 * 1.25)];

                output = new FileOutputStream(file);

                init(SAMPLE_RATE, 1, SAMPLE_RATE, 32);

                mAudioRecord.startRecording();
                // 录音开始时间
                beginTime = System.currentTimeMillis();

                while (GlobalVariable.isRecording && !getIsCancelRecord())
                {
                    double sum = 0;
                    int readSize = mAudioRecord.read(buffer, 0, bufferSize);

                    if(readSize < 0)
                    {
                        throw new IOException();
                    }
                    else if(readSize == 0)
                    {
                        continue;
                    }
                    else
                    {
                        int encResult = encode(buffer, buffer, readSize, mp3buffer);
                        if (encResult < 0)
                        {
                            throw new IOException();
                        }
                        else if (encResult != 0)
                        {
                            output.write(mp3buffer, 0, encResult);
                            for (int i = 0; i < buffer.length; i++)
                            {
                                sum += buffer[i] * buffer[i];
                            }
                        }
                    }

//					int volume = (int) (Math.abs((int)(sum /(float)readSize)/10000) >> 1);

                    final long currentTime = System.currentTimeMillis();
                    final double amplitude = sum / readSize;
                    final int volume = (int) Math.sqrt(amplitude);

                    if(currentTime - lastShowVolume > 50)
                    {
                        lastShowVolume = currentTime;
                        handler.sendMessage(handler.obtainMessage(VOLUME_CHANGE, volume));
                    }

                    remainTime = RECORD_MAX_TIME - (currentTime - beginTime);

                    // timeout + 1 是为了不显示录音 "还有0秒结束" 的情况。
                    if (remainTime / 1000 + 1 <= 10 && currentTime - lastShowTime > 500)
                    {
                        lastShowTime = currentTime;
                        handler.sendMessage(handler.obtainMessage(COUNT_DOWN, (int)(remainTime / 1000) + 1));
                    }
                    if (remainTime <= 0)
                    {
                        GlobalVariable.isRecording = false;
                    }
                }

                int flushResult = flush(mp3buffer);
                if (flushResult < 0)
                {
                    throw new IOException();
                }
                if (flushResult != 0)
                {
                    try {
                        output.write(mp3buffer, 0, flushResult);
                    } catch (IOException e) {
                        throw new IOException();
                    }
                }
            }
            catch (IOException e)
            {
                disposeError(e);
            }
            finally
            {
                mAudioRecord.stop();

                if (output != null)
                {
                    try
                    {
                        output.flush();
                    }
                    catch (IOException e)
                    {
                        disposeError(e);
                    }
                    finally
                    {
                        try
                        {
                            output.close();
                        }
                        catch (IOException e)
                        {
                            disposeError(e);
                        }
                    }
                }
            }
        }

        /**
         * 录音结束并释放资源
         *
         */
        private int processRecordLength()
        {
            // 播放结束录制音频消息的提示音
            AudioPlayManager.getManager().playStartRecordSound();

            int duration = 0;
            final String audioPath = getVoiceSavePath();
            if(!TextUtils.isEmpty(audioPath))
            {
                duration = getRealDuration(audioPath);
                if(duration < 1)
                {
                    final File file = new File(audioPath);
                    if(file.exists())
                    {
                        file.delete();
                    }
                }
            }

            return duration;
        }

        private int getRealDuration(String audioPath)
        {
            MediaPlayer mp = new MediaPlayer();
            try {
                mp.setDataSource(audioPath);
                mp.prepare();
                int duration = mp.getDuration() / 1000;
                if (duration < 0) {
                    duration = 0;
                } else if (duration > 60) {
                    duration = 60;
                }
                return duration;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mp.release();
                mp = null;
            }

            return 0;
        }

        /**
         * 录音结束发送语音
         *
         */
        private void sendVoiceRecord(int duration)
        {
            if (getVoiceSavePath() == null)
            {
                return;
            }

            handler.sendMessage(handler.obtainMessage(SEND_VOICE, duration));
        }

        @Override
        public synchronized void start()
        {
            super.start();
        }

        @Override
        public void onError(MediaRecorder mr, int what, int extra)
        {
            //录音时出错，提示录音功能被占用
            GlobalVariable.isRecording = false;

            dismissDialog();

            recordError();
        }
    }

    /**
     * 取消录音的界面
     *
     * @author
     */
    private void dismissDialog()
    {
        handler.sendMessage(handler.obtainMessage(DISMISSDIALOG));
    }

    /**
     * 动画取消
     *
     * @author
     */
    private void dismissAnimationDialog()
    {
//        if (mWindowManager != null && audioRecordViewContainer != null)
//        {
//            Animation scaleAnimationDismiss = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
//                		Animation.RELATIVE_TO_PARENT, 0.5f,
//                		Animation.RELATIVE_TO_PARENT, 0.5f);
//            scaleAnimationDismiss.setDuration(100);
//            scaleAnimationDismiss.setAnimationListener(new AnimationListener()
//            {
//                @Override
//                public void onAnimationEnd(Animation animation)
//                {
//                    audioRecordViewContainer.removeAllViews();
//                    mWindowManager.removeView(audioRecordViewContainer);
//                    audioRecordViewContainer = null;
//
//                    // 释放长期亮屏的锁
//                    releaseWakeLock();
//
//                    // 回收资源
//                    voiceRecordDestroy();
//
//                }
//
//                @Override
//                public void onAnimationRepeat(Animation animation)
//                {
//                }
//
//                @Override
//                public void onAnimationStart(Animation animation)
//                {
//                }
//            });
//
//            audioRecordView.startAnimation(scaleAnimationDismiss);
//        }

        if (mWindowManager != null && mAudioRecordViewContainer != null)
        {
            mAudioRecordViewContainer.removeAllViews();
            mWindowManager.removeView(mAudioRecordViewContainer);
            mAudioRecordViewContainer = null;

            // 释放长期亮屏的锁
            releaseWakeLock();
            voiceRecordDestroy();
        }
    }

    /**
     * 录音错误
     */
    private void recordError()
    {
        handler.sendMessage(handler.obtainMessage(CHAT_RECORDER_ERROR));
    }

    /**
     * 录音错误处理
     *
     * @author
     */
    private void disposeError(Exception e)
    {
        // 释放长期亮屏的锁
        releaseWakeLock();

        GlobalVariable.isRecording = false;

        dismissDialog();

        recordError();

        e.printStackTrace();

        // voiceRecordDestroy();
    }

    /**
     * 发送语音消息
     *
     */
    private void sendVoice(int duration)
    {
        if (!getIsCancelRecord())
        {
            // 录音时间小于1s时，界面提醒。
            if (duration < 1)
            {
                ShowUtils.showToast(R.string.nim_chat_audio_too_short);
                return;
            }

            final String audioPath = getVoiceSavePath();
            if(!TextUtils.isEmpty(audioPath) && mOnAudioRecordCompletionListener != null)
            {
                mOnAudioRecordCompletionListener.onAudioRecordCompletion(audioPath, duration);
            }
        }
    }

    /**
     * 录音开始，弹出麦克风界面
     */
    private synchronized void showRecordDialog()
    {
        if (!GlobalVariable.isRecording)
        {
            return;
        }

        View audioRecordView = LayoutInflater.from(mContext).inflate(R.layout.nim_chat_audio, null);
        imgRecordAnim = (ImageView)audioRecordView.findViewById(R.id.chat_record_anim);
        imgRecordCancel = (ImageView)audioRecordView.findViewById(R.id.chat_record_cancel);
        txtDuration = (TextView)audioRecordView.findViewById(R.id.chat_voice_text_time);
        txtHint = (TextView)audioRecordView.findViewById(R.id.chat_voice_text_hit);

        imgRecordAnim.setImageResource(R.mipmap.nim_chat_audio_record3);
        txtDuration.setVisibility(View.GONE);
        imgRecordCancel.setVisibility(View.GONE);
        txtHint.setText(R.string.nim_chat_audio_move_up_cancel);
        txtHint.setSelected(false);

        // 初始化动画对象 参数1,动画开始时候透明度 参数2,动画结束时候透明度
        Animation alphaAnimation = new AlphaAnimation(0.2f, 1.0f);
        alphaAnimation.setDuration(1000);
        imgRecordAnim.setAnimation(alphaAnimation);
        alphaAnimation.startNow();

        animAlert(audioRecordView);
    }

    /**
     * 查看动作的出现
     *
     * @param view
     */
    private synchronized void animAlert(View view)
    {
        WindowManager.LayoutParams winlp = new WindowManager.LayoutParams();
        winlp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        winlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        winlp.gravity = Gravity.CENTER;
        winlp.format = PixelFormat.TRANSPARENT;
        winlp.type = WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL ;
        winlp.flags= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

        RelativeLayout audioRecordViewContainer = new RelativeLayout(mContext);
        audioRecordViewContainer.addView(view, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        audioRecordViewContainer.setGravity(Gravity.CENTER);

        if (mWindowManager != null)
        {
            mWindowManager.addView(audioRecordViewContainer, winlp);
        }
        mAudioRecordViewContainer = audioRecordViewContainer;

        Animation scaleAnimationAlert = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                Animation.RELATIVE_TO_PARENT, 0.5f,
                Animation.RELATIVE_TO_PARENT, 0.5f);
        scaleAnimationAlert.setDuration(100);
        view.startAnimation(scaleAnimationAlert);
    }

    /**
     * 回收资源
     *
     * @author
     */
    private void voiceRecordDestroy()
    {
        mAudioRecordViewContainer = null;
    }

    /**
     *
     * 是否取消录音
     *
     * @return boolean
     */
    private boolean getIsCancelRecord()
    {
        return isCancelRecord;
    }

    public void setIsCancelRecord(boolean value)
    {
        isCancelRecord = value;
    }

    public void updateDialogView(boolean cancel)
    {
        if(mAudioRecordViewContainer == null)
        {
            return;
        }

        if(cancel)
        {
            final Animation animation = imgRecordAnim.getAnimation();
            if(animation != null)
            {
                animation.cancel();
            }
            if(txtDuration.getVisibility() == View.GONE)
            {
                imgRecordAnim.clearAnimation();
                imgRecordAnim.setVisibility(View.GONE);
                imgRecordCancel.setVisibility(View.VISIBLE);
            }
            txtHint.setText(R.string.nim_chat_audio_up_cancel);
            txtHint.setSelected(true);
        }
        else
        {
            if(txtDuration.getVisibility() == View.GONE)
            {
                imgRecordCancel.setVisibility(View.GONE);
                imgRecordAnim.setVisibility(View.VISIBLE);
            }
            txtHint.setText(R.string.nim_chat_audio_move_up_cancel);
            txtHint.setSelected(false);
        }

        mAudioRecordViewContainer.invalidate();
    }

    /**
     *
     * 方法表述:保持屏幕长期亮屏
     * @anthor g00187720
     * void
     */
    private void acquireWakeLock()
    {
        // 没有获取过长期亮屏的锁的情况下则需要获取
        if (null == wakeLock)
        {
            PowerManager pm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, this.getClass().getCanonicalName());
            wakeLock.setReferenceCounted(false);
            wakeLock.acquire();
        }
        // 如果定时器已经存在,则取消已经存在的定时器
        else if (null != timerTask)
        {
            timerTask.cancel();
            timerTask = null;
        }
    }

    /**
     *
     * 方法表述:取消屏幕长期亮屏，并定时释放已获得的长期亮屏的锁
     * @anthor g00187720
     * void
     */
    private void releaseWakeLock()
    {
        // 没有获取过长期亮屏的锁的情况下不用做任何处理,直接返回
        if (null == wakeLock || !wakeLock.isHeld())
        {
            return;
        }
        // 如果定时器已经存在,则取消已经存在的定时器
        if (null != timerTask)
        {
            timerTask.cancel();
            timerTask = null;
        }
        // 初始化定时器
        timerTask = new TimerTask()
        {

            @Override
            public void run()
            {
                // 如果长期亮屏的锁未释放
                if (null != wakeLock && wakeLock.isHeld())
                {
                    // 释放已获得的长期亮屏的锁
                    wakeLock.release();
                    wakeLock = null;
                }
            }
        };
        try
        {
            // 系统屏幕超时时间
            int timeout = Settings.System.getInt(mContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
            // 根据系统设置的屏幕超时时间延迟长期亮屏锁的释放,防止音频播放完毕立即黑屏, 如果timeout小于等于0，在htc手机上是永不休眠
            if (0 < timeout)
            {
                if(timer != null)
                {
                    timer.schedule(timerTask, timeout);
                }
            }
        }
        catch (Settings.SettingNotFoundException e)
        {
            e.printStackTrace();
            // 异常情况下,如果长期亮屏的锁未释放则在此处释放
            if (null != wakeLock && wakeLock.isHeld())
            {
                wakeLock.release();
                wakeLock = null;
            }
        }
    }

    public boolean checkDir()
    {
        final int size = mContext.getResources().getInteger(R.integer.chat_audio_size);
        if(!FileUtil.hasSdcard())
        {
            Toast.makeText(mContext, R.string.nim_sdcard_not_exist, Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!FileUtil.hasFreeSpace(Constants.AUDIO_CACHE_DIR, size))
        {
            Toast.makeText(mContext, R.string.nim_sdcard_not_free_space, Toast.LENGTH_SHORT).show();
            return false;
        }
        else
        {
            return FileUtil.ensureAppPath(Constants.AUDIO_CACHE_DIR);
        }
    }
}
