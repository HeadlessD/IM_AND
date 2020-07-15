package com.qbao.newim.manager;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import com.qbao.newim.activity.NIMGcChatActivity;
import com.qbao.newim.activity.NIMScChatActivity;
import com.qbao.newim.activity.NIMUserInfoActivity;
import com.qbao.newim.constdef.FriendTypeDef;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.model.message.BaseMessageModel;
import com.qbao.newim.model.message.GcMessageModel;
import com.qbao.newim.model.message.ScMessageModel;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.SharedPreferenceUtil;
import com.qbao.newim.util.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by chenjian on 2017/6/15.
 */

public class NIMChatNotifyManager {

    private static NIMChatNotifyManager instance;
    private static AtomicBoolean appendSpace = new AtomicBoolean();
    private long lastShowTime;
    private static final int NOTIFICATION_SOUND_TIME_SPAN = 3 * 1000;
    private static Map<Long, Long> timestampMap = new HashMap<>();
    private int count_id = 1000;

    public static NIMChatNotifyManager getInstance() {
        if (instance == null) {
            instance = new NIMChatNotifyManager();
        }

        return instance;
    }

    private Context mContext;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotifyBuilder;
    private SoundPool mSoundPlayer;
    private Uri mSoundUri;

    private static Handler mHandler = new Handler(Looper.getMainLooper());

