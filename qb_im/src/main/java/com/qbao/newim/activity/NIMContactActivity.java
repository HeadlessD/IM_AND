package com.qbao.newim.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.qbao.newim.adapter.ContactAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.ErrorDetail;
import com.qbao.newim.manager.NIMContactManager;
import com.qbao.newim.manager.NIMFriendInfoManager;
import com.qbao.newim.manager.NIMGroupInfoManager;
import com.qbao.newim.manager.NIMOfficialManager;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.model.IMOfficialInfo;
import com.qbao.newim.util.NIMStartActivityUtil;
import com.qbao.newim.processor.FriendDelProcessor;
import com.qbao.newim.processor.GlobalProcessor;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.BaseUtil;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.IDataObserver;
import com.qbao.newim.util.Utils;
import com.qbao.newim.views.ProgressDialog;
import com.qbao.newim.views.StickyRecyclerHeadersDecoration;
import com.qbao.newim.views.quick_bar.DividerDecoration;
import com.qbao.newim.views.quick_bar.MaterialSearchView;
import com.qbao.newim.views.quick_bar.OnQuickSideBarTouchListener;
import com.qbao.newim.views.quick_bar.QuickSideBarTipsView;
import com.qbao.newim.views.quick_bar.QuickSideBarView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chenjian on 2017/8/31.
 */

public class NIMContactActivity extends BaseSearchActivity implements OnQuickSideBarTouchListener, IDataObserver{

    private ImageView iv_back;
    TextView tvTitle;

    RecyclerView recyclerView;
    HashMap<String,Integer> letters = new HashMap<>();
    QuickSideBarView quickSideBarView;
    QuickSideBarTipsView quickSideBarTipsView;
    private List<IMFriendInfo> mLists;
    private ContactAdapter mAdapter;
    StickyRecyclerHeadersDecoration headersDecor;
    private View friends_footer_view;
    private View friends_head_view;
    private MaterialSearchView searchView;
    private TextView tv_total_count;

    private View addFriends;
    private View groups;
    private View publishers;
    private View black_list;

