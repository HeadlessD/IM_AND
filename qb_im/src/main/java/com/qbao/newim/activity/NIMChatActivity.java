package com.qbao.newim.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.qbao.newim.adapter.BottomMenuAdapter;
import com.qbao.newim.adapter.ChatAdapter;
import com.qbao.newim.adapter.ChatFaceAdapter;
import com.qbao.newim.adapter.QBFaceAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.business.DownloadManager;
import com.qbao.newim.business.DownloadObject;
import com.qbao.newim.business.DownloadObserver;
import com.qbao.newim.configure.Constants;
import com.qbao.newim.configure.GlobalVariable;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.ErrorDetail;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.helper.AudioPlayManager;
import com.qbao.newim.helper.ChatAudioDownloadJob;
import com.qbao.newim.helper.IDownloadManager;
import com.qbao.newim.helper.InputLengthFilter;
import com.qbao.newim.helper.Transferee;
import com.qbao.newim.manager.AudioRecordManager;
import com.qbao.newim.manager.ChatMsgBuildManager;
import com.qbao.newim.manager.NIMGroupMsgManager;
import com.qbao.newim.manager.NIMMsgManager;
import com.qbao.newim.manager.NIMSessionManager;
import com.qbao.newim.model.ChatGood;
import com.qbao.newim.model.ChatRedPacket;
import com.qbao.newim.model.FacePageInfo;
import com.qbao.newim.model.MenuMsgItem;
import com.qbao.newim.model.NIMLocationInfo;
import com.qbao.newim.model.NIM_Chat_ID;
import com.qbao.newim.model.message.BaseMessageModel;
import com.qbao.newim.model.message.GcMessageModel;
import com.qbao.newim.model.message.ScMessageModel;
import com.qbao.newim.permission.AndPermission;
import com.qbao.newim.permission.PermissionListener;
import com.qbao.newim.permission.Rationale;
import com.qbao.newim.permission.RationaleListener;
import com.qbao.newim.processor.GlobalProcessor;
import com.qbao.newim.processor.SysProcessor;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.BaseUtil;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.FaceUtil;
import com.qbao.newim.util.IDataObserver;
import com.qbao.newim.util.KPSwitchConflictUtil;
import com.qbao.newim.util.KeyboardUtil;
import com.qbao.newim.util.Logger;
import com.qbao.newim.util.NIMStartActivityUtil;
import com.qbao.newim.util.ScreenUtils;
import com.qbao.newim.util.SharedPreferenceUtil;
import com.qbao.newim.util.ShowUtils;
import com.qbao.newim.views.AudioRecordButton;
import com.qbao.newim.views.ChatAudioView;
import com.qbao.newim.views.FaceEditText;
import com.qbao.newim.views.KPSwitchPanelLinearLayout;
import com.qbao.newim.views.OnPullListener;
import com.qbao.newim.views.SwipeRefreshLayout;
import com.qbao.newim.views.imgpicker.NIM_PhotoPickerAct;
import com.qbao.newim.views.imgpicker.NIM_ToolbarAct;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenjian on 2017/9/26.
 */

