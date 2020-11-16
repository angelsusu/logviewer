package com.shopee.logviewer.data

/**
 * @Author junzhang
 * @Time 2020/11/16
 */
class LogRepository {

    /** 原始的log数据 */
    private val rawLogs: ArrayList<LogInfo> = arrayListOf()

    fun update(infoList: List<LogInfo>) {
        print("infoList.size[${infoList.size}]")
        if (infoList.isEmpty()) {
            return
        }

        rawLogs.clear()
        rawLogs.addAll(infoList)
    }

    fun filterByFilterInfo(filterInfo: FilterInfo): List<LogInfo> {
        return rawLogs.filter { logInfo ->
            filterInfo.matchTag(logInfo) && // tag命中
                    filterInfo.matchMsg(logInfo) // msg命中
        }
    }

    private fun FilterInfo.matchTag(logInfo: LogInfo): Boolean {
        if (null == this.tagList || this.tagList.isEmpty()) {
            // 没有tag过滤目标，默认符合要求
            return true
        }

        if (logInfo.tag.isBlank()) {
            // Log.Tag empty or black，默认不符合要求
            return false
        }

        return this.tagList.any { targetTag ->
            targetTag.isNotBlank() && // 目标tag不为空
                    logInfo.tag.equals(targetTag, true) // 命中tag
        }
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

}