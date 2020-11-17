package com.shopee.logviewer.data

import java.util.concurrent.BlockingDeque
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import javax.swing.SwingUtilities

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
    /** 过滤工作线程 */
    //private val workThread = WorkThread()

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
        //workThread.offer(Runnable {
            val filterResult = rawLogs.filter { logInfo ->
                filterInfo.matchTag(logInfo) && filterInfo.matchMsg(logInfo)
            }

            SwingUtilities.invokeLater {
                observer.onFilterResult(filterInfo, filterResult)
            }
        //})
    }

    private fun FilterInfo.matchTag(logInfo: LogInfo): Boolean {
        if (null == this.tagList || this.tagList?.isEmpty() == true) {
            // 没有tag过滤目标，默认符合要求
            return true
        }

        if (logInfo.tag.isBlank()) {
            // Log.Tag empty or black，默认不符合要求
            return false
        }

        return this.tagList?.any { targetTag ->
            targetTag.isNotBlank() && // 目标tag不为空
                    logInfo.tag.equals(targetTag, true) // 命中tag
        } ?: false
    }

    private fun FilterInfo.matchMsg(logInfo: LogInfo): Boolean {
        if (null == this.msg || this.msg.isBlank()) {
            // 没有msg目标，默认符合要求
            return true
        }

        if (logInfo.content.isBlank()) {
            // Log.Content empty or black，默认不符合要求
            return false
        }

        return logInfo.content.contains(this.msg, true) // Log.Content包含target msg信息，命中
    }

    inner class WorkThread(
            private val rBlockQueue: BlockingQueue<Runnable> = LinkedBlockingQueue<Runnable>()
    ): Thread("filter-thread") {

        fun offer(r: Runnable) {
            while (!rBlockQueue.offer(r)) {
                sleep(500)
            }
        }

        override fun run() {
            while (true) {
                rBlockQueue.take().run()
            }
        }
    }
}

interface ILogRepository {

    fun onFilterResult(filterInfo: FilterInfo, result: List<LogInfo>?)

}