    public void init(Application context) {
        mContext = context;
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mSoundUri = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.nim_msg_receive);
        mSoundPlayer = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        mSoundPlayer.load(mContext, R.raw.nim_msg_receive, 1);
    }

    public void notify(BaseMessageModel chatMsg) {
        if (chatMsg == null) {
            return;
        }
        if (AppUtil.isBackground(mContext)) {
            notifySoundVibrate();
        } else {
            notifySyn(chatMsg);
        }
    }

    public void notify(IMFriendInfo add_info) {
        if (add_info == null) {
            return;
        }
        if (AppUtil.isBackground(mContext)) {
            notifySoundVibrate();
        } else {
            notifySyn(add_info);
        }
    }

    private synchronized void notifySyn(IMFriendInfo add_info) {
        if (mNotifyBuilder == null) {
            Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.nim_notification_icon);
            mNotifyBuilder = new NotificationCompat.Builder(mContext);
            mNotifyBuilder.setLargeIcon(bitmap);
            mNotifyBuilder.setSmallIcon(R.mipmap.nim_notification_icon);
            mNotifyBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
            mNotifyBuilder.setLights(0xff00ff00, 300, 1000);
            mNotifyBuilder.setAutoCancel(true);
        }

        final long currentTime = System.currentTimeMillis();
        boolean space = appendSpace.get();
        mNotifyBuilder.setContentTitle("新的朋友");
        String name = Utils.getUserShowName(new String[]{add_info.nickName, add_info.user_name});
        if (add_info.status == FriendTypeDef.FRIEND_ADD_TYPE.ACCEPT_REQUEST) {
            mNotifyBuilder.setContentText(name + "请求添加好友");
        } else if (add_info.status == FriendTypeDef.FRIEND_ADD_TYPE.PEER_CONFIRM) {
            mNotifyBuilder.setContentText(name + "同意了好友请求 ");
        }
        mNotifyBuilder.setTicker("好友请求");
        mNotifyBuilder.setWhen(currentTime);

        final boolean timeOver = currentTime - lastShowTime > NOTIFICATION_SOUND_TIME_SPAN;
        final boolean threadTimeOver = timestampMap.containsKey(add_info.userId);
        setSound(timeOver, threadTimeOver);
        setVibrate(timeOver, threadTimeOver);

        PendingIntent contentIntent = buildPendingIntent(add_info);
        mNotifyBuilder.setContentIntent(contentIntent);
        Notification notification = mNotifyBuilder.build();

        mNotificationManager.notify(count_id, notification);
        appendSpace.set(!space);
        if (timeOver) {
            lastShowTime = currentTime;
        }
        if (!threadTimeOver) {
            saveTimeStamp(add_info.userId);
        }

        count_id++;
    }

    public void clearAllNotify() {
        if (mNotificationManager != null)
            mNotificationManager.cancelAll();
    }

    private synchronized void notifySyn(BaseMessageModel chatMsg) {
        if (mNotifyBuilder == null) {
            Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.nim_notification_icon);
            mNotifyBuilder = new NotificationCompat.Builder(mContext);
            mNotifyBuilder.setLargeIcon(bitmap);
            mNotifyBuilder.setSmallIcon(R.mipmap.nim_notification_icon);
            mNotifyBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
            mNotifyBuilder.setLights(0xff00ff00, 300, 1000);
            mNotifyBuilder.setAutoCancel(true);
        }

        final long currentTime = System.currentTimeMillis();
        boolean space = appendSpace.get();
        mNotifyBuilder.setContentTitle(getShowName(chatMsg));
        String content = ChatMsgBuildManager.HandleRichText(chatMsg.m_type, chatMsg.msg_content, chatMsg.s_type);
        mNotifyBuilder.setContentText(content);
        mNotifyBuilder.setTicker("一条新消息");
        mNotifyBuilder.setWhen(currentTime);

        final boolean timeOver = currentTime - lastShowTime > NOTIFICATION_SOUND_TIME_SPAN;
        final boolean threadTimeOver = timestampMap.containsKey(chatMsg.message_id);
        setSound(timeOver, threadTimeOver);
        setVibrate(timeOver, threadTimeOver);

        PendingIntent contentIntent = buildPendingIntent(chatMsg);
        mNotifyBuilder.setContentIntent(contentIntent);
        Notification notification = mNotifyBuilder.build();

        mNotificationManager.notify(count_id, notification);
        appendSpace.set(!space);
        if (timeOver) {
            lastShowTime = currentTime;
        }
        if (!threadTimeOver) {
            saveTimeStamp(chatMsg.message_id);
        }

        count_id++;
    }

    // TODO: 2017/9/28 史云杰确认加上公众号和其他消息
    private PendingIntent buildPendingIntent(BaseMessageModel chat_msg) {
        AppUtil.exit();
        Intent intent = null;
        long session_id = 0;
        switch (chat_msg.chat_type) {
            case MsgConstDef.MSG_CHAT_TYPE.PRIVATE:
                ScMessageModel scMessageModel = (ScMessageModel)chat_msg;
                intent.setClass(mContext, NIMScChatActivity.class);
                session_id = scMessageModel.opt_user_id;
                break;
            case MsgConstDef.MSG_CHAT_TYPE.GROUP:
                GcMessageModel gcMessageModel = (GcMessageModel)chat_msg;
                intent.setClass(mContext, NIMGcChatActivity.class);
                session_id = gcMessageModel.group_id;
                break;
        }

        intent.putExtra("id", session_id);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int requestCode = (int) SystemClock.uptimeMillis();
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return contentIntent;
    }

    private void notifySoundVibrate() {
        if (SharedPreferenceUtil.getMsgVoice()) {
            mSoundPlayer.play(1,  1, 1, 0, 0, 1);
        }

        if (SharedPreferenceUtil.getMsgVibrate()) {
            Vibrate();
        }
    }

    private void Vibrate() {
        Vibrator vib = (Vibrator)mContext .getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(500);
    }

    private String getShowName(BaseMessageModel chat_msg) {
        String show_name = "";
        if (chat_msg.chat_type == MsgConstDef.MSG_CHAT_TYPE.PRIVATE) {
            ScMessageModel scMessageModel = (ScMessageModel) chat_msg;
            IMFriendInfo friendInfo = NIMFriendInfoManager.getInstance().getFriendUser(scMessageModel.opt_user_id);
            if (friendInfo != null) {
                show_name = Utils.getUserShowName(new String[]{friendInfo.remark_name, friendInfo.nickName, friendInfo.user_name});
            } else {
                show_name = scMessageModel.send_user_name;
            }

            return show_name;
        } else if (chat_msg.chat_type == MsgConstDef.MSG_CHAT_TYPE.GROUP){
            GcMessageModel gcMessageModel = (GcMessageModel)chat_msg;
            IMGroupInfo groupInfo = NIMGroupInfoManager.getInstance().getGroupInfo(gcMessageModel.group_id);
            if (groupInfo != null) {
                show_name = groupInfo.group_name;
            } else {
                show_name = "群聊";
            }
        }

        return show_name;
    }

    private PendingIntent buildPendingIntent(IMFriendInfo add_info) {
        Intent intent = new Intent(mContext, NIMUserInfoActivity.class);
        intent.putExtra("user_id", add_info.userId);
        intent.putExtra("source_type", add_info.source_type);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int requestCode = (int) SystemClock.uptimeMillis();
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return contentIntent;
    }

    private void setSound(boolean timeOver, boolean threadTimeOver) {
        if (!SharedPreferenceUtil.getMsgVoice()) {
            mNotifyBuilder.setSound(null);
            return;
        }
        if (timeOver && !threadTimeOver) {
            mNotifyBuilder.setSound(mSoundUri);
        } else {
            mNotifyBuilder.setSound(null);
        }
    }

    private void setVibrate(boolean timeOver, boolean threadTimeOver) {
        if (!SharedPreferenceUtil.getMsgVibrate()) {
            mNotifyBuilder.setDefaults(0);
            return;
        }
        if (timeOver && !threadTimeOver) {
            mNotifyBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
        } else {
            mNotifyBuilder.setDefaults(0);
        }
    }

    private void saveTimeStamp(final long msg_id) {
        if (timestampMap.containsKey(msg_id)) return;
        timestampMap.put(msg_id, System.currentTimeMillis());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                timestampMap.remove(msg_id);
            }
        }, 30000);
    }
}
