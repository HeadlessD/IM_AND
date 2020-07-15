package com.qbao.newim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qbao.newim.adapter.GroupInfoAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.ErrorDetail;
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
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.BaseUtil;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.IDataObserver;
import com.qbao.newim.util.Logger;
import com.qbao.newim.views.ProgressDialog;
import com.qbao.newim.views.quick_bar.MaterialSearchView;

import java.util.ArrayList;
import java.util.List;

import static com.qbao.newim.qbim.R.id.group_list;

/**
 * Created by chenjian on 2017/6/16.
 */

public class GroupAllActivity extends BaseSearchActivity implements IDataObserver {

    private RecyclerView recyclerView;
    private GroupInfoAdapter mAdapter;
    private ImageView iv_back;

    private int nPosition;
    private ArrayList<IMGroupInfo> mLists = new ArrayList<>();

    private View notDataView;
    private TextView tv_data_title;
    private TextView tv_data_subtitle;
    private MaterialSearchView searchView;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.nim_activity_all_group);

        Toolbar toolbar = (Toolbar) findViewById(R.id.group_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        iv_back = (ImageView) findViewById(R.id.group_title_back);

        recyclerView = (RecyclerView) findViewById(group_list);

        notDataView = getLayoutInflater().inflate(R.layout.nim_no_data, (ViewGroup) recyclerView.getParent(), false);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new GroupInfoAdapter(mLists);
        recyclerView.setAdapter(mAdapter);

        searchView = (MaterialSearchView) findViewById(R.id.group_search_view);
        searchView.setCursorDrawable(R.drawable.nim_cursor_green);

        tv_data_title = (TextView) notDataView.findViewById(R.id.no_data_title);
        tv_data_subtitle = (TextView) notDataView.findViewById(R.id.no_data_subtitle);
        tv_data_title.setText("你还没有添加任何群到该列表");
        tv_data_subtitle.setText("通过群设置保存到通讯录就能显示了");
    }

    @Override
    protected void setListener() {
        mAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                nPosition = position;
                showSelectDialog();
                return false;
            }
        });

        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                IMGroupInfo group_info = mAdapter.getItem(position);
                NIMStartActivityUtil.startToGcActivity(GroupAllActivity.this, group_info.group_id);
            }
        });

        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public boolean onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                nPosition = position;
                goGroupDetail();
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                if (!searchView.hasAdapter())
                    searchView.setGroupSuggestions(mAdapter.getData());
            }

            @Override
            public void onSearchViewClosed() {
            }
        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(Object object) {
                if (object instanceof IMGroupInfo) {
                    IMGroupInfo group_info = (IMGroupInfo) object;
                    NIMStartActivityUtil.startToGcActivity(GroupAllActivity.this, group_info.group_id);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        mLists = NIMGroupInfoManager.getInstance().getAllGroupSession();
        if (mLists.size() == 0) {
            mAdapter.setEmptyView(notDataView);
        } else {
            mAdapter.addData(mLists);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nim_search_group_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }

    private void showSelectDialog() {
        ArrayList<String> arr_str = new ArrayList<>();
        arr_str.add("查看详情");
        arr_str.add("退出");
        arr_str.add("取消");

        ProgressDialog.showCustomDialog(this, "操作", arr_str, new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (position == 0) {
                    goGroupDetail();
                } else if (position == 1) {
                    quitGroup();
                }
            }
        });
    }

    private void goGroupDetail() {
        IMGroupInfo groupInfo = mAdapter.getItem(nPosition);
        Intent intent = new Intent(this, ChatSettingActivity.class);
        intent.putExtra("id", groupInfo.group_id);
        intent.putExtra("group", true);
        startActivity(intent);
    }

    private void quitGroup() {
        DataObserver.Register(this);
        showPreDialog("");
        IMGroupInfo groupInfo = mAdapter.getItem(nPosition);

        GroupOperateMode model = new GroupOperateMode();
        model.group_id = groupInfo.group_id;
        model.user_id = NIMUserInfoManager.getInstance().GetSelfUserId();
        model.message_id = NetCenter.getInstance().CreateGroupMsgId();
        model.big_msg_type = MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_KICK_USER;
        model.msg_time = BaseUtil.GetServerTime();
        model.operate_user_name = NIMUserInfoManager.getInstance().GetSelfUserName();

        IMGroupUserInfo self_info = NIMGroupUserManager.getInstance().getGroupUserInfo(groupInfo.group_id,
                NIMUserInfoManager.getInstance().GetSelfUserId());
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
    protected void onDestroy() {
        super.onDestroy();
        DataObserver.Cancel(this);
    }

    @Override
    public void OnChange(int param1, Object param2, Object param3) {
        if (param1 == DataConstDef.EVENT_GROUP_OPERATE) {
            hidePreDialog();
            GroupOperateMode operateMode = (GroupOperateMode) param2;
            if (operateMode == null) {
                showToastStr("操作失败");
                return;
            }

            if (operateMode.big_msg_type == MsgConstDef.GROUP_OPERATE_TYPE.GROUP_OFFLINE_CHAT_KICK_USER) {
                IMGroupUserInfo operate_user_info = operateMode.user_info_list.get(0);
                if (operate_user_info.user_id == NIMUserInfoManager.getInstance().GetSelfUserId()) {
                    // 通知会话列表删除
                    DataObserver.Notify(DataConstDef.EVENT_GROUP_DELETE, operateMode.group_id, true);
                    if (mAdapter.getItem(nPosition).group_id == operateMode.group_id) {
                        mAdapter.remove(nPosition);
                    }
                }
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
}
