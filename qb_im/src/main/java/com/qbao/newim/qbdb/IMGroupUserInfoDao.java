package com.qbao.newim.qbdb;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.qbao.newim.model.IMGroupUserInfo;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "IMGROUP_USER_INFO".
*/
public class IMGroupUserInfoDao extends AbstractDao<IMGroupUserInfo, String> {

    public static final String TABLENAME = "IMGROUP_USER_INFO";

    /**
     * Properties of entity IMGroupUserInfo.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Primary_key = new Property(0, String.class, "primary_key", true, "PRIMARY_KEY");
        public final static Property User_id = new Property(1, long.class, "user_id", false, "user_id");
        public final static Property Group_id = new Property(2, long.class, "group_id", false, "group_id");
        public final static Property User_nick_name = new Property(3, String.class, "user_nick_name", false, "user_nick_name");
        public final static Property User_group_index = new Property(4, int.class, "user_group_index", false, "user_group_index");
        public final static Property Pinyin = new Property(5, String.class, "pinyin", false, "pinyin");
        public final static Property Need_agree = new Property(6, boolean.class, "need_agree", false, "need_agree");
    }


    public IMGroupUserInfoDao(DaoConfig config) {
        super(config);
    }
    
    public IMGroupUserInfoDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"IMGROUP_USER_INFO\" (" + //
                "\"PRIMARY_KEY\" TEXT PRIMARY KEY NOT NULL ," + // 0: primary_key
                "\"user_id\" INTEGER NOT NULL ," + // 1: user_id
                "\"group_id\" INTEGER NOT NULL ," + // 2: group_id
                "\"user_nick_name\" TEXT," + // 3: user_nick_name
                "\"user_group_index\" INTEGER NOT NULL ," + // 4: user_group_index
                "\"pinyin\" TEXT," + // 5: pinyin
                "\"need_agree\" INTEGER NOT NULL );"); // 6: need_agree
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"IMGROUP_USER_INFO\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, IMGroupUserInfo entity) {
        stmt.clearBindings();
 
        String primary_key = entity.getPrimary_key();
        if (primary_key != null) {
            stmt.bindString(1, primary_key);
        }
        stmt.bindLong(2, entity.getUser_id());
        stmt.bindLong(3, entity.getGroup_id());
 
        String user_nick_name = entity.getUser_nick_name();
        if (user_nick_name != null) {
            stmt.bindString(4, user_nick_name);
        }
        stmt.bindLong(5, entity.getUser_group_index());
 
        String pinyin = entity.getPinyin();
        if (pinyin != null) {
            stmt.bindString(6, pinyin);
        }
        stmt.bindLong(7, entity.getNeed_agree() ? 1L: 0L);
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, IMGroupUserInfo entity) {
        stmt.clearBindings();
 
        String primary_key = entity.getPrimary_key();
        if (primary_key != null) {
            stmt.bindString(1, primary_key);
        }
        stmt.bindLong(2, entity.getUser_id());
        stmt.bindLong(3, entity.getGroup_id());
 
        String user_nick_name = entity.getUser_nick_name();
        if (user_nick_name != null) {
            stmt.bindString(4, user_nick_name);
        }
        stmt.bindLong(5, entity.getUser_group_index());
 
        String pinyin = entity.getPinyin();
        if (pinyin != null) {
            stmt.bindString(6, pinyin);
        }
        stmt.bindLong(7, entity.getNeed_agree() ? 1L: 0L);
    }

    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }    

    @Override
    public IMGroupUserInfo readEntity(Cursor cursor, int offset) {
        IMGroupUserInfo entity = new IMGroupUserInfo( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // primary_key
            cursor.getLong(offset + 1), // user_id
            cursor.getLong(offset + 2), // group_id
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // user_nick_name
            cursor.getInt(offset + 4), // user_group_index
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // pinyin
            cursor.getShort(offset + 6) != 0 // need_agree
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, IMGroupUserInfo entity, int offset) {
        entity.setPrimary_key(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setUser_id(cursor.getLong(offset + 1));
        entity.setGroup_id(cursor.getLong(offset + 2));
        entity.setUser_nick_name(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setUser_group_index(cursor.getInt(offset + 4));
        entity.setPinyin(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setNeed_agree(cursor.getShort(offset + 6) != 0);
     }
    
    @Override
    protected final String updateKeyAfterInsert(IMGroupUserInfo entity, long rowId) {
        return entity.getPrimary_key();
    }
    
    @Override
    public String getKey(IMGroupUserInfo entity) {
        if(entity != null) {
            return entity.getPrimary_key();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(IMGroupUserInfo entity) {
        return entity.getPrimary_key() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
