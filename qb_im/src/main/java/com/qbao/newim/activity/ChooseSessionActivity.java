package com.qbao.newim.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qbao.newim.adapter.RecentChatAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.manager.GcChatSendManager;
import com.qbao.newim.manager.NIMComplexManager;
import com.qbao.newim.manager.NIMGroupMsgManager;
import com.qbao.newim.manager.NIMMsgManager;
import com.qbao.newim.manager.NIMSessionManager;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.manager.ScChatSendManager;
import com.qbao.newim.model.NIM_Chat_ID;
import com.qbao.newim.model.SessionModel;
import com.qbao.newim.model.message.GcMessageModel;
import com.qbao.newim.model.message.ScMessageModel;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.util.NIMStartActivityUtil;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.BaseUtil;
import com.qbao.newim.util.ShowUtils;
import com.qbao.newim.util.Utils;
import com.qbao.newim.views.MultiEditText;
import com.qbao.newim.views.dialog.Effectstype;
import com.qbao.newim.views.dialog.NiftyDialogBuilder;
import com.qbao.newim.views.imgpicker.NIM_ToolbarAct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by chenjian on 2017/6/12.
 */

public class ChooseSessionActivity extends NIM_ToolbarAct {

    private ArrayList<SessionModel> mList;
    private TextView tv_new_chat;
    private RecentChatAdapter mAdapter;
    private RecyclerView recyclerView;
    private int chat_type;
    private long message_id;
    private long current_id;
    private String data;
    private MultiEditText editText;
    private static final int REQUEST_CODE_NEW_CHAT = 100;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.nim_activity_choose_session);

        tv_new_chat = (TextView) findViewById(R.id.choose_session_new_chat);
        recyclerView = (RecyclerView) findViewById(R.id.choose_session_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        editText = (MultiEditText) findViewById(R.id.keyword);

        if (getIntent() != null) {
            data = getIntent().getStringExtra("data");
            parseData(data);
        }
    }

    @Override
    protected void setListener() {
        mList = NIMSessionManager.getInstance().GetGSessionList(false);
        tv_new_chat.setOnClickListener(this);
        mAdapter = new RecentChatAdapter(mList);
        recyclerView.setAdapter(mAdapter);
        editText.addTextChangedListener(mEditTextWatcher);
        editText.setOnFocusChangeListener(mEditTextOnFocusChangeListener);

        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                SessionModel cur_model = mAdapter.getItem(position);
                String session_name = NIMComplexManager.getInstance().GetSessionName(cur_model.session_id, cur_model.chat_type);
                showRepeatDialog(cur_model.chat_type, cur_model.session_id, session_name);
            }
        });
    }

    private void showRepeatDialog(final int type, final long id, String name) {
        NiftyDialogBuilder dialogBuilder = new NiftyDialogBuilder(ChooseSessionActivity.this);
        View custom_view = LayoutInflater.from(ChooseSessionActivity.this).inflate(R.layout.nim_repeat_content, null);
        dialogBuilder.withDuration(400)
                .withTitle("确定转发到:")
                .withTitleColor(Color.parseColor("#FF2E2D2D"))
                .withButton1Text("取消")
                .withButton2Text("确定")
                .withEffect(Effectstype.SlideBottom)
                .isCancelableOnTouchOutside(false)
                .setCustomViewDialog(custom_view)
                .setButton2Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        repeatMsg(type, id);
                    }
                })
                .setButton1Click(null)
                .show();
        ImageView iv_head = (ImageView) custom_view.findViewById(R.id.repeat_dialog_head);
        TextView tv_name = (TextView) custom_view.findViewById(R.id.repeat_dialog_name);
        String url = type == MsgConstDef.MSG_CHAT_TYPE.GROUP ? AppUtil.getGroupUrl(id) : AppUtil.getHeadUrl(id);
        Glide.with(ChooseSessionActivity.this).load(url).placeholder(R.mipmap.nim_head).into(iv_head);
        tv_name.setText(name);
    }

    private void repeatMsg(int type, long id) {
        if (type == 0 || id == 0) {
            return;
        }

        showPreDialog("");
        // 当前消息是群消息
        if (chat_type == MsgConstDef.MSG_CHAT_TYPE.GROUP){
            GcMessageModel gcMessageModel = NIMGroupMsgManager.getInstance().getGroupMessageInfoByMsgID(current_id, message_id);
            // 群消息转给群
            if (type == MsgConstDef.MSG_CHAT_TYPE.GROUP) {
                gcMessageModel.msg_status = MsgConstDef.MSG_STATUS.SENDING;
                gcMessageModel.is_self = true;
                gcMessageModel.msg_time = BaseUtil.GetServerTime();
                gcMessageModel.send_user_name = NIMUserInfoManager.getInstance().GetSelfUserName();
                gcMessageModel.group_id = id;
                gcMessageModel.chat_type = MsgConstDef.MSG_CHAT_TYPE.GROUP;
                gcMessageModel.message_id = NetCenter.getInstance().CreateGroupMsgId();
                GcChatSendManager.getInstance().send(gcMessageModel);
            // 群消息转给私聊
            } else if (type == MsgConstDef.MSG_CHAT_TYPE.PRIVATE){
                ScMessageModel scMessageModel = new ScMessageModel();
                scMessageModel.opt_user_id = id;
                scMessageModel.is_self = true;
                scMessageModel.message_id = NetCenter.getInstance().CreateMsgID();
                scMessageModel.chat_type = MsgConstDef.MSG_CHAT_TYPE.PRIVATE;
                scMessageModel.msg_time = BaseUtil.GetServerTime();
                scMessageModel.send_user_name = NIMUserInfoManager.getInstance().GetSelfUserName();
                scMessageModel.msg_status = MsgConstDef.MSG_STATUS.SENDING;
                scMessageModel.pic_path = gcMessageModel.pic_path;
                scMessageModel.compress_path = gcMessageModel.compress_path;
                scMessageModel.audio_path = gcMessageModel.audio_path;
                scMessageModel.ext_type = gcMessageModel.ext_type;
                scMessageModel.b_id = gcMessageModel.b_id;
                scMessageModel.c_id = gcMessageModel.c_id;
                scMessageModel.app_id = gcMessageModel.app_id;
                scMessageModel.msg_content = gcMessageModel.msg_content;
                scMessageModel.m_type = gcMessageModel.m_type;
                scMessageModel.s_type = gcMessageModel.s_type;
                scMessageModel.w_id = gcMessageModel.w_id;
                ScChatSendManager.getInstance().send(scMessageModel);
            }
        // 当前消息是私聊消息
        } else if (chat_type == MsgConstDef.MSG_CHAT_TYPE.PRIVATE){
            ScMessageModel scMessageModel = NIMMsgManager.getInstance().GetMessageByMessageID(new NIM_Chat_ID(current_id, message_id));
            // 私聊消息转给群
            if (type == MsgConstDef.MSG_CHAT_TYPE.GROUP) {
                GcMessageModel gcMessageModel = new GcMessageModel();
                gcMessageModel.group_id = id;
                gcMessageModel.is_self = true;
                gcMessageModel.message_id = NetCenter.getInstance().CreateGroupMsgId();
                gcMessageModel.chat_type = MsgConstDef.MSG_CHAT_TYPE.GROUP;
                gcMessageModel.msg_time = BaseUtil.GetServerTime();
                gcMessageModel.send_user_name = NIMUserInfoManager.getInstance().GetSelfUserName();
                gcMessageModel.msg_status = MsgConstDef.MSG_STATUS.SENDING;
                gcMessageModel.pic_path = scMessageModel.pic_path;
                gcMessageModel.compress_path = scMessageModel.compress_path;
                gcMessageModel.ext_type = scMessageModel.ext_type;
                gcMessageModel.b_id = scMessageModel.b_id;
                gcMessageModel.audio_path = scMessageModel.audio_path;
                gcMessageModel.c_id = scMessageModel.c_id;
                gcMessageModel.app_id = scMessageModel.app_id;
                gcMessageModel.msg_content = scMessageModel.msg_content;
                gcMessageModel.m_type = scMessageModel.m_type;
                gcMessageModel.s_type = scMessageModel.s_type;
                gcMessageModel.w_id = scMessageModel.w_id;
                GcChatSendManager.getInstance().send(gcMessageModel);
            // 私聊消息转给私聊
            } else if (type == MsgConstDef.MSG_CHAT_TYPE.PRIVATE){
                scMessageModel.opt_user_id = id;
                scMessageModel.is_self = true;
                scMessageModel.msg_time = BaseUtil.GetServerTime();
                scMessageModel.send_user_name = NIMUserInfoManager.getInstance().GetSelfUserName();
                scMessageModel.msg_status = MsgConstDef.MSG_STATUS.SENDING;
                scMessageModel.chat_type = MsgConstDef.MSG_CHAT_TYPE.PRIVATE;
                scMessageModel.message_id = NetCenter.getInstance().CreateMsgID();
                ScChatSendManager.getInstance().send(scMessageModel);
            }
        }

        hidePreDialog();
        ShowUtils.showToast("已转发");
        onBackPressed();
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.nim_toolbar_title, menu);
        MenuItem menuItem = menu.findItem(R.id.item_toolbar);
        View actionView = menuItem.getActionView();

        TextView tvTitle = (TextView) actionView.findViewById(R.id.title_txt);
        tvTitle.setText("选择");
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
        if (v.getId() == R.id.choose_session_new_chat) {
            NIMStartActivityUtil.startToCardActivity(ChooseSessionActivity.this, data, REQUEST_CODE_NEW_CHAT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_NEW_CHAT:
                    if (data != null) {
                        long id = data.getLongExtra("id", 0);
                        int type = data.getIntExtra("type", 0);
                        String name = data.getStringExtra("name");
                        String back_data = data.getStringExtra("data");
                        parseData(back_data);
                        showRepeatDialog(type, id, name);
                    }
                    break;
            }
        }
    }

    private void parseData(String data) {
        String[] array = data.split("_");
        chat_type = Integer.parseInt(array[0]);
        message_id = Long.parseLong(array[1]);
        current_id = Long.parseLong(array[2]);
    }

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
                mAdapter.setNewData(mList);
            } else {
                boolean is_letter = true;
                char[] nameChar = content.toCharArray();
                for (int i = 0; i < nameChar.length; i++) {
                    is_letter = Utils.isLetter(nameChar[i]);
                    if (!is_letter)
                        break;
                }
                if (mList != null) {
                    content = content.toUpperCase();
                    ArrayList<SessionModel> search_list = new ArrayList<>();
                    for (SessionModel model : mList) {
                        String session_name = NIMComplexManager.getInstance().GetSessionName(model.session_id, model.chat_type);
                        // 纯字母
                        if (is_letter) {
                            String keyword;
                            LinkedHashMap<Integer, String> s_name_index = Utils.converterToSpellMap(session_name);
                            keyword = Utils.containInput(content.toUpperCase(), session_name, s_name_index);
                            if (!TextUtils.isEmpty(keyword)) {
                                search_list.add(model);
                                keyword_map.put(search_list.size() - 1, keyword);
                            }
                            // 非字母
                        } else {
                            String str = "";
                            if (!TextUtils.isEmpty(session_name))
                                str += session_name;
                            if (!TextUtils.isEmpty(str)) {
                                if (str.contains(content)) {
                                    search_list.add(model);
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

    private View.OnFocusChangeListener mEditTextOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            editText.setEnabled(hasFocus);
            if (!hasFocus) {
                final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }
        }
    };
}
