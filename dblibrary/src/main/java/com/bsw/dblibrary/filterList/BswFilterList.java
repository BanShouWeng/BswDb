package com.bsw.dblibrary.filterList;

import java.util.ArrayList;
import java.util.List;

public class BswFilterList<T> extends ArrayList<T> {

    /**
     * 无参构造方法
     */
    public BswFilterList() {

    }

    /**
     * 将List转为当前过滤列表
     *
     * @param list 待转换列表
     */
    public BswFilterList(List<T> list) {
        this.addAll(list);
    }

    public BswListQuery<T> query() {
        return new BswListQuery<>(this);
    }
}
