package com.qbao.newim.activity;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.ParallaxScaleBehavior;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.Gson;
import com.qbao.newim.configure.GlobalVariable;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.ErrorDetail;
import com.qbao.newim.constdef.FriendTypeDef;
import com.qbao.newim.helper.CircleIndexIndicator;
import com.qbao.newim.helper.GlideImageLoader;
import com.qbao.newim.helper.ProgressPieIndicator;
import com.qbao.newim.helper.TransferConfig;
import com.qbao.newim.helper.Transferee;
import com.qbao.newim.manager.NIMFriendInfoManager;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.model.IMUserInfo;
import com.qbao.newim.util.NIMStartActivityUtil;
import com.qbao.newim.processor.FriendAddProcessor;
import com.qbao.newim.processor.FriendDelProcessor;
import com.qbao.newim.processor.GlobalProcessor;
import com.qbao.newim.processor.UserInfoGetProcessor;
import com.qbao.newim.qbdb.manager.UserInfoDbManager;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.BaseUtil;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.DateUtil;
import com.qbao.newim.util.IDataObserver;
import com.qbao.newim.util.Utils;
import com.qbao.newim.views.AnimTextView;
import com.qbao.newim.views.BlurredView;
import com.qbao.newim.views.ProgressDialog;
import com.qbao.newim.views.RoundedNormalIV;

import java.lang.reflect.Method;
import java.util.Arrays;

import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * Created by chenjian on 2017/5/26.
 */

