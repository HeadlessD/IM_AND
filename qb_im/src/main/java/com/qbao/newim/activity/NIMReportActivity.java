package com.qbao.newim.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.qbao.newim.configure.GlobalVariable;
import com.qbao.newim.processor.GlobalProcessor;
import com.qbao.newim.processor.UserInfoGetProcessor;
import com.qbao.newim.qbim.R;
import com.qbao.newim.views.imgpicker.NIM_ToolbarAct;

/**
 * Created by chenjian on 2017/8/11.
 */

public class NIMReportActivity extends NIM_ToolbarAct{

    private EditText editText;
    private RadioButton radioButton1;
    private RadioButton radioButton2;
    private RadioButton radioButton3;
    private RadioButton radioButton4;
    private RadioButton radioButton5;

    private static final byte REPORT_TYPE_1 = 1;
    private static final byte REPORT_TYPE_2 = 2;
    private static final byte REPORT_TYPE_3 = 3;
    private static final byte REPORT_TYPE_4 = 4;
    private static final byte REPORT_TYPE_5 = 5;

    private String report_content;
    private byte type;
    private long user_id;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.nim_activity_report);

        editText = (EditText) findViewById(R.id.report_edit);
        radioButton1 = (RadioButton) findViewById(R.id.report_radio_1);
        radioButton2 = (RadioButton) findViewById(R.id.report_radio_2);
        radioButton3 = (RadioButton) findViewById(R.id.report_radio_3);
        radioButton4 = (RadioButton) findViewById(R.id.report_radio_4);
        radioButton5 = (RadioButton) findViewById(R.id.report_radio_5);

        if (getIntent() != null) {
            user_id = getIntent().getLongExtra("user_id", 0);
        }
    }

    @Override
    protected void setListener() {
        radioButton5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editText.setVisibility(View.VISIBLE);
                    editText.setHint("请输入举报内容(" + GlobalVariable.REPORT_USER_COUNT + "字以内)");
                    editText.requestFocus();
                } else {
                    editText.setVisibility(View.GONE);
                    editText.clearFocus();
                }
            }
        });
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.nim_toolbar_title, menu);
        MenuItem menuItem = menu.findItem(R.id.item_toolbar);
        View actionView = menuItem.getActionView();

        TextView tvTitle = (TextView) actionView.findViewById(R.id.title_txt);
        tvTitle.setText("举报");

        TextView tvRight = (TextView) actionView.findViewById(R.id.title_right);
        tvRight.setVisibility(View.VISIBLE);
        tvRight.setText("发送");
        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (radioButton1.isChecked()) {
                    report_content = radioButton1.getText().toString();
                    type = REPORT_TYPE_1;
                } else if (radioButton2.isChecked()) {
                    report_content = radioButton2.getText().toString();
                    type = REPORT_TYPE_2;
                } else if (radioButton3.isChecked()) {
                    report_content = radioButton3.getText().toString();
                    type = REPORT_TYPE_3;
                } else if (radioButton4.isChecked()) {
                    report_content = radioButton4.getText().toString();
                    type = REPORT_TYPE_4;
                } else if (radioButton5.isChecked()) {
                    report_content = editText.getText().toString().trim();
                    if (report_content.length() > GlobalVariable.REPORT_USER_COUNT) {
                        showToastStr("举报字数限制为" + GlobalVariable.REPORT_USER_COUNT);
                        return;
                    }
                    type = REPORT_TYPE_5;
                }

                showToastStr("已举报");
                UserInfoGetProcessor processor = GlobalProcessor.getInstance().getUser_processor();
                processor.SendReportUser(user_id, type, report_content);
                onBackPressed();
            }
        });

        actionView.findViewById(R.id.title_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        return true;
    }
}
