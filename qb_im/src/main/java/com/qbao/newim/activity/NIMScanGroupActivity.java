package com.qbao.newim.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qbao.newim.adapter.GroupAvatarAdapter;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.ErrorDetail;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.manager.NIMGroupMsgManager;
import com.qbao.newim.manager.NIMGroupUserManager;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.GroupOperateMode;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.model.IMGroupUserInfo;
import com.qbao.newim.model.IMUserInfo;
import com.qbao.newim.model.message.GcMessageModel;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.util.NIMStartActivityUtil;
import com.qbao.newim.processor.GlobalProcessor;
import com.qbao.newim.processor.GroupGetProcessor;
import com.qbao.newim.processor.GroupListProcessor;
import com.qbao.newim.processor.GroupOperateProcessor;
import com.qbao.newim.processor.UserInfoGetProcessor;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.BaseUtil;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.IDataObserver;
import com.qbao.newim.util.Logger;
import com.qbao.newim.util.UploadImageUtil;
import com.qbao.newim.util.Utils;
import com.qbao.newim.views.FullyLinearLayoutManager;
import com.qbao.newim.views.GroupAvatarCreator;
import com.qbao.newim.views.imgpicker.NIM_ToolbarAct;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenjian on 2017/7/14.
 */

public class NIMScanGroupActivity extends NIM_ToolbarAct implements IDataObserver{

