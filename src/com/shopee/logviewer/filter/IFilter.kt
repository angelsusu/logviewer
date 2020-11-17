package com.shopee.logviewer.filter

import com.shopee.logviewer.data.LogInfo

/**
 * @Author junzhang
 * @Time 2020/11/17
 *
 * 过滤规则抽象
 */
interface IFilter {

    fun match(logInfo: LogInfo): Boolean

}

