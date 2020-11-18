package com.shopee.logviewer.filter

import com.shopee.logviewer.data.LogInfo

/**
 * @Author junzhang
 * @Time 2020/11/17
 */
class MessageFilter(
    val message: String?,
    val isRegex: Boolean
): IFilter {

    override fun match(logInfo: LogInfo): Boolean {
        if (message.isNullOrBlank()) {
            return true
        }

        if (logInfo.content.isBlank()) {
            return false
        }

        return if (isRegex) {
            message.toRegex().containsMatchIn(logInfo.content)
        } else {
            logInfo.content.contains(message, true)
        }
    }

}