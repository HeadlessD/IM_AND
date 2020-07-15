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
import com.qbao.newim.adapter.InviteDetailAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.constdef.FriendTypeDef;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.manager.NIMGroupInfoManager;
import com.qbao.newim.manager.NIMGroupUserManager;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.GroupOperateMode;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.model.IMGroupUserInfo;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.util.NIMStartActivityUtil;
import com.qbao.newim.processor.GlobalProcessor;
import com.qbao.newim.processor.GroupOperateProcessor;
import com.qbao.newim.qbdb.manager.GroupUserDbManager;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.BaseUtil;
import com.qbao.newim.util.UploadImageUtil;
import com.qbao.newim.views.GroupAvatarCreator;
import com.qbao.newim.views.imgpicker.NIM_ToolbarAct;

import java.util.ArrayList;

/**
 * Created by chenjian on 2017/7/5.
 */

public class GroupInviteDetailActivity extends NIM_ToolbarAct{

    private long group_id;
    private long user_id;
    private long message_id;
    private RecyclerView recyclerView;
    private ImageView iv_invite_head;
    private TextView tv_invite_name;
    private TextView tv_invite_desc;
    private TextView tv_invite_query;
    ArrayList<IMGroupUserInfo> mInviteList;
    ArrayList<IMGroupUserInfo> mLocalList;
    ArrayList<IMGroupUserInfo> mList;
    InviteDetailAdapter mAdapter;
    private boolean is_change_head;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.nim_activity_invite_detail);
        if (getIntent() != null) {
            user_id = getIntent().getLongExtra("user_id", 0);
            group_id = getIntent().getLongExtra("group_id", 0);
            message_id = getIntent().getLongExtra("message_id", 0);
        }

        recyclerView = (RecyclerView) findViewById(R.id.invite_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        iv_invite_head = (ImageView) findViewById(R.id.invite_head);
        tv_invite_name = (TextView) findViewById(R.id.invite_name);
        tv_invite_desc = (TextView) findViewById(R.id.invite_count);
        tv_invite_query = (TextView) findViewById(R.id.invite_query);

        mList = new ArrayList<>();
        mLocalList = new ArrayList<>();
        mInviteList = new ArrayList<>();
        mAdapter = new InviteDetailAdapter(mList);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void setListener() {
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                NIMStartActivityUtil.startToNIMUserActivity(GroupInviteDetailActivity.this
                        , mAdapter.getItem(position).user_id, FriendTypeDef.FRIEND_SOURCE_TYPE.CHATTING);
            }
        });
        tv_invite_query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createInviteMode();
            }
        });
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        IMGroupUserInfo invite_user = NIMGroupUserManager.getInstance().getGroupUserInfo(group_id, user_id);
        Glide.with(this).load(AppUtil.getHeadUrl(user_id)).placeholder(R.mipmap.nim_head).into(iv_invite_head);
        tv_invite_name.setText(invite_user.user_nick_name);

        mInviteList = GroupUserDbManager.getInstance().getGroupAgreeUser(group_id);
        IMGroupInfo groupInfo = NIMGroupInfoManager.getInstance().getGroupInfo(group_id);
        if (groupInfo.group_count < 9) {
            mLocalList = GroupUserDbManager.getInstance().getGroupNoAgreeUser(group_id);
            is_change_head = true;
        }
        mList.addAll(mInviteList);
        mList.addAll(mLocalList);
        mAdapter.setNewData(mList);

        tv_invite_desc.setText(invite_user.user_nick_name + "邀请" + mInviteList.size() + "位朋友进群");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.nim_toolbar_title, menu);
        MenuItem menuItem = menu.findItem(R.id.item_toolbar);
        View actionView = menuItem.getActionView();

        TextView tvTitle = (TextView) actionView.findViewById(R.id.title_txt);
        tvTitle.setText("邀请详情");

        actionView.findViewById(R.id.title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        return true;
    }

    private void createInviteMode() {
        GroupOperateMode mode1 = new GroupOperateMode();
        mode1.big_msg_type = MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_ADD_USER;
        mode1.group_id = group_id;
        mode1.group_ct = BaseUtil.GetServerTime();
        mode1.msg_time = BaseUtil.GetServerTime();
        mode1.message_id = NetCenter.getInstance().CreateGroupMsgId();
        mode1.message_old_id = message_id;

        mode1.operate_user_name = NIMUserInfoManager.getInstance().GetSelfUserName();
        mode1.user_id = NIMUserInfoManager.getInstance().GetSelfUserId();
        mode1.user_info_list = mInviteList;
        GroupOperateProcessor operateProcessor = GlobalProcessor.getInstance().getGroupOperateProcessor();
        operateProcessor.sendGroupModifyRQ(mode1);
        if (is_change_head) {
            handlerUserHeadUrl();
        }
        onBackPressed();
    }

    private void handlerUserHeadUrl() {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        int count;
        if (recyclerView.getChildCount() > GroupAvatarCreator.MAX_COUNT) {
            count = GroupAvatarCreator.MAX_COUNT - 1;
        } else {
            count = recyclerView.getChildCount();
        }
        for (int i = count - 1; i >= 0; i--) {
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
}
