package com.qbao.newim.qbdb;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.qbao.newim.model.IMOfficialInfo;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "IMOFFICIAL_INFO".
*/
public class IMOfficialInfoDao extends AbstractDao<IMOfficialInfo, Long> {

    public static final String TABLENAME = "IMOFFICIAL_INFO";

    /**
     * Properties of entity IMOfficialInfo.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Official_id = new Property(0, long.class, "official_id", true, "_id");
        public final static Property Official_name = new Property(1, String.class, "official_name", false, "OFFICIAL_NAME");
        public final static Property Official_url = new Property(2, String.class, "official_url", false, "OFFICIAL_URL");
        public final static Property Last_msg_id = new Property(3, long.class, "last_msg_id", false, "LAST_MSG_ID");
    }


    public IMOfficialInfoDao(DaoConfig config) {
        super(config);
    }
    
    public IMOfficialInfoDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"IMOFFICIAL_INFO\" (" + //
                "\"_id\" INTEGER PRIMARY KEY NOT NULL ," + // 0: official_id
                "\"OFFICIAL_NAME\" TEXT," + // 1: official_name
                "\"OFFICIAL_URL\" TEXT," + // 2: official_url
                "\"LAST_MSG_ID\" INTEGER NOT NULL );"); // 3: last_msg_id
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"IMOFFICIAL_INFO\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, IMOfficialInfo entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getOfficial_id());
 
        String official_name = entity.getOfficial_name();
        if (official_name != null) {
            stmt.bindString(2, official_name);
        }
 
        String official_url = entity.getOfficial_url();
        if (official_url != null) {
            stmt.bindString(3, official_url);
        }
        stmt.bindLong(4, entity.getLast_msg_id());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, IMOfficialInfo entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getOfficial_id());
 
        String official_name = entity.getOfficial_name();
        if (official_name != null) {
            stmt.bindString(2, official_name);
        }
 
        String official_url = entity.getOfficial_url();
        if (official_url != null) {
            stmt.bindString(3, official_url);
        }
        stmt.bindLong(4, entity.getLast_msg_id());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.getLong(offset + 0);
    }    

    @Override
    public IMOfficialInfo readEntity(Cursor cursor, int offset) {
        IMOfficialInfo entity = new IMOfficialInfo( //
            cursor.getLong(offset + 0), // official_id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // official_name
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // official_url
            cursor.getLong(offset + 3) // last_msg_id
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, IMOfficialInfo entity, int offset) {
        entity.setOfficial_id(cursor.getLong(offset + 0));
        entity.setOfficial_name(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setOfficial_url(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setLast_msg_id(cursor.getLong(offset + 3));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(IMOfficialInfo entity, long rowId) {
        entity.setOfficial_id(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(IMOfficialInfo entity) {
        if(entity != null) {
            return entity.getOfficial_id();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(IMOfficialInfo entity) {
        throw new UnsupportedOperationException("Unsupported for entities with a non-null key");
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
