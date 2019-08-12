package com.bsw.dblibrary.dbFilterList;

import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.bsw.dblibrary.dbFilterList.BswDbFilterList.TO_THE_END;

public class BswDbListQuery<T> {

    private final ListFilterAdapter<T> filterAdapter;
    private BswDbFilterList<T> list;
    private BswDbReflectUtils<T> reflectUtils;

    BswDbListQuery(BswDbFilterList<T> list) {
        this(null, list);
    }

    BswDbListQuery(ListFilterAdapter<T> filterAdapter, BswDbFilterList<T> list) {
        this.list = list;
        reflectUtils = list.getReflectUtils();
        this.filterAdapter = filterAdapter;
    }

    /**
     * 包含搜索关键字
     */
    @SuppressWarnings("WeakerAccess")
    public static final int PARAM_TYPE_CONTAINS = 0x01;
    /**
     * 相同
     */
    @SuppressWarnings("WeakerAccess")
    public static final int PARAM_TYPE_EQUALS = 0x02;
    /**
     * 范围内
     */
    @SuppressWarnings("WeakerAccess")
    public static final int PARAM_TYPE_RANGE_IN = 0x04;
    /**
     * 范围外
     */
    @SuppressWarnings("WeakerAccess")
    public static final int PARAM_TYPE_RANGE_OUT = 0x08;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PARAM_TYPE_EQUALS
            , PARAM_TYPE_CONTAINS})
    @interface ParamTypeMatch {
    }

    @SuppressWarnings("unused")
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PARAM_TYPE_RANGE_IN
            , PARAM_TYPE_RANGE_OUT})
    @interface ParamTypeRange {
    }

    /**
     * 排序方式：DESC 倒序；ASC 正序
     */
    public static final String DESC = "desc";
    @SuppressWarnings("WeakerAccess")
    public static final String ASC = "asc";


    @Retention(RetentionPolicy.SOURCE)
    @StringDef({DESC, ASC})
    @interface SortType {
    }


    @SuppressWarnings("WeakerAccess")
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
    private List<BswDbQueryParam> queryList;

    /**
     * 用于排序的Key
     */
    private String sortKey = "";

    /**
     * 添加参数：Short
     *
     * @param key   key
     * @param value value
     * @return 查询类
     */
    public BswDbListQuery<T> putParams(String key, Object value) {
        if (null == queryList) {
            queryList = new ArrayList<>();
        }
        queryList.add(new BswDbQueryParam(PARAM_TYPE_CONTAINS, key, value));
        return this;
    }

    /**
     * 添加参数：Short
     *
     * @param matchType matchType
     * @param key       key
     * @param value     value
     * @return 查询类
     */
    @SuppressWarnings("unused")
    public BswDbListQuery<T> putParams(@ParamTypeMatch int matchType, String key, Object value) {
        if (null == queryList) {
            queryList = new ArrayList<>();
        }
        queryList.add(new BswDbQueryParam(matchType, key, value));
        return this;
    }

    /**
     * 添加参数：Short
     *
     * @param rangeType rangeType
     * @param key       key
     * @param min       范围最小值
     * @param max       范围最大值
     * @return 查询类
     */
    @SuppressWarnings("unused")
    public BswDbListQuery<T> putParams(@ParamTypeRange int rangeType, String key, Object min, Object max) {
        if (null == queryList) {
            queryList = new ArrayList<>();
        }
        queryList.add(new BswDbQueryParam(rangeType, key, min, max));
        return this;
    }

    /**
     * 对获取的结果进行排序
     *
     * @param key      排序标识
     * @param sortType 排序方式，默认正序
     * @return 当前搜索类
     */
    public BswDbListQuery<T> sort(String key, @SortType String sortType) {
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
    public BswDbListQuery<T> setQueryType(@QueryType String queryType) {
        this.queryType = queryType;
        return this;
    }

    /**
     * 截取搜索结果集合（一般用于分页）
     *
     * @param from  从第几个开始截取
     * @param count 截取数量
     * @return 截取结果
     */
    @SuppressWarnings("WeakerAccess")
    public synchronized BswDbFilterList<T> getList(@IntRange(from = 0) int from, @IntRange(from = TO_THE_END) int count) {
        int ignoredCount = 0;
        if (null == list || list.size() == 0) {
            return null;
        }

        // 反射一次解析所有的List，避免多次反射消耗资源
        reflectUtils.reflectList(list);

        BswDbFilterList<T> resultList = new BswDbFilterList<>();

        if (null == queryList || queryList.size() == 0) {       // 搜索条件为空则直接返回排序结果
            if (null == filterAdapter)
                resultList = list;
            else {
                for (T t : list) {
                    if (filterAdapter.filter(t)) {
                        resultList.add(t);
                    }
                }
            }
            return sortJudge(resultList).subList(from, count);
        } else {                                                // 搜索条件不为空则先筛选，再返回排序结果
            switch (queryType) {
                case AND:
                    for (T t : list) {
                        if (andJudge(t)) {
                            if (ignoredCount < from) {
                                ignoredCount++;
                            } else {
                                resultList.add(t);
                            }
                        }
                        if (count != TO_THE_END && count <= resultList.size()) {
                            break;
                        }
                    }
                    break;

                case OR:
                    for (T t : list) {
                        if (orJudge(t)) {
                            if (ignoredCount < from) {
                                ignoredCount++;
                            } else {
                                resultList.add(t);
                            }
                            if (count != TO_THE_END && count <= resultList.size()) {
                                break;
                            }
                        }
                    }
                    break;
            }
            // 添加的时候已经做好了筛选
            return sortJudge(resultList);
        }
    }

    /**
     * 获取满足条件的所有
     *
     * @return 返回符合条件的所有
     */
    public synchronized BswDbFilterList<T> getAll() {
        return getList(0, -1);
    }


    /**
     * 获取满足条件的第一个
     *
     * @return 第一个
     */
    public synchronized T getFirst() {
        if (null == list || list.size() == 0) {
            return null;
        }

        BswDbFilterList<T> sortedList = new BswDbFilterList<>();
        if (TextUtils.isEmpty(sortKey)) {
            reflectUtils.reflectList(sortedList);
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
        Map<String, Object> reflectResult = reflectUtils.getReflectResult(t);
        if (null == reflectResult) {
            reflectResult = reflectUtils.reflectT(t);
        }
        for (BswDbQueryParam p : queryList) {
            Object vReflect = reflectResult.get(p.getKey());
            boolean judge = p.judge(vReflect);
            // 若paramType没有问题，则根据当前判断是筛选条件的与判断，有一个不满足则返回，所以验证judge若为false，则不满足返回
            if (!judge) {
                return false;
            }
        }
        // 用户自定义判断，由于与判断，因此判断结果取与
        if (null != filterAdapter) {
            return filterAdapter.filter(t);
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
        Map<String, Object> reflectResult = reflectUtils.getReflectResult(t);
        if (null == reflectResult) {
            reflectResult = reflectUtils.reflectT(t);
        }
        for (BswDbQueryParam p : queryList) {
            Object vReflect = reflectResult.get(p.getKey());
            boolean judge = p.judge(vReflect);
            // 用户自定义判断，由于或判断，因此判断结果取或
            if (null != filterAdapter) {
                judge = judge || filterAdapter.filter(t);
            }
            // 若paramType没有问题，则根据当前判断是筛选条件的与判断，有一个不满足则返回，所以验证judge若为false，则不满足返回
            if (judge) {
                return true;
            }
        }
        return false;
    }

    /**
     * 排序判断
     *
     * @param list 排序的列表
     * @return 排序后的列表
     */
    private BswDbFilterList<T> sortJudge(BswDbFilterList<T> list) {
        if (TextUtils.isEmpty(sortKey)) {
            return list;
        } else {
            List<Object> sortJudgeList = new ArrayList<>();
            BswDbFilterList<T> sortList = new BswDbFilterList<>();
            for (T t : list) {
                Map<String, Object> reflectResult = reflectUtils.getReflectResult(t);
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

    public interface ListFilterAdapter<T> {
        boolean filter(T t);
    }
}
