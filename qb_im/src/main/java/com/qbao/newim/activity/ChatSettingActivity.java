package com.qbao.newim.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qbao.newim.adapter.GroupAvatarAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.ErrorDetail;
import com.qbao.newim.constdef.FriendTypeDef;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.manager.NIMComplexManager;
import com.qbao.newim.manager.NIMFriendInfoManager;
import com.qbao.newim.manager.NIMGroupInfoManager;
import com.qbao.newim.manager.NIMGroupMsgManager;
import com.qbao.newim.manager.NIMGroupUserManager;
import com.qbao.newim.manager.NIMMsgManager;
import com.qbao.newim.manager.NIMSessionManager;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.GroupOperateMode;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.model.IMGroupUserInfo;
import com.qbao.newim.model.SessionModel;
import com.qbao.newim.model.message.GcMessageModel;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.util.NIMStartActivityUtil;
import com.qbao.newim.processor.FriendAddProcessor;
import com.qbao.newim.processor.GlobalProcessor;
import com.qbao.newim.processor.GroupChatProcessor;
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
import com.qbao.newim.views.ProgressDialog;
import com.qbao.newim.views.SwitchButton;
import com.qbao.newim.views.dialog.Effectstype;
import com.qbao.newim.views.dialog.NiftyDialogBuilder;
import com.qbao.newim.views.imgpicker.NIM_ToolbarAct;

import java.util.ArrayList;
import java.util.List;

import static com.qbao.newim.qbim.R.id.group_setting_name_layout;

/**
 * Created by chenjian on 2017/6/14.
 */

public class ChatSettingActivity extends NIM_ToolbarAct implements IDataObserver {

    private RelativeLayout layout_private_head;
    private ImageView ivHead;
    private TextView tvName;
    private View viewAddMember;
    private TextView tv_add_member;
    private SwitchButton ivTop;
    private View viewClear;
    private long setting_id;
    private boolean is_top;
    private boolean is_delete;
    private boolean is_group;
    private boolean is_admin;

    private LinearLayout layout_group_head;          // 单聊头像布局
    private SwitchButton iv_boring;                  // 消息免打扰
    private RelativeLayout layout_report;            // 举报用户
    private LinearLayout layout_group_setting;       // 群聊头像布局
    private TextView tv_group_member;                // 群成员(n人)
    private RelativeLayout layout_group_member;      // 群成员更多点击布局
    private RecyclerView recyclerView;               // 群成员八个头像list
    private TextView tv_group_name;                  // 群聊名称显示内容
    private RelativeLayout layout_group_name;        // 群聊名称点击布局
    private RelativeLayout layout_scan;              // 群聊二维码点击布局
    private TextView tv_max_count;                   // 群聊大小个数显示
    private TextView tv_nick;                        // 我在本群的昵称
    private RelativeLayout layout_nick;              // 我在本群昵称修改点击布局
    private SwitchButton iv_show_nick;               // 是否显示群聊昵称开关按钮
    private LinearLayout layout_news_notify;         // 群消息点击展示类型
    private RelativeLayout layout_news_show;         // 群消息展示类型
    private TextView tv_news_tips;                   // 显示群消息展示类型内容
    private SwitchButton ivSave;                     // 群聊是否显示在通讯录
    private TextView tv_quit;                        // 退出群聊
    private RelativeLayout layout_save;              // 群聊是否显示在通讯录布局
    private RelativeLayout layout_notice;            // 群公告布局
    private TextView tv_notice;                      // 群公告内容
    private RelativeLayout layout_manager;           // 群管理
    private LinearLayout layout_private_setting;     // 个人举报和免打扰

    private IMFriendInfo friendInfo;
    private GroupAvatarAdapter mAdapter;
    private IMGroupInfo groupInfo;
    private IMGroupUserInfo self_info;