    private RecyclerView recyclerView;
    private View layout_member;
    private long group_id;
    private long user_id;
    private ImageView iv_group;
    private TextView tvTitle;
    private TextView tv_group_name;
    private TextView tv_group_member;
    private TextView tv_join_group;
    private IMGroupInfo scan_group_info;
    private ArrayList<IMGroupUserInfo> mList;
    private GroupAvatarAdapter mAdapter;
    private int MAX_COUNT = 8;
    private boolean hand_group_url;
    private View layout_fail;
    private View layout_group;
    private boolean is_member;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.nim_activity_scan_group);
        if (getIntent() != null) {
            group_id = getIntent().getLongExtra("group_id", 0);
            user_id = getIntent().getLongExtra("user_id", 0);
        }

        tv_group_member = (TextView) findViewById(R.id.group_scan_count);
        tv_group_name = (TextView) findViewById(R.id.group_setting_name);
        layout_member = findViewById(R.id.group_scan_member);
        iv_group = (ImageView) findViewById(R.id.group_scan_img);
        tv_join_group = (TextView) findViewById(R.id.group_join_txt);
        recyclerView = (RecyclerView) findViewById(R.id.group_setting_avatar);
        recyclerView.setLayoutManager(new FullyLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setNestedScrollingEnabled(false);
        layout_fail = findViewById(R.id.group_scan_fail_layout);
        layout_group = findViewById(R.id.group_scan_layout);

        mList = new ArrayList<>();
        mAdapter = new GroupAvatarAdapter(this, mList);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void setListener() {
        tv_join_group.setOnClickListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        DataObserver.Register(this);
        showPreDialog("");
        GroupGetProcessor processor = GlobalProcessor.getInstance().getGroupGetProcessor();
        processor.SendScanGroupRQ(group_id, user_id);
    }

    private void updateView() {
        tv_group_member.setText(scan_group_info.group_count + "人");
        tv_group_name.setText(scan_group_info.group_name);
        if(null != tvTitle)
        {
            tvTitle.setText(scan_group_info.group_name);
        }

        if (scan_group_info.group_count >= 9) {
            layout_member.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            hand_group_url = false;
            Glide.with(this).load(AppUtil.getGroupUrl(scan_group_info.group_id)).placeholder(R.mipmap.nim_head).into(iv_group);
        } else {
            layout_member.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            hand_group_url = true;
            GroupListProcessor processor = GlobalProcessor.getInstance().getGroupListProcessor();
            processor.setMember(false);
            processor.sendGroupDetailRQ(group_id);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataObserver.Cancel(this);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.group_join_txt) {
            if (!is_member)
                joinToGroup();
            else
            NIMStartActivityUtil.startToGcActivity(NIMScanGroupActivity.this, group_id);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.nim_toolbar_title, menu);
        MenuItem menuItem = menu.findItem(R.id.item_toolbar);
        View actionView = menuItem.getActionView();

        tvTitle = (TextView) actionView.findViewById(R.id.title_txt);
        actionView.findViewById(R.id.title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if(null != scan_group_info)
        {
            tvTitle.setText(scan_group_info.group_name);
        }
        return true;
    }

    private void joinToGroup() {
        IMGroupUserInfo groupUserInfo = NIMGroupUserManager.getInstance().getGroupUserInfo(group_id, user_id);
        if (groupUserInfo != null) {
            createGroupModel(groupUserInfo.user_nick_name);
        } else {
            UserInfoGetProcessor processor = GlobalProcessor.getInstance().getUser_processor();
            processor.SendUserInfoRQ(String.valueOf(user_id));
        }
    }

    private void createGroupModel(String user_name) {
        GroupOperateMode model = new GroupOperateMode();
        model.group_id = group_id;
        model.user_id = user_id;
        model.message_id = NetCenter.getInstance().CreateGroupMsgId();
        model.big_msg_type = MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_SCAN_ADD_USER;
        model.msg_time = BaseUtil.GetServerTime();
        model.operate_user_name = user_name;

        IMGroupUserInfo group_user = new IMGroupUserInfo();
        group_user.user_id = NIMUserInfoManager.getInstance().GetSelfUserId();
        group_user.user_nick_name = NIMUserInfoManager.getInstance().GetSelfUserName();

        List<IMGroupUserInfo> list = new ArrayList<>();
        list.add(group_user);
        model.user_info_list = list;
        GroupOperateProcessor processor = GlobalProcessor.getInstance().getGroupOperateProcessor();
        processor.sendGroupModifyRQ(model);
    }

    @Override
    public void OnChange(int param1, Object param2, Object param3) {
        switch (param1) {
            case DataConstDef.EVENT_SCAN_GROUP:
                hidePreDialog();
                if (param3 == null) {
                    layout_group.setVisibility(View.GONE);
                    layout_fail.setVisibility(View.VISIBLE);
                    return;
                }

                long share_id = (long)param3;
                IMGroupInfo groupInfo = (IMGroupInfo) param2;
                if (groupInfo.group_id != group_id || share_id != user_id) {
                    layout_group.setVisibility(View.GONE);
                    layout_fail.setVisibility(View.VISIBLE);
                    return;
                }

                layout_group.setVisibility(View.VISIBLE);
                layout_fail.setVisibility(View.GONE);
                scan_group_info = groupInfo;
                updateView();
                if (groupInfo.isMember == 1) {
                    is_member = true;
                    tv_join_group.setText("进入群聊");
                }
                break;
            case DataConstDef.EVENT_SCAN_GROUP_USER:
                mList = (ArrayList<IMGroupUserInfo>) param2;
                mAdapter.setOwner_id(scan_group_info.group_manager_user_id);
                if (mList.size() > MAX_COUNT) {
                    List<IMGroupUserInfo> list = mList.subList(0, MAX_COUNT);
                    mList.clear();
                    mList.addAll(list);
                }
                mAdapter.setNewData(mList);
                break;
            case DataConstDef.EVENT_GROUP_OPERATE:
                GcMessageModel operateMode = (GcMessageModel) param2;
                if(null == operateMode)
                {
                    break;
                }

                if (operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_SCAN_ADD_USER)
                {
                    createHeadUrl(operateMode.group_id);
                }
                break;
            case DataConstDef.EVENT_NET_ERROR:
                Logger.error(TAG, "EVENT_NET_ERROR");
                if (is_active) {
                    int error_code = BaseUtil.MakeErrorResult((int) param2);
                    String error_msg = ErrorDetail.GetErrorDetail(error_code);
                    showToastStr(error_msg);
                }
                break;
            case DataConstDef.EVENT_GET_USER_INFO:
                if ((boolean) param3) {
                    IMUserInfo imUserInfo = (IMUserInfo) param2;
                    if (imUserInfo == null) {
                        return;
                    }
                    if (imUserInfo.userId != user_id) {
                        return;
                    }

                    String show_name = Utils.getUserShowName(new String[]{imUserInfo.nickName, imUserInfo.user_name});
                    createGroupModel(show_name);
                }

                break;
        }
    }

    private void createHeadUrl(Long group_id) {
        if (hand_group_url) {
            ArrayList<Bitmap> bitmaps = new ArrayList<>();
            int count;
            if (recyclerView.getChildCount() > GroupAvatarCreator.MAX_COUNT) {
                count = GroupAvatarCreator.MAX_COUNT;
            } else {
                count = recyclerView.getChildCount();
            }
            for (int i = 0; i < count; i++) {
                ViewGroup view = (ViewGroup) recyclerView.getChildAt(i);
                ImageView avatar = (ImageView) view.getChildAt(0);
                avatar.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(avatar.getDrawingCache());
                bitmaps.add(bitmap);
                avatar.setDrawingCacheEnabled(false);
            }
            Bitmap new_bitmap = GroupAvatarCreator.combineBitmap(bitmaps);
            UploadImageUtil.upLoadFile(new_bitmap, group_id);
        }

        NIMStartActivityUtil.startToGcActivity(this, group_id);
    }
}
