package com.bsw.dblibrary.dbFilterList;

/**
 * 更新的参数
 */
class BswDbUpdateParam {
    private String key;
    private Object value;
    private Object newValue;

    BswDbUpdateParam(String key, Object newValue) {
        this.key = key;
        this.newValue = newValue;
    }

    public BswDbUpdateParam(String key, Object value, Object newValue) {
        this.key = key;
        this.value = value;
        this.newValue = newValue;
    }

    String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public Object getNewValue() {
        return newValue;
    }
}
