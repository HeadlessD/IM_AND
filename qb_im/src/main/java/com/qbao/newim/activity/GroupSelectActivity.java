package com.qbao.newim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.qbao.newim.adapter.GroupInfoAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.manager.NIMGroupInfoManager;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.util.NIMStartActivityUtil;
import com.qbao.newim.qbim.R;
import com.qbao.newim.views.imgpicker.NIM_ToolbarAct;

import java.util.ArrayList;

/**
 * Created by chenjian on 2017/6/21.
 */

public class GroupSelectActivity extends NIM_ToolbarAct {

    private ArrayList<IMGroupInfo> mLists = new ArrayList<>();
    private RecyclerView recyclerView;
    private GroupInfoAdapter mAdapter;

    private int type;
    private static final int CHOOSE_GROUP = 1;
    private static final int CHAT_GROUP = 0;
    private String data;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.nim_activity_congenial);

        if (getIntent() != null) {
            data =  getIntent().getStringExtra("data");
            if (TextUtils.isEmpty(data)) {
                type = CHAT_GROUP;
            } else {
                type = CHOOSE_GROUP;
            }
        }
        recyclerView = (RecyclerView) findViewById(R.id.lv_congenial);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new GroupInfoAdapter(mLists);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void setListener() {
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (type == CHOOSE_GROUP) {
                    Intent intent = new Intent();
                    intent.putExtra("id", mAdapter.getItem(position).group_id);
                    intent.putExtra("name", mAdapter.getItem(position).group_name);
                    intent.putExtra("type", MsgConstDef.MSG_CHAT_TYPE.GROUP);
                    intent.putExtra("data", data);
                    setResult(RESULT_OK, intent);
                    onBackPressed();
                } else {
                    NIMStartActivityUtil.startToGcActivity(GroupSelectActivity.this, mAdapter.getItem(position).group_id);
                }
            }
        });
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        mLists = NIMGroupInfoManager.getInstance().getAllGroupSession();
        mAdapter.addData(mLists);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.nim_toolbar_title, menu);
        MenuItem menuItem = menu.findItem(R.id.item_toolbar);
        View actionView = menuItem.getActionView();

        TextView tvTitle = (TextView) actionView.findViewById(R.id.title_txt);
        tvTitle.setText("选择群聊");

        actionView.findViewById(R.id.title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        return true;
    }
}
