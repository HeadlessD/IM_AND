package com.qbao.qbimsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by chenjian on 2017/8/17.
 */

public class StartActivity extends AppCompatActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.actvity_start);

        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(1000);
                    startToMain();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void startToMain() {
        Intent intent = new Intent(this, Tab_MainActivity.class);
        startActivity(intent);
        finish();
    }
}
