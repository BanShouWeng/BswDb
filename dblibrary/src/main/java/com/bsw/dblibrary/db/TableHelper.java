package com.bsw.dblibrary.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bsw.dblibrary.Logger;

/**
 * 数据库创建
 *
 * @author bsw
 */
public class TableHelper extends SQLiteOpenHelper {
    private final String TAG = "MultiTableHelper";
    private String[] tableNames;
    private String[] sqls;

    private Logger logger = new Logger();

    /**
     * 初始化构造函数
     *
     * @param context
     * @param name    数据库名
     * @param factory 游标工厂（基本不用）
     * @param version 版本号
     */
    public TableHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /**
     * 初始化构造函数
     *
     * @param context
     * @param dbName     数据库名
     * @param version    版本号
     * @param tableNames 表名
     * @param sqls       SQL语句
     */
    public TableHelper(Context context, String dbName, int version, String[] tableNames, String[] sqls) {
        this(context, dbName, null, version);
        this.tableNames = tableNames;
        this.sqls = sqls;
    }

    // 当调用SQLiteDatabase中的getWritableDatabase()函数的时候会检测表是否存在，如果不存在onCreate将被调用创建表,否则将不会在被调用。
    @Override
    public void onCreate(SQLiteDatabase db) {
        if (db != null) {
            for (int i = 0; i < tableNames.length; i++) {
                logger.d(TAG, "tableName =" + tableNames[i]);
                logger.d(TAG, "sql=" + sqls[i]);
                db.execSQL("create table if not exists " + tableNames[i] + sqls[i]);
            }
        }
    }

    public void createNewTable(String[] tableNames, String[] sqls) {
        SQLiteDatabase db = getWritableDatabase();
        for (int i = 0; i < tableNames.length; i++) {
            logger.d(TAG, "tableName =" + tableNames[i]);
            logger.d(TAG, "sql=" + sqls[i]);
            db.execSQL("create table if not exists " + tableNames[i] + sqls[i]);
        }
    }

    // 版本更新
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        logger.d(TAG, "oldVersion=" + oldVersion);
        logger.d(TAG, "newVersion=" + newVersion);
        if (db != null) {
            // 如果表存在就删除
            for (int i = 0; i < tableNames.length; i++) {
                db.execSQL("drop table if exists " + tableNames[i]);
            }
            // 重新初始化
            onCreate(db);
        }
    }
}
