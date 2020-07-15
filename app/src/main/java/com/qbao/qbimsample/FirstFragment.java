package com.qbao.qbimsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qbao.newim.activity.NIMSessionActivity;
import com.qbao.newim.manager.NIMContactManager;
import com.qbao.newim.manager.NIMFriendInfoManager;
import com.qbao.newim.manager.NIMMsgCountManager;
import com.qbao.newim.netcenter.NetCenter;

/**
 * Created by chenjian on 2017/8/17.
 */

public class FirstFragment extends Fragment {

    View mView;
    RelativeLayout layout_session;
    TextView tv_notify;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_first, container, false);
        layout_session = (RelativeLayout) mView.findViewById(R.id.layout_session);
        tv_notify = (TextView) mView.findViewById(R.id.session_unread);
        int count = NIMMsgCountManager.getInstance().GetAllUnreadCount()
                + NIMFriendInfoManager.getInstance().getUnread_count()
                + NIMContactManager.getInstance().getCount();
        tv_notify.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        layout_session.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(NetCenter.getInstance().IsLogined())
                {
                    Intent intent = new Intent(getActivity(), NIMSessionActivity.class);
                    startActivity(intent);
                }
                else
                {
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }
            }
        });

        return mView;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            int count = NIMMsgCountManager.getInstance().GetAllUnreadCount()
                    + NIMFriendInfoManager.getInstance().getUnread_count()
                    + NIMContactManager.getInstance().getCount();
            tv_notify.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        }
    }

}