public abstract class NIMChatActivity extends NIM_ToolbarAct implements AudioRecordManager.OnAudioRecordCompletionListener,
        AudioPlayManager.OnAudioPlayListener, DownloadObserver, SensorEventListener, IDataObserver, OnPullListener {
    private static final String TAG = NIMChatActivity.class.getSimpleName();

    /**
     * 编辑框
     */
    private FaceEditText editText;
    private TextView tvSend;
    public ImageView ivSetting;
    private RecyclerView recyclerView;
    public TextView tvName;

    /**
     * 底部表情按钮添加
     */
    private ImageButton btnAddFace;

    /**
     * 底部语音和键盘转换按钮
     */
    private ImageButton btnOpenKeyBo;

    /**
     * 底部多媒体添加按钮
     */
    private ImageButton btnAddMuti;

    /**
     * 表情面板和多媒体面板
     */
    private View mSubPanel1, mSubPanel2;

    private ArrayList<BaseMessageModel> mLists = new ArrayList<>();
    private ChatAdapter mAdapter;
    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView bottom_recycler;

    /**
     * 当前语音录制管理器
     */
    private AudioRecordManager mAudioRecordManager;

    /**
     * 所有面板布局
     */
    private KPSwitchPanelLinearLayout vBottomExpandLayout;

    /**
     * 语音录制布局，即按住说话按钮
     */
    private AudioRecordButton btnAudioRecord;

    private LinearLayoutManager layoutManager;
    private HeadsetReceiver receiver;

    /**
     * 表情包
     */
    private ArrayList<GridView> mFaceLists;
    private ArrayList<FacePageInfo> mFacePageInfo = new ArrayList<>();
    private ViewPager mFaceViewPager;
    private LinearLayout mFacePagerIndicator;
    private ImageView chatFaceTab;
    private ImageView chatQbFaceTab;

    private InputLengthFilter mChatBodyLengthFilter;

    private BottomMenuAdapter bottom_adapter;
    private TextView audioModeSpeaker;
    private BaseMessageModel mDownloadingAudioMsg = null;
    private DownloadManager mDownloadManager;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private ImageView ivEar;
    private PowerManager localPowerManager;
    private PowerManager.WakeLock localWakeLock;
    private AudioPlayManager audioPlayManager;

    private int chat_type;
    private boolean hasGetKeyBordHeight;
    private ProgressBar progressBar;

    protected double lat;
    protected double lon;
    protected String address;

    //底部菜单项；
    protected ArrayList<MenuMsgItem> mMenuMsgItems = new ArrayList<>();
    protected int MenuCount = 6;
    private int[] mMenuIcons = {
            R.mipmap.nim_app_panel_pic_icon,
            R.mipmap.nim_app_panel_camear_icon,
            R.mipmap.nim_app_panel_location_icon,
            R.mipmap.nim_app_panel_friendcard_icon,
            R.mipmap.nim_app_panel_red_packet_icon,
            R.mipmap.nim_app_panel_favorite_icon};
    private int[] mMenuStrs = {
            R.string.nim_chat_picture,
            R.string.nim_chat_camera,
            R.string.nim_chat_position,
            R.string.nim_chat_card,
            R.string.nim_chat_red_packet,
            R.string.nim_chat_favorite};


    //正在输入的标识位（标识已经发送了composing消息，但还未发paused消息）
    private boolean writingSendedFlag = false;
    //最近一次发送“正在输入”消息的时间
    private long latestSendComposingTime = 0;
    /**
     * 正在输入间隔时间
     */
    public static final int COMPOSING_PAUSED_SPAN = 10 * 1000;

    // 语音自动播放滚动到指定位置
    private static final int SCROLL_TO_POSITION1 = 9999;
    private static final int UPDATE_FILE_PROGRESS = 106;

    public Transferee transferee;

    private static final int AUDIO_MODE_SPEAKER_HIDE = 1019;
    private static final int SEND_COMPOSING = 1008;
    private static final int REQUEST_CODE_CHOOSE_PHOTO = 102;
    private static final int REQUEST_CODE_LOCATION = 104;
    private static final int REQUEST_CODE_CARD = 105;
    private static final int LOAD_MORE_DATA = 107;

    protected static final int REQUEST_CODE_CHAT_SETTING = 103;
    public static final int REQUEST_CODE_DELETE_FRIEND = 110;

    private static final int REQUEST_CODE_AUDIO_PERMISSION = 94;
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 95;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 96;

    /**
     * 每次加载的聊天记录条数
     */
    public static final int CHAT_PAGE_SIZE = 20;

    protected Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SCROLL_TO_POSITION1:        // 语音自动播放到下一个，滚动聊天UI
                    mHandler.removeMessages(SCROLL_TO_POSITION1);
                    layoutManager.scrollToPositionWithOffset(msg.arg1, msg.arg2);
                    break;
                case AUDIO_MODE_SPEAKER_HIDE:   // 语音扬声器模式隐藏
                    if (audioModeSpeaker.getVisibility() != View.GONE) {
                        audioModeSpeaker.setVisibility(View.GONE);
                    }
                    break;
                case SEND_COMPOSING:            // 发送“正在输入...”消息
                    if (!writingSendedFlag) {
                        writingSendedFlag = true;
                        latestSendComposingTime = System.currentTimeMillis();
//                            sendComposingOrPaused(ChatResponse.INPUT_STATUS_COMPOSING);
                    } else if (System.currentTimeMillis() - latestSendComposingTime >= COMPOSING_PAUSED_SPAN) {
                        latestSendComposingTime = System.currentTimeMillis();
//                            sendComposingOrPaused(ChatResponse.INPUT_STATUS_COMPOSING);
                    }
                    break;
                case UPDATE_FILE_PROGRESS:
                    int position = msg.arg1;
                    int progress = msg.arg2;
                    View update_view = layoutManager.findViewByPosition(position);
                    if (update_view != null) {
                        TextView txt_progress = (TextView) update_view.findViewById(R.id.chat_tv_progress);
                        if (txt_progress != null) {
                            if (progress >= 99) {
                                if (txt_progress.getVisibility() == View.VISIBLE)
                                    txt_progress.setVisibility(View.GONE);
                            } else {
                                txt_progress.setText(progress + "%");
                            }
                        }
                    }
                    break;
                case LOAD_MORE_DATA:
                    ArrayList<BaseMessageModel> next_list = getData();
                    if(next_list != null && next_list.size() > 0)
                    {
                        mLists.addAll(0, next_list);
                        mAdapter.notifyDataSetChanged();
                        layoutManager.scrollToPositionWithOffset(next_list.size() - 1, 0);

                        progressBar.setVisibility(View.GONE);
                        mRefreshLayout.setRefreshing(false);
                    }
                    break;
            }

            return false;
        }
    });

    public abstract int getChatType();
    public abstract ArrayList<BaseMessageModel> initData();
    public abstract ArrayList<BaseMessageModel> getData();
    public abstract void sendMessage(BaseMessageModel msg);

    @Override
    protected void initView(Bundle savedInstanceState) {
        transferee = Transferee.getDefault(this);
        setContentView(R.layout.nim_activity_main);
        ScreenUtils.initStatusBar(this);
        chat_type = getChatType();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        editText = (FaceEditText) findViewById(R.id.chat_edittext);
        tvSend = (TextView) findViewById(R.id.chat_btn_send);

        btnAddFace = (ImageButton) findViewById(R.id.chat_multimedia_face);
        btnAddFace.setSelected(false);
        btnAddMuti = (ImageButton) findViewById(R.id.chat_add_multimedia);
        btnOpenKeyBo = (ImageButton) findViewById(R.id.chat_add_audio);
        vBottomExpandLayout = (KPSwitchPanelLinearLayout) findViewById(R.id.chat_bottom_layout_big);

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        progressBar = (ProgressBar) findViewById(R.id.pull_to_refresh_progress);
        recyclerView = (RecyclerView) findViewById(R.id.chat_list);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        bottom_recycler = (RecyclerView) findViewById(R.id.chat_bottom_multimedia_list);
        bottom_recycler.setLayoutManager(new GridLayoutManager(this, 3));
        bottom_recycler.setHasFixedSize(true);
        mSubPanel1 = vBottomExpandLayout.findViewById(R.id.sub_panel_1);
        mSubPanel2 = vBottomExpandLayout.findViewById(R.id.sub_panel_2);

        btnAudioRecord = (AudioRecordButton) findViewById(R.id.chat_record_audio);
        btnAudioRecord.setChatActivity(this);

        mFaceViewPager = (ViewPager) findViewById(R.id.face_viewpager);
        mFacePagerIndicator = (LinearLayout) findViewById(R.id.face_pager_indicator);

        chatFaceTab = (ImageView) findViewById(R.id.chat_face_tab);
        chatFaceTab.setSelected(true);
        chatQbFaceTab = (ImageView) findViewById(R.id.chat_qbface_tab);
        audioModeSpeaker = (TextView) findViewById(R.id.chat_audio_mode_speaker);

        mAudioRecordManager = new AudioRecordManager(this);
        mAudioRecordManager.setOnAudioRecordCompletionListener(this);
    }

    @Override
    protected void setListener() {
        initBottomMenu();
        tvSend.setOnClickListener(this);
        editText.addTextChangedListener(mEditTextWatcher);
        mRefreshLayout.setOnPullListener(this);

        //键盘的状态监听，指弹出和隐藏
        KeyboardUtil.attach(this, vBottomExpandLayout,
                new KeyboardUtil.OnKeyboardShowingListener() {
                    @Override
                    public void onKeyboardShowing(boolean isShowing) {
                        scrollChatViewToBottom();
                        // 检查当前软键盘是否以及弹出过，如果是，则重新绘制面板
                        if (!hasGetKeyBordHeight) {
                            bottom_adapter.notifyDataSetChanged();
                            checkKeyBoard();
                        }
                    }
                });
        // 软键盘和面板的互相转换
        KPSwitchConflictUtil.attach(vBottomExpandLayout, editText,
                new KPSwitchConflictUtil.SwitchClickListener() {
                    @Override
                    public void onClickSwitch(boolean switchToPanel) {
                        if (switchToPanel) {
                            editText.clearFocus();
                        } else {
                            editText.requestFocus();
                        }
                        scrollToBottom();
                    }
                },
                new KPSwitchConflictUtil.SubPanelAndTrigger(mSubPanel1, btnAddMuti, 0),
                new KPSwitchConflictUtil.SubPanelAndTrigger(mSubPanel2, btnAddFace, 0),
                new KPSwitchConflictUtil.SubPanelAndTrigger(btnAudioRecord, btnOpenKeyBo, 1));

        /**
         * 触摸聊天界面，隐藏软键盘
         */
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    KPSwitchConflictUtil.hidePanelAndKeyboard(vBottomExpandLayout);
                }

                return false;
            }
        });
        mFaceViewPager.addOnPageChangeListener(new FaceOnPageChangeListener());
        chatFaceTab.setOnClickListener(this);
        chatQbFaceTab.setOnClickListener(this);

        /**
         * 多媒体面板
         */
        bottom_adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                KPSwitchConflictUtil.hidePanelAndKeyboard(vBottomExpandLayout);
                if (position >= 0 && position < mMenuMsgItems.size()) {
                    MenuMsgItem item = mMenuMsgItems.get(position);
                    if (item.Item_Name_Id == R.string.nim_chat_picture) {
                        startActivityForResult(NIM_PhotoPickerAct.newIntent(NIMChatActivity.this, null, 9, null, false),
                                REQUEST_CODE_CHOOSE_PHOTO);
                    } else if (item.Item_Name_Id == R.string.nim_chat_camera) {
                        requestCameraPermission();
                    } else if (item.Item_Name_Id == R.string.nim_chat_position) {
                        requestLocationPermission(lat, lon, address);
                    } else if (item.Item_Name_Id == R.string.nim_chat_card) {
                        NIMStartActivityUtil.startToCardActivity(NIMChatActivity.this, null, REQUEST_CODE_CARD);
                    } else if (item.Item_Name_Id == R.string.nim_chat_red_packet) {
                        ChatRedPacket redPacket = new ChatRedPacket();
                        redPacket.id = 111;
                        redPacket.desc = "大红包";
                        redPacket.type = 2;
                        sendRedMessage(new Gson().toJson(redPacket));
                    } else if (item.Item_Name_Id == R.string.nim_chat_favorite) {
                        ChatGood good = new ChatGood();
                        good.desc = "xxx";
                        good.img_url = "";
                        good.title = "sss";
                        sendStoreMessage(new Gson().toJson(good));
                    }
                }
            }
        });

    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        mLists = initData();
        mAdapter = new ChatAdapter(this, mLists);
        recyclerView.setAdapter(mAdapter);

        checkKeyBoard();
        scrollChatViewToBottom();

        audioPlayManager = AudioPlayManager.getManager();
        audioPlayManager.setOnAudioPlayListener(this);

        mDownloadManager = DownloadManager.getInstance();
        mDownloadManager.registerDownloadObserver(this);

        mChatBodyLengthFilter = new InputLengthFilter(NIMChatActivity.this, getResources().getInteger(R.integer.chat_body_max_length));
        editText.setFilters(new InputFilter[]{mChatBodyLengthFilter});

        DataObserver.Register(this);

        // 显示草稿内容
