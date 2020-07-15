package com.qbao.newim.manager;

import com.qbao.newim.constdef.MsgConstDef;
import com.qbao.newim.model.IMGroupInfo;
import com.qbao.newim.qbdb.manager.GroupInfoDbManager;
import com.qbao.newim.qbdb.manager.GroupUserDbManager;
import com.qbao.newim.util.Logger;
import com.qbao.newim.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by chenjian on 2017/5/23.
 */

public class NIMGroupInfoManager
{
    private static final String TAG = "NIM_GroupInfoManager";
    private static NIMGroupInfoManager _instance = null;

    private HashMap<Long, IMGroupInfo> mapGroupInfo = new HashMap<>();
    private GroupInfoDbManager dbManager = null;

    public static NIMGroupInfoManager getInstance()
    {
        if(null == _instance)
        {
            _instance = new NIMGroupInfoManager();
        }

        return _instance;
    }

    public void init()
    {
        if(null == dbManager)
        {
            dbManager = new GroupInfoDbManager();
        }

        List<IMGroupInfo> list_group_info = dbManager.getAllGroupSession();
        for(int index = 0; index < list_group_info.size(); index++)
        {
            IMGroupInfo group_info = list_group_info.get(index);
            if(!mapGroupInfo.containsKey(group_info.group_id))
            {
                mapGroupInfo.put(group_info.group_id, group_info);
            }
        }
    }

    // 添加或者更新
    public void AddGroup(IMGroupInfo group_info)
    {
        if (group_info == null)
        {
            Logger.warning(TAG, "group info is null");
            return;
        }

        Long group_id = group_info.group_id;
        if(group_id <= 0)
        {
            Logger.warning(TAG, "group info is invalid group_id = " + String.valueOf(group_id));
            return;
        }

        if (mapGroupInfo.containsKey(group_id))
        {
            Logger.warning(TAG, "group_id = " + group_info.group_id + "is exist");
        }

        mapGroupInfo.put(group_id, group_info);

        dbManager.insertOrReplace(group_info);
    }

    // 修改群信息的时候调用
    public void updateGroup(IMGroupInfo group_info)
    {
        if (group_info == null)
        {
            Logger.warning(TAG, "group info is null");
            return;
        }

        Long group_id = group_info.group_id;
        if (!mapGroupInfo.containsKey(group_info.group_id))
        {
            Logger.error(TAG, "group_id is not exist group_id = " + String.valueOf(group_id));
            return ;
        }

        mapGroupInfo.put(group_info.group_id, group_info);

        dbManager.update(group_info);
    }

    public boolean checkIsInAssist(long group_id)
    {
        if(mapGroupInfo.containsKey(group_id))
        {
            IMGroupInfo group_info = mapGroupInfo.get(group_id);

            if(group_info.notify_type == MsgConstDef.GROUP_MESSAGE_STATUS.GROUP_MESSAGE_IN_HELP_NO_HIT)
            {
                return true;
            }
        }
        else
        {
            Logger.error(TAG, "group_id is invalid group_id = " + String.valueOf(group_id));
        }

        return false;
    }

    public boolean DeleteGroup(long group_id)
    {
        if (group_id <= 0)
        {
            Logger.warning(TAG, "invalid user group_id = " + String.valueOf(group_id));
            return false;
        }

        if(!mapGroupInfo.containsKey(group_id))
        {
            Logger.warning(TAG, "invalid user group_id = " + String.valueOf(group_id) + " not exist");
            return false;
        }

        dbManager.delete(mapGroupInfo.get(group_id));
        mapGroupInfo.remove(group_id);

        // [todo] 待确认
        GroupUserDbManager.getInstance().deleteAllGroupUser(group_id);

        return false;
    }

    public IMGroupInfo getGroupInfo(long group_id)
    {
        return mapGroupInfo.get(group_id);
    }

    public ArrayList<Long> getIdList()
    {
        ArrayList<Long> list_ids = new ArrayList<>();
        Iterator iterator = mapGroupInfo.entrySet().iterator();

        while (iterator.hasNext())
        {
            HashMap.Entry entry = (HashMap.Entry) iterator.next();
            Long key = (long) entry.getKey();
            list_ids.add(key);
        }

        return list_ids;
    }

    public boolean checkUserIsGroupManager(Long group_id, Long user_id)
    {
        IMGroupInfo group_info = mapGroupInfo.get(group_id);
        if(null == group_info)
        {
            return false;
        }

        if(group_info.group_manager_user_id != user_id)
        {
            return false;
        }

        return true;
    }

    public int getGroupCount()
    {
        return mapGroupInfo.size();
    }

    public boolean isGroupExist(long group_id)
    {
        return mapGroupInfo.containsKey(group_id);
    }

    public void clear()
    {
        mapGroupInfo.clear();
    }

    public ArrayList<IMGroupInfo> getAllGroupSession()
    {
        return getAllGroupSession(true);
    }

    public ArrayList<IMGroupInfo> getAllGroupSession(boolean save)
    {
        ArrayList<IMGroupInfo> list = new ArrayList<>();
        Iterator iterator = mapGroupInfo.entrySet().iterator();
        while (iterator.hasNext())
        {
            HashMap.Entry entry = (HashMap.Entry) iterator.next();
            IMGroupInfo value = (IMGroupInfo) entry.getValue();
            if (save)
            {
                if(value.is_save_contact)
                {
                    list.add(value);
                }
            }
            else
            {
                value.name_pinyin = Utils.converterToSpellMap(value.group_name);
                list.add(value);
            }
        }

        return list;
    }
}
