package com.bsw.dblibrary.db;

import android.database.Cursor;
import android.text.TextUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.bsw.dblibrary.CommonUtils;
import com.bsw.dblibrary.Logger;
import com.bsw.dblibrary.dbFilterList.BswDbFilterList;
import com.google.gson.Gson;

/**
 * 数据库查询类
 *
 * @author bsw
 */
public class DbQuery<T> extends DbBase {
    /**
     * 数据库工具类
     */
    private DbUtils dbUtils;

    /**
     * 目标类
     */
    private Class<T> clz;

    /**
     * 搜索参数
     */
    private List<Param> queryList;

    private String sortString = null;
    private String queryType = AND;

    /**
     * 分页单页显示数量
     */
    private int pageSize = 0;
    /**
     * 分页页码
     */
    private int pageIndex = 0;

    private Logger logger = new Logger();

    /**
     * @param dbUtils 数据库工具类
     * @param clz     待查询类
     */
    DbQuery(DbUtils dbUtils, Class<T> clz) {
        this.dbUtils = dbUtils;
        this.clz = clz;
    }

    /**
     * 初始化查询map
     */
    private void initQueryMap() {
        if (null == queryList) {
            queryList = new ArrayList<>();
        }
    }

    public DbQuery<T> setQueryType(@QueryType String queryType) {
        this.queryType = queryType;
        return this;
    }

    /**
     * 添加参数：String
     *
     * @param key   key
     * @param value value
     * @return 查询类
     */
    public DbQuery<T> putParams(String key, Object value) {
        initQueryMap();
        queryList.add(new Param(key, value));
        return this;
    }

    /**
     * 分页查询数据
     *
     * @param pageIndex 页码
     * @param pageSize  单页条数
     * @return 查询类
     */
    public DbQuery<T> forPaging(int pageIndex, int pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        return this;
    }

    /**
     * 排序
     *
     * @param key      排序依据
     * @param sortType 排序方式
     * @return 查询类
     */
    public DbQuery<T> sort(String key, @SortType String sortType) {
        sortString = key.concat(sortType);
        return this;
    }

