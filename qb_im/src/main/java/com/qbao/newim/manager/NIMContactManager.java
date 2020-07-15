package com.qbao.newim.manager;

import android.Manifest;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import com.qbao.newim.model.Contact;
import com.qbao.newim.model.IMFriendInfo;
import com.qbao.newim.permission.AndPermission;
import com.qbao.newim.processor.GlobalProcessor;
import com.qbao.newim.processor.UserListProcessor;
import com.qbao.newim.util.AppUtil;
import com.qbao.newim.util.Utils;
import com.qbao.newim.util.ValidatorUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.qbao.newim.util.Utils.encrypt;

/**
 * Created by chenjian on 2017/8/2.
 */

public class NIMContactManager {
    private static NIMContactManager instance;
    private ArrayList<IMFriendInfo> random_list = new ArrayList();
    private HashMap<Long, String> contact_map = new HashMap<>();
    private static final int MAX_RANDOM = 500;
    private boolean is_clear;

    public static NIMContactManager getInstance() {
        if (instance == null) {
            instance = new NIMContactManager();
        }

        return instance;
    }

    public void getRandomContact() {
        if (AndPermission.hasPermission(AppUtil.GetContext(),
                Manifest.permission.READ_CONTACTS)) {
            QueryContact queryContact = new QueryContact();
            queryContact.execute();
        }
    }

    class QueryContact extends AsyncTask<Void, Void, ArrayList<Contact>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<Contact> doInBackground(Void... params) {
            ArrayList<Contact> list = init();
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<Contact> result) {
            queryUserExist(result);
            super.onPostExecute(result);
        }
    }

    private ArrayList<Contact> init() {
        long phone = NIMUserInfoManager.getInstance().getSelfUser().getMobile();

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
        Cursor cursor = AppUtil.GetContext().
                getContentResolver().query(uri, projection, null, null, "sort_key COLLATE LOCALIZED asc");

        ArrayList<Contact> list = new ArrayList<>();
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
                number = number.replaceAll("[^+\\d]", "");
                boolean is_phone = ValidatorUtil.isMobile(number);
                if (!is_phone || String.valueOf(phone).equals(number)) {
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
                if (info == null) {
                    if (!list.contains(contact)) {
                        list.add(contact);
                    }
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }

        return list;
    }

    private void queryUserExist(List<Contact> contacts) {
        int nSize;
        boolean need_random = false;
        int[] random_array = null;
        if (contacts.size() > MAX_RANDOM) {
            nSize = MAX_RANDOM;
            random_array = Utils.randomArray(0, contacts.size() - 1, MAX_RANDOM);
            need_random= true;
        } else {
            nSize = contacts.size();
        }

        long[] users = new long[nSize];
        for (int i = 0; i < nSize; i++) {
            if (need_random) {
                users[i] = Long.parseLong(contacts.get(random_array[i]).contactNum);
                contact_map.put(users[i], contacts.get(random_array[i]).contactName);
            } else {
                users[i] = Long.parseLong(contacts.get(i).contactNum);
                contact_map.put(users[i], contacts.get(i).contactName);
            }
        }

        if (random_list.size() > 0) {
            random_list.clear();
        }

        UserListProcessor processor = GlobalProcessor.getInstance().getUser_list_processor();
        processor.SendUserMobileListRQ(users);
    }

    public int getCount() {
        if (is_clear) {
            return 0;
        }
        return random_list.size();
    }

    public void setIs_clear(boolean is_clear) {
        this.is_clear = is_clear;
    }

    public ArrayList<IMFriendInfo> getRandom_list() {
        return random_list;
    }

    public String getContactName(long num) {
        return contact_map.get(num);
    }

    public void addContact(IMFriendInfo imUserInfo) {
        if (!random_list.contains(imUserInfo))
            random_list.add(imUserInfo);
    }

    public void clear() {
        random_list.clear();
        is_clear = true;
    }
}
