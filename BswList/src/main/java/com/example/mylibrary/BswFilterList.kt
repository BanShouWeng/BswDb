package com.example.mylibrary

import androidx.annotation.StringDef
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

class BswFilterList<T : Any> : ArrayList<T> {

    constructor()

    constructor(list: List<T>) {
        list.forEach {
            this.add(it)
        }
    }

    /**
     * kotlin的静态方法
     */
    companion object {
        @StringDef(DESC, ASC)
        @Retention(AnnotationRetention.SOURCE)
        annotation class SortType

        /**
         * 查询结果排序方式：DESC倒序、ASC正序
         */
        const val DESC: String = " desc"
        const val ASC: String = " asc"

        @StringDef(AND, OR)
        @Retention(AnnotationRetention.SOURCE)
        annotation class QueryType

        /**
         * 查询结果排序方式：DESC倒序、ASC正序
         */
        const val AND: String = " and "
        const val OR: String = " or "
    }

    fun query(): BswQuery<T> {
        return BswQuery<T>(this);
    }

    class BswQuery<T : Any>(val list: BswFilterList<T>) {
        private var queryList: ArrayList<Params> = ArrayList();
        private var queryType: String = AND
        private var sortType: String = ASC
        private var sortString: String = ""
        private lateinit var reflectProperty: Collection<KProperty1<T, *>>

        private val reflectResultMap: MutableMap<T, MutableMap<String, Any?>> = HashMap();


        /**
         * 添加参数：Short
         *
         * @param key   key
         * @param value value
         * @return 查询类
         */
        fun putParams(key: String, value: Any): BswQuery<T> {
            queryList.add(Params(key, value))
            return this
        }

        /**
         * 排序设置
         *
         * @param key 排序的关键字
         * @param sortType 排序的方式：正序
         */
        fun sort(key: String, @SortType sortType: String): BswQuery<T> {
            this.sortType = sortType
            sortString = key
            return this
        }

        /**
         * 设置搜索方式，是或还是与
         * @param queryType 搜索的类型{@link }
         */
        fun setQueryType(@QueryType queryType: String): BswQuery<T> {
            this.queryType = queryType
            return this
        }

        /**
         * 获取复核条件的集合
         */
        fun getAll(): BswFilterList<T>? {
            if (list.isEmpty()) {
                return null
            }
            // 反射解析Class
            reflectKClass(list.get(0).javaClass.kotlin)
            // 反射一次解析所有的List，避免多次反射消耗资源
            reflectList(list)

            if (queryList.isEmpty())        // 搜索条件为空则直接返回排序结果
                return sortJudge(list)
            else {                          // 搜索条件不为空则先筛选，再返回排序结果
                val resultList: BswFilterList<T> = BswFilterList()
                list.forEach {
                    if (when (queryType) {
                                AND -> andJudge(it)
                                else -> orJudge(it)
                            }) {
                        resultList.add(it);
                    }
                }
                return sortJudge(resultList);
            }
        }

        /**
         * 获取符合条件的第一条
         */
        fun getFirst(): T? {
            if (list.isEmpty()) {
                return null
            }
            // 反射解析Class
            reflectKClass(list.get(0).javaClass.kotlin)

            val sortedList: BswFilterList<T>

            if (sortString.isNotEmpty()) {
                // 反射一次解析所有的List，避免多次反射消耗资源
                reflectList(list)
                sortedList = sortJudge(list);
            } else
                sortedList = list

            var t: T? = null

            sortedList.forEach {
                when {
                    when {
                        AND == queryType -> andJudge(it)
                        else -> orJudge(it)
                    } -> {
                        t = it;return it
                    }

                }
            }
            return t;
        }

        /**
         * 排序判断方法
         *
         * @param list 待排序的集合
         * @return 排序后的集合
         */
        private fun sortJudge(list: BswFilterList<T>): BswFilterList<T> {
            if (sortString.isNotEmpty()) {
                val sortJudgeList: ArrayList<Any> = ArrayList()
                val sortList: BswFilterList<T> = BswFilterList()
                list.forEach {
                    val reflectResult = reflectResultMap.get(it)
                    if (null != reflectResult) {
                        val item = reflectResult.get(sortString);
                        if (null == item)
                            sortList.add(sortList.size, it)
                        else {
                            // 由于无法对泛型直接使用sortBy，所以将待筛选项取出排序
                            sortJudgeList.add(item)
                            if (item is Int)
                                sortJudgeList.sortBy {
                                    it.toString().toInt()
                                }
                            else if (item is Long)
                                sortJudgeList.sortBy {
                                    it.toString().toLong()
                                }
                            else if (item is Short)
                                sortJudgeList.sortBy {
                                    it.toString().toShort()
                                }
                            else if (item is Byte)
                                sortJudgeList.sortBy {
                                    it.toString().toByte()
                                }
                            else if (item is Boolean)
                                sortJudgeList.sortBy {
                                    it.toString().toBoolean()
                                }
                            else
                                sortJudgeList.sortBy {
                                    it.toString()
                                }

                            sortList.add(sortJudgeList.indexOf(item), it)
                        }
                    }
                }

                if (sortType.equals(DESC))
                    return BswFilterList(sortList.reversed())
                else
                    return sortList
            } else {
                return list
            }
        }

        /**
         * 取与时判断，若满足一个则都满足
         * @param t 被搜索的类
         */
        private fun andJudge(t: T): Boolean {
            // 默认满足
            var judgeResult = true
            val reflectResult = reflectT(t)
            queryList.forEach judgeResult@{
                val value: Any? = reflectResult.get(it.key)
                // 只要有一个不满足则不满足
                if (value == null || value != it.value) {
                    judgeResult = false
                    return@judgeResult
                }
            }
            return judgeResult
        }

        /**
         * 取或时判断，若满足一个则都满足
         * @param t 被搜索的类
         */
        private fun orJudge(t: T): Boolean {
            // 默认不满足
            var judgeResult = false
            val reflectResult = reflectT(t)
            queryList.forEach judgeResult@{
                val value: Any? = reflectResult.get(it.key)
                // 只要有一个满足则满足
                if (value != null && value == it.value) {
                    judgeResult = true
                    return@judgeResult
                }
            }
            return judgeResult
        }

        private fun reflectT(t: T): MutableMap<String, Any?> {
            val reflectResult: MutableMap<String, Any?> = HashMap();
            for (p in reflectProperty) {
                p.isAccessible = true
                reflectResult.put(p.name, p.get(t))
            }
            return reflectResult
        }

        private fun reflectList(list: BswFilterList<T>) {
            list.forEach {
                val reflectResult: MutableMap<String, Any?> = HashMap();

                for (p in reflectProperty) {
                    p.isAccessible = true
                    reflectResult.put(p.name, p.get(it))
                }
                reflectResultMap.put(it, reflectResult)
            }
        }

        private fun reflectKClass(clazz: KClass<T>) {
            reflectProperty = clazz.declaredMemberProperties
        }
    }
}
