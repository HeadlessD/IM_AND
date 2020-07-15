package com.qbao.qbimsample;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.qbao.newim.business.ApiRequest;
import com.qbao.newim.configure.GlobalVariable;
import com.qbao.newim.constdef.NetConstDef;
import com.qbao.newim.helper.DESCode;
import com.qbao.newim.helper.Signatures;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.LoginModel;
import com.qbao.newim.niminterface.NIMCoreSDK;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.KeyboardUtil;
import com.qbao.newim.util.SharedPreferenceUtil;
import com.qbao.newim.util.ShowUtils;
import com.qbao.newim.util.Utils;
import com.qbao.newim.views.ProgressDialog;
import com.qbao.qbimsample.sdktemplate.NIMNetTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by chenjian on 2017/10/10.
 */

public class LoginActivity extends Activity {

    private String username = "13000180002";
    private String password = "111111";
    private String randomCode;
    private String tgt;
    private String st;
    private EditText etPort;
    private EditText etPortNum;
    private RadioButton cb_1;
    private RadioButton cb_2;
    private RadioButton cb_3;

    private TextView tv_login;
    private SpinnerEditText<String> et_name;
    private EditText et_password;
    private TextView tv_skip;
    private String[] test_user = new String[]{"13999990001", "13999990002","13999990003","18800000089",
            "18800000089",
            "18800000088",
            "18800000087",
            "13000180001",
            "13000180002"};

    public Dialog progressDialog;

