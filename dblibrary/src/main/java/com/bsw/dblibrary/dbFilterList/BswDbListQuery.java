package com.bsw.dblibrary.dbFilterList;

import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.StringDef;

import com.bsw.dblibrary.db.DbQuery;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.bsw.dblibrary.dbFilterList.BswDbFilterList.TO_THE_END;

/**
 * 数据库查找
 *
 * @param <T> 被查询泛型
 */
public class BswDbListQuery<T> {

    /**
     * 列表过滤适配器
     */
    private final ListFilterAdapter<T> filterAdapter;
    /**
     * 查找列表
     */
    private BswDbFilterList<T> list;
    /**
     * 反射工具类，用于反射解析待查找列表的数据
     */
    private BswDbReflectUtils<T> reflectUtils;

    /**
     * 普通构造函数，只传入待查找列表
     *
     * @param list 待查找列表
     */
    BswDbListQuery(BswDbFilterList<T> list) {
        this(list, null);
    }

    /**
     * 用于用户自定义搜索条件的查找工具
     *
     * @param list          待查找列表
     * @param filterAdapter 过滤适配器
     */
    BswDbListQuery(BswDbFilterList<T> list, ListFilterAdapter<T> filterAdapter) {
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

        if (null == queryList || queryList.size() == 0) {
            // 搜索条件为空则直接返回排序结果
            if (null == filterAdapter) {
                resultList = list;
            } else {
                for (T t : list) {
                    if (filterAdapter.filter(t)) {
                        resultList.add(t);
                    }
                }
            }
            return sortJudge(resultList).subList(from, count);
        } else {
            // 搜索条件不为空则先筛选，再返回排序结果
            switch (queryType) {
                // 与关系搜索
                case AND:
                    for (T t : list) {
                        // 是否满足条件判断
                        if (andJudge(t)) {
                            if (ignoredCount < from) {
                                // 当有获取条目限制时（如分页），忽略数到查询起始条目前忽略
                                ignoredCount++;
                            } else {
                                resultList.add(t);
                            }
                        }
                        if (count != TO_THE_END && count <= resultList.size()) {
                            // 当有获取条目限制时（如分页），查询终止条目满后跳出
                            break;
                        }
                    }
                    break;

                // 或关系搜索
                case OR:
                    for (T t : list) {
                        // 是否满足条件判断
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

                default:
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
        } else {
            sortedList = list;
        }

        switch (queryType) {
            case AND:
                for (T t : sortedList) {
                    if (andJudge(t)) {
                        // 由于获取第一个，因此有一个满足的直接结束
                        return t;
                    }
                }
                break;

            case OR:
                for (T t : sortedList) {
                    if (orJudge(t)) {
                        return t;
                    }
                }
                break;

            default:
                break;
        }
        return null;
    }

    public boolean has(T t) {
        // 反射一次解析所有的List，避免多次反射消耗资源
        return reflectUtils.has(list, t);
    }

    /**
     * 与判断
     *
     * @param t 待判断类
     * @return 是否满足与条件
     */
    private synchronized boolean andJudge(T t) {
        Map<String, BswDbReflectParam> reflectResult = reflectUtils.getReflectResult(t);
        if (null == reflectResult) {
            return false;
        }
        for (BswDbQueryParam p : queryList) {
            BswDbReflectParam param = reflectResult.get(p.getKey());
            if (null == param) {
                return false;
            }
            Object vReflect = param.getValue();
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
    private synchronized boolean orJudge(T t) {
        Map<String, BswDbReflectParam> reflectResult = reflectUtils.getReflectResult(t);
        if (null == reflectResult) {
            return false;
        }
        for (BswDbQueryParam p : queryList) {
            BswDbReflectParam param = reflectResult.get(p.getKey());
            if (null == param) {
                continue;
            }
            Object vReflect = param.getValue();
            boolean judge = p.judge(vReflect);
            // 用户自定义判断，由于或判断，因此判断结果取或
            if (null != filterAdapter) {
                judge = judge || filterAdapter.filter(t);
            }
            // 若paramType没有问题，则根据当前判断是筛选条件的与判断，有一个满足则返回，所以验证judge若为true，则满足返回
            if (judge) {
                return true;
            }
        }
        return false;
    }

    /**
     * 排序判断
     *
     * @param listForSort 待排序的列表
     * @return 排序后的列表
     */
    private synchronized BswDbFilterList<T> sortJudge(BswDbFilterList<T> listForSort) {
        if (TextUtils.isEmpty(sortKey)) {
            return listForSort;
        } else {
            // 排序判断条件
            List<Object> sortJudgeList = new ArrayList<>();
            // 排序结果
            BswDbFilterList<T> sortList = new BswDbFilterList<>();
            for (T t : listForSort) {
                Map<String, BswDbReflectParam> reflectResult = reflectUtils.getReflectResult(t);
                // 若无法解析，或解析结果有误，则跳过
                if (null == reflectResult || reflectResult.size() == 0) {
                    continue;
                }
                // 若待排序项无满足排序条件的属性，则跳过
                BswDbReflectParam param = reflectResult.get(sortKey);
                if (null == param) {
                    continue;
                }
                // 若有满足排序条件的属性，但是该属性为空，则将该条目派到列表最后
                Object item = param.getValue();
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
     * 列表过滤适配器
     *
     * @param <T> 查看条目
     */
    public interface ListFilterAdapter<T> {
        /**
         * 依据过滤是否添加
         *
         * @param t 被判断条目
         * @return 是否符合条件
         */
        boolean filter(T t);
    }
}
