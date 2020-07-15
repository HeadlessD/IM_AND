package com.qbao.newim.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.qbao.newim.adapter.BlackListAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.manager.NIMFriendInfoManager;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.util.NIMStartActivityUtil;
import com.qbao.newim.qbim.R;
import com.qbao.newim.views.imgpicker.NIM_ToolbarAct;

import java.util.ArrayList;

/**
 * Created by chenjian on 2017/7/26.
 */

public class NIMBlackListActivity extends NIM_ToolbarAct{

    private RecyclerView recyclerView;
    private BlackListAdapter mAdapter;
    private ArrayList<IMFriendInfo> mLists = new ArrayList<>();
    private static final int REQUEST_CODE_USER_INfO = 100;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.nim_activity_black_list);
        recyclerView = (RecyclerView) findViewById(R.id.black_list_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new BlackListAdapter(mLists);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void setListener() {
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                IMFriendInfo friendInfo = mAdapter.getItem(position);
                NIMStartActivityUtil.startToUserForResult(NIMBlackListActivity.this, friendInfo.userId
                , friendInfo.source_type, REQUEST_CODE_USER_INfO, position);
                finish();
            }
        });
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        mLists.addAll(NIMFriendInfoManager.getInstance().getBlackList());
        mAdapter.setNewData(mLists);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.nim_toolbar_title, menu);
        MenuItem menuItem = menu.findItem(R.id.item_toolbar);
        View actionView = menuItem.getActionView();

        TextView tvTitle = (TextView) actionView.findViewById(R.id.title_txt);
        tvTitle.setText("黑名单");

        actionView.findViewById(R.id.title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        return true;
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK) {
//            if (requestCode == REQUEST_CODE_USER_INfO) {
//                boolean black = data.getBooleanExtra("black", false);
//                if (black) {
//                    Intent intent = new Intent();
//                    intent.putExtra("black", black);
//                    setResult(RESULT_OK);
//                    finish();
//                }
//            }
//        }
//    }
}
