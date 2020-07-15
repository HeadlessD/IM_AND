package com.qbao.newim.manager;

import com.qbao.newim.model.IMOfficialInfo;
import com.qbao.newim.qbdb.manager.OfficialDbManager;
import com.qbao.newim.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by chenjian on 2017/5/17.
 */

public class NIMOfficialManager {
    private static NIMOfficialManager instance;
    private static LinkedHashMap<Long, IMOfficialInfo> official_map = new LinkedHashMap<>();
    private static OfficialDbManager dbManager;

    public static NIMOfficialManager getInstance() {
        if (instance == null) {
            instance = new NIMOfficialManager();
        }

        return instance;
    }

    /**
     * 设置所有公众号id列表
     * @param list id和name
     */
    public void setOfficialList(List<IMOfficialInfo> list) {
        if (official_map.size() > 0) {
            official_map.clear();
        }
        for (IMOfficialInfo info : list) {
            official_map.put(info.official_id, info);
        }
    }

    public void init() {
        dbManager = new OfficialDbManager();
        List<IMOfficialInfo> list = dbManager.getAllOfficial();
        for (IMOfficialInfo info : list) {
            if (official_map.containsKey(info.official_id))
                official_map.put(info.official_id, info);
        }
    }

    public boolean isOfficialFans(long official_id) {
        return official_map.containsKey(official_id);
    }

    public List<IMOfficialInfo> getOfficialList() {
        List<IMOfficialInfo> list = new ArrayList<>();
        Iterator iterator = official_map.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry entry = (HashMap.Entry) iterator.next();
            IMOfficialInfo value = (IMOfficialInfo) entry.getValue();
            value.name_pinyin = Utils.converterToSpellMap(value.official_name);
            list.add(value);
        }
        return list;
    }

    public void clear()
    {
        official_map.clear();
    }
}
