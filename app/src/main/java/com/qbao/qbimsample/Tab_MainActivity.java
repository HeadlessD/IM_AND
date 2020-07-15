package com.qbao.qbimsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.LoginModel;
import com.qbao.newim.niminterface.NIMCoreSDK;
import com.qbao.qbimsample.sdktemplate.NIMViewTemplate;

public class Tab_MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener{

    private ViewPager viewPager;
    BottomNavigationView navigation;
    private Fragment fragment;
    private NIMViewTemplate m_view_template = new NIMViewTemplate();


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            viewPager.setCurrentItem(item.getOrder());
            return true;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab__main);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.addOnPageChangeListener(this);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        fragment = new FirstFragment();
                        break;
                    case 1:
                        fragment = new SecondFragment();
                        break;
                    case 2:
                        fragment = new LoginFragment();
                        break;
                }
                return fragment;
            }

            @Override
            public int getCount() {
                return 3;
            }
        });

        //检查权限
        NIMCoreSDK.getInstance().CheckPermission(this, new NIMCoreSDK.NIMPermissionDelegate()
        {
            @Override
            public void OnSuccess()
            {
                LoginModel user_model = NIMUserInfoManager.getInstance().GetLoginModel();
                //之前已经登录过尝试自动登录
                if(user_model != null && user_model.user_id > 0 && !user_model.tgt.isEmpty())
                {
                    NIMCoreSDK.getInstance().Start(user_model.user_id, user_model.tgt);
                }
                else
                {
                    Intent intent = new Intent(Tab_MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void OnCancel()
            {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        NIMCoreSDK.getInstance().Resume();
        super.onResume();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        navigation.getMenu().getItem(position).setChecked(true);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.nim_pop_in, R.anim.nim_not_change);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        overridePendingTransition(R.anim.nim_pop_in, R.anim.nim_not_change);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.nim_not_change, R.anim.nim_pop_out);
    }
}
