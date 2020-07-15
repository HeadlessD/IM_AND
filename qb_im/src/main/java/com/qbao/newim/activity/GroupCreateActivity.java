package com.qbao.newim.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qbao.newim.adapter.GroupCreateAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.ErrorDetail;
import com.qbao.newim.constdef.FriendTypeDef;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.manager.NIMFriendInfoManager;
import com.qbao.newim.manager.NIMGroupInfoManager;
import com.qbao.newim.manager.NIMGroupUserManager;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.GroupOperateMode;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.model.IMGroupUserInfo;
import com.qbao.newim.model.NIMGroupCreateInfo;
import com.qbao.newim.model.message.GcMessageModel;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.util.NIMStartActivityUtil;
import com.qbao.newim.processor.FriendAddProcessor;
import com.qbao.newim.processor.GlobalProcessor;
import com.qbao.newim.processor.GroupGetProcessor;
import com.qbao.newim.processor.GroupOperateProcessor;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.BaseUtil;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.IDataObserver;
import com.qbao.newim.util.Logger;
import com.qbao.newim.util.UploadImageUtil;
import com.qbao.newim.util.Utils;
import com.qbao.newim.views.GroupAvatarCreator;
import com.qbao.newim.views.ProgressDialog;
import com.qbao.newim.views.StickyRecyclerHeadersDecoration;
import com.qbao.newim.views.imgpicker.NIM_ToolbarAct;
import com.qbao.newim.views.quick_bar.DividerDecoration;
import com.qbao.newim.views.quick_bar.OnQuickSideBarTouchListener;
import com.qbao.newim.views.quick_bar.QuickSideBarTipsView;
import com.qbao.newim.views.quick_bar.QuickSideBarView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chenjian on 2017/6/21.
 */

public class GroupCreateActivity extends NIM_ToolbarAct implements OnQuickSideBarTouchListener, IDataObserver {

    public static final int MODE_CREATE_GROUP = 1;                //创建群
    public static final int MODE_TRANSLATE_TO_GROUP = 2;          //从单聊升级为群
    public static final int MODE_ADD_NEW_MEMBER = 3;              //添加新成员
    public static final int MODE_CHOOSE = 4;                      //分享选择

    private RecyclerView recyclerView;
    private EditText mSearchInputView;
    private LinearLayout mSearchLayout;
    private LinearLayout mHeaderView;
    private TextView mHeaderSelectGroupView;
    private HorizontalScrollView avatarScrollView;
    private LinearLayout avatarLayout;

    private LayoutInflater mInflater;

    HashMap<String,Integer> letters = new HashMap<>();
    QuickSideBarView quickSideBarView;
    QuickSideBarTipsView quickSideBarTipsView;
    StickyRecyclerHeadersDecoration headersDecor;

    private GroupCreateAdapter mAdapter;

    private ArrayList<IMFriendInfo> mFriends;
    private ArrayList<IMFriendInfo> mNewChatMembers;
    private static Handler groupHandler = new Handler();

    private int editMinWidth = 0;
    private int group_model;
    private long setting_id;
    private IMGroupInfo groupInfo;
    private int nCreateType;

