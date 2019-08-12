package com.bsw.dblibrary.dbFilterList;

import androidx.annotation.IntRange;

import java.util.ArrayList;
import java.util.List;

public class BswDbFilterList<T> extends ArrayList<T> {
    public static final int TO_THE_END = -1;

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
    public BswDbListQuery<T> query(BswDbListQuery.ListFilterAdapter filterAdapter) {
        return new BswDbListQuery<>(filterAdapter, this);
    }

    /**
     * 搜索
     *
     * @return 搜索的类
     */
    public BswDbListUpdate<T> update() {
        return new BswDbListUpdate<>(this);
    }

    /**
     * 搜索
     *
     * @return 搜索的类
     */
    public BswDbFilterList<T> update(T t) {
        BswDbListUpdate<T> bswDbListUpdate = new BswDbListUpdate<>(this);
        bswDbListUpdate.update(t);
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
}
