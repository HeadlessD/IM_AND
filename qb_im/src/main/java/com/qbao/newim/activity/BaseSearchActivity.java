package com.qbao.newim.activity;

import android.app.Dialog;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.qbao.newim.qbim.R;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.views.ProgressDialog;

/**
 * Created by chenjian on 2017/9/1.
 */

public abstract class BaseSearchActivity extends AppCompatActivity implements View.OnClickListener{
    protected String TAG;
    public Dialog progressDialog;
    public boolean is_active;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = this.getClass().getSimpleName();
        AppUtil.addActivity(this);

        initView(savedInstanceState);
        setListener();
        processLogic(savedInstanceState);
    }

    /**
     * 初始化布局以及View控件
     */
    protected abstract void initView(Bundle savedInstanceState);

    /**
     * 给View控件添加事件监听器
     */
    protected abstract void setListener();

    /**
     * 处理业务逻辑，状态恢复等操作
     *
     * @param savedInstanceState
     */
    protected abstract void processLogic(Bundle savedInstanceState);

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
//            Transition explode = TransitionInflater.from(this).inflateTransition(android.R.transition.explode);
//            getWindow().setEnterTransition(explode);
//        }
        super.setContentView(layoutResID);
    }

    /**
     * 加载前对话框
     *
     * @param str 提示文字，默认点击不消失
     */
    public void showPreDialog(String str) {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        progressDialog = ProgressDialog.createRequestDialog(this, str, false);
        progressDialog.show();
    }

    public void hidePreDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        is_active = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        is_active = false;
    }

    @Override
    public void startActivity(Intent intent) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            super.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
//        } else {
            super.startActivity(intent);
            overridePendingTransition(R.anim.nim_pop_in, R.anim.nim_not_change);
//        }
    }

    public void startActivityForResultNoAnim(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        overridePendingTransition(R.anim.nim_pop_in, R.anim.nim_not_change);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            super.startActivityForResult(intent, requestCode, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
//        } else {
            super.startActivityForResult(intent, requestCode);
            overridePendingTransition(R.anim.nim_pop_in, R.anim.nim_not_change);
//        }
    }

    @Override
    public void finish() {
        super.finish();
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            overridePendingTransition(R.anim.nim_not_change, R.anim.nim_pop_out);
//        }
    }

    public void showToastStr(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void showToastStr(int res) {
        Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        AudioManager am = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                return true;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 查找View
     *
     * @param id   控件的id
     * @param <VT> View类型
     * @return
     */
    protected <VT extends View> VT getViewById(@IdRes int id) {
        return (VT) findViewById(id);
    }
}