    private TextView tv_new_friend_count;
    private int nCurPosition;
    private static final int REQUEST_CODE_USER_INFO = 100;
    private List<IMGroupInfo> mGroups;
    private List<IMOfficialInfo> mOfficials;
    private List<Object> objectList;
    private boolean has_change_data;
    private boolean is_handle_friend;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.nim_activity_contact);

        Toolbar toolbar = (Toolbar) findViewById(R.id.contact_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        iv_back = (ImageView) findViewById(R.id.title_back);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        quickSideBarView = (QuickSideBarView) findViewById(R.id.quickSideBarView);
        quickSideBarTipsView = (QuickSideBarTipsView) findViewById(R.id.quickSideBarTipsView);

        friends_head_view = LayoutInflater.from(this).inflate(R.layout.friends_head_view, null);

        addFriends = friends_head_view.findViewById(R.id.addFriends);
        tv_new_friend_count = (TextView) friends_head_view.findViewById(R.id.tv_new_friend);
        groups = friends_head_view.findViewById(R.id.groups);
        publishers = friends_head_view.findViewById(R.id.publishers);
        black_list = friends_head_view.findViewById(R.id.black_list);
        friends_footer_view = LayoutInflater.from(this).inflate(R.layout.friends_footer_view, null);
        tv_total_count = (TextView) friends_footer_view.findViewById(R.id.totalCount);

        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setCursorDrawable(R.drawable.nim_cursor_green);
    }

    @Override
    protected void setListener() {
        quickSideBarView.setOnQuickSideBarTouchListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new ContactAdapter(null);
        mAdapter.addHeaderView(friends_head_view);
        mAdapter.addFooterView(friends_footer_view);
        recyclerView.setAdapter(mAdapter);

        addFriends.setOnClickListener(this);
        groups.setOnClickListener(this);
        publishers.setOnClickListener(this);
        black_list.setOnClickListener(this);
        iv_back.setOnClickListener(this);

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                if (has_change_data) {
                    if (objectList != null) {
                        objectList.clear();
                    }
                }
                if (objectList == null || objectList.size() == 0) {
                    objectList = new ArrayList<>();
                    objectList.addAll(mLists);
                }

                quickSideBarView.setVisibility(View.GONE);
                if (mGroups == null || mOfficials == null || has_change_data) {
                    mGroups = NIMGroupInfoManager.getInstance().getAllGroupSession(false);
                    mOfficials = NIMOfficialManager.getInstance().getOfficialList();
                    objectList.addAll(mGroups);
                    objectList.addAll(mOfficials);
                }

                searchView.setSuggestions(objectList);
            }

            @Override
            public void onSearchViewClosed() {
                quickSideBarView.setVisibility(View.VISIBLE);
                has_change_data = false;
            }
        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(Object object) {
                if (object instanceof IMFriendInfo) {
                    IMFriendInfo friendInfo = (IMFriendInfo) object;
                    NIMStartActivityUtil.startToScActivity(NIMContactActivity.this, friendInfo.userId);
                } else if (object instanceof IMGroupInfo) {
                    IMGroupInfo groupInfo = (IMGroupInfo) object;
                    NIMStartActivityUtil.startToGcActivity(NIMContactActivity.this, groupInfo.group_id);
                } else if (object instanceof IMOfficialInfo){
                    IMOfficialInfo officialInfo = (IMOfficialInfo) object;
                    if(officialInfo != null && officialInfo.official_id > 0)
                        NIMStartActivityUtil.startToOfficialChat(NIMContactActivity.this, officialInfo.official_id);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                nCurPosition = position;
                final IMFriendInfo userInfo = mAdapter.getItem(position);
                ArrayList<String> arrayList = new ArrayList<>();
                arrayList.add("删除");
                arrayList.add("取消");
                final String name = Utils.getUserShowName(new String[] {
                        userInfo.remark_name,
                        userInfo.nickName,
                        userInfo.user_name});
                ProgressDialog.showCustomDialog(NIMContactActivity.this, name, arrayList,
                        new BaseQuickAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                                if (position == 0) {
                                    confirmDeleteFriend(userInfo.userId, name);
                                }
                            }
                        });
                return false;
            }
        });

        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                nCurPosition = position;
                IMFriendInfo friendInfo = mAdapter.getItem(position);
                    NIMStartActivityUtil.startToUserForResult(NIMContactActivity.this, friendInfo.userId,
                            friendInfo.source_type, REQUEST_CODE_USER_INFO, position);
            }
        });
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        DataObserver.Register(this);
        setFriendUnread(NIMFriendInfoManager.getInstance().getUnread_count()
                + NIMContactManager.getInstance().getCount());

        sortList();
    }

    private void sortList() {
        mLists = NIMFriendInfoManager.getInstance().getAllFriendList();
        Collections.sort(mLists);

        int position = 0;
        List<String> customLetters = quickSideBarView.getLetters();
        if (!customLetters.contains("#"))
            customLetters.add("#");
        for (IMFriendInfo friendInfo : mLists) {
            friendInfo.is_star = false;
            String letter = friendInfo.getInitial();
            if (letter.equals("|")){
                letter = "#";
            }

            if(!letters.containsKey(letter)){
                letters.put(letter, position);
            }
            if (!customLetters.contains(letter)) {
                customLetters.add(letter);
            }
            position++;
        }

        updateTotalCount();
        quickSideBarView.setLetters(customLetters);

        mAdapter.setNewData(mLists);
        if (headersDecor == null) {
            headersDecor = new StickyRecyclerHeadersDecoration(mAdapter);
            recyclerView.addItemDecoration(headersDecor);
            recyclerView.addItemDecoration(new DividerDecoration(this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nim_search_contact_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            Intent intent = new Intent(NIMContactActivity.this, FriendSearchActivity.class);
            startActivity(intent);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataObserver.Cancel(this);
    }

    private void setFriendUnread(int unread) {
        if (unread == 0) {
            tv_new_friend_count.setVisibility(View.INVISIBLE);
        } else {
            tv_new_friend_count.setVisibility(View.VISIBLE);
            if (unread > 99) {
                tv_new_friend_count.setBackgroundResource(R.drawable.nim_red_max_circle);
            } else {
                tv_new_friend_count.setBackgroundResource(R.drawable.nim_red_circle);
            }

            tv_new_friend_count.setText(String.valueOf(unread));
        }
    }

    private void updateTotalCount() {
        if (mLists == null || mLists.size() == 0) {
            tv_total_count.setText("你还没有任何好友");
        } else {
            String text = mLists.size() + " 位联系人";
            tv_total_count.setText(text);
        }
    }

    private void confirmDeleteFriend(final long friendId, final String title){
        String message = String.format("将联系人\"%s\"删除，同时删除与他的聊天记录", title);
        ProgressDialog.showCustomDialog(this, "删除好友",message, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPreDialog("");
                FriendDelProcessor friendDelProcessor = GlobalProcessor.getInstance().getFriendDelProcessor();
                friendDelProcessor.sendFriendDelRQ(friendId);
            }
        });
    }

    @Override
    public void onLetterChanged(String letter, int position, float y) {
        quickSideBarTipsView.setText(letter, position, y);
        //有此key则获取位置并滚动到该位置
        if(letters.containsKey(letter)) {
            recyclerView.scrollToPosition(letters.get(letter) + mAdapter.getHeaderLayoutCount());
        }
    }

    @Override
    public void onLetterTouching(boolean touching) {
        quickSideBarTipsView.setVisibility(touching? View.VISIBLE:View.INVISIBLE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.addFriends) {
            setFriendUnread(0);
            NIMContactManager.getInstance().setIs_clear(true);
            NIMFriendInfoManager.getInstance().clearUnread_count();
            is_handle_friend = true;
            Intent intent = new Intent(NIMContactActivity.this, FriendRequestActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.groups) {
            Intent intent = new Intent(NIMContactActivity.this, GroupAllActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.publishers) {
            NIMStartActivityUtil.startToOfficialContact(NIMContactActivity.this);
        } else if (v.getId() == R.id.black_list) {
            Intent intent = new Intent(NIMContactActivity.this, NIMBlackListActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.title_back) {
            onBackPressed();
        }
    }

    @Override
    public void OnChange(int param1, Object param2, Object param3) {
        if (param1 == DataConstDef.EVENT_FRIEND_DEL) {
            hidePreDialog();
            if ((boolean)param3) {
                long user_id = (long) param2;
                has_change_data = true;
                if (isExistFriend(user_id)) {
                    sortList();
                }
            }
        } else if (param1 == DataConstDef.EVENT_NET_ERROR) {
            int error_code = BaseUtil.MakeErrorResult((int) param2);
            String error_msg = ErrorDetail.GetErrorDetail(error_code);
            showToastStr(error_msg);
        } else if (param1 == DataConstDef.EVENT_FRIEND_ADD_REQUEST) {
            if (!(boolean) param3) {
                return;
            }

            setFriendUnread(NIMFriendInfoManager.getInstance().getUnread_count()
                    + NIMContactManager.getInstance().getCount());
        } else if (param1 == DataConstDef.EVENT_FRIEND_CONFIRM) {
            sortList();
            setFriendUnread(NIMFriendInfoManager.getInstance().getUnread_count()
                    + NIMContactManager.getInstance().getCount());
        }
    }

    private boolean isExistFriend(long user_id) {
        boolean exist = false;
        for (IMFriendInfo friendInfo : mAdapter.getData()) {
            if (friendInfo.userId == user_id) {
                exist = true;
            }
        }

        return exist;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_USER_INFO) {
                int pos = data.getIntExtra("pos", -1);
                if (pos == nCurPosition) {
                    has_change_data = true;
                    boolean black = data.getBooleanExtra("black", false);
                    boolean remark = data.getBooleanExtra("remark", false);
                    if (black || remark) {
                        sortList();
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (is_handle_friend) {
            setResult(RESULT_OK);
        }
        super.onBackPressed();
    }

}
