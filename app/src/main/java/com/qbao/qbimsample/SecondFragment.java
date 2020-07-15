package com.qbao.qbimsample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by chenjian on 2017/8/17.
 */

public class SecondFragment extends Fragment {

    View mView;
    TextView tvTest;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_second, container, false);
        tvTest = (TextView)mView.findViewById(R.id.test_click);
        tvTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TestOfficial();
            }
        });
        return mView;
    }

    private void TestOfficial() {
//        MessageModel messageModel = new MessageModel();
//        messageModel.message_id = NetCenter.getInstance().CreateGroupMsgId();
//        messageModel.msg_time = BaseUtil.GetServerTime();
//        messageModel.msg_content = "系统消息发给所有人";
//        messageModel.send_user_name = "系统消息";
//        messageModel.chat_type = MsgConstDef.MSG_CHAT_TYPE.PUBLIC;
//        messageModel.chat_session_id = 1000l;
//        OfficialProcessor processor = GlobalProcessor.getInstance().getOfficialProcessor();
//        processor.getFanSysMsg();
    }
}
