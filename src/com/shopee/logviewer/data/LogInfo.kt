package com.shopee.logviewer.data

/**
 * author: beitingsu
 * created on: 2020/11/13
 * 界面展示的日志信息
 */
data class LogInfo(
    val time: String,
    val pid: String = "",
    val tid: String = "",
    val tag: String,
    val strLevel: String,
    val enumLevel: EnumLogLv,
    val content: String
)