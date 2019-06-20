package com.bsw.dblibrary.filterList;

import android.support.annotation.StringDef;
import android.text.TextUtils;

import com.bsw.dblibrary.db.PrimaryKey;
import com.bsw.dblibrary.db.Require;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BswListQuery<T> {
    private BswFilterList<T> list;

    BswListQuery(BswFilterList<T> list) {
        this.list = list;
    }

    /**
     * 排序方式：DESC 倒序；ASC 正序
     */
    public static final String DESC = "desc";
    public static final String ASC = "asc";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({DESC, ASC})
    @interface SortType {
    }


    public static final String AND = "and";
    public static final String OR = "or";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({AND, OR})
    @interface QueryType {
    }

    /**
     * 搜索类型，默认与
     */
    private String queryType = AND;
    /**
     * 排序类型，默认正序
     */
    private String sortType = ASC;

    /**
     * 筛选条件列表
     */
    private List<BswParam> queryList;

    /**
     * 用于排序的Key
     */
    private String sortKey = "";

    /**
     * 解析后的属性集合
     */
    private List<Field> reflectFields;

    /**
     * List反射解析结果缓存，防止多次反射浪费资源
     */
    private Map<T, Map<String, Object>> reflectResultMap;

    /**
     * 添加参数：Short
     *
     * @param key   key
     * @param value value
     * @return 查询类
     */
    public BswListQuery<T> putParams(String key, Object value) {
        if (null == queryList) {
            queryList = new ArrayList<>();
        }
        queryList.add(new BswParam(key, value));
        return this;
    }

    /**
     * 对获取的结果进行排序
     *
     * @param key      排序标识
     * @param sortType 排序方式，默认正序
     * @return 当前搜索类
     */
    public BswListQuery<T> sort(String key, @SortType String sortType) {
        this.sortKey = key;
        this.sortType = sortType;
        return this;
    }

    /**
     * 设置搜索类型
     *
     * @param queryType 搜索的类型，默认AND，所有搜索条件都满足，可设置OR，满足一条即可
     * @return 当前搜索类
     */
    public BswListQuery<T> setQueryType(@QueryType String queryType) {
        this.queryType = queryType;
        return this;
    }

    /**
     * 获取满足条件的所有列表
     *
     * @return 返回符合条件的所有列表
     */
    public BswFilterList<T> getAll() {
        if (null == list || list.size() == 0) {
            return null;
        }

        // 反射解析Class
        reflectClass(list.get(0).getClass());
        // 反射一次解析所有的List，避免多次反射消耗资源
        reflectList(list);

        if (null == queryList || queryList.size() == 0) {       // 搜索条件为空则直接返回排序结果
            return sortJudge(list);
        } else {                                                // 搜索条件不为空则先筛选，再返回排序结果
            BswFilterList<T> resultList = new BswFilterList<>();

            switch (queryType) {
                case AND:
                    for (T t : list) {
                        if (andJudge(t))
                            resultList.add(t);
                    }
                    break;

                case OR:
                    for (T t : list) {
                        if (orJudge(t))
                            resultList.add(t);
                    }
                    break;
            }

            return resultList;
        }
    }


    public T getFirst() {
        if (null == list || list.size() == 0) {
            return null;
        }

        reflectClass(list.get(0).getClass());

        BswFilterList<T> sortedList = new BswFilterList<>();
        if (TextUtils.isEmpty(sortKey)) {
            reflectList(sortedList);
            sortedList = sortJudge(list);
        } else
            sortedList = list;

        switch (queryType) {
            case AND:
                for (T t : sortedList) {
                    if (andJudge(t))
                        return t;
                }
                break;

            case OR:
                for (T t : sortedList) {
                    if (orJudge(t))
                        return t;
                }
                break;
        }
        return null;
    }

    /**
     * 与判断
     *
     * @param t 待判断类
     * @return 是否满足与条件
     */
    private boolean andJudge(T t) {
        Map<String, Object> reflectResult = reflectResultMap.get(t);
        if (null == reflectResult) {
            reflectResult = reflectT(t);
        }
        for (BswParam p : queryList) {
            Object value = reflectResult.get(p.getKey());
            if (value == null || !value.equals(p.getValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 或判断
     *
     * @param t 待判断类
     * @return 是否满足或条件
     */
    private boolean orJudge(T t) {
        Map<String, Object> reflectResult = reflectResultMap.get(t);
        if (null == reflectResult) {
            reflectResult = reflectT(t);
        }
        for (BswParam p : queryList) {
            Object value = reflectResult.get(p.getKey());
            if (value != null && value.equals(p.getValue())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析泛型属性
     *
     * @param t 待解析泛型
     * @return 解析的属性名与属性值的map
     */
    private Map<String, Object> reflectT(T t) {
        Map<String, Object> reflectResult = new HashMap<>();
        for (Field field : reflectFields) {
            field.setAccessible(true);
            String key = null;
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                key = field.getAnnotation(PrimaryKey.class).name();
            } else if (field.isAnnotationPresent(Require.class)) {
                key = field.getAnnotation(Require.class).name();
            } else {
                key = field.getName();
            }
            try {
                if (TextUtils.isEmpty(key)) {
                    continue;
                }
                reflectResult.put(key, field.get(t));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return reflectResult;
    }

    /**
     * 排序判断
     *
     * @param list 排序的列表
     * @return 排序后的列表
     */
    private BswFilterList<T> sortJudge(BswFilterList<T> list) {
        if (TextUtils.isEmpty(sortKey)) {
            return list;
        } else {
            List<Object> sortJudgeList = new ArrayList<>();
            BswFilterList<T> sortList = new BswFilterList<>();
            for (T t : list) {
                Map<String, Object> reflectResult = reflectResultMap.get(t);
                if (null == reflectResult || reflectResult.size() == 0) {
                    continue;
                }
                Object item = reflectResult.get(sortKey);
                if (null == item) {
                    sortList.add(sortList.size(), t);
                } else {
                    // 由于无法对泛型直接排序，所以将待筛选项取出排序
                    sortJudgeList.add(item);
                    // 先正序排列
                    Collections.sort(sortJudgeList, new Comparator<Object>() {
                        @Override
                        public int compare(Object o1, Object o2) {
                            if (o1 instanceof Integer) {
                                return (Integer) o1 < (Integer) o2 ? -1 : 1;
                            } else if (o1 instanceof Long) {
                                return (Long) o1 < (Long) o2 ? -1 : 1;
                            } else if (o1 instanceof Short) {
                                return (Short) o1 < (Short) o2 ? -1 : 1;
                            } else if (o1 instanceof Byte) {
                                return (Byte) o1 < (Byte) o2 ? -1 : 1;
                            } else if (o1 instanceof Boolean) {
                                return (Boolean) o1 ? -1 : 1;
                            } else {
                                return o1.toString().compareTo(o2.toString());
                            }
                        }
                    });

                    sortList.add(sortJudgeList.indexOf(item), t);
                }
            }

            if (sortType.equals(DESC)) {
                Collections.reverse(sortList);
            }
            return sortList;
        }
    }

    /**
     * 反射解析列表
     *
     * @param list 待反射解析的列表
     */
    private void reflectList(BswFilterList<T> list) {
        reflectResultMap = new HashMap<>();
        for (T t : list) {
            reflectResultMap.put(t, reflectT(t));
        }
    }

    /**
     * 反射解析类
     *
     * @param aClass 待反射解析的类
     */
    private void reflectClass(Class<?> aClass) {
        reflectFields = new ArrayList<>(Arrays.asList(aClass.getDeclaredFields()));
    }
}
