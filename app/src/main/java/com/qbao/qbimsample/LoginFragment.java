package com.qbao.qbimsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qbao.newim.netcenter.NetCenter;

/**
 * Created by chenjian on 2017/8/17.
 */

public class LoginFragment extends Fragment {
    View mView;
    TextView tv_login_status;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_login, container, false);
        initView();
        initEvent();
        initData();
        return mView;
    }

    private void initView() {
        tv_login_status = (TextView) mView.findViewById(R.id.login_status);
    }

    private void initEvent() {
        tv_login_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initData() {
        if (NetCenter.getInstance().IsLogined()) {
            tv_login_status.setText("切换登录");
        } else {
            tv_login_status.setText("未登录");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }
}
