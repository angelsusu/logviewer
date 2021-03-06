package com.shopee.logviewer.filter

import com.shopee.logviewer.data.FilterInfo
import com.shopee.logviewer.data.LogInfo

/**
 * @Author junzhang
 * @Time 2020/11/17
 *
 * TAG + msg复合过滤规则
 */
class CombineFilter(
    val filterInfo: FilterInfo
): IFilter {

    override fun match(logInfo: LogInfo): Boolean {
        return matchTag(logInfo) && matchMsg(logInfo)
    }

    private fun matchTag(logInfo: LogInfo): Boolean {
        if (null == filterInfo.tagList || filterInfo.tagList.isNullOrEmpty()) {
            // 没有tag过滤目标，默认符合要求
            return true
        }

        if (logInfo.tag.isBlank()) {
            // Log.Tag empty or black，默认不符合要求
            return false
        }

        return filterInfo.tagList?.any { targetTag ->
            targetTag.isNotBlank() && // 目标tag不为空
                logInfo.tag.equals(targetTag, true) // 命中tag
        } ?: false
    }

    private fun matchMsg(logInfo: LogInfo): Boolean {
        val msg = filterInfo.msg
        if (null == msg || msg.isNullOrBlank()) {
            // 没有msg目标，默认符合要求
            return true
        }

        if (logInfo.content.isBlank()) {
            // Log.Content empty or black，默认不符合要求
            return false
        }

        return if (filterInfo.isRegex) {
            msg.toRegex().containsMatchIn(logInfo.content)
        } else {
            logInfo.content.contains(msg, true) // Log.Content包含target msg信息，命中
        }
    }

}