package com.bsw.dblibrary.dbFilterList;

import com.bsw.dblibrary.db.PrimaryKey;
import com.bsw.dblibrary.db.Require;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class BswDbReflectUtils<T> {
    private final String KEY = "key";
    private final String VALUE = "value";

    /**
     * 解析后的属性集合
     */
    private List<Field> reflectFields;

    /**
     * List反射解析结果缓存，防止多次反射浪费资源
     */
    private Map<T, Map<String, Object>> reflectResultMap = new HashMap<>();
    /**
     * List反射解析后，主键以及对应的位置，用于根据主键替换Bean
     */
    private Map<Object, Integer> reflectPrimaryKeyMap = new HashMap<>();

    /**
     * 主键字符串
     */
    private String primaryKey;

    /**
     * 反射解析泛型类
     *
     * @param clz 待解析的类
     */
    private void reflectClass(Class clz) {
        reflectFields = new ArrayList<>(Arrays.asList(clz.getDeclaredFields()));
    }

    /**
     * 反射解析列表
     *
     * @param list 待反射解析的列表
     */
    void reflectList(BswDbFilterList<T> list) {
        for (int i = 0; i < list.size(); i++) {
            T t = list.get(i);
            Map<String, Object> result = reflectT(t, i);
            if (null != result)
                reflectResultMap.put(t, result);
        }
    }

    /**
     * 解析泛型属性
     *
     * @param t 待解析泛型
     * @return 解析的属性名与属性值的map
     */
    Map<String, Object> reflectT(T t) {
        return reflectT(t, null);
    }

    /**
     * 解析泛型属性
     *
     * @param t     待解析泛型
     * @param index 被解析的类在列表中的坐标
     * @return 解析的属性名与属性值的map
     */
    Map<String, Object> reflectT(T t, Integer index) {
        if (null == reflectFields) {
            reflectClass(t.getClass());
        }
        Map<String, Object> reflectResult = new HashMap<>();
        for (Field field : reflectFields) {
            field.setAccessible(true);
            String key = null;
            try {
                if (field.isAnnotationPresent(PrimaryKey.class)) {
                    key = field.getAnnotation(PrimaryKey.class).name();
                    if (null != index)
                        reflectPrimaryKeyMap.put(field.get(t), index);
                } else if (field.isAnnotationPresent(Require.class)) {
                    key = field.getAnnotation(Require.class).name();
                }
                if (TextUtils.isEmpty(key)) {
                    key = field.getName();
                }
                if (TextUtils.isEmpty(key)) {
                    continue;
                }
                // 存储参数自身的属性名
                reflectResult.put(key, field.get(t));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }
        return reflectResult;
    }

    /**
     * 解析泛型属性
     *
     * @param t 待解析泛型
     * @return 解析的属性名与属性值的map
     */
    Map<String, Object> getPrimaryKey(T t) {
        if (null == reflectFields) {
            reflectClass(t.getClass());
        }

        for (Field field : reflectFields) {
            field.setAccessible(true);
            String key;
            try {
                if (field.isAnnotationPresent(PrimaryKey.class)) {
                    key = field.getAnnotation(PrimaryKey.class).name();
                    if (TextUtils.isEmpty(key)) {
                        key = field.getName();
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put(KEY, key);
                    map.put(VALUE, field.get(t));
                    return map;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * 获取列表中主键匹配的条目
     *
     * @param ts 待匹配的列表
     * @param t  被检索bean
     * @return 匹配的位置
     */
    Integer getPrimaryKeyMatchT(BswDbFilterList<T> ts, T t) {
        Integer index = null;
        Map<String, Object> primaryKeyMap = getPrimaryKey(t);
        if (primaryKeyMap.size() > 0) {
            String key = (String) primaryKeyMap.get(KEY);
            Object value = primaryKeyMap.get(VALUE);
            if (null == value) {
                return index;
            }
            for (int i = 0; i < ts.size(); i++) {
                Map<String, Object> map = getReflectResult(ts.get(i));
                if (value.equals(map.get(key))) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    Map<String, Object> getReflectResult(T t) {
        if (null == reflectResultMap) {
            return null;
        }
        return reflectResultMap.get(t);
    }
}
