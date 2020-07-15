package com.qbao.newim.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.qbao.newim.adapter.GroupCreateAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.manager.NIMFriendInfoManager;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.model.NIMCardInfo;
import com.qbao.newim.util.NIMStartActivityUtil;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.Utils;
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
 * Created by chenjian on 2017/7/19.
 */

public class NIMChooseCardActivity extends NIM_ToolbarAct implements OnQuickSideBarTouchListener {

    private LayoutInflater mInflater;

    private EditText mSearchInputView;
    private LinearLayout mSearchLayout;
    private LinearLayout mHeaderView;
    private TextView mHeaderSelectGroupView;
    HashMap<String,Integer> letters = new HashMap<>();
    QuickSideBarView quickSideBarView;
    QuickSideBarTipsView quickSideBarTipsView;
    StickyRecyclerHeadersDecoration headersDecor;
    private RecyclerView recyclerView;
    private GroupCreateAdapter mAdapter;

    private ArrayList<IMFriendInfo> mFriends;

    private int type;
    private static final int CHOOSE_CARD = 0;
    private static final int CHOOSE_CHAT = 1;

    private static final int REQUEST_CODE_GROUP = 100;
    private String data;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.nim_activity_group_create);
        mInflater = LayoutInflater.from(this);

        if (getIntent() != null) {
            data = getIntent().getStringExtra("data");
            if (TextUtils.isEmpty(data)) {
                type = CHOOSE_CARD;
            } else {
                type = CHOOSE_CHAT;
            }
        }

        mSearchInputView = (EditText) findViewById(R.id.search_edittext);

        recyclerView = (RecyclerView) findViewById(R.id.group_create_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new GroupCreateAdapter(null);
        recyclerView.setAdapter(mAdapter);
        quickSideBarView = (QuickSideBarView) findViewById(R.id.quickSideBarView);
        quickSideBarTipsView = (QuickSideBarTipsView) findViewById(R.id.quickSideBarTipsView);

        mSearchLayout = (LinearLayout) findViewById(R.id.search_layout1);
        mHeaderView = (LinearLayout) mInflater.inflate(R.layout.nim_group_create_header, null);
        mHeaderSelectGroupView = (TextView) mHeaderView.findViewById(R.id.tv_select_group);
        if (type == CHOOSE_CHAT) {
            mHeaderSelectGroupView.setText("选择一个群");
        } else {
            mHeaderSelectGroupView.setText("公众号");
        }


        mSearchLayout.setEnabled(false);
        mAdapter.addHeaderView(mHeaderView);
    }

    @Override
    protected void setListener() {
        quickSideBarView.setOnQuickSideBarTouchListener(this);
        mSearchInputView.setOnFocusChangeListener(mEditTextOnFocusChangeListener);
        mSearchInputView.addTextChangedListener(mEditTextWatcher);
        mHeaderSelectGroupView.setOnClickListener(onSelectGroupClickListener);
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                chooseItem(mAdapter.getItem(position));
            }
        });
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        mFriends = new ArrayList<>();
        getFriendData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nim_toolbar_title, menu);
        MenuItem menuItem = menu.findItem(R.id.item_toolbar);
        View actionView = menuItem.getActionView();

        TextView tvTitle = (TextView) actionView.findViewById(R.id.title_txt);

        if (type == CHOOSE_CHAT) {
            tvTitle.setText("选择聊天");
        } else {
            tvTitle.setText("选择联系人");
        }
        actionView.findViewById(R.id.title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {

            String content = s.toString();
            HashMap<Integer, String> keyword_map = new HashMap<>();
            if (TextUtils.isEmpty(content)) {
                mAdapter.setKeyword_map(keyword_map);
                mAdapter.setNewData(mFriends);
                quickSideBarView.setVisibility(View.VISIBLE);
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
                    quickSideBarView.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };


    private void chooseItem(IMFriendInfo friend) {
        Intent intent = new Intent();
        if (type == CHOOSE_CARD) {
            NIMCardInfo info = new NIMCardInfo();
            info.type = 0;
            info.id = friend.userId;
            info.name = friend.nickName;
            info.user_name = friend.user_name;
            intent.putExtra("card_info", new Gson().toJson(info));
        } else {
            intent.putExtra("id", friend.userId);
            intent.putExtra("name", Utils.getUserShowName(new String[]{friend.remark_name, friend.nickName, friend.user_name}));
            intent.putExtra("type", MsgConstDef.MSG_CHAT_TYPE.PRIVATE);
            intent.putExtra("data", data);
        }

        setResult(RESULT_OK, intent);
        onBackPressed();
    }

    private View.OnClickListener onSelectGroupClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (type == CHOOSE_CHAT) {
                NIMStartActivityUtil.startToChooseGroupActivity(NIMChooseCardActivity.this, data, REQUEST_CODE_GROUP);
            } else {
                showToastStr("进入公众号");
            }
        }
    };

    private void getFriendData() {
        List<IMFriendInfo> friends = NIMFriendInfoManager.getInstance().getAllFriendList();
        mFriends.addAll(friends);
        Collections.sort(mFriends);
        int position = 0;
        List<String> customLetters = quickSideBarView.getLetters();
        customLetters.add("#");

        for (IMFriendInfo user_info : mFriends) {
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

        mAdapter.setNewData(mFriends);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_GROUP:
                    if (data != null) {
                        setResult(RESULT_OK, data);
                        onBackPressed();
                    }
                    break;
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
