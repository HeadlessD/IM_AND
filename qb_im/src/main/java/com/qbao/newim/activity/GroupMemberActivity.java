package com.qbao.newim.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qbao.newim.adapter.GroupMemberAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.configure.GlobalVariable;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.ErrorDetail;
import com.qbao.newim.constdef.FriendTypeDef;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.helper.IGroupKickMember;
import com.qbao.newim.manager.NIMGroupInfoManager;
import com.qbao.newim.manager.NIMGroupUserManager;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.GroupOperateMode;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.model.IMGroupUserInfo;
import com.qbao.newim.model.message.GcMessageModel;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.util.NIMStartActivityUtil;
import com.qbao.newim.processor.GlobalProcessor;
import com.qbao.newim.processor.GroupOperateProcessor;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.BaseUtil;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.IDataObserver;
import com.qbao.newim.util.Logger;
import com.qbao.newim.util.Utils;
import com.qbao.newim.views.MultiEditText;
import com.qbao.newim.views.ProgressDialog;
import com.qbao.newim.views.StickyRecyclerHeadersDecoration;
import com.qbao.newim.views.imgpicker.NIM_ToolbarAct;
import com.qbao.newim.views.quick_bar.DividerDecoration;
import com.qbao.newim.views.quick_bar.OnQuickSideBarTouchListener;
import com.qbao.newim.views.quick_bar.QuickSideBarTipsView;
import com.qbao.newim.views.quick_bar.QuickSideBarView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chenjian on 2017/6/29.
 */

public class GroupMemberActivity extends NIM_ToolbarAct implements OnQuickSideBarTouchListener, IDataObserver{

    public static final int GROUP_PERMISSION = 2;        // 当前群主转让界面
    public static final int GROUP_MEMBER = 1;            // 当前群成员界面
    private int nType = -1;

    private MultiEditText mSearchInputView;
    private LinearLayout mSearchLayout;

    private ArrayList<IMGroupUserInfo> mLists = new ArrayList<>();
    private long group_id;

    private boolean is_edit_model;
    private boolean is_admin;
    private TextView tvRight;

    HashMap<String,Integer> letters = new HashMap<>();
    QuickSideBarView quickSideBarView;
    QuickSideBarTipsView quickSideBarTipsView;
    StickyRecyclerHeadersDecoration headersDecor;
    private RecyclerView recyclerView;

    private GroupMemberAdapter mAdapter;