    TextView tvRight;
    private static final int REQUEST_CODE_GROUP_SELECTED = 200;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.nim_activity_group_create);

        mInflater = LayoutInflater.from(this);

        mSearchInputView = (EditText) findViewById(R.id.search_edittext);
        recyclerView = (RecyclerView) findViewById(R.id.group_create_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new GroupCreateAdapter(null, true);
        recyclerView.setAdapter(mAdapter);
        recyclerView.getItemAnimator().setChangeDuration(0);
        quickSideBarView = (QuickSideBarView) findViewById(R.id.quickSideBarView);
        quickSideBarTipsView = (QuickSideBarTipsView) findViewById(R.id.quickSideBarTipsView);

        mSearchLayout = (LinearLayout) findViewById(R.id.search_layout1);
        avatarScrollView = (HorizontalScrollView) findViewById(R.id.muti_select_friend_scroll);
        avatarLayout = (LinearLayout) findViewById(R.id.muti_select_friend_avatar_layout);

        mHeaderView = (LinearLayout) mInflater.inflate(R.layout.nim_group_create_header, null);
        mHeaderSelectGroupView = (TextView) mHeaderView.findViewById(R.id.tv_select_group);

        mSearchLayout.setEnabled(false);
        mAdapter.addHeaderView(mHeaderView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nim_toolbar_title, menu);
        MenuItem menuItem = menu.findItem(R.id.item_toolbar);
        View actionView = menuItem.getActionView();

        tvRight = (TextView) actionView.findViewById(R.id.title_right);
        tvRight.setVisibility(View.VISIBLE);
        tvRight.setText("确定");

        TextView tvTitle = (TextView) actionView.findViewById(R.id.title_txt);
        group_model = getIntent().getIntExtra("model", 0);

        if (MODE_TRANSLATE_TO_GROUP == group_model || MODE_ADD_NEW_MEMBER == group_model) {
            mHeaderSelectGroupView.setVisibility(View.GONE);
        }
        if (MODE_CREATE_GROUP == group_model) {
            tvTitle.setText("发起群聊");
        } else if (MODE_ADD_NEW_MEMBER == group_model) {
            tvTitle.setText("添加新成员");
        } else {
            tvTitle.setText("发起聊天");
        }

        actionView.findViewById(R.id.title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        setSelectedCount();
        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSelected();
            }
        });

        return true;
    }

    @Override
    protected void setListener() {
        mSearchInputView.setOnFocusChangeListener(mEditTextOnFocusChangeListener);
        mSearchInputView.addTextChangedListener(mEditTextWatcher);
        mSearchInputView.setOnKeyListener(mOnKeyListener);
        mHeaderSelectGroupView.setOnClickListener(onSelectGroupClickListener);
        quickSideBarView.setOnQuickSideBarTouchListener(this);

        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                chooseItem(position);
            }
        });
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        DataObserver.Register(this);

        if (getIntent() != null) {
            group_model = getIntent().getIntExtra("model", 0);
            setting_id = getIntent().getLongExtra("setting_id", 0);
        }

        IMFriendInfo self_info = new IMFriendInfo();
        self_info.userId = NIMUserInfoManager.getInstance().GetSelfUserId();
        self_info.nickName = NIMUserInfoManager.getInstance().GetSelfUserName();

        // 单聊升级为群，默认添加自己和聊天好友
        if (group_model == MODE_TRANSLATE_TO_GROUP) {
            IMFriendInfo friendInfo = NIMFriendInfoManager.getInstance().getFriendUser(setting_id);
            addSelectedAvatar(self_info);
            addSelectedAvatar(friendInfo);
        } else if (group_model == MODE_CREATE_GROUP) {
            addSelectedAvatar(self_info);
        }

        editMinWidth = getResources().getDimensionPixelSize(R.dimen.nim_friend_search_edit_min_width);
        getFriendData();
    }

    private void getFriendData() {
        mFriends = new ArrayList<>();
        List<IMFriendInfo> friends = NIMFriendInfoManager.getInstance().getAllFriendSortList();

        int position = 0;
        List<String> customLetters = quickSideBarView.getLetters();
        customLetters.add("#");

        for (IMFriendInfo user_info : friends) {
            switch (group_model) {
                case MODE_TRANSLATE_TO_GROUP:
                    if (user_info.userId == setting_id) {
                        continue;
                    }
                    break;
                case MODE_CREATE_GROUP:
                    break;
                case MODE_ADD_NEW_MEMBER:
                    IMGroupUserInfo groupUserInfo = NIMGroupUserManager.getInstance().getGroupUserInfo(setting_id, user_info.userId);
                    if (groupUserInfo != null) {
                        continue;
                    }
                    break;
            }

            mFriends.add(user_info);
            boolean can_not_invite = user_info.delete_type == FriendTypeDef.ACTIVE_TYPE.PASSIVE
                    || user_info.black_type == FriendTypeDef.ACTIVE_TYPE.PASSIVE;
            if (can_not_invite) {
                user_info.is_star = true;
            }

            String letter = user_info.getInitial();
            if (letter.equals("|")) {
                letter = "#";
            } else if (letter.equals("~")) {
                letter = "*";
            }

            if (!letters.containsKey(letter)) {
                letters.put(letter, position);
            }

            if (!customLetters.contains(letter)) {
                customLetters.add(letter);
            }
            position++;
        }

        quickSideBarView.setLetters(customLetters);
        headersDecor = new StickyRecyclerHeadersDecoration(mAdapter);
        recyclerView.addItemDecoration(headersDecor);
        recyclerView.addItemDecoration(new DividerDecoration(this));

        mAdapter.setNewData(mFriends);
    }

    private void saveSelected() {
        if (!checkCreateUser()) {
            return;
        }
        if (MODE_CREATE_GROUP == group_model) {
            getCreateInfo();
        } else if (MODE_TRANSLATE_TO_GROUP == group_model) {
            getCreateInfo();
        } else if (MODE_ADD_NEW_MEMBER == group_model) {
            createGroup(mNewChatMembers);
        }
    }

    private boolean checkCreateUser() {
        switch (group_model) {
            case MODE_CREATE_GROUP:
                if (mNewChatMembers.size() == 1) {
                    showToastStr("请选择好友");
                    return false;
                } else if (mNewChatMembers.size() == 2) {
                    IMFriendInfo friendInfo = mNewChatMembers.get(1);
                    long userId = friendInfo.userId;
                    NIMStartActivityUtil.startToScActivity(GroupCreateActivity.this, userId);
                    onBackPressed();
                    return false;
                }
                break;
            case MODE_TRANSLATE_TO_GROUP:
                if (mNewChatMembers.size() <= 2) {
                    showToastStr("请选择好友");
                    return false;
                }
                break;
            case MODE_ADD_NEW_MEMBER:
                if (mNewChatMembers.size() == 0) {
                    showToastStr("请选择好友");
                    return false;
                }
                break;
        }

        return true;
    }

    private void getCreateInfo() {
        GroupGetProcessor processor = GlobalProcessor.getInstance().getGroupGetProcessor();
        processor.SendGroupCreateInfoRQ();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void createGroup(ArrayList<IMFriendInfo> list) {
        showPreDialog("正在操作");
        GroupOperateMode mode1 = new GroupOperateMode();

        if (group_model == MODE_CREATE_GROUP) {
            mode1.big_msg_type = MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_CREATE;
            mode1.group_type = nCreateType;
        } else if (group_model == MODE_TRANSLATE_TO_GROUP) {
            mode1.big_msg_type = MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_CREATE;
            mode1.group_type = nCreateType;
        } else if (group_model == MODE_ADD_NEW_MEMBER) {
            mode1.big_msg_type = MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_ADD_USER;
            groupInfo = NIMGroupInfoManager.getInstance().getGroupInfo(setting_id);
            mode1.group_id = setting_id;
            mode1.group_name = groupInfo.group_name;
        }

        mode1.group_ct = BaseUtil.GetServerTime();
        mode1.msg_time = BaseUtil.GetServerTime();
        mode1.message_id = NetCenter.getInstance().CreateGroupMsgId();

        mode1.operate_user_name = NIMUserInfoManager.getInstance().GetSelfUserName();
        mode1.user_id = NIMUserInfoManager.getInstance().GetSelfUserId();
        ArrayList<IMGroupUserInfo> group_users = new ArrayList<>();

        String group_name = "";
        boolean bDone = false;
        for (IMFriendInfo friendInfo : list) {
            // 取出10个字的群名，不在循环
            if (groupInfo == null || TextUtils.isEmpty(groupInfo.group_name)) {
                if (!bDone) {
                    group_name += friendInfo.nickName;
                    if (group_name.length() < 10) {
                        group_name += "、";
                    } else {
                        group_name += "等";
                        bDone = true;
                    }
                }
            }

            IMGroupUserInfo group_user = new IMGroupUserInfo();
            group_user.user_nick_name = Utils.getUserShowName(new String[]{friendInfo.nickName, friendInfo.user_name});
            group_user.user_id = friendInfo.userId;
            group_users.add(group_user);
        }

        if (groupInfo == null || TextUtils.isEmpty(groupInfo.group_name)) {
            group_name = group_name + "的钱宝群";
            mode1.group_name = group_name;
        }

        mode1.user_info_list = group_users;
        GroupOperateProcessor operateProcessor = GlobalProcessor.getInstance().getGroupOperateProcessor();
        if (group_model == MODE_ADD_NEW_MEMBER) {
            operateProcessor.sendGroupModifyRQ(mode1);
        } else {
            operateProcessor.sendGroupCreateRQ(mode1);
        }
    }

    private View.OnFocusChangeListener mEditTextOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            mSearchLayout.setEnabled(hasFocus);
            if (!hasFocus) {
                final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mSearchInputView.getWindowToken(), 0);
            }
        }
    };

    private TextWatcher mEditTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String content = s.toString();
            HashMap<Integer, String> keyword_map = new HashMap<>();

            if (TextUtils.isEmpty(content)) {
                mAdapter.setKeyword_map(keyword_map);
                mAdapter.setNewData(mFriends);
            } else {
                boolean is_letter = true;
                char[] nameChar = content.toCharArray();
                for (int i = 0; i < nameChar.length; i++) {
                    is_letter = Utils.isLetter(nameChar[i]);
                    if (!is_letter)
                        break;
                }
                if (mFriends != null) {
                    content = content.toUpperCase();
                    ArrayList<IMFriendInfo> search_list = new ArrayList<>();
                    for (IMFriendInfo friend : mFriends) {
                        // 纯字母
                        if (is_letter) {
                            String keyword;
                            keyword = Utils.containInput(content.toUpperCase(), friend.remark_name, friend.remark_index);
                            if (TextUtils.isEmpty(keyword)) {
                                keyword = Utils.containInput(content.toUpperCase(), friend.nickName, friend.nick_index);
                            }
                            if (!TextUtils.isEmpty(keyword)) {
                                search_list.add(friend);
                                keyword_map.put(search_list.size() - 1, keyword);
                            }
                            // 非字母
                        } else {
                            String str = "";
                            if (!TextUtils.isEmpty(friend.remark_name))
                                str += friend.remark_name;
                            if (!TextUtils.isEmpty(friend.nickName))
                                str += friend.nickName;
                            if (!TextUtils.isEmpty(str)) {
                                if (str.contains(content)) {
                                    search_list.add(friend);
                                    keyword_map.put(search_list.size() - 1, content);
                                }
                            }
                        }
                    }

                    mAdapter.setKeyword_map(keyword_map);
                    mAdapter.setNewData(search_list);
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private View.OnKeyListener mOnKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                String content = mSearchInputView.getText().toString();
                int length = content.length();
                if (length == 0) {
                    removeSelectedAvatarFromLast();
                }
            }
            return false;
        }
    };

    private View.OnClickListener onSelectGroupClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(GroupCreateActivity.this, GroupSelectActivity.class);
            intent.putExtra("model", group_model);
            startActivityForResult(intent, REQUEST_CODE_GROUP_SELECTED);
        }
    };

    private void chooseItem(int position) {
        final IMFriendInfo friend = mAdapter.getItem(position);
        if (groupInfo != null) {
            if (mNewChatMembers.size() + groupInfo.group_count >= groupInfo.group_max_count) {
                showToastStr("超过群限制人数");
                return;
            }

            if (mNewChatMembers.size() > groupInfo.group_add_max_count) {
                showToastStr("一次性最多可邀请" + groupInfo.group_add_max_count);
                return;
            }
        }

        if (group_model == MODE_ADD_NEW_MEMBER) {
            IMGroupUserInfo groupUserInfo = NIMGroupUserManager.getInstance().getGroupUserInfo(setting_id, friend.userId);
            if (groupUserInfo != null) {
                showToastStr("好友已是群成员，请添加其他好友");
                return;
            }
        }

        if (friend.black_type == FriendTypeDef.ACTIVE_TYPE.PASSIVE) {
            String name = Utils.getUserShowName(new String[]{friend.remark_name, friend.nickName, friend.user_name});
            showToastStr(name + "拒绝加入群聊");
            return;
        }

        if (friend.delete_type == FriendTypeDef.ACTIVE_TYPE.PASSIVE) {
            String name = Utils.getUserShowName(new String[]{friend.remark_name, friend.nickName, friend.user_name});
            ProgressDialog.showCustomDialog(GroupCreateActivity.this, "提示", name + "未把你添加到通讯录，需要发送好友申请，等对方通过。是否发送?"
                    , new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendFriendReq(friend);
                            showToastStr("已发送");
                        }
                    });
            return;
        }

        if (MODE_CHOOSE == group_model && mNewChatMembers.size() > 0 && !friend.is_select) {
            return;
        }

        friend.is_select = !friend.is_select;
        if (friend.is_select) {
            addSelectedAvatar(friend);
        } else {
            removeSelectedAvatar(friend);
        }

        mAdapter.setData(position, friend);
        groupHandler.postDelayed(mCheckScrollViewWidthRunnable, 50);

        setSelectedCount();
    }


    private void sendFriendReq(IMFriendInfo friendInfo) {
        IMFriendInfo info = new IMFriendInfo();
        info.opt_msg = "我是" + NIMUserInfoManager.getInstance().GetSelfUserName();
        info.userId = friendInfo.userId;
        info.source_type = friendInfo.source_type;
        info.nickName = NIMUserInfoManager.getInstance().GetSelfUserName();

        FriendAddProcessor processor = GlobalProcessor.getInstance().getFriendAddProcessor();
        processor.sendFriendAddRQ(info);
    }

    private void addSelectedAvatar(IMFriendInfo friend) {
        if (mNewChatMembers == null) {
            mNewChatMembers = new ArrayList<>();
        }
        mNewChatMembers.add(friend);

        if (mNewChatMembers.size() > 0) {
            if (avatarScrollView.getVisibility() == View.GONE)
                avatarScrollView.setVisibility(View.VISIBLE);
        }

        View avatarView = mInflater.inflate(R.layout.nim_select_friend_avatar, null);
        ImageView imgAvatar = (ImageView) avatarView.findViewById(R.id.friend_avatar);
        Glide.with(this).load(AppUtil.getHeadUrl(friend.userId)).placeholder(R.mipmap.nim_head).into(imgAvatar);
        avatarView.setTag(friend);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        avatarLayout.addView(avatarView, params);

        avatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag() instanceof IMFriendInfo) {
                    IMFriendInfo tFriend = (IMFriendInfo) v.getTag();
                    if (tFriend.userId == NIMUserInfoManager.getInstance().GetSelfUserId()) {
                        showToastStr("建群无法取消自己");
                        return;
                    }

                    if (tFriend.userId == setting_id) {
                        showToastStr("无法取消当前好友");
                        return;
                    }

                    int position = getIndexByFriend(tFriend);
                    if (position >= 0) {
                        tFriend.is_select = false;
                        mAdapter.setData(position, tFriend);
                    }

                    removeSelectedAvatar(tFriend);
                    removeFromSelectedList(tFriend);
                    groupHandler.postDelayed(mCheckScrollViewWidthRunnable, 50);
                    setSelectedCount();
                }
            }
        });
    }

    private int getIndexByFriend(IMFriendInfo friendInfo) {
        int nSize = mAdapter.getData().size();
        for (int i = 0; i < nSize; i++) {
            if (friendInfo.userId == mAdapter.getItem(i).userId) {
                return i;
            }
        }

        return -1;
    }

    private void removeSelectedAvatar(IMFriendInfo friend) {
        removeFromSelectedList(friend);

        int count = avatarLayout.getChildCount();
        int i = 0;
        for (; i < count; i++) {
            View avatarView = avatarLayout.getChildAt(i);
            if (avatarView.getTag() instanceof IMFriendInfo) {
                IMFriendInfo tFriend = (IMFriendInfo) avatarView.getTag();
                if (friend != null && friend == tFriend) {
                    break;
                }
            }
        }

        if (i < count) {
            avatarLayout.removeViewAt(i);
        }
    }

    private void removeSelectedAvatarFromLast() {
        int count = avatarLayout.getChildCount();
        if (count == 0) return;
        View avatarView = avatarLayout.getChildAt(count - 1);
        if (avatarView == null) {
            return;
        }
        IMFriendInfo friend = (IMFriendInfo) avatarView.getTag();
        if (friend.userId == NIMUserInfoManager.getInstance().GetSelfUserId()) {
            showToastStr("建群无法取消自己");
            return;
        }

        if (friend.userId == setting_id) {
            showToastStr("无法取消当前好友");
            return;
        }

        int position = getIndexByFriend(friend);
        if (position >= 0) {
            friend.is_select = false;
            mAdapter.setData(position, friend);
        }

        avatarLayout.removeView(avatarView);
        removeFromSelectedList(friend);
        groupHandler.postDelayed(mCheckScrollViewWidthRunnable, 50);
        setSelectedCount();
    }

    private void setSelectedCount() {
        if (mNewChatMembers == null) {
            mNewChatMembers = new ArrayList<>();
        }
        if (mNewChatMembers.isEmpty()) {
            tvRight.setText("确定");
            tvRight.setEnabled(false);
        } else {
            String title = String.format("确定(%s)", mNewChatMembers.size());
            tvRight.setText(title);
            tvRight.setEnabled(true);
        }
    }

    private void removeFromSelectedList(IMFriendInfo friend) {
        friend.is_select = false;
        mNewChatMembers.remove(friend);

        if (mNewChatMembers.size() == 0) {
            if (avatarScrollView.getVisibility() == View.VISIBLE)
                avatarScrollView.setVisibility(View.GONE);
        }
    }

    private Runnable mCheckScrollViewWidthRunnable = new Runnable() {
        @Override
        public void run() {
            checkScrollViewWidth();
            groupHandler.postDelayed(mScrollRunnable, 50);
        }
    };

    private Runnable mScrollRunnable = new Runnable() {
        @Override
        public void run() {
            avatarScrollView.smoothScrollTo(avatarLayout.getWidth(), 0);
        }
    };

    private void checkScrollViewWidth() {
        int maxScrollViewWidth = mSearchLayout.getWidth() - editMinWidth;
        if (avatarLayout.getWidth() > maxScrollViewWidth) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) avatarScrollView.getLayoutParams();
            params.width = maxScrollViewWidth;
            avatarScrollView.setLayoutParams(params);
        } else {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) avatarScrollView.getLayoutParams();
            params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            avatarScrollView.setLayoutParams(params);
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
            case DataConstDef.EVENT_GROUP_CREATE:
            {
                hidePreDialog();
                GcMessageModel operateMode = (GcMessageModel) param2;
                createHeadUrl(operateMode.group_id);
                break;
            }
            case DataConstDef.EVENT_GROUP_OPERATE:
            {
                hidePreDialog();
                onBackPressed();
                break;
            }
            case DataConstDef.EVENT_MESSAGE_TIME_OUT:
            case DataConstDef.EVENT_NET_ERROR:
                hidePreDialog();
                Logger.error(TAG, "EVENT_NET_ERROR");
                if (is_active) {
                    int error_code = BaseUtil.MakeErrorResult((int) param2);
                    String error_msg = ErrorDetail.GetErrorDetail(error_code);
                    showToastStr(error_msg);
                }
                break;
            case DataConstDef.EVENT_GROUP_CREATE_TYPE:
                NIMGroupCreateInfo create_type_info = (NIMGroupCreateInfo) param2;
                if (create_type_info.group_add_max_count < mNewChatMembers.size()) {
                    showToastStr("建群一次性最多可邀请" + create_type_info.group_add_max_count);
                    return;
                }
                nCreateType = create_type_info.group_type;
                createGroup(mNewChatMembers);
                break;
        }
    }

    private void createHeadUrl(Long group_id) {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        int count;
        if (avatarLayout.getChildCount() > GroupAvatarCreator.MAX_COUNT) {
            count = GroupAvatarCreator.MAX_COUNT;
        } else {
            count = avatarLayout.getChildCount();
        }
        for (int i = 0; i < count; i++) {
            ViewGroup view = (ViewGroup) avatarLayout.getChildAt(i);
            ImageView avatar = (ImageView) view.getChildAt(0);
            avatar.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(avatar.getDrawingCache());
            bitmaps.add(bitmap);
            avatar.setDrawingCacheEnabled(false);
        }
        Bitmap new_bitmap = GroupAvatarCreator.combineBitmap(bitmaps);
        UploadImageUtil.upLoadFile(new_bitmap, group_id);

        NIMStartActivityUtil.startToGcActivity(this, group_id);
    }

    @Override
    public void onLetterChanged(String letter, int position, float y) {
        quickSideBarTipsView.setText(letter, position, y);
        //有此key则获取位置并滚动到该位置
        if(letters.containsKey(letter)) {
            recyclerView.scrollToPosition(letters.get(letter));
        }
    }

    @Override
    public void onLetterTouching(boolean touching) {
        quickSideBarTipsView.setVisibility(touching? View.VISIBLE:View.INVISIBLE);
    }

}
