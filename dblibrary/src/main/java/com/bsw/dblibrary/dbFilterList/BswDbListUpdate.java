package com.bsw.dblibrary.dbFilterList;

class BswDbListUpdate<T> {
    private BswDbFilterList<T> list;
    private BswDbReflectUtils<T> reflectUtils;

    BswDbListUpdate(BswDbFilterList<T> list) {
        this.list = list;
        reflectUtils = list.getReflectUtils();
        reflectUtils.reflectList(list);
    }

    void update(T t) {
        update(t, true);
    }

    void update(T t, boolean isNullAdd) {
        Integer index = reflectUtils.getPrimaryKeyMatchT(list, t);
        if (null != index) {
            list.remove((int) index);
            list.add(t);
        } else {
            if (isNullAdd) {
                list.add(t);
            }
        }
    }


}
