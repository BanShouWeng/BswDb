package com.bsw.dblibrary.dbFilterList;

/**
 * 搜索的参数
 */
class BswDbQueryParam {
    /**
     * 搜索类型
     * {@link BswDbListQuery#PARAM_TYPE_CONTAINS}是否包含{@link BswDbQueryParam#value}判断
     * {@link BswDbListQuery#PARAM_TYPE_EQUALS}是否与{@link BswDbQueryParam#value}相等判断
     * {@link BswDbListQuery#PARAM_TYPE_RANGE_IN}是否在最小值{@link BswDbQueryParam#min}与最大值{@link BswDbQueryParam#max}范围内，
     * {@link BswDbListQuery#PARAM_TYPE_RANGE_IN}是否在最小值{@link BswDbQueryParam#min}与最大值{@link BswDbQueryParam#max}范围外，
     */
    private final int paramType;
    private String key;
    private Object value;
    private Object min;
    private Object max;

    /**
     * 单参匹配
     *
     * @param type  {@link BswDbQueryParam#paramType}匹配类型，包含或者相同
     * @param key   {@link BswDbQueryParam#key}Bean中对应字段名
     * @param value {@link BswDbQueryParam#value}Bean中对应字段参数
     */
    BswDbQueryParam(@BswDbListQuery.ParamTypeMatch int type, String key, Object value) {
        paramType = type;
        this.key = key;
        this.value = value;
    }

    /**
     * 范围匹配
     *
     * @param type {@link BswDbQueryParam#paramType}匹配类型，范围内或者范围外
     * @param key  {@link BswDbQueryParam#key}Bean中对应字段名
     * @param min  {@link BswDbQueryParam#min}范围最小值  min大于max会崩溃
     * @param max  {@link BswDbQueryParam#max}范围最大值
     */
    @SuppressWarnings("unused")
    BswDbQueryParam(@BswDbListQuery.ParamTypeRange int type, String key, Object min, Object max) {
        if (null == min || null == max) {
            throw new IllegalArgumentException("Min num or max num can't be null");
        }
        paramType = type;
        this.key = key;
        this.min = min;
        this.max = max;
    }

    String getKey() {
        return key;
    }

    private Object getValue() {
        return value;
    }

    private int getParamType() {
        return paramType;
    }

    private Object getMin() {
        return min;
    }

    private Object getMax() {
        return max;
    }

    /**
     * 当前搜索类型是否可用，PARAM_TYPE_CONTAINS 为0001； PARAM_TYPE_EQUALS 为0010； PARAM_TYPE_RANGE_IN 为0100； PARAM_TYPE_RANGE_OUT 为1000
     * 因此paramType与1111按位取与，若不为0则表示在这个范围内
     *
     * @return paramType是否可用
     */
    private boolean isParamTypeNotAvailable() {
        return (paramType & 0x0f) == 0;
    }

    /**
     * 判断反射获取数值是否符合筛选条件
     *
     * @param vReflect 反射获取的待判断参数
     * @return 是否符合条件
     */
    boolean judge(Object vReflect) {
        // 类型不可用则判断结果为不满足
        if (isParamTypeNotAvailable()) {
            return false;
        }
        switch (getParamType()) {
            case BswDbListQuery.PARAM_TYPE_CONTAINS:
                Object vContains = getValue();
                if (null == vContains || null == vReflect) {
                    return null == vContains && null == vReflect;
                } else if (vReflect instanceof String && vContains instanceof String) {
                    return String.valueOf(vReflect).contains(String.valueOf(vContains));
                }

            case BswDbListQuery.PARAM_TYPE_EQUALS:
                Object vEquals = getValue();
                if (null == vReflect) {
                    return null == vEquals;
                } else {
                    return vReflect.equals(vEquals);
                }

            case BswDbListQuery.PARAM_TYPE_RANGE_IN:
                Boolean isInRange = isInRange(vReflect, getMin(), getMax());
                if (null == isInRange) {
                    return false;
                } else {
                    return isInRange;
                }

            case BswDbListQuery.PARAM_TYPE_RANGE_OUT:
                Boolean isNotOutRangee = isInRange(vReflect, getMin(), getMax());
                if (null == isNotOutRangee) {
                    return false;
                } else {
                    return !isNotOutRangee;
                }

            default:
                return false;
        }
    }

    /**
     * 判断待判断数值（只有数值能比大小）vReflect是否在vMin ~ vMax范围内
     *
     * @param vReflect 待比较数值
     * @param vMin     最小值
     * @param vMax     最大值
     * @return 是否满足条件
     */
    private Boolean isInRange(Object vReflect, Object vMin, Object vMax) {
        try {
            Double reflectNum = Double.valueOf(vReflect.toString());
            Double minNum = Double.valueOf(vMin.toString());
            Double maxNum = Double.valueOf(vMax.toString());

            if (minNum > maxNum) {
                throw new IllegalArgumentException("The maximum must be greater than the minimum.");
            }

            return minNum <= reflectNum && reflectNum <= maxNum;
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }
}
