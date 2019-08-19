package com.bsw.dblibrary.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * 数据库工具
 *
 * @author bsw
 */
public class DbUtils extends DbBase {
    String dbName = "bsw.db";// 数据库名
    public int version = 1;// 版本号
    DbManager mDbManager;
    Context mContext;

    private Cursor mCursor;

    public DbUtils(Context mContext) {
        this.mContext = mContext;
        mDbManager = new DbManager(mContext);
        Map<String, String[]> tableMap = getDB(mContext);
        if (null != tableMap) {
            mDbManager.create(dbName, version, tableMap.get(DB_TABLES), tableMap.get(DB_SQLS));
        }
    }

    /**
     * 异步操作
     *
     * @param onTransaction 异步接口
     */
    public void executeTransaction(final OnTransaction onTransaction) {
        Executors.newFixedThreadPool(3).execute(new Runnable() {
            @Override
            public void run() {
                onTransaction.execute(DbUtils.this);
            }
        });
    }

    /**
     * 目标类
     *
     * @param clz 目标类
     * @return 查询类
     */
    public <T> DbQuery<T> where(Class<T> clz) {
        return new DbQuery<>(this, clz);
    }

    /**
     * @param t   被插入
     * @param <T> 泛型
     * @return 是插入还是更新：true为更新，false为插入
     */
    public <T> boolean update(T t) {
        return insert(t);
    }

    /**
     * 删除数据
     *
     * @param t   待删除数据
     * @param <T> 泛型
     */
    public <T> void delete(T t) {
        reflectPrimaryKey(t);
        mDbManager.mDelete(tableName, primaryKeyPojo.getName().concat("=?"), new String[]{primaryKeyPojo.getValueString()});
    }

    /**
     * 批量删除（逐条删除，避免SQL语句出问题时，都不删除了）
     *
     * @param ts  待删除集合
     * @param <T> 泛型
     */
    public <T> void delete(List<T> ts) {
        for (T t : ts) {
            reflectPrimaryKey(t);
            mDbManager.mDelete(tableName, primaryKeyPojo.getName().concat("=?"), new String[]{primaryKeyPojo.getValueString()});
        }
    }

    /**
     * 清空特定表
     *
     * @param clz 被辛苦表类
     * @param <T> 泛型
     */
    public <T> void clear(Class<T> clz) {
        reflectTableName(clz);
        mDbManager.mDeleteTable(tableName);
    }

    /**
     * 更新数据
     *
     * @param ts  待更新列表
     * @param <T> 泛型
     */
    public <T> void update(List<T> ts) {
        for (T t : ts) {
            update(t);
        }
    }

    /**
     * 插入数据
     *
     * @param t   被插入数据
     * @param <T> 泛型
     * @return 是插入还是更新：true为更新，false为插入
     */
    public <T> boolean insert(T t) {
        reflect(t);
        ContentValues contentValues = new ContentValues();
        DbCreate<T> dbCreate = null;
        if (!mDbManager.isTableExist(tableName)) {
            dbCreate = new DbCreate<>(this, tableName);
        }
        if (null == primaryKeyPojo) {
            throw new IllegalArgumentException("Primary key hasn't been set");
        } else {
            String columnType = primaryKeyPojo.getType();
            if (null != dbCreate) {
                dbCreate.addPrimaryKey(primaryKeyPojo.getName(), columnType);
            }
            switch (columnType) {
                case INT:
                    contentValues.put(primaryKeyPojo.getName(), (Integer) primaryKeyPojo.getValue());
                    break;
                case STRING:
                    contentValues.put(primaryKeyPojo.getName(), (String) primaryKeyPojo.getValue());
                    break;
                case DOUBLE:
                    contentValues.put(primaryKeyPojo.getName(), (Double) primaryKeyPojo.getValue());
                    break;
                case FLOAT:
                    contentValues.put(primaryKeyPojo.getName(), (Float) primaryKeyPojo.getValue());
                    break;
                case LONG:
                    contentValues.put(primaryKeyPojo.getName(), (Long) primaryKeyPojo.getValue());
                    break;
                case SHORT:
                    contentValues.put(primaryKeyPojo.getName(), (Short) primaryKeyPojo.getValue());
                    break;
                case BYTE:
                    contentValues.put(primaryKeyPojo.getName(), (Byte) primaryKeyPojo.getValue());
                    break;
                case BOOLEAN:
                    contentValues.put(primaryKeyPojo.getName(), (String) primaryKeyPojo.getValue());
                    break;
            }
        }
        if (null != columnPojos && columnPojos.size() > 0) {
            for (ColumnPojo columnPojo : columnPojos) {
                String columnType = columnPojo.getType();
                switch (columnType) {
                    case INT:
                        contentValues.put(columnPojo.getName(), (Integer) columnPojo.getValue());
                        break;
                    case STRING:
                        // 避免内部类传入Activity或者Fragment
                        if (columnPojo.getName().contains("this")) {
                            break;
                        }
                        contentValues.put(columnPojo.getName(), (String) columnPojo.getValue());
                        break;
                    case DOUBLE:
                        contentValues.put(columnPojo.getName(), (Double) columnPojo.getValue());
                        break;
                    case FLOAT:
                        contentValues.put(columnPojo.getName(), (Float) columnPojo.getValue());
                        break;
                    case LONG:
                        contentValues.put(columnPojo.getName(), (Long) columnPojo.getValue());
                        break;
                    case SHORT:
                        contentValues.put(columnPojo.getName(), (Short) columnPojo.getValue());
                        break;
                    case BYTE:
                        contentValues.put(columnPojo.getName(), (Byte) columnPojo.getValue());
                        break;
                    case BOOLEAN:
                        contentValues.put(columnPojo.getName(), String.valueOf(columnPojo.getValue()));
                        break;
                }
                if (null != dbCreate) {
                    dbCreate.add(columnPojo.getName(), columnType);
                }
            }
        }
        if (null != dbCreate) {
            dbCreate.create();
        }
        if (!contentValues.containsKey(UPDATE_TIME)) {
            contentValues.put(UPDATE_TIME, System.currentTimeMillis());
        }
        boolean hasBean = new DbQuery<>(this, t.getClass()).hasBean(t);
        if (hasBean) {
            logger.i(getName(), "has this ".concat(t.getClass().getSimpleName()));
            mDbManager.mUpdate(tableName, contentValues, primaryKeyPojo.getName().concat("=?"), new String[]{primaryKeyPojo.getValueString()});
        } else {
            logger.i(getName(), "does not have ".concat(t.getClass().getSimpleName()));
            mDbManager.mInsert(tableName, "", contentValues);
        }
        return hasBean;
    }

    /**
     * 异步接口
     */
    public interface OnTransaction {
        void execute(DbUtils dbUtils);
    }
}
