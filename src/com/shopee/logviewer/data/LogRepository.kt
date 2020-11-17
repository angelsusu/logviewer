package com.shopee.logviewer.data

import com.shopee.logviewer.filter.IFilter
import com.shopee.logviewer.filter.TagMsgFilter
import javax.swing.SwingUtilities
import kotlin.reflect.KClass

/**
 * @Author junzhang
 * @Time 2020/11/16
 *
 * @param observer 所有的过滤[filter]结果通过observer统一回调
 */
class LogRepository(
    private val observer: ILogRepository
) {

    /** 原始的log数据 */
    private val rawLogs: ArrayList<LogInfo> = arrayListOf()
    /**
     * 过滤规则组合，目前的功能中每种[IFilter]仅支持一个
     */
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
    fun filter(filterInfo: FilterInfo) {
        if (filters.has(TagMsgFilter::class)) {
            print("filter() >>> already had same type of filter: TagMsgFilter")
            return
        }

        val cFilters = filters.let {
            it.add(TagMsgFilter(filterInfo = filterInfo))
            it.toList()
        }

        workThread.run {
            val filterResult = rawLogs.filter { logInfo ->
                cFilters.all { filter ->
                    filter.match(logInfo)
                }
            }

            SwingUtilities.invokeLater {
                observer.onFilterResult(filterInfo, filterResult)
            }
        }
    }

    private fun List<IFilter>.has(klz: KClass<out IFilter>): Boolean = this.any {
        it::class == klz
    }
}

interface ILogRepository {

    fun onFilterResult(filterInfo: FilterInfo, result: List<LogInfo>?)

}