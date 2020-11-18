package com.shopee.logviewer.data

import com.shopee.logviewer.filter.IFilter
import com.shopee.logviewer.filter.LogLevelFilter
import com.shopee.logviewer.filter.CombineFilter
import com.shopee.logviewer.filter.MessageFilter
import javax.swing.SwingUtilities
import kotlin.reflect.KClass

/**
 * @Author junzhang
 * @Time 2020/11/16
 *
 * @param observer 所有的过滤[addFilter]结果通过observer统一回调
 */
class LogRepository(
    private val observer: ILogRepository
) {

    /** 原始的log数据 */
    private val rawLogs: ArrayList<LogInfo> = arrayListOf()
    /** 过滤规则组合，目前的功能中每种[IFilter]仅支持一个 */
    private val filters: ArrayList<IFilter> = arrayListOf()
    /** 过滤工作线程 */
    private val workThread = Thread("filter-thread")

    /** 更新元数据 */
    fun updateMeta(infoList: List<LogInfo>) {
        print("updateMeta() >>> infoList.size[${infoList.size}]")
        if (infoList.isEmpty()) {
            return
        }

        rawLogs.clear()
        rawLogs.addAll(infoList)
    }

    /** @param filterInfo 根据[FilterInfo]过滤 */
    fun addFilter(filterInfo: FilterInfo) {
        if (rawLogs.isEmpty()) {
            print("addFilter() >>> empty raw")
            return
        }

        val newFilter = CombineFilter(filterInfo = filterInfo)

        if (filters.has(CombineFilter::class)) {
            print("addFilter() >>> already had same type of filter, replace")
            asyncFilter(filters.replaceAndCopy(newFilter), last = newFilter)
            return
        }

        asyncFilter(filters.addAndCopy(newFilter = newFilter), last = newFilter)
    }

    /** @param logLevel 根据日志等级进行过滤 */
    fun addFilter(logLevel: EnumLogLv) {
        if (rawLogs.isEmpty()) {
            print("addFilter() >>> empty raw")
            return
        }

        if (!filters.has(LogLevelFilter::class)) {
            if (EnumLogLv.V.value >= logLevel.value) {
                // 没有留存LogLevelFilter，且新Lv是Verbose，没有Add Filter的必要
                print("filter() >>> no existing log level, and new log level is Verbose")
                return
            }

            print("filter() >>> async filter with log level[${logLevel.value}]")
            val newFilter = LogLevelFilter(enumTarget = logLevel)
            asyncFilter(filters.addAndCopy(newFilter), last = newFilter)
            return
        }

        val currentLogLv = (filters.firstOrNull { it::class == LogLevelFilter::class } as? LogLevelFilter)?.enumTarget
        currentLogLv ?: run {
            print("filter() >>> fail to get current log level")
            return
        }

        if (currentLogLv == logLevel) {
            // 有留存LogLevelFilter，且新Lv与旧Lv一致
            print("filter() >>> equalled log level[$currentLogLv]")
            return
        }

        if (logLevel.value <= EnumLogLv.V.value) {
            // Verbose其实是删除
            val cFilters = filters.removeAndCopy(LogLevelFilter::class)
            print("filter() >>> async filter with log level[${logLevel.value}] cFilters.size[${cFilters.size}]")
            asyncFilter(
                cFilters,
                last = LogLevelFilter(enumTarget = EnumLogLv.V) // fake filter
            )
            return
        }

        print("filter() >>> async filter with log level[${logLevel.value}]")
        val newFilter = LogLevelFilter(enumTarget = logLevel)
        asyncFilter(filters.replaceAndCopy(newFilter), last = newFilter)
    }

    /** 文本过滤器 */
    fun addFilter(message: String?, isRegex: Boolean) {
        if (rawLogs.isEmpty()) {
            print("addFilter() >>> empty raw")
            return
        }

        if (!filters.has(MessageFilter::class)) {
            print("filter() >>> async filter with regex[$isRegex] message[$message]")
            val newFilter = MessageFilter(message = message, isRegex = isRegex)
            asyncFilter(filters.addAndCopy(newFilter), last = newFilter)
            return
        }

        print("filter() >>> async filter with regex[$isRegex] message[$message]")
        val newFilter = MessageFilter(message = message, isRegex = isRegex)
        asyncFilter(filters.replaceAndCopy(newFilter), last = newFilter)
    }

    /**
     * 删除[FilterInfo]后重新发起过滤
     * @param filterInfo 因为目前一种过滤方式仅一种Filter，暂时没有什么用处
     */
    fun removeFilter(filterInfo: FilterInfo) {
        if (rawLogs.isEmpty()) {
            print("removeFilter() >>> empty raw")
            return
        }

        if (!filters.has(CombineFilter::class)) {
            print("removeFilter() >>> didn't have combine filter in current filter list")
            return
        }

        print("removeFilter() >>> async filter after removing CombineFilter")
        asyncFilter(
            filters.removeAndCopy(CombineFilter::class),
            last = null
        )
    }

    /**
     * 删除所有现存的[IFilter] [filters]
     */
    fun removeFilters() {
        if (filters.isEmpty()) {
            print("removeFilters() >>> empty filters")
            return
        }

        filters.clear()
        if (rawLogs.isEmpty()) {
            print("removeFilters() >>> empty raw")
            return
        }

        asyncFilter(listOf(), null)
    }

    private inline fun ArrayList<IFilter>.addAndCopy(newFilter: IFilter): List<IFilter> {
        add(newFilter)
        return toList()
    }

    private inline fun ArrayList<IFilter>.removeAndCopy(klz: KClass<out IFilter>): List<IFilter> {
        removeIf {
            it::class == klz
        }

        return toList()
    }

    private inline fun ArrayList<IFilter>.replaceAndCopy(newFilter: IFilter): List<IFilter> {
        removeIf {
            it::class == newFilter::class
        }

        add(newFilter)
        return toList()
    }

    private fun List<IFilter>.has(klz: KClass<out IFilter>): Boolean = this.any {
        it::class == klz
    }

    private fun asyncFilter(filters: List<IFilter>, last: IFilter?) = workThread.run {
        if (filters.isEmpty()) {
            SwingUtilities.invokeLater {
                observer.onFilterResult(last, rawLogs)
            }
            return@run
        }

        val filterResult = rawLogs.filter { logInfo ->
            filters.all { filter ->
                filter.match(logInfo)
            }
        }

        SwingUtilities.invokeLater {
            observer.onFilterResult(last, filterResult)
        }
    }

    /**
     * 返回原始数据
     */
    fun getRawLogs(): ArrayList<LogInfo> {
        return rawLogs
    }
}

interface ILogRepository {

    /**
     * @param lastFilter 最近一次触发搜索的Filter。如果是删除Filter导致的搜索，返回null
     */
    fun onFilterResult(lastFilter: IFilter?, result: List<LogInfo>?)

}