    /**
     * 查询数据库中是否存在对应数据
     *
     * @param t   待验证Bean
     * @param <T> 泛型
     * @return 是否存在
     */
    @SuppressWarnings("TypeParameterHidesVisibleType")
    <T> boolean hasBean(T t) {
        reflectPrimaryKey(t);
        try {
            Cursor cursor = dbUtils.mDbManager.mQuery(tableName, null
                    , primaryKeyPojo.getName().concat(" like ?")
                    , new String[]{primaryKeyPojo.getValueString()}
                    , null, null, dbUtils.UPDATE_TIME.concat(DESC));
            int count = cursor.getCount();
            if (!cursor.isClosed()) {
                cursor.close();
            }
            return count != 0;
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * 获取数据库中的第一项
     *
     * @return 第一项Bean
     */
    public T getFirst() {
        Map<String, String[]> tableMap = getDB(dbUtils.mContext);
        if (null == tableMap) {
            return null;
        }
        reflect(clz);
        Cursor cursor = getCursor();
        if (null == cursor) {
            return null;
        }
        while (cursor.moveToNext()) {
            try {
                Constructor c0 = clz.getDeclaredConstructor();
                c0.setAccessible(true);
                Object o = c0.newInstance();
                insertValue(o, cursor);
                if (!cursor.isClosed()) {
                    cursor.close();
                }
                //noinspection unchecked
                return (T) o;
            } catch (InstantiationException e) {
                logger.e(getName(), e);
                return null;
            } catch (InvocationTargetException e) {
                logger.e(getName(), e);
                return null;
            } catch (NoSuchMethodException e) {
                logger.e(getName(), e);
                return null;
            } catch (IllegalAccessException e) {
                logger.e(getName(), e);
                return null;
            }
        }
        return null;
    }

    /**
     * 获取满足搜索条件的全部Bean列表
     *
     * @return Bean列表
     */
    public BswDbFilterList<T> getAll() {
        Map<String, String[]> tableMap = getDB(dbUtils.mContext);
        if (null == tableMap) {
            return null;
        }
        reflect(clz);
        Cursor cursor = getCursor();
        if (null == cursor || cursor.getCount() == 0) {
            return null;
        }
        BswDbFilterList<T> list = new BswDbFilterList<>();
        while (cursor.moveToNext()) {
            try {
                Constructor c0 = clz.getDeclaredConstructor();
                c0.setAccessible(true);
                Object o = c0.newInstance();
                insertValue(o, cursor);
                //noinspection unchecked
                list.add((T) o);
            } catch (InstantiationException e) {
                logger.e(getName(), e);
            } catch (InvocationTargetException e) {
                logger.e(getName(), e);
            } catch (NoSuchMethodException e) {
                logger.e(getName(), e);
            } catch (IllegalAccessException e) {
                logger.e(getName(), e);
            }
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return list;
    }

    /**
     * 获取数据库游标
     *
     * @return 满足条件的数据库游标
     */
    private Cursor getCursor() {
        Map<String, Object> format = formatSelection();
        String selection = (String) format.get("selection");
        String[] selectionArgs = (String[]) format.get("selectionArgs");
        //noinspection unchecked
        List<String> selectionArgList = (List<String>) format.get("selectionArgList");

        if (0 == (pageIndex + pageSize)) {
            return dbUtils.mDbManager.mQuery(tableName
                    , null
                    , TextUtils.isEmpty(selection) ? null : (selection.substring(0, selection.length() - (AND.equals(queryType) ? 5 : 4)))
                    , null == selectionArgs ? null : selectionArgList.toArray(selectionArgs)
                    , null
                    , null
                    , TextUtils.isEmpty(sortString) ? dbUtils.UPDATE_TIME.concat(DESC) : sortString);
        } else {
            int offset = (pageIndex - 1) * pageSize;
            return dbUtils.mDbManager.mQuery(tableName
                    , null
                    , TextUtils.isEmpty(selection) ? null : (selection.substring(0, selection.length() - (AND.equals(queryType) ? 5 : 4)))
                    , null == selectionArgs ? null : selectionArgList.toArray(selectionArgs)
                    , null
                    , null
                    , TextUtils.isEmpty(sortString) ? dbUtils.UPDATE_TIME.concat(DESC) : sortString
                    , String.format(Locale.getDefault(), "%d,%d", offset, pageSize));
        }
    }

    private Map<String, Object> formatSelection() {
        StringBuilder selectionBuffer = new StringBuilder();
        List<String> selectionArgList = new ArrayList<>();

        String selection = null;
        String[] selectionArgs = null;
        if (CommonUtils.judgeListNull(queryList) != 0) {
            for (Param param : queryList) {
                selectionBuffer.append(param.key).append("=?".concat(queryType));
                selectionArgList.add(param.getValue());
            }
            selection = selectionBuffer.toString();
            selectionArgs = new String[selectionArgList.size()];
            logger.i(selection);
            logger.i(selectionArgList.toString());
        }
        Map<String, Object> format = new HashMap<>();
        format.put("selection", selection);
        format.put("selectionArgs", selectionArgs);
        format.put("selectionArgList", selectionArgList);
        return format;
    }

    /**
     * 将数据库中的数据插入到Bean对象中
     *
     * @param o       Bean对象
     * @param mCursor 数据库结果游标
     * @throws IllegalAccessException 转换异常
     */
    private void insertValue(Object o, Cursor mCursor) throws IllegalAccessException {
        for (String key : columnMap.keySet()) {
            ColumnPojo pojo = columnMap.get(key);
            if (null == pojo){
                return;
            }
            switch (pojo.getType()) {
                case INT:
                    pojo.getField().set(o, mCursor.getInt(mCursor.getColumnIndex(key)));
                    break;
                case STRING:
                    pojo.getField().set(o, mCursor.getString(mCursor.getColumnIndex(key)));
                    break;
                case DOUBLE:
                    pojo.getField().set(o, mCursor.getDouble(mCursor.getColumnIndex(key)));
                    break;
                case FLOAT:
                    pojo.getField().set(o, mCursor.getFloat(mCursor.getColumnIndex(key)));
                    break;
                case LONG:
                    pojo.getField().set(o, mCursor.getLong(mCursor.getColumnIndex(key)));
                    break;
                case SHORT:
                    pojo.getField().set(o, mCursor.getShort(mCursor.getColumnIndex(key)));
                    break;
                case BYTE:
                    pojo.getField().set(o, Byte.valueOf(mCursor.getString(mCursor.getColumnIndex(key))));
                    break;
                case BOOLEAN:
                    pojo.getField().set(o, Boolean.valueOf(mCursor.getString(mCursor.getColumnIndex(key))));
                    break;
                default:
                    break;
            }
        }
    }

    class Param {
        String key;
        Object value;

        Param(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        String getValue() {
            return new Gson().toJson(value);
        }
    }
}
