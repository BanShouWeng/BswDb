package com.bsw.dblibrary.dbFilterList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BswDbListUpdate<T> {
    private BswDbFilterList<T> list;
    private BswDbReflectUtils<T> reflectUtils;
    /**
     * 筛选条件列表
     */
    private List<BswDbUpdateParam> updateList = new ArrayList<>();

    BswDbListUpdate(BswDbFilterList<T> list) {
        this.list = list;
        reflectUtils = list.getReflectUtils();
        reflectUtils.reflectList(list);
    }

    /**
     * 替换对应项
     *
     * @param t         最新数据项
     * @param isNullAdd 如果列表中无对应项，是否需要添加
     */
    public synchronized BswDbFilterList<T> run(T t, boolean isNullAdd) {
        Integer index = reflectUtils.getPrimaryKeyMatchT(list, t);
        if (null != index) {
            list.remove((int) index);
            list.add(index, t);
        } else {
            if (isNullAdd) {
                list.add(t);
            }
        }
        return list;
    }

    /**
     * 设置更新值
     *
     * @param key      用于查找被更新项的属性名
     * @param newValue 新的值
     */
    public BswDbListUpdate<T> putParams(String key, Object newValue) {
        putParams(key, BswDbUpdateParam.NULL_VALUE, newValue);
        return this;
    }

    /**
     * 设置更新值
     *
     * @param key
     * @param originalValue
     * @param newValue
     */
    public BswDbListUpdate<T> putParams(String key, Object originalValue, Object newValue) {
        updateList.add(new BswDbUpdateParam(key, originalValue, newValue));
        return this;
    }

    public synchronized BswDbFilterList<T> run() {
        for (T t : list) {
            Map<String, BswDbReflectParam> reflectResult = reflectUtils.getReflectResult(t);
            if (null == reflectResult) {
                reflectResult = reflectUtils.reflectT(t);
            }
            for (BswDbUpdateParam p : updateList) {
                BswDbReflectParam param = reflectResult.get(p.getKey());
                if (null == param) {
                    continue;
                }
                if (p.judge(param.getValue())) {
                    try {
                        param.getField().set(t, p.getNewValue());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return list;
    }

    synchronized BswDbFilterList<T> run(ListUpdateAdapter<T> adapter) {
        BswDbFilterList<T> resultList = new BswDbFilterList<>();
        for (int i = 0; i < list.size(); i++) {
            if (null != adapter) {
                T aT = list.get(i);
                T bT = adapter.update(aT);
                resultList.add(null == bT ? aT : bT);
            }
        }
        return resultList;
    }

    public interface ListUpdateAdapter<T> {
        T update(T item);
    }
}
