package com.bsw.dblibrary.dbFilterList;

/**
 * 更新的参数
 */
class BswDbUpdateParam {
    static final String NULL_VALUE = "NULL_VALUE !@#$%^&*()_+ NULL_VALUE";
    private String key;
    private Object value;
    private Object newValue;

    BswDbUpdateParam(String key, Object value, Object newValue) {
        this.key = key;
        this.value = value;
        this.newValue = newValue;
    }

    String getKey() {
        return key;
    }

    Object getValue() {
        return value;
    }

    Object getNewValue() {
        return newValue;
    }

    boolean judge(Object vReflect) {
        if (NULL_VALUE.equals(value)) {
            return true;
        } else {
            if (null == vReflect) {
                return null == value;
            } else {
                return vReflect.equals(value);
            }
        }
    }
}
