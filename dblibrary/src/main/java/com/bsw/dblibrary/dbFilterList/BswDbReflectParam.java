package com.bsw.dblibrary.dbFilterList;

import java.lang.reflect.Field;

/**
 * 搜索的参数
 */
class BswDbReflectParam {
    private String key;
    private Object value;
    private Field field;

    BswDbReflectParam(String key, Object value, Field field) {
        this.key = key;
        this.value = value;
        this.field = field;
    }

    String getKey() {
        return key;
    }

    Object getValue() {
        return value;
    }

    Field getField() {
        return field;
    }
}
