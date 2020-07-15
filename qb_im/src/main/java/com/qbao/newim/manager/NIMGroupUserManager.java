package com.qbao.newim.manager;

import com.qbao.newim.model.IMGroupUserInfo;
import com.qbao.newim.qbdb.manager.GroupUserDbManager;
import com.qbao.newim.util.Logger;
import com.qbao.newim.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chenjian on 2017/6/19.
 */

public class NIMGroupUserManager {
    private static final String TAG = "NIMGroupUserManager";
    private static NIMGroupUserManager _instance = new NIMGroupUserManager();

    public static NIMGroupUserManager getInstance() {
        return _instance;
    }

    private HashMap<Long, HashMap<Long, IMGroupUserInfo>> group_info_map = new HashMap<>();

    public void AddGroupUser(long group_id, IMGroupUserInfo info) {
        AddGroupUser(group_id, info, true);
    }

    public void AddGroupUser(long group_id, IMGroupUserInfo info, boolean bWriteSQL) {
        if (group_id <= 0 || info == null || info.user_id <= 0) {
            Logger.warning(TAG, "invalid user");
            return;
        }

        info.GenPrimaryKey();
        info.group_id = group_id;
        HashMap<Long, IMGroupUserInfo> map;
        // 当前包含group_id
        if (group_info_map.containsKey(group_id)) {
            map = group_info_map.get(group_id);
            // 当前user已经存在了
            if (map.containsKey(info.user_id)) {
                // 当前用户名一样，不用更改
                if (map.get(info.user_id).user_nick_name.equals(info.user_nick_name)) {
                    return;
                // 当前用户名不一样，更新
                } else {
                    map.get(info.user_id).user_nick_name = info.user_nick_name;
                    map.get(info.user_id).pinyin = Utils.converterToSpell(info.user_nick_name);
                    if (bWriteSQL) {
                        IMGroupUserInfo exist_user = getGroupUserInfo(group_id, info.user_id);
                        if (exist_user != null) {
                            exist_user.user_nick_name = info.user_nick_name;
                            exist_user.pinyin = map.get(info.user_id).pinyin;
                            group_info_map.put(group_id, map);
                            GroupUserDbManager.getInstance().update(exist_user);
                            return;
                        }
                    }
                }
            // 当前user不存在,add进去
            } else {
                info.pinyin = Utils.converterToSpell(info.user_nick_name);
                map.put(info.user_id, info);
            }
        // 如果不包含
        } else {
            map = new HashMap<>();
            info.pinyin = Utils.converterToSpell(info.user_nick_name);
            map.put(info.user_id, info);
        }

        group_info_map.put(group_id, map);

        if (bWriteSQL) {
            GroupUserDbManager.getInstance().insertOrReplace(info);
        }
    }

    public IMGroupUserInfo getGroupUserInfo(long group_id, long user_id) {
        if (group_id <= 0 || user_id <= 0) {
            return null;
        }

        IMGroupUserInfo groupUserInfo;
        if (group_info_map.containsKey(group_id)
                && group_info_map.get(group_id).containsKey(user_id)) {
            groupUserInfo = group_info_map.get(group_id).get(user_id);
        } else {
            groupUserInfo = GroupUserDbManager.getInstance().getGroupUser(group_id, user_id);
        }

        return groupUserInfo;
    }

    public ArrayList<IMGroupUserInfo> getGroupAllUser(long group_id) {
        return getGroupAllUser(group_id, true);
    }

    public ArrayList<IMGroupUserInfo> getGroupAllUser(long group_id, boolean sort_index) {
        if (group_id <= 0) {
            return null;
        }

        ArrayList<IMGroupUserInfo> list = new ArrayList<>();

        if (group_info_map.containsKey(group_id)) {
            for (Long user_id :group_info_map.get(group_id).keySet()) {
                group_info_map.get(group_id).get(user_id).sortByIndex = sort_index;
                list.add(group_info_map.get(group_id).get(user_id));
            }
        } else {
            List<IMGroupUserInfo> sql_list = GroupUserDbManager.getInstance().getGroupAllUser(group_id);
            for (IMGroupUserInfo group_user : sql_list) {
                AddGroupUser(group_id, group_user);
                group_user.sortByIndex = sort_index;
                group_user.nick_index = Utils.converterToSpellMap(group_user.user_nick_name);
            }
            list.addAll(sql_list);
        }
        Collections.sort(list);

        return list;
    }

    public List<IMGroupUserInfo> getGroupAllUserByCount(long group_id) {
        List<IMGroupUserInfo> list = new ArrayList<>();
        list.addAll(getGroupAllUser(group_id));
        if (list.size() > 8) {
            list = list.subList(0, 8);
        }
        return list;
    }

    public boolean deleteGroupUser(long group_id, long user_id) {
        if (group_id < 0) {
            return false;
        }

        IMGroupUserInfo info = getGroupUserInfo(group_id, user_id);

        if (info == null) {
            return true;
        }

        if (group_info_map.containsKey(group_id)) {
            if (group_info_map.get(group_id).containsKey(user_id)) {
                group_info_map.get(group_id).remove(user_id);
            }
        }

        return GroupUserDbManager.getInstance().delete(info);
    }

    public void clear() {
        group_info_map.clear();
    }
}
