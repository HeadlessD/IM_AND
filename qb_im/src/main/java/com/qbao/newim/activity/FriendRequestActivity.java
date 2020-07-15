package com.qbao.newim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qbao.newim.adapter.FriendRequestAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.configure.GlobalVariable;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.ErrorDetail;
import com.qbao.newim.constdef.FriendTypeDef;
import com.qbao.newim.manager.NIMContactManager;
import com.qbao.newim.manager.NIMFriendInfoManager;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.util.NIMStartActivityUtil;
import com.qbao.newim.processor.FriendAddProcessor;
import com.qbao.newim.processor.FriendDelProcessor;
import com.qbao.newim.processor.GlobalProcessor;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.BaseUtil;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.IDataObserver;
import com.qbao.newim.util.Logger;
import com.qbao.newim.views.ProgressDialog;
import com.qbao.newim.views.imgpicker.NIM_ToolbarAct;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenjian on 2017/6/7.
 */

public class FriendRequestActivity extends NIM_ToolbarAct implements IDataObserver{
    private final static String TAG = FriendRequestActivity.class.getSimpleName();

    private RecyclerView recyclerView;
    private ArrayList<IMFriendInfo> mLists;
    private FriendRequestAdapter mAdapter;

    private View notDataView;
    private View netErrorView;
    private View loadingView;