//        showDraft();
    }

    public void setShowNickname(boolean show){
        mAdapter.setIs_show_nick(show);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.nim_toolbar_title, menu);
        MenuItem menuItem = menu.findItem(R.id.item_toolbar);
        View actionView = menuItem.getActionView();

        tvName = (TextView) actionView.findViewById(R.id.title_txt);

        RelativeLayout right_layout = (RelativeLayout)actionView.findViewById(R.id.title_right_img_layout);
        right_layout.setVisibility(View.VISIBLE);
        actionView.findViewById(R.id.friend_img).setVisibility(View.GONE);
        ivEar = (ImageView) actionView.findViewById(R.id.title_txt_img);
        ivEar.setVisibility(audioPlayManager.isEarMode() ? View.VISIBLE : View.GONE);

        ivSetting = (ImageView)actionView.findViewById(R.id.add_chat_friend);

        actionView.findViewById(R.id.title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //		saveDraft();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    // 键盘布局强行改变RecycleView
    private void scrollChatViewToBottom() {
        final int pos = recyclerView.getAdapter().getItemCount() - 1;
        View target = layoutManager.findViewByPosition(pos);

        // 如果存在直接滚动到底部
        if (target != null) {
            layoutManager.scrollToPositionWithOffset(pos,
                    recyclerView.getMeasuredHeight() - target.getMeasuredHeight());
        } else {
            layoutManager.scrollToPositionWithOffset(pos, 0);
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    final View target = layoutManager.findViewByPosition(pos);
                    if (target != null) {
                        layoutManager.scrollToPositionWithOffset(pos,
                                recyclerView.getMeasuredHeight() - target.getMeasuredHeight());
                    }
                }
            });

        }
    }

    // 多面板覆盖RecycleView改变布局
    private void scrollToBottom() {
        final int pos = recyclerView.getAdapter().getItemCount() - 1;
        View target = layoutManager.findViewByPosition(pos);
        if (target != null && mSubPanel1.getHeight() > 0) {
            layoutManager.scrollToPositionWithOffset(pos, recyclerView.getMeasuredHeight() -
                    target.getMeasuredHeight() - mSubPanel1.getMeasuredHeight());
        } else {
            layoutManager.scrollToPositionWithOffset(pos, 0);
            mSubPanel1.post(new Runnable() {
                @Override
                public void run() {
                    final View target = layoutManager.findViewByPosition(pos);
                    if (target != null) {
                        layoutManager.scrollToPositionWithOffset(pos, recyclerView.getMeasuredHeight() -
                                target.getMeasuredHeight() - mSubPanel1.getMeasuredHeight());
                    }
                }
            });
        }
    }

    private void checkKeyBoard() {
        int default_keyH = getResources().
                getDimensionPixelSize(R.dimen.chat_bottom_min_height);
        int real_keyH = SharedPreferenceUtil.getKeyBoardHeight(default_keyH);
        if (real_keyH != default_keyH) {
            hasGetKeyBordHeight = true;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        /**
         * 返回键先隐藏软键盘和面板
         */
        if (event.getAction() == KeyEvent.ACTION_UP &&
                event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (vBottomExpandLayout.getVisibility() == View.VISIBLE) {
                KPSwitchConflictUtil.hidePanelAndKeyboard(vBottomExpandLayout);
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK ) {
            switch (requestCode) {
                case REQUEST_CODE_CHOOSE_PHOTO:
                    ArrayList<String> imgList = NIM_PhotoPickerAct.getSelectedImages(data);
                    for (int i = 0; i < imgList.size(); i++) {
                        sendImageMessage(imgList.get(i));
                    }
                    break;
                case REQUEST_CODE_CHAT_SETTING:
                    if (data != null) {
                        boolean is_delete = data.getBooleanExtra("delete", false);
                        if (is_delete) {
                            mLists.clear();
                            mAdapter.notifyDataSetChanged();
                        }

                        boolean delete_user = data.getBooleanExtra("delete_user", false);
                        if (delete_user) {
                            finish();
                        }
                    }
                    break;
                case REQUEST_CODE_LOCATION:
                    if (data != null) {
                        double lat = data.getDoubleExtra("lat", 0);
                        double lon = data.getDoubleExtra("lon", 0);
                        String address = data.getStringExtra("address");
                        sendLocationMessage(lat, lon, address);
                        break;
                    }
                case REQUEST_CODE_CARD:
                    if (data != null) {
                        String card_info = data.getStringExtra("card_info");
                        sendCardMessage(card_info);
                    }
                    break;
                case REQUEST_CODE_DELETE_FRIEND:
                    finish();
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        transferee = Transferee.getDefault(this);

        localPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        if (receiver == null) {
            receiver = new HeadsetReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_HEADSET_PLUG);
            filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
            registerReceiver(receiver, filter);
        }

        SysProcessor processor = GlobalProcessor.getInstance().getSys_processor();
        processor.sendTimeSyncRQ();
    }

    @Override
    protected void onPause() {
        Logger.error("AUDIO", "onPause");
        if (isScreenOn()) {
            stopPlayAudio();
            KeyboardUtil.hideKeyboard(getCurrentFocus());
        }

        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.error("AUDIO", "onStop");
    }

    @Override
    public void finish() {
        // 清除当前聊天会话ID，存储所有未读消息
        NIMSessionManager.getInstance().SetCurSession(0);
        super.finish();
    }

    // 判断屏幕是否亮着
    private boolean isScreenOn() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return localPowerManager.isInteractive();
        } else {
            return localPowerManager.isScreenOn();
        }
    }

    /**
     * 多媒体面板初始化
     */
    private void initBottomMenuList() {
        mMenuMsgItems.clear();
        for (int i = MenuCount - 1; i >= 0; i--) {
            MenuMsgItem item = new MenuMsgItem(0, 0);
            item.Item_Icon_Id = mMenuIcons[i];
            item.Item_Name_Id = mMenuStrs[i];
            mMenuMsgItems.add(0, item);
        }
        bottom_adapter = null;
        bottom_adapter = new BottomMenuAdapter(mMenuMsgItems);
        bottom_recycler.setAdapter(bottom_adapter);
    }

    private void initBottomMenu() {
        initBottomMenuList();
        initFace();
    }

    /**
     * 表情包面板初始化
     */
    private void initFace() {
        if (mFaceLists != null) {
            return;
        }
        mFaceLists = new ArrayList<>();
        mFacePageInfo.clear();

        String[] faceCodes = getResources().getStringArray(R.array.nim_face_code);
        String[] faceNames = getResources().getStringArray(R.array.nim_face_name);

        int gridViewCount = faceCodes.length / ChatFaceAdapter.PAGER_SIZE;
        if (faceCodes.length % ChatFaceAdapter.PAGER_SIZE > 0) {
            gridViewCount++;
        }

        for (int i = 0; i < gridViewCount; i++) {
            GridView gridView = (GridView) LayoutInflater.from(this).inflate(R.layout.nim_chat_face_list, null);
            gridView.setOnItemClickListener(mOnFaceGridItemClickListener);
            ChatFaceAdapter faceAdapter = new ChatFaceAdapter(NIMChatActivity.this, faceNames, faceCodes);
            faceAdapter.setPageIndex(i);
            gridView.setAdapter(faceAdapter);
            mFaceLists.add(gridView);

            FacePageInfo facePageInfo = new FacePageInfo();
            facePageInfo.totalPageCount = gridViewCount;
            facePageInfo.innerIndex = i;
            facePageInfo.startIndex = 0;
            facePageInfo.endIndex = 1;
            mFacePageInfo.add(facePageInfo);
        }

        String[] qbfaceCodes = getResources().getStringArray(R.array.nim_qb_face_code);
        String[] qbfaceNames = getResources().getStringArray(R.array.nim_qb_face_name);
        gridViewCount = qbfaceCodes.length / QBFaceAdapter.PAGER_SIZE;
        if (qbfaceCodes.length % QBFaceAdapter.PAGER_SIZE > 0) {
            gridViewCount++;
        }
        for (int i = 0; i < gridViewCount; i++) {
            GridView gridView = (GridView) LayoutInflater.from(this).inflate(R.layout.nim_chat_qbface_list, null);
            gridView.setOnItemClickListener(mOnQBFaceGridItemClickListener);
            QBFaceAdapter qbFaceAdapter = new QBFaceAdapter(NIMChatActivity.this, qbfaceNames, qbfaceCodes);
            qbFaceAdapter.setPageIndex(i);
            gridView.setAdapter(qbFaceAdapter);
            mFaceLists.add(gridView);

            FacePageInfo facePageInfo = new FacePageInfo();
            facePageInfo.totalPageCount = gridViewCount;
            facePageInfo.innerIndex = i;
            facePageInfo.startIndex = 2;
            facePageInfo.endIndex = 3;
            mFacePageInfo.add(facePageInfo);
        }

        FaceViewPagerAdapter adapter = new FaceViewPagerAdapter();
        mFaceViewPager.setAdapter(adapter);

        final int currentPosition = mFaceViewPager.getCurrentItem();
        FacePageInfo facePageInfo = mFacePageInfo.get(currentPosition);
        updatePageIndicator(facePageInfo);
    }

    private void updatePageIndicator(FacePageInfo facePageInfo) {
        int childCount = mFacePagerIndicator.getChildCount();
        if (childCount == facePageInfo.totalPageCount) {
            for (int i = 0; i < facePageInfo.totalPageCount; i++) {
                View view = mFacePagerIndicator.getChildAt(i);
                view.setSelected(i == facePageInfo.innerIndex);
            }
        } else {
            mFacePagerIndicator.removeAllViews();
            for (int i = 0; i < facePageInfo.totalPageCount; i++) {
                ImageView view = new ImageView(this);
                view.setImageResource(R.drawable.nim_face_page_indicator);
                view.setPadding(10, 0, 10, 0);
                view.setSelected(i == facePageInfo.innerIndex);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.weight = 20;
                params.height = 20;
                mFacePagerIndicator.addView(view, params);
            }
        }
    }

    protected boolean UpdateViewByMessageID(BaseMessageModel s_model)
    {
        for(int index = mAdapter.getItemCount() - 1; index >= 0; index--)
        {
            BaseMessageModel m_model = mAdapter.getItem(index);
            if(m_model.message_id == s_model.message_id)
            {
                mAdapter.updateItemView(index);
                return true;
            }
        }

        return false;
    }

    @Override
    public void onDownloadChanged(IDownloadManager manager, DownloadObject downloadObject) {
        if (downloadObject instanceof ChatAudioDownloadJob) {
            ChatAudioDownloadJob audioDownloadJob = (ChatAudioDownloadJob) downloadObject;
            if (audioDownloadJob.getDownloadStatus() == MsgConstDef.MSG_STATUS.DOWNLOAD_SUCCESS) {
                BaseMessageModel msgItem = audioDownloadJob.getChatAudioMsg();
                UpdateViewByMessageID(msgItem);
                if (mDownloadingAudioMsg == msgItem) {
                    mDownloadingAudioMsg = null;
                    playAudio(msgItem, audioPlayManager.getLastPlayPosition());
                }

            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // 耳机模式不处理
        if (audioPlayManager.isHeadHetMode()){
            return;
        }

        if (ChatAudioView.playingEntry == null) {
            hideAudioTipView();
            Logger.error("AUDIO", "audio is null");
            return;
        }

        float range = event.values[0];
        if (range == mSensor.getMaximumRange()){
            Logger.error("AUDIO", "远离距离感应器,传感器的值:" + range);
        } else {
            Logger.error("AUDIO", "靠近距离感应器,传感器的值:" + range);
        }

        if (range == mSensor.getMaximumRange()) {
            changeAdapterType(true);
            setScreenOn();
            Logger.error("AUDIO", "audio is speak");
        } else {
            changeAdapterType(false);
            setScreenOff();
            Logger.error("AUDIO", "audio is ear");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void setScreenOff(){
        if (localWakeLock == null){
            localWakeLock = localPowerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, TAG);
        }
        localWakeLock.acquire();
    }
    private void setScreenOn(){
        if (localWakeLock != null){
            localWakeLock.setReferenceCounted(false);
            localWakeLock.release();
            localWakeLock = null;
        }
    }

    /**
     * 听筒扩音切换
     * @param on 表示扩音，否则表示听筒
     */
    private void changeAdapterType(boolean on) {
        if (on) {
            // 自定义听筒模式
            if (audioPlayManager.isCustomEarMode()) {
                return;
            }
            //开启语音扬声器模式
            audioPlayManager.changeToSpeakerMode();
            audioModeSpeaker.setVisibility(View.VISIBLE);
            audioModeSpeaker.setText(R.string.nim_audio_mode_speaker);
            mHandler.removeMessages(AUDIO_MODE_SPEAKER_HIDE);
            mHandler.sendEmptyMessageDelayed(AUDIO_MODE_SPEAKER_HIDE, 4000);
        } else {
            //耳麦听筒
            audioPlayManager.changeToEarpieceMode();
            mHandler.sendEmptyMessage(AUDIO_MODE_SPEAKER_HIDE);
        }
    }

    private TextWatcher mEditTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            int selectionStart = editText.getSelectionStart();
            int selectionEnd = editText.getSelectionEnd();
            if (s.length() > GlobalVariable.SEND_MESSAGE_LENGTH) {
                showToastStr(getString(R.string.nim_message_txt_count, GlobalVariable.SEND_MESSAGE_LENGTH));
                s.delete(selectionStart - 1, selectionEnd);
            }

            String content = editText.getText().toString();
            if ("".equals(content)) {
                if (tvSend.getVisibility() == View.VISIBLE) {
                    btnAddMuti.setVisibility(View.VISIBLE);
                    tvSend.setVisibility(View.GONE);
                }
            } else {
                if (tvSend.getVisibility() == View.GONE) {
                    btnAddMuti.setVisibility(View.GONE);
                    tvSend.setVisibility(View.VISIBLE);
                }

                /**
                 * 发送方：1.首次输入时发送“正在输入...”消息
                 *         2.afterTextChanged中检测是否10s已经过去，如果过去了就再次发送“正在输入...”消息
                 * 接收方：1.收到“正在输入...”消息消息时，显示“正在输入...”，同时启动10s定时器，10s后自动隐藏“正在输入...”
                 *         2.收到非“正在输入...”消息时，隐藏“正在输入...”消息
                 *
                 * 不用发送“停止输入”消息，由接收方自己判断，防止网络不好等原因，接收方一直收不到“停止输入”消息
                 */
                //如果输入框内容有变化,发送”正在输入...“消息
                mHandler.sendEmptyMessage(SEND_COMPOSING);
            }
        }
    };


    @Override
    public void OnChange(int param1, Object param2, Object param3) {
        switch (param1) {
            case DataConstDef.EVENT_MESSAGE_TIME_OUT:         // 超时
                Logger.error(TAG, "EVENT_MESSAGE_TIME_OUT");
                NIM_Chat_ID out_chat = (NIM_Chat_ID) param2;
                //如果不是当前会话的不用管
                if(out_chat.session_id == NIMSessionManager.getInstance().GetCurSession())
                    handlerTimeOutMsg(out_chat);
                break;
            case DataConstDef.EVENT_NET_ERROR:                // 网络错误
                Logger.error(TAG, "EVENT_NET_ERROR");
                if (is_active) {
                    int error_code = BaseUtil.MakeErrorResult((int) param2);
                    String error_msg = ErrorDetail.GetErrorDetail(error_code);
                    showToastStr(error_msg);
                }
                break;
            case DataConstDef.EVENT_VOICE_MODE:                // 收到语音播放模式切换
                boolean cur_mode = (boolean)param2;
                showToastStr(cur_mode ? "已切换为听筒模式" : "已切换为扬声器模式");
                ivEar.setVisibility(cur_mode ? View.VISIBLE : View.GONE);
                break;
        }

    }

    // 超时处理
    private void handlerTimeOutMsg(NIM_Chat_ID out_msg) {
        updateSendStatusMsgById(out_msg.message_id, MsgConstDef.MSG_STATUS.SEND_FAILED);
    }

    protected boolean updateSendStatusMsgById(long message_id, short status) {
        for(int index = mAdapter.getItemCount() - 1; index >= 0; index--)
        {
            BaseMessageModel m_model = mAdapter.getItem(index);
            if(m_model.message_id == message_id)
            {
                m_model.msg_status = status;
                mLists.set(index, m_model);
                mAdapter.updateItemView(index);
                return true;
            }
        }

        return false;
    }

    protected boolean updateFileStatusMsgById(long message_id, short status) {
        for(int index = mAdapter.getItemCount() - 1; index >= 0; index--)
        {
            BaseMessageModel m_model = mAdapter.getItem(index);
            if(m_model.message_id == message_id)
            {
                m_model.msg_status = status;
                mLists.set(index, m_model);
                if (recyclerView.isComputingLayout() == false)
                    mAdapter.updateItemView(index);

                return true;
            }
        }

        return false;
    }

    protected boolean updateProgressMsgById(BaseMessageModel message) {
        for(int index = mAdapter.getItemCount() - 1; index >= 0; index--)
        {
            BaseMessageModel m_model = mAdapter.getItem(index);
            if(m_model.message_id == message.message_id)
            {
                mLists.set(index, message);
                int pos = layoutManager.findFirstVisibleItemPosition();
                if (pos <= index) {
                    mHandler.sendMessage(mHandler.obtainMessage(UPDATE_FILE_PROGRESS, index, message.progress));
                }
                return true;
            }
        }

        return false;
    }

    private void handlerFailMsg(BaseMessageModel fail_msg) {
        updateSendStatusMsgById(fail_msg.message_id, MsgConstDef.MSG_STATUS.SEND_FAILED);
    }

    // 文件上传状态
    protected void handlerUploadStatus(BaseMessageModel upload_msg, boolean is_uploading) {
        if (upload_msg == null) {
            Logger.error(TAG, "EVENT_UPLOAD_STATUS upload_msg is null");
            return;
        }

        upload_msg.msg_status = is_uploading ? MsgConstDef.MSG_STATUS.SEND_SUCCESS : MsgConstDef.MSG_STATUS.SEND_FAILED;
        updateFileStatusMsgById(upload_msg.message_id, upload_msg.msg_status);
    }

    // 文件上传进度监听
    protected void handlerUploadProgress(BaseMessageModel upload_msg) {
        if (upload_msg == null) {
            Logger.error(TAG, "EVENT_UPLOAD_STATUS upload_msg is null");
            return;
        }
        updateProgressMsgById(upload_msg);
    }

    protected void addNewMsgToView(BaseMessageModel model) {
        mAdapter.addDataAtEnd(model);
        int distance_offset = recyclerView.computeVerticalScrollOffset();
        int distance_extent = recyclerView.computeVerticalScrollExtent();
        int distance_range = recyclerView.computeVerticalScrollRange();
        int mHeight = recyclerView.getHeight();
        if (distance_range - distance_extent - distance_offset < mHeight / 2) {
            scrollChatViewToBottom();
        }
    }

    protected void addNewMsgToView(BaseMessageModel model, int position) {
        if(position >= mLists.size())
        {
            mLists.add(model);
        }
        else
        {
            mLists.add(position, model);
        }

        mAdapter.notifyItemInserted(position);

        int distance_offset = recyclerView.computeVerticalScrollOffset();
        int distance_extent = recyclerView.computeVerticalScrollExtent();
        int distance_range = recyclerView.computeVerticalScrollRange();
        int mHeight = recyclerView.getHeight();
        if (distance_range - distance_extent - distance_offset < mHeight / 2)
        {
            scrollChatViewToBottom();
        }
    }

    protected void updateMsgToView(BaseMessageModel message, int position) {
        if (position >= mLists.size()) {
            return;
        }
        mLists.set(position, message);
        mAdapter.updateItemView(position);
    }

    protected void deleteMsgFromView(int position)
    {
        if(position < 0 || position >= mLists.size())
        {
            return;
        }

        if (position == 0 && mLists.size() == 1) {
            mLists.clear();
            mAdapter.notifyDataSetChanged();
        } else  {
            mAdapter.deleteItemView(position);
        }
    }

    @Override
    public void onPulling(View view) {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCanRefreshing(View view) {

    }

    @Override
    public void onRefreshing(View view) {
        mHandler.sendEmptyMessageDelayed(LOAD_MORE_DATA, 800);
    }


    public class FaceViewPagerAdapter extends PagerAdapter {

        public FaceViewPagerAdapter() {
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mFaceLists.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mFaceLists.get(position), 0);
            return mFaceLists.get(position);
        }

        @Override
        public int getCount() {
            return mFaceLists.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
    }

    /**
     * 表情包延迟发送
     */
    private AdapterView.OnItemClickListener mOnQBFaceGridItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            final String faceCode = (String) view.getTag();
            KPSwitchConflictUtil.hidePanelAndKeyboard(vBottomExpandLayout);
            sendSmileyMessage(faceCode);
        }
    };

    private AdapterView.OnItemClickListener mOnFaceGridItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            if (position == parent.getAdapter().getCount() - 1) {
                if (editText.getText().toString().length() > 0) {
                    final KeyEvent keyEventDown = new KeyEvent(
                            KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);
                    editText.onKeyDown(KeyEvent.KEYCODE_DEL, keyEventDown);
                }
                return;
            }

            final int start = editText.getSelectionStart();
            final Editable editable = editText.getText();

            final String faceCode = (String) view.getTag();

            final int maxlength = mChatBodyLengthFilter.getMaxLength();
            if ((editable.length() + faceCode.length()) <= maxlength) {
                CharSequence text = FaceUtil.getInstance().formatTextToFace(
                        faceCode, FaceUtil.FACE_TYPE.CHAT_EDITTEXT);

                editable.insert(start, text);
                editText.setText(editable);

                int index = start + faceCode.length();
                editText.setSelection(getSelectionIndex(index));
                editText.requestFocus();
            } else {
                ShowUtils.showToast(R.string.nim_chat_content_exceed_max_length);
            }
        }
    };

    public class FaceOnPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            boolean smallSelected = position <= 1;
            chatFaceTab.setSelected(smallSelected);
            chatQbFaceTab.setSelected(!smallSelected);

            FacePageInfo facePageInfo = mFacePageInfo.get(position);
            updatePageIndicator(facePageInfo);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }

    private int getSelectionIndex(int length) {
        int maxChatBodyLength = mChatBodyLengthFilter.getMaxLength();
        return length > maxChatBodyLength ? maxChatBodyLength : length;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.title_back) {
            onBackPressed();
        } else if (v.getId() == R.id.chat_btn_send) {
            String textContent = editText.getText().toString();
            if (TextUtils.isEmpty(textContent.trim())) {
                showToastStr(R.string.nim_chat_msg_body_empty);
                return;
            }

            if (TextUtils.isEmpty(textContent.replace(" ",""))) {
                showToastStr(R.string.nim_chat_msg_body_empty);
                return;
            }

            writingSendedFlag = false;
            sendTextMessage(textContent);
            editText.setText("");
        }else if (v.getId() == R.id.chat_face_tab) {
            chatFaceTab.setSelected(true);
            chatQbFaceTab.setSelected(false);
            mFaceViewPager.setCurrentItem(0);
        }else if (v.getId() == R.id.chat_qbface_tab) {
            chatFaceTab.setSelected(false);
            chatQbFaceTab.setSelected(true);
            mFaceViewPager.setCurrentItem(2);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消注册观察者模式
        transferee.destroy();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);// 注销传感器监听
        }
        if (receiver != null) {
            unregisterReceiver(receiver);
        }

        localPowerManager = null;
        DataObserver.Cancel(this);
    }

    /**
     * 停止录音播放
     */
    public void stopPlayAudio() {
        if (ChatAudioView.playingEntry != null) {
            ChatAudioView.playingEntry = null;
            if (audioPlayManager != null) {
                audioPlayManager.requestStop(ChatAudioView.playingEntry);
            }
            mAdapter.notifyItemChanged(audioPlayManager.getLastPlayPosition());
        }
    }

    public void startRecord() {
        // 按下录音键时判断是否有录音正在播放，如果有，则停止。
        audioPlayManager.requestStop(null);
        mAudioRecordManager.startRecord();
    }

    public void requestAudioPermission() {
        final String tip = getString(R.string.nim_permission_audio_fail);
        AndPermission.with(NIMChatActivity.this)
                .requestCode(REQUEST_CODE_AUDIO_PERMISSION)
                .callback(permissionListener)
                .failTips(tip)
                .permission(Manifest.permission.RECORD_AUDIO)
                // rationale作用是：用户拒绝一次权限，再次申请时先征求用户同意，再打开授权对话框，避免用户勾选不再提示。
                .rationale(new RationaleListener() {
                    @Override
                    public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
                        AndPermission.rationaleDialog(NIMChatActivity.this, tip, rationale).show();
                    }
                }).start();
    }

    private void requestCameraPermission() {
        final String tip = getString(R.string.nim_permission_camera_fail);
        AndPermission.with(NIMChatActivity.this)
                .requestCode(REQUEST_CODE_CAMERA_PERMISSION)
                .failTips(tip)
                .permission(Manifest.permission.CAMERA)
                .callback(permissionListener)
                // rationale作用是：用户拒绝一次权限，再次申请时先征求用户同意，再打开授权对话框，避免用户勾选不再提示。
                .rationale(new RationaleListener() {
                    @Override
                    public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
                        AndPermission.rationaleDialog(NIMChatActivity.this, tip, rationale).show();
                    }
                }).start();
    }

    public void requestLocationPermission(double lat, double lon, String address) {
        this.lat = lat;
        this.lon = lon;
        this.address = address;
        final String tip = getString(R.string.nim_permission_location_fail);
        AndPermission.with(NIMChatActivity.this)
                .requestCode(REQUEST_CODE_LOCATION_PERMISSION)
                .callback(permissionListener)
                .failTips(tip)
                .permission(Manifest.permission.ACCESS_FINE_LOCATION)
                // rationale作用是：用户拒绝一次权限，再次申请时先征求用户同意，再打开授权对话框，避免用户勾选不再提示。
                .rationale(new RationaleListener() {
                    @Override
                    public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
                        AndPermission.rationaleDialog(NIMChatActivity.this, tip, rationale).show();
                    }
                }).start();
    }

    private PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
            switch (requestCode) {
                case REQUEST_CODE_AUDIO_PERMISSION:
                    mAudioRecordManager = new AudioRecordManager(NIMChatActivity.this);
                    mAudioRecordManager.setOnAudioRecordCompletionListener(NIMChatActivity.this);
                    break;
                case REQUEST_CODE_CAMERA_PERMISSION:
                    File takePhotoDir = new File(Environment.getExternalStorageDirectory(), Constants.BASE_PATH + "/photo");
                    startActivityForResult(NIM_PhotoPickerAct.newIntent(NIMChatActivity.this, takePhotoDir, 1, null, false),
                            REQUEST_CODE_CHOOSE_PHOTO);
                    break;
                case REQUEST_CODE_LOCATION_PERMISSION:
                    if (lat > 0 && lon > 0) {
                        NIMStartActivityUtil.startToLocationActivity(NIMChatActivity.this, lat, lon, address);
                        lat = 0;
                        lon = 0;
                    } else {
                        NIMStartActivityUtil.startToLocationActivityForResult(NIMChatActivity.this, REQUEST_CODE_LOCATION);
                    }
                    break;
            }

        }

        @Override
        public void onCancel(int requestCode, Context context) {

        }
    };


    public AudioRecordManager getAudioRecordManager() {
        return mAudioRecordManager;
    }

    @Override
    public void onAudioRecordCompletion(String audioPath, int recordDuration) {
        if (recordDuration < 1 || TextUtils.isEmpty(audioPath)) {
            return;
        }
        sendAudioMessage(recordDuration, audioPath);
    }

    /**
     * 发送文字内容
     *
     * @param textContent
     */
    protected void sendTextMessage(String textContent) {
        BaseMessageModel chatMsg = ChatMsgBuildManager.buildTextMsg(chat_type, textContent);
        sendMessage(chatMsg);
    }

    /**
     * 发送语音消息
     *
     * @param audioDuration
     * @param audioPath
     */
    protected void sendAudioMessage(int audioDuration, String audioPath) {
        BaseMessageModel chatMsg = ChatMsgBuildManager.buildAudioMsg(chat_type, audioDuration, audioPath);
        sendMessage(chatMsg);
    }

    /**
     * 发送位置消息
     */
    protected void sendLocationMessage(double lat, double lon, String address) {
        NIMLocationInfo info = new NIMLocationInfo();
        info.setAddress(address);
        info.setLat(lat);
        info.setLng(lon);
        BaseMessageModel chatMsg = ChatMsgBuildManager.buildLocationMsg(chat_type, new Gson().toJson(info));
        sendMessage(chatMsg);
    }

    /**
     * 发送红包
     */
    protected void sendRedMessage(String desc) {
        BaseMessageModel chatMsg = ChatMsgBuildManager.buildRedMsg(chat_type, desc);
        sendMessage(chatMsg);
    }

    /**
     * 发送收藏
     */
    protected void sendStoreMessage(String desc) {
        BaseMessageModel chatMsg = ChatMsgBuildManager.buildLinkMsg(chat_type, desc);
        sendMessage(chatMsg);
    }

    /**
     * 发送名片消息
     */
    protected void sendCardMessage(String json) {
        BaseMessageModel chatMsg = ChatMsgBuildManager.buildCardMsg(chat_type, json);
        sendMessage(chatMsg);
    }

    protected void sendSmileyMessage(String text) {
        BaseMessageModel chatMsg = ChatMsgBuildManager.buildGifMsg(chat_type, text);
        sendMessage(chatMsg);
    }

    private void sendImageMessage(String picPath) {
        BaseMessageModel chatMsg = ChatMsgBuildManager.buildPicMsg(chat_type, picPath);
        sendMessage(chatMsg);
    }

    protected BaseMessageModel getLastChatMsg() {
        if (mAdapter != null) {
            return mAdapter.getLastMsg();
        }

        return null;
    }

    @Override
    public void onPlayStart(BaseMessageModel Item) {
        if(audioPlayManager.isEarMode())
        {
            audioModeSpeaker.setVisibility(View.VISIBLE);
            audioModeSpeaker.setText(R.string.nim_audio_mode_ear);
            mHandler.removeMessages(AUDIO_MODE_SPEAKER_HIDE);
            mHandler.sendEmptyMessageDelayed(AUDIO_MODE_SPEAKER_HIDE, 4000);
        }
    }

    @Override
    public void onPlayStop(int position) {
        mAdapter.notifyItemChanged(position);
    }

    @Override
    public void onPlayCompletion(BaseMessageModel Item, int position, boolean autoNext) {
        if (ChatAudioView.playingEntry != null) {
            ChatAudioView.playingEntry = null;
            mAdapter.notifyItemChanged(position);
        }

        /************自动播放系一条 start************/
        BaseMessageModel nextItem = null;
        int i = position + 1;
        if (autoNext) {
            for (; i < mAdapter.getItemCount(); i++) {
                BaseMessageModel item = mAdapter.getItem(i);
                boolean unread_voice_msg = (item.msg_status != MsgConstDef.MSG_STATUS.PLAYED &&
                        item.m_type == MsgConstDef.MSG_M_TYPE.VOICE);
                if (!item.is_self && unread_voice_msg) {
                    nextItem = item;
                    break;
                }
            }
        }

        if (nextItem == null) {
            hideAudioTipView();
        } else {
            mAdapter.notifyItemChanged(i);
        }

        playAudio(nextItem, i);
    }

    private void playAudio(BaseMessageModel msgItem, int position) {
        if (msgItem == null) {
            return;
        }

        if (position > layoutManager.findLastVisibleItemPosition()) {
            int avatarHeight = getResources().getDimensionPixelSize(R.dimen.chat_avatar_size);
            mHandler.sendMessage(mHandler.obtainMessage(SCROLL_TO_POSITION1, position, recyclerView.getHeight() - avatarHeight));
        }

        audioPlayManager.updatePosition(position);
        if (TextUtils.isEmpty(msgItem.audio_path) || !new File(msgItem.audio_path).exists()) {
            mDownloadingAudioMsg = msgItem;
            mDownloadManager.downloadAudio(msgItem);
        } else {
            //开始播放
            ChatAudioView.playingEntry = msgItem;
            audioPlayManager.requestPlay(msgItem, position);




            //// TODO: 2017/9/28 史云杰，加上群和公众号
            switch (msgItem.chat_type)
            {
                case MsgConstDef.MSG_CHAT_TYPE.PRIVATE:
                {
                    ScMessageModel sc_model = (ScMessageModel)msgItem;
                    NIMMsgManager.getInstance().SetMessageStatus(sc_model.opt_user_id, sc_model.message_id, MsgConstDef.MSG_STATUS.PLAYED);
                }
                break;
                case MsgConstDef.MSG_CHAT_TYPE.GROUP:
                {
                    GcMessageModel gc_model = (GcMessageModel)msgItem;
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

    private void hideAudioTipView() {
        if (audioModeSpeaker.getVisibility() == View.VISIBLE) {
            audioModeSpeaker.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
//        boolean bCreate = getIntent().getBooleanExtra("create", false);
//
//        Intent intent = new Intent();
//        intent.putExtra("session_id", send_id);
//        intent.putExtra("create", bCreate);
//        intent.putExtra("delete_last", is_delete_last);
//        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    class HeadsetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                //插入和拔出耳机会触发此广播
                case Intent.ACTION_HEADSET_PLUG:
                    int state = intent.getIntExtra("state", 0);
                    if (state == 1){
                        audioPlayManager.changeToHeadsetMode();
                    } else {

                    }
                    break;
                //拔出耳机会触发此广播,拔出不会触发,且此广播比上一个早,故可在此暂停播放,收到上一个广播时在恢复播放
                case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                    audioPlayManager.leaveHeadSet();
                    break;
                default:
                    break;
            }
        }
    }
}