public class NIMUserInfoActivity extends AppCompatActivity implements IDataObserver, AppBarLayout.OnOffsetChangedListener
        , View.OnClickListener, ParallaxScaleBehavior.OnScaleListener {

    AnimTextView tv_signature;
    Toolbar mToolbar;
    BlurredView iv_bg;
    TextView tv_user_name;
    TextView tv_age_sex;
    TextView tv_address;
    RoundedNormalIV iv_user_head;
    private CollapsingToolbarLayout toolbarLayout;
    private AppBarLayout appBarLayout;
    private long user_id;

    private boolean mIsBottomTitleVisible = false;
    private boolean bFriend = true;
    private LinearLayout llyt_bottom;
    private TextView tv_bottom;
    private IMUserInfo userInfo;
    private String show_name;
    private byte source_type;            // 当前非好友，从哪个页面进来，并添加好友
    int maxScroll;
    public Transferee transferee;
    private FloatingActionButton fbs_photo;
    private boolean need_accept;
    private boolean is_self;
    private boolean is_remark;

    private MenuItem edit_menu;
    private MenuItem delete_menu;
    private MenuItem report_menu;
    private MenuItem black_menu;
    private boolean black_status;
    private IMFriendInfo friendInfo;
    private boolean delete_friend;
    private boolean black_friend;

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.9f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;
    private String[] str_arr = new String[]{"http://t2.27270.com/uploads/tu/201706/9999/d38274f15c.jpg",
            "http://t2.27270.com/uploads/tu/201706/9999/061548f1fb.jpg"
            , "http://t2.27270.com/uploads/tu/201706/9999/4a85dd9bd9.jpg",
            "http://t2.27270.com/uploads/tu/201706/9999/a6c57f438d.jpg",
            "http://t2.27270.com/uploads/tu/201706/9999/b6ae25c618.jpg",
            "http://t2.27270.com/uploads/tu/201612/562/lua4uwojfds.jpg",
            "http://t2.27270.com/uploads/tu/201612/562/4hp4d1fcocu.jpg",
            "http://t2.27270.com/uploads/tu/201612/562/d2madqozild.jpg"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            Transition explode = TransitionInflater.from(this).inflateTransition(android.R.transition.explode);
            getWindow().setEnterTransition(explode);
        }
        setStatusColor();
        setContentView(R.layout.nim_activity_user);

        initView();
        setListener();
        processLogic();
    }

    protected void initView() {
        if (getIntent() != null) {
            user_id = getIntent().getLongExtra("user_id", 0);
            source_type = getIntent().getByteExtra("source_type", (byte) -1);
            String json_user = getIntent().getStringExtra("json_user");
            if (!TextUtils.isEmpty(json_user)) {
                userInfo = new Gson().fromJson(json_user, IMUserInfo.class);
                user_id = userInfo.userId;
            }

            is_self = user_id == NIMUserInfoManager.getInstance().GetSelfUserId();
        }

        mToolbar = (Toolbar) findViewById(R.id.user_toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        appBarLayout = (AppBarLayout) findViewById(R.id.user_appbarlayout);
        ParallaxScaleBehavior behavior = ParallaxScaleBehavior.from(appBarLayout);
        behavior.setListener(this);
        tv_user_name = (TextView) findViewById(R.id.user_name);
        tv_age_sex = (TextView) findViewById(R.id.user_age);
        tv_address = (TextView) findViewById(R.id.user_address);
        iv_user_head = (RoundedNormalIV) findViewById(R.id.user_image);

        tv_signature = (AnimTextView) findViewById(R.id.user_signature_txt);

        iv_bg = (BlurredView) findViewById(R.id.iv_background);

        toolbarLayout.setExpandedTitleGravity(Gravity.CENTER_HORIZONTAL);

        llyt_bottom = (LinearLayout) findViewById(R.id.user_add_friend_container);
        tv_bottom = (TextView) findViewById(R.id.user_add_friend);
        fbs_photo = (FloatingActionButton) findViewById(R.id.user_fab);
    }

    protected void setListener() {
        appBarLayout.addOnOffsetChangedListener(this);
        llyt_bottom.setOnClickListener(this);
        DataObserver.Register(this);
        fbs_photo.setOnClickListener(this);
        Glide.with(this).load("http://t2.27270.com/uploads/tu/201706/9999/d38274f15c.jpg").asBitmap().placeholder
                (R.mipmap.nim_photo_wall_loading).transform(new BlurTransformation(this, 8, 4)).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                if (resource != null) {
                    iv_bg.setBlurredImg(resource);
                }
            }
        });
    }

    protected void processLogic() {
        if (userInfo == null) {
            if (is_self) {
                IMUserInfo self_info = UserInfoDbManager.getInstance().
                        getSingleIMUser(NIMUserInfoManager.getInstance().GetSelfUserId());
                if (self_info != null) {
                    setDataView(self_info);
                }
            } else {
                IMUserInfo userInfo = NIMUserInfoManager.getInstance().getIMUser(user_id) ;
                if (userInfo != null) {
                    setDataView(userInfo);
                } else {
                    IMFriendInfo friend_info = NIMFriendInfoManager.getInstance().getFriendUser(user_id);
                    if (friend_info != null)
                        setDataView(friend_info);
                }

                UserInfoGetProcessor processor = GlobalProcessor.getInstance().getUser_processor();
                processor.SendUserInfoRQ(String.valueOf(user_id));
            }

        } else {
            setDataView(userInfo);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.nim_user_menu, menu);
        edit_menu = menu.findItem(R.id.menu_edit_name);
        report_menu = menu.findItem(R.id.menu_report_user);
        delete_menu = menu.findItem(R.id.menu_delete_user);
        black_menu = menu.findItem(R.id.menu_black_list);
        if (is_self) {
            edit_menu.setVisible(false);
            report_menu.setVisible(false);
            delete_menu.setVisible(false);
            black_menu.setVisible(false);
        } else {
            initMenu();
        }
        return true;
    }

    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (menu != null) {
            if (menu.getClass() == MenuBuilder.class) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                }
            }
        }
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (item.getItemId() == R.id.menu_edit_name) {
            editFriendName();
        } else if (item.getItemId() == R.id.menu_report_user) {
            NIMStartActivityUtil.startToReportActivity(NIMUserInfoActivity.this, user_id);
        } else if (item.getItemId() == R.id.menu_delete_user) {
            deleteFriend();
        } else if (item.getItemId() == R.id.menu_black_list) {
            setBlackType();
        }
        return true;
    }

    private void initMenu() {
        if (edit_menu == null || report_menu == null || delete_menu == null || black_menu == null) {
            return;
        }
        edit_menu.setVisible(bFriend);
        report_menu.setVisible(true);
        delete_menu.setVisible(bFriend);
        black_menu.setVisible(bFriend);
        if (black_status) {
            black_menu.setTitle("移除黑名单");
        } else {
            black_menu.setTitle("加入黑名单");
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.user_add_friend_container) {
            if (bFriend) {
                NIMStartActivityUtil.startToScActivity(this, user_id);
            } else {
                if (NIMFriendInfoManager.getInstance().OutFriendMaxCount()) {
                    showToastStr("你已达到好友上限" + GlobalVariable.FRIEND_MAX_COUNT + "人");
                    return;
                }
                if (need_accept) {
                    FriendAddProcessor processor = GlobalProcessor.getInstance().getFriendAddProcessor();
                    processor.sendFriendAcceptRQ(friendInfo);
                } else {
                    Intent intent = new Intent(this, FriendVerifyActivity.class);
                    intent.putExtra("user_id", userInfo.userId);
                    intent.putExtra("source_type", source_type);
                    startActivity(intent);
                }
            }
        } else if (v.getId() == R.id.user_fab) {
            showPhotoWall();
        }
    }

    private void deleteFriend() {
        confirmDeleteFriend(user_id, show_name);
    }

    private void showPhotoWall() {
        TransferConfig config = TransferConfig.build()
                .setNowThumbnailIndex(0)
                .setSourceImageList(Arrays.asList(str_arr))
                .setMissPlaceHolder(R.mipmap.nim_pp_ic_holder_dark)
                .setOriginImageList(iv_bg.getOriginImageView())
                .setProgressIndicator(new ProgressPieIndicator())
                .setIndexIndicator(new CircleIndexIndicator())
                .setImageLoader(GlideImageLoader.with(getApplicationContext()))
                .create();
        transferee.apply(config).show();
    }

    private void editFriendName() {
        if (userInfo == null) {
            return;
        }
        NIMStartActivityUtil.startToNIMEditActivity(this, user_id, NIMEditNameActivity.FRIEND_EDIT_NAME, show_name);
    }

    // 确定当前用户的关系
    private void queryCurUserType() {
        friendInfo = NIMFriendInfoManager.getInstance().getFriendUser(user_id);
        if (friendInfo == null) {
            bFriend = false;
        } else {
            bFriend = friendInfo.status > FriendTypeDef.FRIEND_ADD_TYPE.TIME_OUT;
            black_status = friendInfo.black_type == FriendTypeDef.ACTIVE_TYPE.ACTIVE ||
                    friendInfo.black_type == FriendTypeDef.ACTIVE_TYPE.EACH;
        }

        if (bFriend) {
            tv_bottom.setText("发送消息");
            tv_bottom.setTextColor(Color.parseColor("#146fdf"));
            llyt_bottom.setBackgroundResource(R.drawable.nim_user_send_message_selector);

            show_name = Utils.getUserShowName(new String[]{friendInfo.remark_name, friendInfo.nickName, friendInfo.user_name});
        } else {

            if (friendInfo != null && (getIntent().getIntExtra("pos", -1) >= 0 ||
                    friendInfo.status == FriendTypeDef.FRIEND_ADD_TYPE.ACCEPT_REQUEST)) {
                tv_bottom.setText("通过验证");
                need_accept = true;
                tv_bottom.setTextColor(Color.parseColor("#757575"));
                llyt_bottom.setBackgroundResource(R.drawable.nim_user_accept_selector);
            } else {
                tv_bottom.setText("加为好友");
                tv_bottom.setTextColor(Color.parseColor("#ffffff"));
                llyt_bottom.setBackgroundResource(R.drawable.nim_user_bottom_selector);
            }
            if (userInfo == null) {
                return;
            }
            show_name = Utils.getUserShowName(new String[]{userInfo.nickName, userInfo.user_name});
        }

        toolbarLayout.setTitle(show_name);
        tv_user_name.setText(toolbarLayout.getTitle());

        initMenu();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataObserver.Cancel(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        transferee = Transferee.getDefault(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        transferee.destroy();
    }

    private void setDataView(IMUserInfo userInfo) {
        Glide.with(this).load(AppUtil.getHeadUrl(user_id)).asBitmap().placeholder(R.mipmap.nim_head).into(iv_user_head);
        Drawable right;
        if (userInfo.sex == 1) {
            right = ContextCompat.getDrawable(this, R.mipmap.nim_icon_boy);
            tv_age_sex.setCompoundDrawablesWithIntrinsicBounds(right, null, null, null);
            tv_age_sex.setBackgroundResource(R.drawable.nim_age_boy_bg);
        } else {
            right = ContextCompat.getDrawable(this, R.mipmap.nim_icon_girl);
            tv_age_sex.setCompoundDrawablesWithIntrinsicBounds(right, null, null, null);
            tv_age_sex.setBackgroundResource(R.drawable.nim_age_girl_bg);
        }

        if (userInfo.birthday > 0) {
            tv_age_sex.setVisibility(View.VISIBLE);
            tv_age_sex.setText(String.valueOf(DateUtil.longToAge(userInfo.birthday)));
        } else {
            tv_age_sex.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(userInfo.signature)) {
            tv_signature.setShowText(userInfo.signature);
        } else {
            tv_signature.setShowText("");
        }

        tv_address.setText(userInfo.locationPro + " " + userInfo.locationCity);

        if (is_self) {
            show_name = userInfo.nickName;
            toolbarLayout.setTitle(show_name);
            tv_user_name.setText(toolbarLayout.getTitle());
            llyt_bottom.setVisibility(View.GONE);
        } else {
            queryCurUserType();
        }
    }

    private void setDataView(IMFriendInfo userInfo) {
        Glide.with(this).load(userInfo.head_url).asBitmap().placeholder(R.mipmap.nim_head).into(iv_user_head);
        Drawable right;
        if (userInfo.sex == 1) {
            right = ContextCompat.getDrawable(this, R.mipmap.nim_icon_boy);
            tv_age_sex.setCompoundDrawablesWithIntrinsicBounds(right, null, null, null);
            tv_age_sex.setBackgroundResource(R.drawable.nim_age_boy_bg);
        } else {
            right = ContextCompat.getDrawable(this, R.mipmap.nim_icon_girl);
            tv_age_sex.setCompoundDrawablesWithIntrinsicBounds(right, null, null, null);
            tv_age_sex.setBackgroundResource(R.drawable.nim_age_girl_bg);
        }

        if (userInfo.birthday > 0) {
            tv_age_sex.setVisibility(View.VISIBLE);
            tv_age_sex.setText(String.valueOf(DateUtil.longToAge(userInfo.birthday)));
        } else {
            tv_age_sex.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(userInfo.signature)) {
            tv_signature.setShowText(userInfo.signature);
        } else {
            tv_signature.setShowText("");
        }

        tv_address.setText(userInfo.locationPro + " " + userInfo.locationCity);

        bFriend = userInfo.status > FriendTypeDef.FRIEND_ADD_TYPE.TIME_OUT;
        black_status = userInfo.black_type == FriendTypeDef.ACTIVE_TYPE.ACTIVE ||
                userInfo.black_type == FriendTypeDef.ACTIVE_TYPE.EACH;

        if (bFriend) {
            tv_bottom.setText("发送消息");
            tv_bottom.setTextColor(Color.parseColor("#146fdf"));
            llyt_bottom.setBackgroundResource(R.drawable.nim_user_send_message_selector);

            show_name = Utils.getUserShowName(new String[]{userInfo.remark_name, userInfo.nickName, userInfo.user_name});
        } else {

            if (userInfo != null && (getIntent().getIntExtra("pos", -1) >= 0 ||
                    userInfo.status == FriendTypeDef.FRIEND_ADD_TYPE.ACCEPT_REQUEST)) {
                tv_bottom.setText("通过验证");
                need_accept = true;
                tv_bottom.setTextColor(Color.parseColor("#757575"));
                llyt_bottom.setBackgroundResource(R.drawable.nim_user_accept_selector);
            } else {
                tv_bottom.setText("加为好友");
                tv_bottom.setTextColor(Color.parseColor("#ffffff"));
                llyt_bottom.setBackgroundResource(R.drawable.nim_user_bottom_selector);
            }
            show_name = Utils.getUserShowName(new String[]{userInfo.remark_name, userInfo.nickName, userInfo.user_name});
        }

        toolbarLayout.setTitle(show_name);
        tv_user_name.setText(toolbarLayout.getTitle());

        initMenu();
    }

    private void setBlackType() {
        if (!black_status) {
            ProgressDialog.showCustomDialog(this, "加入黑名单，你将不在收到对方的消息", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FriendDelProcessor processor = GlobalProcessor.getInstance().getFriendDelProcessor();
                    processor.sendBlackTypeRQ(user_id, 1);
                }
            });
        } else {
            FriendDelProcessor processor = GlobalProcessor.getInstance().getFriendDelProcessor();
            processor.sendBlackTypeRQ(user_id, 0);
        }
    }

    @Override
    public void OnChange(int param1, Object param2, Object param3) {
        switch (param1) {
            case DataConstDef.EVENT_GET_USER_INFO:
                if ((boolean) param3) {
                    IMUserInfo imUserInfo = (IMUserInfo) param2;
                    if (imUserInfo == null) {
                        return;
                    }
                    if (imUserInfo.userId != user_id) {
                        return;
                    }

                    userInfo = imUserInfo;
                    setDataView(userInfo);
                } else {
                    String msg = (String)param2;
                    showToastStr(msg + "用户不存在");
                }

                break;
            case DataConstDef.EVENT_FRIEND_ADD_REQUEST:
                if (!(boolean)param3) {
                    ProgressDialog.showCustomDialog(NIMUserInfoActivity.this, "对方拒绝接受你的请求");
                    return;
                }
                // 当前添加好友通过后
                long friend_id = (long) param2;
                IMFriendInfo addInfo = NIMFriendInfoManager.getInstance().getFriendReqInfo(friend_id);
                if (addInfo != null && addInfo.userId == userInfo.userId) {
                    if (addInfo.status == FriendTypeDef.FRIEND_ADD_TYPE.ACCEPT_REQUEST) {
                        bFriend = false;
                        tv_bottom.setText("通过验证");
                        need_accept = true;
                        friendInfo = addInfo;
                        tv_bottom.setTextColor(Color.parseColor("#757575"));
                        llyt_bottom.setBackgroundResource(R.drawable.nim_user_accept_selector);
                    }
                }
                break;
            case DataConstDef.EVENT_FRIEND_CONFIRM:
                IMFriendInfo confirm_info = NIMFriendInfoManager.getInstance().getFriendReqInfo((long) param2);
                if (confirm_info != null && confirm_info.userId == userInfo.userId) {
                    showToastStr("已成为好友");
                    bFriend = true;
                    friendInfo = confirm_info;
                    tv_bottom.setText("发送消息");
                    tv_bottom.setTextColor(Color.parseColor("#146fdf"));
                    llyt_bottom.setBackgroundResource(R.drawable.nim_user_send_message_selector);
                    initMenu();
                }
                break;
            case DataConstDef.EVENT_FRIEND_EDIT:   // 修改备注名
                String remark = (String) param2;
                long remark_id = (long) param3;
                if (remark_id == user_id) {
                    is_remark = true;
                    if (TextUtils.isEmpty(remark)) {
                        show_name = friendInfo.nickName;
                    } else {
                        show_name = remark;
                    }
                    toolbarLayout.setTitle(show_name);
                    tv_user_name.setText(toolbarLayout.getTitle());
                    friendInfo.remark_name = remark;
                    NIMFriendInfoManager.getInstance().updateFriend(friendInfo);
                }
                break;
            case DataConstDef.EVENT_NET_ERROR:
                int error_code = BaseUtil.MakeErrorResult((int) param2);
                String error_msg = ErrorDetail.GetErrorDetail(error_code);
                showToastStr(error_msg);
                break;
            case DataConstDef.EVENT_FRIEND_DEL:
                friendInfo = null;
                queryCurUserType();
                showToastStr("好友已删除");
                delete_friend = true;
                break;
            case DataConstDef.EVENT_GET_BLACK_LIST:
                if (friendInfo.black_type == FriendTypeDef.ACTIVE_TYPE.ACTIVE
                        || friendInfo.black_type == FriendTypeDef.ACTIVE_TYPE.EACH) {
                    black_friend = true;
                    showToastStr("已加入黑名单");
                } else {
                    black_friend = false;
                    showToastStr("已移除黑名单");
                }
                queryCurUserType();
                break;
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(verticalOffset) / (float) maxScroll;
        float real_per = (float) (verticalOffset) / (float) maxScroll;
        int level = (int) (50 * real_per);
        iv_bg.setBlurredLevel(50 - level);

        //文字效果处理
        handleToolbarTitleVisibility(percentage);
    }

    private void handleToolbarTitleVisibility(float percentage) {

        if (percentage >= 1 - PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {
            if (mIsBottomTitleVisible) {
                startAlphaAnimation(tv_age_sex, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                startAlphaAnimation(tv_address, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                startAlphaAnimation(tv_signature, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                startAlphaAnimation(iv_user_head, 4 * ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsBottomTitleVisible = false;
            }
        } else {
            if (!mIsBottomTitleVisible) {
                startAlphaAnimation(tv_age_sex, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                startAlphaAnimation(tv_address, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                startAlphaAnimation(iv_user_head, 4 * ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                startAlphaAnimation(tv_signature, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsBottomTitleVisible = true;
            } else {
                tv_age_sex.setVisibility(View.INVISIBLE);
                tv_address.setVisibility(View.INVISIBLE);
                iv_user_head.setVisibility(View.INVISIBLE);
            }
        }
    }

    //设置透明度
    public static void startAlphaAnimation(View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }

    public void setStatusColor() {
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
        //SDK大于等于19
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    public void showToastStr(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void startActivity(Intent intent) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            super.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
//        } else {
            super.startActivity(intent);
            overridePendingTransition(R.anim.nim_pop_in, R.anim.nim_not_change);
//        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            super.startActivityForResult(intent, requestCode, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
//        } else {
            super.startActivityForResult(intent, requestCode);
            overridePendingTransition(R.anim.nim_pop_in, R.anim.nim_not_change);
//        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.nim_not_change, R.anim.nim_pop_out);
    }

    @Override
    public void onScale(float scale) {
        toolbarLayout.setTitle(scale < 1.2f ? show_name : "");
    }

    private void confirmDeleteFriend(final long friendId, final String title) {
        String sex;
        if (userInfo == null) {
            sex = friendInfo.sex == 1 ? "他" : "她";
        } else {
            sex = userInfo.sex == 1 ? "他" : "她";
        }
        String message = String.format("将好友\"%s\"删除，同时删除与" + sex + "的聊天记录", title);
        ProgressDialog.showCustomDialog(this, "删除好友", message, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FriendDelProcessor friendDelProcessor = GlobalProcessor.getInstance().getFriendDelProcessor();
                friendDelProcessor.sendFriendDelRQ(friendId);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if ((need_accept && bFriend) || is_remark || delete_friend || black_friend) {
            Intent intent = new Intent();
            intent.putExtra("pos", getIntent().getIntExtra("pos", -1));
            intent.putExtra("delete", delete_friend);
            intent.putExtra("black", black_friend);
            intent.putExtra("remark", is_remark);
            setResult(RESULT_OK, intent);
        }
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        AudioManager am = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                return true;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