    IMGroupInfo groupInfo;
    private List<IMGroupUserInfo> kick_list = new ArrayList<>();

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.nim_chat_card_list);

        if (getIntent() != null) {
            group_id = getIntent().getLongExtra("group_id", 0);
            nType = getIntent().getIntExtra("type", -1);
        }

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new GroupMemberAdapter(null);
        recyclerView.setAdapter(mAdapter);
        quickSideBarView = (QuickSideBarView) findViewById(R.id.quickSideBarView);
        quickSideBarTipsView = (QuickSideBarTipsView) findViewById(R.id.quickSideBarTipsView);

        mSearchInputView = (MultiEditText) findViewById(R.id.keyword);
        mSearchInputView.setText("");
        mSearchLayout = (LinearLayout) findViewById(R.id.search_layout1);

        mSearchLayout.setEnabled(false);

    }

    @Override
    protected void setListener() {
        quickSideBarView.setOnQuickSideBarTouchListener(this);
        mSearchInputView.setOnFocusChangeListener(mEditTextOnFocusChangeListener);
        mSearchInputView.addTextChangedListener(mEditTextWatcher);
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                IMGroupUserInfo user_info = mAdapter.getItem(position);
                long userId = user_info.user_id;
                if (nType == GROUP_MEMBER) {
                    NIMStartActivityUtil.startToNIMUserActivity(GroupMemberActivity.this, userId,
                            FriendTypeDef.FRIEND_SOURCE_TYPE.CHATTING);
                } else {
                    if (userId == groupInfo.group_manager_user_id) {
                        showToastStr("已经是群主了");
                        return;
                    }
                    showNewManagerDialog(user_info);
                }
            }
        });

        mAdapter.setListener(new IGroupKickMember() {
            @Override
            public void onKick(IMGroupUserInfo info, boolean checked) {
                if (checked) {
                    if (kick_list.size() >= GlobalVariable.MANAGER_KICK_COUNT) {
                        showToastStr("一次最多删除" + GlobalVariable.MANAGER_KICK_COUNT + "人");
                        return;
                    }
                    int index = mLists.indexOf(info);
                    info.is_select = true;
                    mLists.set(index, info);
                    kick_list.add(info);
                } else {
                    int index = mLists.indexOf(info);
                    info.is_select = false;
                    mLists.set(index, info);
                    kick_list.remove(info);
                }

                if (kick_list.size() > 0) {
                    tvRight.setText("删除(" + kick_list.size() + ")");
                } else {
                    tvRight.setText("取消");
                }
            }
        });
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        DataObserver.Register(this);

        if (getIntent() != null)
            is_admin = getIntent().getBooleanExtra("admin", false);

        groupInfo = NIMGroupInfoManager.getInstance().getGroupInfo(group_id);
        mAdapter.setGroup_manager_id(groupInfo.group_manager_user_id);

        getData();
    }

    private void getData() {

        mLists = NIMGroupUserManager.getInstance().getGroupAllUser(group_id, false);
        if (mLists.isEmpty()) {
            return;
        }

        Collections.sort(mLists);
        int position = 0;
        List<String> customLetters = quickSideBarView.getLetters();
        customLetters.add("#");

        for (IMGroupUserInfo user_info : mLists) {

            String letter = user_info.getInitial();
            if (letter.equals("|")) {
                letter = "#";
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

        mAdapter.setNewData(mLists);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nim_toolbar_title, menu);
        MenuItem menuItem = menu.findItem(R.id.item_toolbar);
        View actionView = menuItem.getActionView();

        if (getIntent() != null) {
            is_admin = getIntent().getBooleanExtra("admin", false);
            nType = getIntent().getIntExtra("type", -1);
        }

        tvRight = (TextView) actionView.findViewById(R.id.title_right);
        tvRight.setVisibility(View.GONE);
        if (nType == GROUP_MEMBER) {
            if (is_admin) {
                tvRight.setVisibility(View.VISIBLE);
                tvRight.setText(is_edit_model ? "取消" : "编辑");
            }
        }

        TextView tvTitle = (TextView) actionView.findViewById(R.id.title_txt);

        if (nType == GROUP_MEMBER) {
            tvTitle.setText("群成员");
        } else {
            tvTitle.setText("选择新群主");
        }

        actionView.findViewById(R.id.title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_edit_model) {
                    if (kick_list.size() > 0) {
                        showDeleteDialog();
                    } else {
                        is_edit_model = !is_edit_model;
                        kick_list.clear();
                        mAdapter.setIs_edit_model(is_edit_model);
                        mAdapter.notifyDataSetChanged();
                        tvRight.setText("编辑");
                    }
                } else {
                    is_edit_model = !is_edit_model;
                    tvRight.setText("取消");
                    mAdapter.setIs_edit_model(is_edit_model);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

        return true;
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
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            String content = s.toString();
            HashMap<Integer, String> keyword_map = new HashMap<>();

            if (TextUtils.isEmpty(content)) {
                mAdapter.setKeyword_map(keyword_map);
                mAdapter.setNewData(mLists);

            } else {
                boolean is_letter = true;
                char[] nameChar = content.toCharArray();
                for (int i = 0; i < nameChar.length; i++) {
                    is_letter = Utils.isLetter(nameChar[i]);
                    if (!is_letter)
                        break;
                }
                if (mLists != null) {
                    content = content.toUpperCase();
                    ArrayList<IMGroupUserInfo> search_list = new ArrayList<>();
                    for (IMGroupUserInfo user_info : mLists) {
                        // 纯字母
                        if (is_letter) {
                            String keyword;
                            keyword = Utils.containInput(content.toUpperCase(), user_info.user_nick_name, user_info.nick_index);
                            if (!TextUtils.isEmpty(keyword)) {
                                search_list.add(user_info);
                                keyword_map.put(search_list.size() - 1, keyword);
                            }
                            // 非字母
                        } else {
                            String str = "";
                            if (!TextUtils.isEmpty(user_info.pinyin))
                                str += user_info.pinyin;
                            if (!TextUtils.isEmpty(user_info.user_nick_name))
                                str += "" + user_info.user_nick_name;
                            if (!TextUtils.isEmpty(str)) {
                                if (str.contains(content)) {
                                    search_list.add(user_info);
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
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private void createQuitModel() {
        showPreDialog("");
        GroupOperateMode model = new GroupOperateMode();
        model.group_id = group_id;
        model.user_id = NIMUserInfoManager.getInstance().GetSelfUserId();
        model.message_id = NetCenter.getInstance().CreateGroupMsgId();
        model.big_msg_type = MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_KICK_USER;
        model.msg_time = BaseUtil.GetServerTime();
        model.operate_user_name = NIMUserInfoManager.getInstance().GetSelfUserName();

        model.user_info_list = kick_list;
        GroupOperateProcessor processor = GlobalProcessor.getInstance().getGroupOperateProcessor();
        processor.sendGroupModifyRQ(model);
    }

    private void selectNewManager(long group_id, IMGroupUserInfo groupUserInfo) {
        GroupOperateMode model = new GroupOperateMode();
        model.group_id = group_id;
        model.user_id = NIMUserInfoManager.getInstance().GetSelfUserId();
        model.message_id = NetCenter.getInstance().CreateGroupMsgId();
        model.big_msg_type = MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_LEADER_CHANGE;
        model.msg_time = BaseUtil.GetServerTime();
        model.operate_user_name = NIMUserInfoManager.getInstance().GetSelfUserName();

        List<IMGroupUserInfo> list = new ArrayList<>();
        list.add(groupUserInfo);
        model.user_info_list = list;
        GroupOperateProcessor processor = GlobalProcessor.getInstance().getGroupOperateProcessor();
        processor.sendGroupLeaderChangeRQ(model);
    }

    private void showNewManagerDialog(final IMGroupUserInfo user_info) {
        ProgressDialog.showCustomDialog(this, "提示", "确定选择" + user_info.user_nick_name + "为新群主, 你将自动放弃群主身份"
                , new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectNewManager(group_id, user_info);
                        setResult(RESULT_OK);
                        onBackPressed();
                    }
                });
    }

    private void showDeleteDialog() {
        ProgressDialog.showCustomDialog(this, "提示", "确定删除?", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createQuitModel();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataObserver.Cancel(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void OnChange(int param1, Object param2, Object param3) {
        if (param1 == DataConstDef.EVENT_GROUP_OPERATE) {
            hidePreDialog();
            GcMessageModel operateMode = (GcMessageModel) param2;
            if (operateMode == null) {
                showToastStr("操作失败");
                return;
            }

            if (operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_KICK_USER) {
                showToastStr("删除成功");
                tvRight.setText("编辑");
                mAdapter.setIs_edit_model(false);
                mAdapter.notifyDataSetChanged();
                kick_list.clear();
                getData();
            }
        } else if (param1 == DataConstDef.EVENT_NET_ERROR) {
            Logger.error(TAG, "EVENT_NET_ERROR");
            if (is_active) {
                int error_code = BaseUtil.MakeErrorResult((int) param2);
                String error_msg = ErrorDetail.GetErrorDetail(error_code);
                showToastStr(error_msg);
            }
        }
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
