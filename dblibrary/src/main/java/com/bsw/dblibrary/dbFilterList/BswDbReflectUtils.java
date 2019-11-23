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
    private final String PRIMARY_KEY_PARAM = "primary_key_param";

    /**
     * 解析后的属性集合
     */
    private List<Field> reflectFields;

    /**
     * List反射解析结果缓存，防止多次反射浪费资源
     */
    private Map<T, Map<String, BswDbReflectParam>> reflectResultMap = new HashMap<>();
    /**
     * List反射解析后，主键以及对应的位置，用于根据主键替换Bean
     */
    private Map<Object, Integer> reflectPrimaryKeyMap = new HashMap<>();
    /**
     * 当主键变动时，调整缓存列表
     */
    private Map<Object, T> primaryKeyTMap = new HashMap<>();

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
            Map<String, BswDbReflectParam> result = reflectT(t, i);
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
    Map<String, BswDbReflectParam> reflectT(T t) {
        return reflectT(t, null);
    }

    /**
     * 解析泛型属性
     *
     * @param t     待解析泛型
     * @param index 被解析的类在列表中的坐标
     * @return 解析的属性名与属性值的map
     */
    private synchronized Map<String, BswDbReflectParam> reflectT(T t, Integer index) {
        if (null == reflectFields) {
            reflectClass(t.getClass());
        }
        Map<String, BswDbReflectParam> reflectResult = new HashMap<>();
        for (Field field : reflectFields) {
            if ("serialVersionUID".equals(field.getName())          // Serializable序列化Bean时系统自动添加参数，这里不做解析
                    || "$change".equals(field.getName())) {         // android studio2的Instant Run添加参数，这里不做解析
                continue;
            }

            field.setAccessible(true);
            String key = null;
            try {
                if (field.isAnnotationPresent(PrimaryKey.class)) {
                    key = field.getAnnotation(PrimaryKey.class).name();
                    if (null != index) {
                        reflectPrimaryKeyMap.put(field.get(t), index);
                        primaryKeyTMap.put(field.get(t), t);
                    }
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
                reflectResult.put(key, new BswDbReflectParam(key, field.get(t), field));
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
    private synchronized Map<String, BswDbReflectParam> reflectPrimaryKey(T t) {
        if (null == reflectFields) {
            reflectClass(t.getClass());
        }

        for (Field field : reflectFields) {
            if ("serialVersionUID".equals(field.getName())          // Serializable序列化Bean时系统自动添加参数，这里不做解析
                    || "$change".equals(field.getName())) {         // android studio2的Instant Run添加参数，这里不做解析
                continue;
            }

            field.setAccessible(true);
            String key;
            try {
                if (field.isAnnotationPresent(PrimaryKey.class)) {
                    key = field.getAnnotation(PrimaryKey.class).name();
                    if (TextUtils.isEmpty(key)) {
                        key = field.getName();
                    }
                    Map<String, BswDbReflectParam> map = new HashMap<>();
                    map.put(PRIMARY_KEY_PARAM, new BswDbReflectParam(key, field.get(t), field));
                    return map;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 解析泛型属性
     *
     * @param t 待解析泛型
     * @return 主键对应的对象
     */
    private synchronized Object getPrimaryKey(T t) {
        if (null == reflectFields) {
            reflectClass(t.getClass());
        }

        for (Field field : reflectFields) {
            field.setAccessible(true);
            try {
                if (field.isAnnotationPresent(PrimaryKey.class)) {
                    return field.get(t);
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
    synchronized Integer getPrimaryKeyMatchT(BswDbFilterList<T> ts, T t) {
        Integer index = null;
        Map<String, BswDbReflectParam> primaryKeyMap = reflectPrimaryKey(t);
        if (null != primaryKeyMap && primaryKeyMap.size() > 0) {
            try {
                BswDbReflectParam param = primaryKeyMap.get(PRIMARY_KEY_PARAM);
                if (null == param) {
                    return null;
                }
                String key = param.getKey();
                Object value = param.getValue();
                if (null == value) {
                    return null;
                }
                for (int i = 0; i < ts.size(); i++) {
                    Map<String, BswDbReflectParam> map = getReflectResult(ts.get(i));
                    BswDbReflectParam reflectParam = map.get(key);
                    if (null == reflectParam) {
                        continue;
                    }
                    if (value.equals(reflectParam.getValue())) {
                        index = i;
                        break;
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
                return null;
            }
        }
        return index;
    }

    /**
     * 获取T的解析结果
     *
     * @param t 泛型
     * @return 解析的结果
     */
    Map<String, BswDbReflectParam> getReflectResult(T t) {
        if (null == reflectResultMap) {
            Map<String, BswDbReflectParam> paramMap = reflectT(t);
            reflectResultMap.put(t, paramMap);
            return paramMap;
        }
        Map<String, BswDbReflectParam> paramMap = reflectResultMap.get(t);
        if (null == paramMap) {
            paramMap = reflectT(t);
            reflectResultMap.put(t, paramMap);
        }
        return paramMap;
    }

    /**
     * 判断集合中是否包含待检测对应主键对应对象
     *
     * @param list 待检测集合
     * @param t    被验证对象
     * @return 是否包含
     */
    boolean has(BswDbFilterList<T> list, T t) {
        for (T tItem : list) {
            try {
                if (getPrimaryKey(t).equals(getPrimaryKey(tItem))) {
                    return true;
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