    private static final int FRIEND_OPERATE = 100;
    private int nCurPosition;


    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.nim_activity_add);
        recyclerView = (RecyclerView) findViewById(R.id.friend_request_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        notDataView = getLayoutInflater().inflate(R.layout.nim_no_data, (ViewGroup) recyclerView.getParent(), false);
        netErrorView = getLayoutInflater().inflate(R.layout.nim_net_error, (ViewGroup) recyclerView.getParent(), false);
        loadingView = getLayoutInflater().inflate(R.layout.nim_loading, (ViewGroup) recyclerView.getParent(), false);

        mAdapter = new FriendRequestAdapter(mLists);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void setListener() {
        mLists = new ArrayList<>();
        DataObserver.Register(this);

        notDataView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processLogic(null);
            }
        });

        netErrorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processLogic(null);
            }
        });

        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public boolean onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (NIMFriendInfoManager.getInstance().OutFriendMaxCount()) {
                    showToastStr("你已达到好友上限" + GlobalVariable.FRIEND_MAX_COUNT + "人");
                    return false;
                }
                IMFriendInfo friendInfo = mAdapter.getItem(position);
                if (friendInfo.is_select) {
                    Intent intent = new Intent(FriendRequestActivity.this, FriendVerifyActivity.class);
                    intent.putExtra("user_id", friendInfo.userId);
                    intent.putExtra("source_type", FriendTypeDef.FRIEND_SOURCE_TYPE.CONTACTS);
                    startActivity(intent);
                } else {
                    showPreDialog("");
                    FriendAddProcessor processor = GlobalProcessor.getInstance().getFriendAddProcessor();
                    IMFriendInfo add_info = NIMFriendInfoManager.getInstance().getFriendReqInfo(friendInfo.userId);
                    processor.sendFriendAcceptRQ(add_info);
                }

                return false;
            }
        });

        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                IMFriendInfo add_info = mAdapter.getItem(position);
                nCurPosition = position;
                NIMStartActivityUtil.startToUserForResult(FriendRequestActivity.this, add_info.userId,
                        add_info.source_type, FRIEND_OPERATE, nCurPosition);
            }
        });

        mAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                nCurPosition = position;
                showDeleteDialog();
                return false;
            }
        });
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        mAdapter.setEmptyView(loadingView);

        if (!NetCenter.getInstance().IsLogined()) {
            mAdapter.setEmptyView(netErrorView);
            return;
        }

        mLists.addAll(NIMContactManager.getInstance().getRandom_list());

        // 当前如果已经是好友了，则用户信息到内存读取，如果不是，批量获取
        List<IMFriendInfo> add_list = NIMFriendInfoManager.getInstance().getAllFriendReqList();
        mLists.addAll(add_list);

        if (mLists.size() == 0) {
            mAdapter.setEmptyView(notDataView);
        } else {
            mAdapter.addData(mLists);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.nim_toolbar_title, menu);
        MenuItem menuItem = menu.findItem(R.id.item_toolbar);
        View actionView = menuItem.getActionView();


        TextView tvTitle = (TextView) actionView.findViewById(R.id.title_txt);
        tvTitle.setText("新的朋友");

        actionView.findViewById(R.id.title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (progressDialog == null) {
            DataObserver.Cancel(this);
        }
    }

    private void showDeleteDialog() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("删除该消息");
        arrayList.add("取消");
        ProgressDialog.showCustomDialog(this, "操作", arrayList, new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (position == 0) {
                    showPreDialog("");
                    IMFriendInfo friendInfo = mAdapter.getItem(nCurPosition);
                    if (friendInfo.status == FriendTypeDef.FRIEND_ADD_TYPE.ACCEPT_REQUEST) {
                        FriendDelProcessor processor = GlobalProcessor.getInstance().getFriendDelProcessor();
                        processor.sendReqDeleteRQ(friendInfo.userId);
                    } else {
                        deleteAddInfo(friendInfo.userId);
                    }
                }
            }
        });
    }

    private void deleteAddInfo(long user_id) {
        IMFriendInfo info = mAdapter.getItem(nCurPosition);
        if (info.userId != user_id) {
            return;
        }

        NIMFriendInfoManager.getInstance().delFriendAdd(info.userId);
        mAdapter.remove(nCurPosition);
        mAdapter.removeSession(user_id);
        if (mAdapter.getData().size() == 0) {
            mAdapter.setEmptyView(notDataView);
        }
        hidePreDialog();
    }

    @Override
    public void OnChange(int param1, Object param2, Object param3) {
        switch (param1) {
            case DataConstDef.EVENT_MESSAGE_TIME_OUT:
            case DataConstDef.EVENT_NET_ERROR:
                hidePreDialog();
                mAdapter.setEmptyView(netErrorView);
                Logger.error(TAG, "EVENT_NET_ERROR");
                if (is_active) {
                    int error_code = BaseUtil.MakeErrorResult((int) param2);
                    String error_msg = ErrorDetail.GetErrorDetail(error_code);
                    showToastStr(error_msg);
                }
                break;
            case DataConstDef.EVENT_FRIEND_ADD_REQUEST:
                hidePreDialog();
                if (!(boolean) param3) {
                    return;
                }
                long user_id = (long) param2;

                IMFriendInfo info = NIMFriendInfoManager.getInstance().getFriendReqInfo(user_id);
                if (info != null) {
                    getUpdatePosition(info);
                    NIMFriendInfoManager.getInstance().clearUnread_count();
                }
                break;
            case DataConstDef.EVENT_FRIEND_REQUEST_DELETE:
                if (param2 == null) {
                    return;
                }
                deleteAddInfo((long)param2);
                break;
            case DataConstDef.EVENT_FRIEND_CONFIRM:
                hidePreDialog();
                IMFriendInfo confirm_info = NIMFriendInfoManager.getInstance().getFriendReqInfo((long) param2);
                if (confirm_info != null) {
                    confirm_info.is_select = false;
                    getUpdatePosition(confirm_info);
                }
                break;
            case DataConstDef.EVENT_FRIEND_REQ_TIMEOUT:
                hidePreDialog();
                IMFriendInfo timeout_info = NIMFriendInfoManager.getInstance().getFriendReqInfo((long) param2);
                if (timeout_info != null) {
                    getUpdatePosition(timeout_info);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case FRIEND_OPERATE:
                    if (data != null) {
                        int position = data.getIntExtra("pos", -1);
                        if (position >= 0) {
                            mAdapter.getData().get(position).status = FriendTypeDef.FRIEND_ADD_TYPE.OWN_CONFIRM;
                            mAdapter.notifyItemChanged(position);
                        }
                    }
                    break;
            }
        }
    }

    private void getUpdatePosition(IMFriendInfo im_info) {
        if (mAdapter.contains(im_info.userId)) {
            int nSize = mAdapter.getData().size();
            for (int i = 0; i < nSize; i++) {
                IMFriendInfo origin_info = mAdapter.getItem(i);
                if (im_info.userId == origin_info.userId) {
                    origin_info.status = im_info.status;
                    mAdapter.setData(i, origin_info);
                    break;
                }
            }
        } else {
            mAdapter.addData(im_info);
        }
    }
}
