package com.qbao.newim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.qbao.newim.adapter.CongenialAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.business.ApiRequest;
import com.qbao.newim.configure.GlobalVariable;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.ErrorDetail;
import com.qbao.newim.constdef.FriendTypeDef;
import com.qbao.newim.manager.NIMFriendInfoManager;
import com.qbao.newim.model.CongenialItem;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.netcenter.NetCenter;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.BaseUtil;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.IDataObserver;
import com.qbao.newim.util.Logger;
import com.qbao.newim.views.imgpicker.NIM_ToolbarAct;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by chenjian on 2017/5/31.
 * 趣味相投的人
 */

public class CongenialActivity extends NIM_ToolbarAct implements IDataObserver{

    private RecyclerView lv_congenial;
    private CongenialAdapter congenialAdapter;
    private View notDataView;
    private View netErrorView;
    private View loadingView;
    private ProgressBar progressBar;
    private TextView tvRight;

    private ArrayList<CongenialItem> items = new ArrayList<>();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.nim_toolbar_title, menu);
        MenuItem menuItem = menu.findItem(R.id.item_toolbar);
        View actionView = menuItem.getActionView();

        tvRight = (TextView) actionView.findViewById(R.id.title_right);
        tvRight.setVisibility(View.VISIBLE);
        tvRight.setText("换一组");

        TextView tvTitle = (TextView) actionView.findViewById(R.id.title_txt);
        tvTitle.setText("趣味相投的人");

        progressBar = (ProgressBar) actionView.findViewById(R.id.progressBar);
        actionView.findViewById(R.id.title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCongenialData();
            }
        });
        return true;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.nim_activity_congenial);

        lv_congenial = (RecyclerView) findViewById(R.id.lv_congenial);
        notDataView = getLayoutInflater().inflate(R.layout.nim_no_data, (ViewGroup) lv_congenial.getParent(), false);
        netErrorView = getLayoutInflater().inflate(R.layout.nim_net_error, (ViewGroup) lv_congenial.getParent(), false);
        loadingView = getLayoutInflater().inflate(R.layout.nim_loading, (ViewGroup) lv_congenial.getParent(), false);

        lv_congenial.setLayoutManager(new LinearLayoutManager(this));
        congenialAdapter = new CongenialAdapter(items);
        lv_congenial.setAdapter(congenialAdapter);
    }

    @Override
    protected void setListener() {
        DataObserver.Register(this);

        notDataView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCongenialData();
            }
        });

        netErrorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCongenialData();
            }
        });

        congenialAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Intent intent = new Intent(CongenialActivity.this, NIMUserInfoActivity.class);
                intent.putExtra("source_type", FriendTypeDef.FRIEND_SOURCE_TYPE.CONGENIAL);
                long user_id = Long.parseLong(items.get(position).getUserId());
                intent.putExtra("user_id", user_id);
                startActivity(intent);
            }
        });

        congenialAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public boolean onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (NIMFriendInfoManager.getInstance().OutFriendMaxCount()) {
                    showToastStr("你已达到好友上限" + GlobalVariable.FRIEND_MAX_COUNT + "人");
                    return false;
                }
                Intent intent = new Intent(CongenialActivity.this, FriendVerifyActivity.class);
                intent.putExtra("source_type", FriendTypeDef.FRIEND_SOURCE_TYPE.CONGENIAL);
                long user_id = Long.parseLong(items.get(position).getUserId());
                intent.putExtra("user_id", user_id);
                startActivity(intent);
                return false;
            }
        });
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        getCongenialData();
//        boolean bLogin = TestLoginActivity.getInstance().isbSuccess();
//        if (bLogin) {
//            getCongenialData();
//        } else {
//            // 测试登录过程
//            TestLoginActivity.getInstance().start_login(new TestLoginActivity.LoginCallback() {
//                @Override
//                public void on_login(boolean bSuccess) {
//                    if (bSuccess) {
//                        getCongenialData();
//                    } else {
//                        showToastStr("登录失败");
//                    }
//
//                    hidePreDialog();
//                }
//            });
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataObserver.Cancel(this);
    }

    private void setRightLoadStatus(boolean load_status) {
        if (tvRight == null || progressBar == null) {
            return;
        }

        if (load_status) {
            tvRight.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            tvRight.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void getCongenialData() {
        congenialAdapter.setEmptyView(loadingView);

        if (!NetCenter.getInstance().IsLogined()) {
            congenialAdapter.setEmptyView(netErrorView);
            return;
        }

        setRightLoadStatus(true);

        String url = "https://m.qbao.com/api/v32/account4Client/interestList";
        Call<ResponseBody> call = ApiRequest.getApiQbao().sendPostRequest(url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                setRightLoadStatus(false);
                if (response.isSuccessful()) {
                    try {
                        String result = response.body().string();
                        JsonObject obj = new JsonParser().parse(result).getAsJsonObject();
                        if (obj.get("responseCode").toString().equals("1000")) {
                            JsonArray array = obj.getAsJsonArray("data");
                            Type type = new TypeToken<List<CongenialItem>>() {}.getType();
                            List<CongenialItem> mLists = new Gson().fromJson(array, type);
                            if (mLists.isEmpty()) {
                                congenialAdapter.setEmptyView(notDataView);
                                return;
                            }
                            items.clear();
                            items.addAll(mLists);
                            congenialAdapter.addData(items);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Logger.error("", "error");
                congenialAdapter.setEmptyView(netErrorView);
            }
        });
    }

    @Override
    public void OnChange(int param1, Object param2, Object param3) {
        switch (param1) {
            case DataConstDef.EVENT_FRIEND_ADD_REQUEST:
                // 当前添加好友通过后
                long user_id = (int)param2;
                IMFriendInfo addInfo = NIMFriendInfoManager.getInstance().getFriendUser(user_id);
                setUpdateStatus(addInfo);
                break;
            case DataConstDef.EVENT_NET_ERROR:
                Logger.error(TAG, "EVENT_NET_ERROR");
                if (is_active) {
                    int error_code = BaseUtil.MakeErrorResult((int) param2);
                    String error_msg = ErrorDetail.GetErrorDetail(error_code);
                    showToastStr(error_msg);
                }
                break;
        }
    }

    private void setUpdateStatus(IMFriendInfo add_info) {
        int length = items.size();
        int status = 0;
        if (add_info != null) {
            status = add_info.getStatus();
        }
        for (int i = 0; i < length; i++) {
            CongenialItem item = items.get(i);
            if (item.getUserId().equals(String.valueOf(add_info.userId))) {
                if (status == FriendTypeDef.FRIEND_ADD_TYPE.SEND_REQUEST) {      // 待验证
                    congenialAdapter.notifyDataSetChanged();
                } else if (status == FriendTypeDef.FRIEND_ADD_TYPE.PEER_CONFIRM) {     // 已成为好友删除
                    items.remove(i);
                    congenialAdapter.notifyDataSetChanged();
                }
                break;
            }
        }
    }
}
