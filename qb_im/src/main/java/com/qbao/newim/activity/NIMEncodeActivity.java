package com.qbao.newim.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.qbao.newim.configure.Constants;
import com.qbao.newim.manager.NIMGroupInfoManager;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.QRCodeUtil;
import com.qbao.newim.util.Utils;
import com.qbao.newim.views.imgpicker.NIM_ToolbarAct;

/**
 * Created by chenjian on 2017/6/27.
 */

public class NIMEncodeActivity extends NIM_ToolbarAct {

    private boolean is_user;
    private long qr_id;
    private ImageView iv_encode;
    private ImageView iv_avatar;
    private TextView tv_name;
    private TextView tv_hint;
    private String text;
    String qr_img;
    private boolean is_exist;
    private TextView tv_tips;

    private static final int IMAGE_HALF_WIDTH = 20;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.nim_activity_encode);
        iv_encode = (ImageView) findViewById(R.id.img_encode);
        iv_avatar = (ImageView) findViewById(R.id.user_icon);
        tv_name = (TextView) findViewById(R.id.user_name);
        tv_hint = (TextView) findViewById(R.id.qr_code_hint);
        tv_tips = (TextView) findViewById(R.id.img_encode_tips);

        iv_encode.setFocusable(true);
    }

    @Override
    protected void setListener() {
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent != null) {
            is_user = intent.getBooleanExtra("is_user", false);
            qr_id = intent.getLongExtra("id", 0);
            if (!is_user) {
                text = "group=" + qr_id + "_" + NIMUserInfoManager.getInstance().GetSelfUserId();
                IMGroupInfo groupInfo = NIMGroupInfoManager.getInstance().getGroupInfo(qr_id);
                setGroupInfo(groupInfo);
            }
        } else {
            onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nim_toolbar_title, menu);
        MenuItem menuItem = menu.findItem(R.id.item_toolbar);
        View actionView = menuItem.getActionView();

        actionView.findViewById(R.id.title_right).setVisibility(View.GONE);
        TextView tvTitle = (TextView) actionView.findViewById(R.id.title_txt);
        if (is_user) {
            tvTitle.setText("我的二维码");
            actionView.findViewById(R.id.title_right_img_layout).setVisibility(View.VISIBLE);
            actionView.findViewById(R.id.friend_img).setVisibility(View.GONE);
            ImageView imageView = (ImageView) actionView.findViewById(R.id.add_chat_friend);
            imageView.setImageResource(R.mipmap.nim_icon_share);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showToastStr("分享出去");
                }
            });
        } else {
            tvTitle.setText("二维码名片");
        }

        actionView.findViewById(R.id.title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void setGroupInfo(IMGroupInfo chatInfo) {
        if (chatInfo.group_add_is_agree > 0) {
            tv_hint.setText("扫一扫功能被禁用");
            tv_tips.setVisibility(View.VISIBLE);
            String[] names = new String[]{chatInfo.group_name, "未知"};
            String showName = Utils.getUserShowName(names);
            tv_name.setText(showName);
            Glide.with(this).load(AppUtil.getGroupUrl(chatInfo.group_id)).placeholder(R.mipmap.nim_head).into(iv_avatar);
            return;
        }

        tv_tips.setVisibility(View.GONE);
        tv_hint.setText("扫一扫二维码，加入该群");
        String[] names = new String[]{chatInfo.group_name, "未知"};
        String showName = Utils.getUserShowName(names);
        tv_name.setText(showName);

        qr_img = Constants.ICON_CACHE_DIR + "/" + qr_id;
        Bitmap bitmap = BitmapFactory.decodeFile(qr_img);
        if (bitmap == null) {
            showPreDialog("正在生成二维码");
            is_exist = false;
        } else {
            iv_encode.setImageBitmap(bitmap);
            is_exist = true;
        }

        Glide.with(this).load(AppUtil.getGroupUrl(chatInfo.group_id)).asBitmap().placeholder(R.mipmap.nim_head)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        iv_avatar.setImageBitmap(resource);
                        if (!is_exist) {
                            getQrImage();
                        }
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        iv_avatar.setImageResource(R.mipmap.nim_head);
                        if (!is_exist) {
                            getQrImage();
                        }
                    }
                });
    }

    private void getQrImage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                int halfWidth = (int) (IMAGE_HALF_WIDTH * metrics.density);

                boolean success = QRCodeUtil.createQRImage(text, 800, 800, createLogoBitmap(halfWidth), qr_img);

                if (success) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            iv_encode.setImageBitmap(BitmapFactory.decodeFile(qr_img));
                            hidePreDialog();
                        }
                    });
                } else {
                    showToastStr(R.string.msg_encode_contents_failed);
                    hidePreDialog();
                }
            }
        }).start();
    }

    private Bitmap createLogoBitmap(int halfWidth) {
        iv_avatar.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(iv_avatar.getDrawingCache());
        iv_avatar.setDrawingCacheEnabled(false);
        if (bitmap == null) {
            return null;
        }
        Matrix m = new Matrix();
        float sx = (float) 2 * halfWidth / bitmap.getWidth();
        float sy = (float) 2 * halfWidth / bitmap.getHeight();
        m.setScale(sx, sy);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, false);
        return bitmap;
    }
}
