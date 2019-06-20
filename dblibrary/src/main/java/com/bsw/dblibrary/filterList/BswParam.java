package com.bsw.dblibrary.filterList;

class BswParam {
    private String key;
    private Object value;

    BswParam(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    String getKey() {
        return key;
    }

    Object getValue() {
        return value;
    }
}
