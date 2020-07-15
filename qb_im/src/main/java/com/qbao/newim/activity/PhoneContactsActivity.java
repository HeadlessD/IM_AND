package com.qbao.newim.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.qbao.newim.adapter.ContactListAdapter;
import com.qbao.newim.adapter.adapterhelper.BaseQuickAdapter;
import com.qbao.newim.configure.GlobalVariable;
import com.qbao.newim.constdef.DataConstDef;
import com.qbao.newim.constdef.ErrorDetail;
import com.qbao.newim.constdef.FriendTypeDef;
import com.qbao.newim.manager.NIMFriendInfoManager;
import com.qbao.newim.manager.NIMUserInfoManager;
import com.qbao.newim.model.Contact;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.model.IMUserInfo;
import com.qbao.newim.permission.AndPermission;
import com.qbao.newim.permission.PermissionListener;
import com.qbao.newim.permission.Rationale;
import com.qbao.newim.permission.RationaleListener;
import com.qbao.newim.processor.GlobalProcessor;
import com.qbao.newim.processor.UserInfoGetProcessor;
import com.qbao.newim.qbim.R;
import com.qbao.newim.util.BaseUtil;
import com.qbao.newim.util.DataObserver;
import com.qbao.newim.util.IDataObserver;
import com.qbao.newim.util.Logger;
import com.qbao.newim.util.Utils;
import com.qbao.newim.util.ValidatorUtil;
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

import static com.qbao.newim.util.Utils.encrypt;

/**
 * Created by chenjian on 2017/5/27.
 */

public class PhoneContactsActivity extends BaseSearchActivity implements OnQuickSideBarTouchListener, IDataObserver{

    private ImageView iv_back;
    TextView tvTitle;

    RecyclerView recyclerView;
    QuickSideBarView quickSideBarView;
    QuickSideBarTipsView quickSideBarTipsView;
    HashMap<String,Integer> letters = new HashMap<>();
    private List<Contact> mLists;
    private ContactListAdapter mAdapter;
    StickyRecyclerHeadersDecoration headersDecor;
    private MaterialSearchView searchView;

    private int nCurPosition;
    private Contact cur_contact;

    private static final int REQUEST_CONTACT_PERMISSION = 91;
    private static final int REQUEST_SMS_PERMISSION = 92;

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContentView(R.layout.nim_activity_contacts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.contact_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        iv_back = (ImageView) findViewById(R.id.title_back);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        quickSideBarView = (QuickSideBarView) findViewById(R.id.quickSideBarView);
        quickSideBarTipsView = (QuickSideBarTipsView) findViewById(R.id.quickSideBarTipsView);

        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setCursorDrawable(R.drawable.nim_cursor_green);
    }

    @Override
    protected void setListener() {
        DataObserver.Register(this);

        quickSideBarView.setOnQuickSideBarTouchListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new ContactListAdapter(null);
        recyclerView.setAdapter(mAdapter);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                quickSideBarView.setVisibility(View.GONE);
                if (!searchView.hasAdapter())
                    searchView.setPhoneSuggestions(mAdapter.getData());
            }

