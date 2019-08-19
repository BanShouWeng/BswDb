package com.bsw.dblibrary.dbFilterList;

import android.content.Context;

import androidx.annotation.IntRange;

import com.bsw.dblibrary.db.DbUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BswDbFilterList<T> extends ArrayList<T> {
    static final int TO_THE_END = -1;

    /**
     * 反射工具
     */
    private BswDbReflectUtils<T> reflectUtils;

    /**
     * 获取反射工具，用于在更新或查询时同步解析结果，因此放到BswFilterList中
     *
     * @return 工具
     */
    BswDbReflectUtils<T> getReflectUtils() {
        if (null == reflectUtils) {
            reflectUtils = new BswDbReflectUtils<>();
        }
        return reflectUtils;
    }

    /**
     * 无参构造方法
     */
    public BswDbFilterList() {
    }

    /**
     * 将List转为当前过滤列表
     *
     * @param list 待转换列表
     */
    public BswDbFilterList(List<T> list) {
        this.addAll(list);
    }

    /**
     * 将List转为当前过滤列表
     *
     * @param list 待转换数组
     */
    public BswDbFilterList(T[] list) {
        this.addAll(new ArrayList<>(Arrays.asList(list)));
    }

    /**
     * 搜索
     *
     * @return 搜索的类
     */
    public BswDbListQuery<T> query() {
        return new BswDbListQuery<>(this);
    }

    /**
     * 搜索
     *
     * @return 搜索的类
     */
    public BswDbListQuery<T> query(BswDbListQuery.ListFilterAdapter<T> filterAdapter) {
        return new BswDbListQuery<>(this, filterAdapter);
    }

    /**
     * 列表中是否包含注解相同的条目
     *
     * @param t 泛型
     * @return 是否有
     */
    public boolean has(T t) {
        return new BswDbListQuery<>(this).has(t);
    }

    /**
     * 更新，需配置参数的更新方式
     *
     * @return 更新的类
     */
    public BswDbListUpdate<T> update() {
        return new BswDbListUpdate<>(this);
    }

    /**
     * 更新，通过适配器用户自行调整
     *
     * @param adapter 更新适配器
     * @return 更新后的结果
     */
    public BswDbFilterList<T> update(BswDbListUpdate.ListUpdateAdapter<T> adapter) {
        return new BswDbListUpdate<>(this).run(adapter);
    }

    /**
     * 搜索
     *
     * @return 搜索的类
     */
    public BswDbFilterList<T> update(T t) {
        update(t, true);
        return this;
    }

    /**
     * 搜索
     *
     * @return 搜索的类
     */
    public BswDbFilterList<T> update(T t, boolean isNullAdd) {
        BswDbListUpdate<T> bswDbListUpdate = new BswDbListUpdate<>(this);
        bswDbListUpdate.run(t, isNullAdd);
        return this;
    }

    /**
     * 插入数据
     *
     * @param t 待插入数据
     * @return 返回当前列表
     */
    public BswDbFilterList<T> insert(T t) {
        add(t);
        return this;
    }

    /**
     * 插入数据
     *
     * @param t 待插入数据
     * @return 返回当前列表
     */
    public BswDbFilterList<T> insert(int position, T t) {
        add(position, t);
        return this;
    }

    /**
     * 截取集合（一般用于分页）
     *
     * @param from  从第几个开始截取
     * @param count 截取数量
     * @return 截取结果
     */
    public BswDbFilterList<T> subList(@IntRange(from = 0) int from, @IntRange(from = TO_THE_END) int count) {
        if (0 == from && -1 == count)
            return this;
        else {
            int listSize = size();
            if (listSize < from) {          // 若列表数量小于起始搜索条目角标，则返回空列表
                return new BswDbFilterList<>();
            } else {                        // 若里列表数量大于起始搜索条目角标，则返回符合条件列表
                int end = (from + count) < listSize ? (from + count) : listSize;  // 若目标获取的最后角标大于总数量，则以列表总数量作为判定条件，避免数组越界
                BswDbFilterList<T> subList = new BswDbFilterList<>();
                for (int i = from; i < end; i++) {
                    subList.add(get(i));
                }
                return subList;
            }
        }
    }

    /**
     * 同步数据库
     *
     * @param mContext 上下文
     * @param tClass   泛型的类，用于当列表为空时，清空对应表
     */
    public void synchronizedDb(Context mContext, Class<T> tClass) {
        DbUtils dbUtils = new DbUtils(mContext);
        if (size() > 0) {
            dbUtils.update(this);
            BswDbFilterList<T> dbList = dbUtils.where(tClass).getAll();
            BswDbFilterList<T> deleteList = new BswDbFilterList<>();
            for (T t : dbList) {
                if (!has(t)) {
                    deleteList.add(t);
                }
            }
            dbUtils.delete(deleteList);
        } else {
            dbUtils.clear(tClass);
        }
    }
}