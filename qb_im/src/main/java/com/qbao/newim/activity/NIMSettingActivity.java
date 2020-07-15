package com.qbao.newim.activity;

import android.media.AudioManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.qbao.newim.helper.AudioPlayManager;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.SharedPreferenceUtil;
import com.qbao.newim.views.SwitchButton;
import com.qbao.newim.views.imgpicker.NIM_ToolbarAct;

/**
 * Created by chenjian on 2017/8/4.
 */

public class NIMSettingActivity extends NIM_ToolbarAct implements CompoundButton.OnCheckedChangeListener{

    private SwitchButton ivVoice;
    private SwitchButton ivVibrate;
    private SwitchButton ivPlay;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.nim_activity_setting);

        ivVoice = (SwitchButton) findViewById(R.id.setting_voice);
        ivVibrate = (SwitchButton) findViewById(R.id.setting_vibrate);
        ivPlay = (SwitchButton) findViewById(R.id.setting_play);
    }

    @Override
    protected void setListener() {
        ivVoice.setOnCheckedChangeListener(this);
        ivPlay.setOnCheckedChangeListener(this);
        ivVibrate.setOnCheckedChangeListener(this);
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        setStatus(ivVoice, SharedPreferenceUtil.getMsgVoice());
        setStatus(ivVibrate, SharedPreferenceUtil.getMsgVibrate());
        setStatus(ivPlay, AudioPlayManager.getManager().isCustomEarMode());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.nim_toolbar_title, menu);
        MenuItem menuItem = menu.findItem(R.id.item_toolbar);
        View actionView = menuItem.getActionView();

        TextView tvTitle = (TextView) actionView.findViewById(R.id.title_txt);
        tvTitle.setText("消息提醒设置");

        actionView.findViewById(R.id.title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        return true;
    }

    private void setStatus(SwitchButton imageView, boolean active) {
        imageView.setChecked(active);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.setting_voice) {
            SharedPreferenceUtil.saveMsgVoice(isChecked);
            setStatus(ivVoice, isChecked);
        } else if (buttonView.getId() == R.id.setting_vibrate) {
            SharedPreferenceUtil.saveMsgVibrate(isChecked);
            setStatus(ivVibrate, isChecked);
        } else if (buttonView.getId() == R.id.setting_play) {
            if(isChecked)
            {
                AudioPlayManager.getManager().setCustomMode(AudioPlayManager.MODE_EARPIECE);
            }
            else
            {
                AudioPlayManager.getManager().setCustomMode(AudioPlayManager.MODE_SPEAKER);
            }
            setStatus(ivPlay, isChecked);
        }
    }
}