    private static final int MAX_AVATAR_COUNT = 8;
    private static final int GROUP_ADD_MEMBER_CODE = 100;
    private static final int REMARK_FRIEND_NAME_CODE = 101;
    private ArrayList<IMGroupUserInfo> mList = new ArrayList<>();
    private NiftyDialogBuilder dialogBuilder;
    private byte nType;
    private boolean is_kicked;
    private NiftyDialogBuilder kick_dialog;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.nim_activity_chat_setting);
        if (getIntent() != null) {
            setting_id = getIntent().getLongExtra("id", 0);
            is_group = getIntent().getBooleanExtra("group", false);
            SessionModel session_model = NIMSessionManager.getInstance().GetSessionByID(setting_id);
            if(session_model != null)
            {
                is_top = session_model.is_top;
            }
        }

        layout_private_head = (RelativeLayout) findViewById(R.id.private_head_layout);
        ivHead = (ImageView) findViewById(R.id.chat_setting_img_head);
        tvName = (TextView) findViewById(R.id.chat_setting_tv_name);
        layout_private_setting = (LinearLayout) findViewById(R.id.private_setting_layout);
        iv_boring = (SwitchButton) findViewById(R.id.chat_setting_boring);
        layout_report = (RelativeLayout) findViewById(R.id.private_setting_report_layout);

        if (is_group) {
            layout_group_head = (LinearLayout) findViewById(R.id.group_setting_member);
            tv_group_member = (TextView) findViewById(R.id.group_setting_count);
            layout_group_member = (RelativeLayout) findViewById(R.id.group_setting_count_layout);
            recyclerView = (RecyclerView) findViewById(R.id.group_setting_avatar);
            recyclerView.setLayoutManager(new FullyLinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            recyclerView.setNestedScrollingEnabled(false);
            layout_group_name = (RelativeLayout) findViewById(group_setting_name_layout);
            layout_group_setting = (LinearLayout) findViewById(R.id.group_setting_layout);
            tv_group_name = (TextView) findViewById(R.id.group_setting_name);
            layout_scan = (RelativeLayout) findViewById(R.id.group_setting_scan_layout);
            tv_max_count = (TextView) findViewById(R.id.group_setting_max);
            layout_nick = (RelativeLayout) findViewById(R.id.group_setting_nick_layout);
            tv_nick = (TextView) findViewById(R.id.group_setting_nick);
            iv_show_nick = (SwitchButton) findViewById(R.id.chat_setting_show_nick);
            layout_news_notify = (LinearLayout) findViewById(R.id.group_setting_notify_layout);
            layout_news_show = (RelativeLayout) findViewById(R.id.group_setting_news_layout);
            tv_news_tips = (TextView) findViewById(R.id.group_setting_notify);
            layout_save = (RelativeLayout) findViewById(R.id.group_setting_save);
            ivSave = (SwitchButton) findViewById(R.id.chat_setting_save);
            tv_quit = (TextView) findViewById(R.id.group_quit_txt);
            layout_notice = (RelativeLayout) findViewById(R.id.group_setting_notice_layout);
            tv_notice = (TextView) findViewById(R.id.group_setting_notice_content);
            layout_manager = (RelativeLayout) findViewById(R.id.group_setting_manager_layout);

            layout_private_head.setVisibility(View.GONE);
            layout_private_setting.setVisibility(View.GONE);
            layout_report.setVisibility(View.GONE);

            layout_group_head.setVisibility(View.VISIBLE);
            layout_group_setting.setVisibility(View.VISIBLE);
            layout_save.setVisibility(View.VISIBLE);
            tv_quit.setVisibility(View.VISIBLE);
            layout_news_notify.setVisibility(View.VISIBLE);
        }

        viewAddMember = findViewById(R.id.chat_setting_add_member);
        tv_add_member = (TextView) findViewById(R.id.setting_add_txt);
        tv_add_member.setText(is_group ? R.string.add_chat_member : R.string.add_new_member);

        ivTop = (SwitchButton) findViewById(R.id.chat_setting_top);
        viewClear = findViewById(R.id.chat_setting_clear);
    }

    @Override
    protected void setListener() {
        ivHead.setOnClickListener(this);
        viewAddMember.setOnClickListener(this);
        ivTop.setOnCheckedChangeListener(new SwitchButtonEvent());
        iv_boring.setOnCheckedChangeListener(new SwitchButtonEvent());
        layout_report.setOnClickListener(this);
        viewClear.setOnClickListener(this);

        if (is_group) {
            layout_group_member.setOnClickListener(this);
            layout_group_name.setOnClickListener(this);
            layout_scan.setOnClickListener(this);
            iv_show_nick.setOnCheckedChangeListener(new SwitchButtonEvent());
            ivSave.setOnCheckedChangeListener(new SwitchButtonEvent());
            layout_nick.setOnClickListener(this);
            tv_quit.setOnClickListener(this);
            layout_manager.setOnClickListener(this);
            layout_notice.setOnClickListener(this);
            layout_news_show.setOnClickListener(this);
        }
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        DataObserver.Register(this);
        if (is_group) {
            groupInfo = NIMGroupInfoManager.getInstance().getGroupInfo(setting_id);
            String str_max_count = getString(R.string.nim_gc_member_count, groupInfo.group_count);
            tv_group_member.setText(str_max_count);
            tv_max_count.setText(groupInfo.group_max_count <= 0 ? "10人" : groupInfo.group_max_count + "人");
            if (!TextUtils.isEmpty(groupInfo.group_remark)) {
                tv_notice.setText(groupInfo.group_remark);
            } else {
                tv_notice.setVisibility(View.GONE);
            }
            updateSelfInfo();

            setState(iv_show_nick);
            setState(ivTop);
            setState(ivSave);
            nType = groupInfo.notify_type;
            setNewsTips(nType);
            mAdapter = new GroupAvatarAdapter(this, mList);
            mAdapter.setOwner_id(groupInfo.group_manager_user_id);
            recyclerView.setAdapter(mAdapter);
            List<IMGroupUserInfo> group_user_list = NIMGroupUserManager.getInstance().getGroupAllUserByCount(setting_id);
            if (group_user_list != null && !group_user_list.isEmpty() && group_user_list.size() == groupInfo.group_count) {
                mList.clear();
                mList.addAll(group_user_list);
                mAdapter.setNewData(mList);
                tv_group_member.setText(getString(R.string.nim_gc_member_count, group_user_list.size()));
            } else {
                GroupListProcessor processor = GlobalProcessor.getInstance().getGroupListProcessor();
                processor.sendGroupDetailRQ(setting_id);
            }

            tv_group_name.setText(groupInfo.group_name);

            mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    IMGroupUserInfo groupUserInfo = mAdapter.getItem(position);
                    Intent intent = new Intent(ChatSettingActivity.this, NIMUserInfoActivity.class);
                    intent.putExtra("user_id", groupUserInfo.user_id);
                    startActivity(intent);
                }
            });

        } else {
            friendInfo = NIMFriendInfoManager.getInstance().getFriendUser(setting_id);
            String name = Utils.getUserShowName(new String[]{friendInfo.remark_name, friendInfo.nickName, friendInfo.user_name});
            tvName.setText(name);
            Glide.with(this).load(AppUtil.getHeadUrl(setting_id)).placeholder(R.mipmap.nim_head).into(ivHead);

            setState(iv_boring);
        }

        setState(ivTop);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.nim_toolbar_title, menu);
        MenuItem menuItem = menu.findItem(R.id.item_toolbar);
        View actionView = menuItem.getActionView();


        TextView tvTitle = (TextView) actionView.findViewById(R.id.title_txt);
        tvTitle.setText(is_group ? R.string.group_setting : R.string.chat_setting);
        actionView.findViewById(R.id.title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        return true;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (is_kicked) {
            showKickDialog();
            return;
        }
        if (v.getId() == R.id.chat_setting_img_head) {                    // 聊天头像

            NIMStartActivityUtil.startToUserForResult(ChatSettingActivity.this,
                    setting_id, FriendTypeDef.FRIEND_SOURCE_TYPE.CHATTING, REMARK_FRIEND_NAME_CODE, 0);

        } else if (v.getId() == R.id.chat_setting_add_member) {           // 添加新聊天成员

            int group_model;
            if (is_group) {
                if (groupInfo.group_count >= groupInfo.group_max_count) {
                    showToastStr(getString(R.string.group_member_up_limit));
                    return;
                }
                group_model = GroupCreateActivity.MODE_ADD_NEW_MEMBER;
            } else {
                friendInfo = NIMFriendInfoManager.getInstance().getFriendUser(setting_id);
                if (friendInfo != null) {
                    if (friendInfo.delete_type == FriendTypeDef.ACTIVE_TYPE.PASSIVE) {
                        String name = Utils.getUserShowName(new String[]{friendInfo.remark_name, friendInfo.nickName, friendInfo.user_name});
                        ProgressDialog.showCustomDialog(ChatSettingActivity.this, getString(R.string.msg_tips),
                                getString(R.string.firend_apply_send, name), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        sendFriendReq(friendInfo);
                                        showToastStr(getString(R.string.msg_sended));
                                    }
                                });
                        return;
                    } else if (friendInfo.black_type == FriendTypeDef.ACTIVE_TYPE.PASSIVE
                            || friendInfo.black_type == FriendTypeDef.ACTIVE_TYPE.EACH) {
                        showToastStr(getString(R.string.refuse_to_group));
                        return;
                    }
                }
                group_model = GroupCreateActivity.MODE_TRANSLATE_TO_GROUP;
            }
            NIMStartActivityUtil.startToNGCAForResult(
                    ChatSettingActivity.this, setting_id, group_model, GROUP_ADD_MEMBER_CODE);

        } else if (v.getId() == R.id.private_setting_report_layout) {     // 举报用户

            NIMStartActivityUtil.startToReportActivity(ChatSettingActivity.this, setting_id);

        } else if (v.getId() == R.id.chat_setting_clear) {

            showClearDialog();

        } else if (v.getId() == R.id.group_setting_count_layout) {        // 群成员

            NIMStartActivityUtil.startToNIMGroupMemberActivity(ChatSettingActivity.this, setting_id, is_admin
                    , GroupMemberActivity.GROUP_MEMBER);

        } else if ((v.getId() == R.id.group_setting_name_layout)) {       // 群聊名称

            NIMStartActivityUtil.startToNIMEditActivity(ChatSettingActivity.this, setting_id,
                    NIMEditNameActivity.GROUP_EDIT_NAME, groupInfo.group_name);

        } else if (v.getId() == R.id.group_setting_scan_layout) {         // 群二维码

            NIMStartActivityUtil.startToNIMEncodeActivity(ChatSettingActivity.this, setting_id, false);

        } else if (v.getId() == R.id.group_setting_nick_layout) {         // 修改我的群昵称

            if (self_info != null) {
                NIMStartActivityUtil.startToNIMEditActivity(ChatSettingActivity.this, setting_id,
                        NIMEditNameActivity.GROUP_USER_EDIT_NAME, self_info.user_nick_name);
            }

        } else if (v.getId() == R.id.group_setting_news_layout) {       // 群消息提醒设置

            showGroupMsgType();

        } else if (v.getId() == R.id.group_quit_txt) {

            quitGroupChat();

        } else if (v.getId() == R.id.group_setting_notice_layout) {

            if (TextUtils.isEmpty(groupInfo.group_remark) && !is_admin) {
                ProgressDialog.showCustomDialog(ChatSettingActivity.this, getString(R.string.edit_announce_permission));
            } else {
                NIMStartActivityUtil.startToNIMRemarkActivity(ChatSettingActivity.this, setting_id,
                        is_admin, groupInfo.group_remark);
            }

        } else if (v.getId() == R.id.group_setting_manager_layout) {

            NIMStartActivityUtil.startToGroupManagerActivity(ChatSettingActivity.this, setting_id);

        }
    }

    private void sendFriendReq(IMFriendInfo friendInfo) {
        IMFriendInfo info = new IMFriendInfo();
        info.opt_msg = getString(R.string.i_am_who, NIMUserInfoManager.getInstance().GetSelfUserName());
        info.userId = friendInfo.userId;
        info.source_type = friendInfo.source_type;
        info.nickName = NIMUserInfoManager.getInstance().GetSelfUserName();

        FriendAddProcessor processor = GlobalProcessor.getInstance().getFriendAddProcessor();
        processor.sendFriendAddRQ(info);
    }

    private void showClearDialog() {
        String message;
        if (is_group) {
            message = getString(R.string.nim_sure_to_clear_chat);
        } else {
            String session_name = NIMComplexManager.getInstance().GetSessionName(setting_id, MsgConstDef.MSG_CHAT_TYPE.PRIVATE);
            message = getString(R.string.nim_sure_to_clear_private_chat, session_name);
        }
        ProgressDialog.showCustomDialog(this, getString(R.string.nim_clear_chat), message, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPreDialog("");
                boolean delete_success;
                if(is_group)
                {
                    delete_success = NIMGroupMsgManager.getInstance().delGroupMessageInfoByGroupID(setting_id, true);
                }
                else
                {
                    delete_success = NIMMsgManager.getInstance().ClearMessage(setting_id, true);
                }
                hidePreDialog();
                if (delete_success) {
                    is_delete = true;
                    showToastStr(getString(R.string.nim_has_clear_all));
                }
            }
        });
    }

    class SwitchButtonEvent implements CompoundButton.OnCheckedChangeListener{

        @Override
        public void onCheckedChanged(CompoundButton view, boolean isChecked) {
            if (view.getId() == R.id.chat_setting_top) {
                is_top = isChecked;
                DataObserver.Notify(DataConstDef.EVENT_SESSION_TOP, setting_id, isChecked);
            } else if (view.getId() == R.id.chat_setting_save) {
                groupInfo.is_save_contact = isChecked;
                saveContactModel(isChecked);
            } else if (view.getId() == R.id.chat_setting_show_nick) {
                groupInfo.is_show_nick = isChecked;
                NIMGroupInfoManager.getInstance().updateGroup(groupInfo);
            } else if (view.getId() == R.id.chat_setting_boring) {
                friendInfo.notify = isChecked;
                setMsgBoring(isChecked);
            }
        }
    }

    private void setState(SwitchButton view) {
        boolean cur_state = false;
        if (view.getId() == R.id.chat_setting_top) {
            cur_state = is_top;
        } else if (view.getId() == R.id.chat_setting_save) {
            cur_state = groupInfo.is_save_contact;
        } else if (view.getId() == R.id.chat_setting_show_nick) {
            cur_state = groupInfo.is_show_nick;
        } else if (view.getId() == R.id.chat_setting_boring) {
            cur_state = !friendInfo.notify;
        }

        if (cur_state) {
            view.setChecked(true);
        } else {
            view.setChecked(false);
        }
    }

    private void quitGroupChat() {
        String message = getString(R.string.nim_exit_and_clear_msg);
        ProgressDialog.showCustomDialog(this, getString(R.string.nim_exit_gc), message, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPreDialog("");
                createQuitModel();
            }
        });
    }

    private void updateSelfInfo() {
        self_info = NIMGroupUserManager.getInstance().getGroupUserInfo(setting_id,
                NIMUserInfoManager.getInstance().GetSelfUserId());
        if (self_info != null) {
            is_admin = self_info.user_id == groupInfo.group_manager_user_id;
//            layout_manager.setVisibility(is_admin ? View.VISIBLE : View.GONE);
            tv_nick.setText(self_info.user_nick_name);
        } else {
            tv_nick.setText(NIMUserInfoManager.getInstance().GetSelfUserName());
        }
    }

    private void saveContactModel(boolean is_checked) {
        GroupGetProcessor processor = GlobalProcessor.getInstance().getGroupGetProcessor();
        processor.SendSaveContactRQ(setting_id, is_checked ? (byte) 1 : 0);
    }

    private void setMsgBoring(boolean is_checked) {
        UserInfoGetProcessor processor = GlobalProcessor.getInstance().getUser_processor();
        processor.SendMsgStatus(setting_id, is_checked ? (byte) 1 : 0);
    }

    private void createQuitModel() {
        GroupOperateMode model = new GroupOperateMode();
        model.group_id = setting_id;
        model.user_id = NIMUserInfoManager.getInstance().GetSelfUserId();
        model.message_id = NetCenter.getInstance().CreateGroupMsgId();
        model.big_msg_type = MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_KICK_USER;
        model.msg_time = BaseUtil.GetServerTime();
        model.operate_user_name = NIMUserInfoManager.getInstance().GetSelfUserName();

        if (self_info == null) {
            return;
        }
        List<IMGroupUserInfo> list = new ArrayList<>();
        list.add(self_info);
        model.user_info_list = list;
        GroupOperateProcessor processor = GlobalProcessor.getInstance().getGroupOperateProcessor();
        processor.sendGroupModifyRQ(model);
    }

    @Override
    public void onBackPressed() {
        if (is_delete) {
            Intent intent = new Intent();
            intent.putExtra("delete", true);
            setResult(RESULT_OK, intent);
        }

        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GROUP_ADD_MEMBER_CODE:
                    if (is_delete) {
                        Intent intent = new Intent();
                        intent.putExtra("delete", true);
                        setResult(RESULT_OK, intent);
                    }
                    finish();
                    break;
                case REMARK_FRIEND_NAME_CODE:
                    friendInfo = NIMFriendInfoManager.getInstance().getFriendUser(setting_id);
                    if (friendInfo == null) {
                        Intent intent = new Intent();
                        intent.putExtra("delete_user", true);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        tvName.setText(friendInfo.remark_name);
                    }
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataObserver.Cancel(this);
    }

    @Override
    public void OnChange(int param1, Object param2, Object param3) {
        switch (param1) {
            case DataConstDef.EVENT_GROUP_DETAIL:
                ArrayList<IMGroupUserInfo> group_list = NIMGroupUserManager.getInstance().getGroupAllUser(setting_id);
                mList.clear();
                if (group_list.size() > 8) {
                    mList.addAll(group_list.subList(0, MAX_AVATAR_COUNT));
                } else {
                    mList.addAll(group_list);
                    mAdapter.setNewData(mList);
                    updateSelfInfo();
                }

                tv_group_member.setText(getString(R.string.nim_gc_member_count, group_list.size()));
                break;
            case DataConstDef.EVENT_GROUP_OPERATE:
                hidePreDialog();
                GcMessageModel operateMode = (GcMessageModel) param2;
                if (operateMode == null || operateMode.group_info == null)
                {
                    Logger.error(TAG, "operate is nil or group_info is nil");
                    return;
                }

                switch (operateMode.big_msg_type)
                {
                    case MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_ADD_USER:
                        HandlerAddGroupUser(operateMode);
                        break;
                    case MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_KICK_USER:
                        HandlerKickGroupUser(operateMode);
                        break;
                    case MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_MODIFY_GROUP_NAME:
                        handlerGroupName(operateMode.group_modify_content);
                        break;
                    case MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_MODIFY_GROUP_REMARK:
                        handlerGroupRemark(operateMode.group_modify_content);
                        break;
                    case MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_MODIFY_GROUP_USER_NAME:
                        handlerGroupUserName(operateMode.group_modify_content);
                        break;
                }

                break;
            case DataConstDef.EVENT_MESSAGE_STATUS:
                if ((long)param3 != setting_id) {
                    return;
                }

                nType = (byte)param2;
                setNewsTips(nType);
                break;
            case DataConstDef.EVENT_FRIEND_MSG_STATUS:
                if (param3 == null || (long)param2 != setting_id) {
                    iv_boring.setChecked(!friendInfo.notify);
                    return;
                }
                friendInfo.notify = (boolean) param3;
                break;
            case DataConstDef.EVENT_NET_ERROR:
                if (is_active) {
                    int error_code = BaseUtil.MakeErrorResult((int) param2);
                    String error_msg = ErrorDetail.GetErrorDetail(error_code);
                    showToastStr(error_msg);
                }
                break;
            case DataConstDef.EVENT_SAVE_CONTACT:
                if (param3 == null || (long) param2 != setting_id) {
                    ivSave.setChecked(groupInfo.is_save_contact);
                    return;
                }
                groupInfo.is_save_contact = (boolean) param3;
                break;
        }
    }

    private void HandlerKickGroupUser(GcMessageModel operateMode)
    {
        if(null == operateMode || null == operateMode.group_info)
        {
            return ;
        }

        if (operateMode.user_info_list == null || operateMode.user_info_list.get(0) == null)
        {
            return;
        }
        // 当前踢人是自己，即退群
        if (operateMode.user_id == NIMUserInfoManager.getInstance().GetSelfUserId())
        {
            // 当前踢人的操作者是自己时，直接退群
            IMGroupUserInfo operate_user_info = operateMode.user_info_list.get(0);
            if (operate_user_info.user_id == NIMUserInfoManager.getInstance().GetSelfUserId())
            {
                DataObserver.Notify(DataConstDef.EVENT_GROUP_DELETE, setting_id, true);
                handlerUserHeadUrl(operateMode);
                onBackPressed();
                return;
            }
        }
        else
        {
            // 被群主踢出群
            boolean contain_self = false;
            for (IMGroupUserInfo groupUserInfo : operateMode.user_info_list)
            {
                if (groupUserInfo.user_id == NIMUserInfoManager.getInstance().GetSelfUserId())
                {
                    contain_self = true;
                }
            }

            if (contain_self)
            {
                is_kicked = true;
                showKickDialog();
            }
        }

        for (IMGroupUserInfo userInfo : operateMode.user_info_list)
        {
            deleteUser(userInfo.user_id);
        }

        groupInfo.group_count = operateMode.group_info.group_count;
        String str_max_count = getString(R.string.nim_gc_member_count, groupInfo.group_count);
        tv_group_member.setText(str_max_count);
    }

    private void deleteUser(long user_id) {
        for (int i = 0; i < mAdapter.getData().size(); i++) {
            if (mAdapter.getData().get(i).user_id == user_id){
                mAdapter.remove(i);
            }
        }
    }

    private void showKickDialog() {
        kick_dialog = ProgressDialog.showSingleDialog(this, getString(R.string.nim_kicked_group), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void HandlerAddGroupUser(GcMessageModel operateMode)
    {
        List<IMGroupUserInfo> invite_list = operateMode.user_info_list;
        if (invite_list == null)
        {
            return;
        }

        mAdapter.addData(invite_list);
        if (operateMode.user_id == NIMUserInfoManager.getInstance().GetSelfUserId())
        {
            handlerUserHeadUrl(operateMode);
        }

        groupInfo.group_count = operateMode.group_info.group_count;
        NIMGroupInfoManager.getInstance().updateGroup(groupInfo);

        String str_max_count = getString(R.string.nim_gc_member_count, groupInfo.group_count);
        tv_group_member.setText(str_max_count);
    }

    private void handlerUserHeadUrl(GcMessageModel operateMode)
    {
        if(operateMode == null || operateMode.group_info == null)
        {
            return ;
        }

        groupInfo.group_count = operateMode.group_info.group_count;
        NIMGroupInfoManager.getInstance().AddGroup(groupInfo);

        String str_max_count = getString(R.string.nim_gc_member_count, groupInfo.group_count);
        tv_group_member.setText(str_max_count);
        if (groupInfo.group_count >= GroupAvatarCreator.MAX_COUNT
                || groupInfo.group_count >= GroupAvatarCreator.MAX_COUNT)
        {
            return;
        }
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        int count;
        if (recyclerView.getChildCount() > GroupAvatarCreator.MAX_COUNT)
        {
            count = GroupAvatarCreator.MAX_COUNT - 1;
        }
        else
        {
            count = recyclerView.getChildCount();
        }
        for (int i = 0; i < count; i++)
        {
            ViewGroup view = (ViewGroup) recyclerView.getChildAt(i);
            ImageView avatar = (ImageView) view.getChildAt(0);
            avatar.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(avatar.getDrawingCache());
            bitmaps.add(bitmap);
            avatar.setDrawingCacheEnabled(false);
        }
        Bitmap new_bitmap = GroupAvatarCreator.combineBitmap(bitmaps);
        UploadImageUtil.upLoadFile(new_bitmap, operateMode.group_id);
        if (is_delete)
        {
            Intent intent = new Intent();
            intent.putExtra("delete", true);
            setResult(RESULT_OK, intent);
        }

        finish();
    }

    private void handlerGroupName(String name) {
        tv_group_name.setText(name);
        DataObserver.Notify(DataConstDef.EVENT_SESSION_NAME, name, groupInfo.group_id);
    }

    private void handlerGroupRemark(String content) {
        if (TextUtils.isEmpty(content)) {
            tv_notice.setVisibility(View.GONE);
        } else {
            tv_notice.setVisibility(View.VISIBLE);
            tv_notice.setText(content);
        }
    }

    private void handlerGroupUserName(String name) {
        tv_nick.setText(name);
    }

    private void showGroupMsgType() {
        dialogBuilder = new NiftyDialogBuilder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.nim_setting_msg_dialog, null);
        dialogBuilder.setCustomView(view)
                .withDuration(400)
                .withEffect(Effectstype.SlideBottom)
                .isCancelableOnTouchOutside(true)
                .show();
        view.findViewById(R.id.group_msg_setting_item_cancel).setOnClickListener(new DialogOnClickListener());
        view.findViewById(R.id.group_msg_setting_item_3).setOnClickListener(new DialogOnClickListener());
        view.findViewById(R.id.group_msg_setting_item_2).setOnClickListener(new DialogOnClickListener());
        view.findViewById(R.id.group_msg_setting_item_1).setOnClickListener(new DialogOnClickListener());
    }

    private void handlerGroupLeaderChange(GroupOperateMode operateMode) {
        if (operateMode.user_info_list.isEmpty()) {
            return;
        }
        groupInfo.group_manager_user_id = operateMode.user_info_list.get(0).user_id;
        NIMGroupInfoManager.getInstance().AddGroup(groupInfo);
        mAdapter.setOwner_id(groupInfo.group_manager_user_id);
        mAdapter.notifyDataSetChanged();
        updateSelfInfo();
    }

    class DialogOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            dialogBuilder.dismiss();
            if (v.getId() == R.id.group_msg_setting_item_1) {
                if (nType == MsgConstDef.GROUP_MESSAGE_STATUS.GROUP_MESSAGE_STATUS_NORMAL) {
                    return;
                }
                setTipsToService(MsgConstDef.GROUP_MESSAGE_STATUS.GROUP_MESSAGE_STATUS_NORMAL);
            } else if (v.getId() == R.id.group_msg_setting_item_2) {
                if (nType == MsgConstDef.GROUP_MESSAGE_STATUS.GROUP_MESSAGE_STATUS_NO_HIT) {
                    return;
                }
                setTipsToService(MsgConstDef.GROUP_MESSAGE_STATUS.GROUP_MESSAGE_STATUS_NO_HIT);
            } else if (v.getId() == R.id.group_msg_setting_item_3) {
                if (nType == MsgConstDef.GROUP_MESSAGE_STATUS.GROUP_MESSAGE_IN_HELP_NO_HIT) {
                    return;
                }
                setTipsToService(MsgConstDef.GROUP_MESSAGE_STATUS.GROUP_MESSAGE_IN_HELP_NO_HIT);
            }
        }
    }

    private void setNewsTips(int type) {
        if (type == MsgConstDef.GROUP_MESSAGE_STATUS.GROUP_MESSAGE_STATUS_NORMAL) {
            tv_news_tips.setText(R.string.nim_recv_and_notify);
        } else if (type == MsgConstDef.GROUP_MESSAGE_STATUS.GROUP_MESSAGE_STATUS_NO_HIT) {
            tv_news_tips.setText(R.string.nim_recv_and_not_notify);
        } else if (type == MsgConstDef.GROUP_MESSAGE_STATUS.GROUP_MESSAGE_IN_HELP_NO_HIT) {
            tv_news_tips.setText(R.string.nim_recv_to_assist);
        }
    }

    private void setTipsToService(byte type) {
        GroupChatProcessor processor = GlobalProcessor.getInstance().getGc_processor();
        processor.sendMsgStatusRQ(setting_id, type);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (kick_dialog != null && kick_dialog.isShowing()) {
                onBackPressed();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