            @Override
            public void onSearchViewClosed() {
                quickSideBarView.setVisibility(View.VISIBLE);
            }
        });

        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public boolean onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                nCurPosition = position;
                addPhoneFriend(mAdapter.getItem(position));
                return false;
            }
        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(Object object) {
                addPhoneFriend((Contact)object);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    protected void processLogic(Bundle savedInstanceState) {
        hasPermission();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.nim_search_contact_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_add);
        menuItem.setVisible(false);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataObserver.Cancel(this);
    }

    @Override
    public void OnChange(int param1, Object param2, Object param3) {
        switch (param1) {
            case DataConstDef.EVENT_GET_USER_INFO:
                hidePreDialog();
                if ((boolean)param3) {
                    IMUserInfo info = (IMUserInfo)param2;
                    if (info == null) {
                        return;
                    }

                    if (info.userId == NIMUserInfoManager.getInstance().GetSelfUserId()) {
                        return;
                    }

                    Contact contact = mAdapter.getItem(nCurPosition);
                    if (String.valueOf(info.mobile).equals(contact.contactNum)) {
                        contact.user_id = info.userId;
                        mAdapter.setData(nCurPosition, contact);
                    }

                    Intent intent = new Intent(this, FriendVerifyActivity.class);
                    intent.putExtra("user_id", info.userId);
                    intent.putExtra("source_type", FriendTypeDef.FRIEND_SOURCE_TYPE.CONTACTS);
                    startActivity(intent);
                } else {
                    ProgressDialog.showCustomDialog(PhoneContactsActivity.this, "提示", "对方还未注册，邀请一起优活？"
                            , new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    getSMSPermission();
                                }
                            });
                }

                break;
            case DataConstDef.EVENT_NET_ERROR:
                Logger.error(TAG, "EVENT_NET_ERROR");
                if (is_active) {
                    int error_code = BaseUtil.MakeErrorResult((int) param2);
                    String error_msg = ErrorDetail.GetErrorDetail(error_code);
                    showToastStr(error_msg);
                }
                break;
            case DataConstDef.EVENT_FRIEND_ADD_REQUEST:
                if (!(boolean)param3) {
                    ProgressDialog.showCustomDialog(PhoneContactsActivity.this, "对方拒绝接受你的请求");
                    return;
                }
                break;
            case DataConstDef.EVENT_FRIEND_CONFIRM:
                IMFriendInfo confirm_info = NIMFriendInfoManager.getInstance().getFriendReqInfo((long) param2);

                Contact contact = mAdapter.getItem(nCurPosition);
                if (String.valueOf(confirm_info.mobile).equals(contact.contactNum)) {
                    if (contact.is_add) {
                        return;
                    }
                    contact.user_id = confirm_info.userId;
                    contact.is_add = true;
                    contact.contactName += "(" + confirm_info.nickName + ")";
                    mAdapter.setData(nCurPosition, contact);
                }

                break;
        }
    }

    @Override
    public void onLetterChanged(String letter, int position, float y) {
        quickSideBarTipsView.setText(letter, position, y);
        //有此key则获取位置并滚动到该位置
        if(letters.containsKey(letter)) {
            recyclerView.scrollToPosition(letters.get(letter));
        }
    }

    @Override
    public void onLetterTouching(boolean touching) {
        quickSideBarTipsView.setVisibility(touching? View.VISIBLE:View.INVISIBLE);
    }


    private void addPhoneFriend(Contact contact) {
        if (NIMFriendInfoManager.getInstance().OutFriendMaxCount()) {
            showToastStr("你已达到好友上限" + GlobalVariable.FRIEND_MAX_COUNT + "人");
            return;
        }
        cur_contact = contact;
        UserInfoGetProcessor processor = GlobalProcessor.getInstance().getUser_processor();
        processor.SendUserInfoRQ(contact.contactNum);
    }

    private ArrayList<Contact> init() {
        long phone = 0;
        try{
            phone = NIMUserInfoManager.getInstance().getSelfUser().getMobile();
        } catch (Exception e) {
            ProgressDialog.showCustomDialog(this, "获取通讯录失败", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI; // 联系人的Uri
        String[] projection = {
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.DATA1,
                "sort_key",
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY
        }; // 查询的列
        Cursor cursor = getContentResolver().query(uri, projection, null, null, "sort_key COLLATE LOCALIZED asc");

        ArrayList<Contact> list = new ArrayList<Contact>();
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                String name = cursor.getString(1);
                String number = cursor.getString(2);
                int contactId = cursor.getInt(4);
                Long photoId = cursor.getLong(5);
                Contact contact = new Contact();
                contact.contactName = name;
                contact.pinyin = Utils.converterToSpell(name);
                contact.pinyin_index = Utils.converterToSpellMap(name);
                number = number.replaceAll("[^+\\d]", "");
                boolean is_phone = ValidatorUtil.isMobile(number);
                if (!is_phone || phone == Long.parseLong(number) ) {
                    continue;
                }
                if (number.startsWith("+86")) {
                    number = number.replace("+86", "");
                }
                contact.contactNum = number;
                contact.hashValue = encrypt(number);
                contact.contactId = contactId;
                contact.contactPhoto = photoId;
                IMFriendInfo info = NIMFriendInfoManager.getInstance().queryPhone(number);
                if (info != null) {
                    contact.user_id = info.userId;
                    String show_name = Utils.getUserShowName(new String[]{info.remark_name, info.nickName, info.user_name});
                    contact.contactName = name + "(" + show_name + ")";
                    contact.is_add = true;
                }
                if (!list.contains(contact)) {
                    list.add(contact);
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }

        return list;
    }

    private void hasPermission() {
        final String tip = getString(R.string.nim_permission_contact_fail);
        AndPermission.with(this)
                .requestCode(REQUEST_CONTACT_PERMISSION)
                .permission(Manifest.permission.READ_CONTACTS)
                .failTips(tip)
                .callback(permissionListener)
                // rationale作用是：用户拒绝一次权限，再次申请时先征求用户同意，再打开授权对话框，避免用户勾选不再提示。
                .rationale(new RationaleListener() {
                    @Override
                    public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
                        AndPermission.rationaleDialog(PhoneContactsActivity.this, tip, rationale).show();
                    }
                }).start();
    }

    private void getSMSPermission() {
        final String tip = getString(R.string.nim_permission_sms_fail);
        AndPermission.with(this)
                .requestCode(REQUEST_SMS_PERMISSION)
                .permission(Manifest.permission.SEND_SMS)
                .failTips(tip)
                .callback(permissionListener)
                // rationale作用是：用户拒绝一次权限，再次申请时先征求用户同意，再打开授权对话框，避免用户勾选不再提示。
                .rationale(new RationaleListener() {
                    @Override
                    public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
                        AndPermission.rationaleDialog(PhoneContactsActivity.this, tip, rationale).show();
                    }
                }).start();
    }

    private PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
            switch (requestCode) {
                case REQUEST_CONTACT_PERMISSION:
                    QueryContact queryContact = new QueryContact();
                    queryContact.execute();
                    break;
                case REQUEST_SMS_PERMISSION:
                    sendSMS(cur_contact);
                    break;
            }
        }

        @Override
        public void onCancel(int requestCode, Context context) {

        }
    };

    private void sendSMS(Contact contact) {
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("address", contact.contactNum);
        smsIntent.putExtra("sms_body", getSendSMSContent("测试码xxx", "www.qbao.com"));
        startActivity(smsIntent);
    }

    private String getSendSMSContent(String code, String url) {
        StringBuilder content = new StringBuilder("好基友，玩钱宝这样的大事你以为我会不告诉你吗！我的专属邀请码");
        content.append(code);
        content.append("，快去下一个一起来吧~");
        content.append(url);
        return content.toString();
    }

    class QueryContact extends AsyncTask<Void, Void, ArrayList<Contact>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showPreDialog("正在加载通讯录");
        }

        @Override
        protected ArrayList<Contact> doInBackground(Void... params) {
            ArrayList<Contact> list = init();
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<Contact> result) {
            mLists = result;
            initData();
            hidePreDialog();
            super.onPostExecute(result);
        }
    }

    private void initData() {
        Collections.sort(mLists);
        mAdapter.setNewData(mLists);

        int position = 0;
        List<String> customLetters = quickSideBarView.getLetters();
        for (Contact contact : mLists) {
            String letter = contact.getInitial();
            if (letter.equals("~")){
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

        quickSideBarView.setLetters(customLetters);
        headersDecor = new StickyRecyclerHeadersDecoration(mAdapter);
        recyclerView.addItemDecoration(headersDecor);
        recyclerView.addItemDecoration(new DividerDecoration(this));
    }
}
