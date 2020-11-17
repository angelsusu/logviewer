package com.shopee.logviewer.filter

import com.shopee.logviewer.data.EnumLogLv
import com.shopee.logviewer.data.LogInfo

/**
 * @Author junzhang
 * @Time 2020/11/17
 *
 * 日志等级过滤，log.level >= target.level
 */
class LogLevelFilter(
    val enumTarget: EnumLogLv
): IFilter {

    override fun match(logInfo: LogInfo): Boolean {
        return logInfo.enumLevel.value >= enumTarget.value
    }
}