    private  NIMNetTemplate m_nim_net_template = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        initView();
        initEvent();
        initData();
    }

    private void initView() {
        tv_login = (TextView)findViewById(R.id.test_login);
        tv_login.requestFocus();
        et_name = (SpinnerEditText<String>) findViewById(R.id.test_name);

        et_password = (EditText)findViewById(R.id.test_password);
        tv_skip = (TextView) findViewById(R.id.test_skip);

        etPort = (EditText)findViewById(R.id.test_port);
        etPortNum = (EditText)findViewById(R.id.test_port_num);
        etPortNum.setVisibility(View.GONE);
        cb_1 = (RadioButton)findViewById(R.id.rg_select_1);
        cb_2 = (RadioButton) findViewById(R.id.rg_select_2);
        cb_3 = (RadioButton)findViewById(R.id.rg_select_3);

        String text = etPort.getText().toString();
        etPort.setSelection(text.length());

        et_password.setHint(password);

        List<String> user_list = Arrays.asList(test_user);
        et_name.setList(user_list);
        et_name.setSelection(0);
        et_name.setNeedShowSpinner(true);
    }

    private void initEvent() {

        et_name.setOnItemClickListener(new SpinnerEditText.OnItemClickListener<String>() {
            @Override
            public void onItemClick(String s, SpinnerEditText<String> var1, View var2, int position, long var4, String selectContent) {
                username = s;
                KeyboardUtil.hideKeyboard(et_name);
            }
        });


        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                getRandomCode();
            }
        });

        tv_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                NIMCoreSDK.getInstance().SetConnectInfo("", 0, false);
                NIMCoreSDK.getInstance().Start(AppUtil.getTestUserId(), "abc");
            }
        });

        cb_3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etPortNum.setVisibility(View.VISIBLE);
                } else {
                    etPortNum.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initData() {
        m_nim_net_template = new NIMNetTemplate();
        m_nim_net_template.SetActivity(this);
    }

    private void getRandomCode() {
        getParam();
        showPreDialog("开始登录");
        String url = "https://passport.qbao.com/api/v34/getRandomCode";
        Call<ResponseBody> call = ApiRequest.getApiQbao().getRandomCode(url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String dataString = response.body().string();
                    JsonObject obj = new JsonParser().parse(dataString).getAsJsonObject();
                    String code = obj.get("responseCode").toString();
                    if (code.equals("1000")) {
                        randomCode = obj.get("data").toString().replace("\"", "");
                        getToken();
                    } else {
                        ShowUtils.showToast("getRandomCode" + code);
                        hidePreDialog();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                showToastStr(t.getMessage());
            }
        });
    }

    private void getParam() {
        String host = etPort.getText().toString().trim();
        int port = 0;
        if (cb_1.isChecked()) {
            port = Integer.parseInt(cb_1.getText().toString());
        } else if (cb_2.isChecked()) {
            port = Integer.parseInt(cb_2.getText().toString());
        } else {
            if (TextUtils.isEmpty(etPortNum.getText().toString())) {
                showToastStr("输入端口号");
                return;
            }
            port = Integer.parseInt(etPortNum.getText().toString());
        }

        NIMCoreSDK.getInstance().SetConnectInfo(host, port, false);
    }

    private void getToken() {
        HashMap<String, String> params = new HashMap<>();
        String name = et_name.getText().toString();
        if (TextUtils.isEmpty(name)) {
            name = username;
            params.put("username", username);
        } else {
            params.put("username", name);
        }
        StringBuilder encrypt = new StringBuilder();
        String token = "OLzELOMEH+o=";
        try {
            String pwd = et_password.getText().toString();
            if (TextUtils.isEmpty(pwd)) {
                pwd = password;
            }
            String pwd_des = DESCode.encrypt(pwd, token);
            params.put("password", pwd_des);
            encrypt.append(pwd);
            encrypt.append("()");
            encrypt.append("[" + name + "]");
            String m = middleString(randomCode);
            String e = "\"yuanqq\"";
            encrypt.append(e);
            encrypt.append(m);
            params.put("envID", Utils.getEvnId(AppUtil.GetContext()));
            params.put("signature", Signatures.getSignature(encrypt.toString(), randomCode));
            params.put("device", android.os.Build.MODEL);
        } catch (Exception e) {
            e.printStackTrace();
        }

        params.put("t", String.valueOf(System.currentTimeMillis()));
        String url = "https://passport.qbao.com/api/v35/cas/qbao/tickets";
        Call<ResponseBody> call = ApiRequest.getApiQbao().getToken(url, params);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String dataString = response.body().string();
                    JsonObject obj = new JsonParser().parse(dataString).getAsJsonObject();
                    String code = obj.get("responseCode").toString();
                    if (code.equals("1000")) {
                        tgt = obj.get("data").toString().replaceAll("\"", "");
                        getST();
                    } else {
                        ShowUtils.showToast("getToken" + code);
                        hidePreDialog();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                showToastStr(t.getMessage());
            }
        });
    }

    private void getST() {
        HashMap<String, String> params = new HashMap<>();
        params.put("service", "https://m.qbao.com/j_spring_cas_security_check");
        String url = "https://passport.qbao.com/api/v35/cas/tickets/" + tgt;
        Call<ResponseBody> call = ApiRequest.getApiQbao().getST(url, params);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String dataString = response.body().string();
                    JsonObject obj = new JsonParser().parse(dataString).getAsJsonObject();
                    String code = obj.get("responseCode").toString();
                    if (code.equals("1000")) {
                        st = obj.get("data").toString().replaceAll("\"", "");
                        login();
                    } else {
                        ShowUtils.showToast("getST" + code);
                        hidePreDialog();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                showToastStr(t.getMessage());
            }
        });
    }

    private void login() {
        HashMap<String, String> params = new HashMap<>();
        params.put("st", st);
        params.put("envID", Utils.getEvnId(AppUtil.GetContext()));
        String url = "http://m.qbao.com/api/v31/account4Client/login";

        Call<ResponseBody> call = ApiRequest.getApiQbao().login(url, params);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    hidePreDialog();
                    if (response.body() != null) {
                        String dataString = response.body().string();
                        JsonObject obj = new JsonParser().parse(dataString).getAsJsonObject();
                        String code = obj.get("responseCode").toString();
                        if (code.equals("1000")) {
                            String data = obj.get("data").toString();
                            JsonObject data_obj = new JsonParser().parse(data).getAsJsonObject();
                            String user_id = data_obj.get("userId").toString();
                            showToastStr("id--->" + user_id);
                            NIMCoreSDK.getInstance().Start(Long.parseLong(user_id), tgt);
                        } else {
                            ShowUtils.showToast("login" + code);
                            hidePreDialog();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                showToastStr(t.getMessage());
            }
        });
    }

    private String middleString(String p) {
        StringBuilder sbBuilder = new StringBuilder();
        sbBuilder.append("(");
        sbBuilder.append(p + ")");
        String str = GlobalVariable.USER_AGENT;
        sbBuilder.append("{" + str);
        sbBuilder.append("}");

        return sbBuilder.toString();
    }

    public void showToastStr(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void hidePreDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public void showPreDialog(String str) {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        progressDialog = ProgressDialog.createRequestDialog(this, str, false);
        progressDialog.show();
    }
}
