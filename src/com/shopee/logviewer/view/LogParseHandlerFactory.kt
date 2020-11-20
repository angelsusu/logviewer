package com.shopee.logviewer.view

import com.shopee.logviewer.parser.ILogParseHandler
import com.shopee.logviewer.parser.LoganParseHandler
import com.shopee.logviewer.parser.LogcatParseHandler
import com.shopee.logviewer.parser.ParseFinishListener

/**
 * author: beitingsu
 * created on: 2020/11/20
 */
object LogParseHandlerFactory {

    private const val LOGCAT_SUFFIX = "txt"

    /**
     * 获取对应的解密类
     * 目前只支持logan、logcat格式，以后缀区分，txt后缀默认为logcat文件
     */
    fun getParseHandler(fileName: String, parseFinishListener: ParseFinishListener? = null): ILogParseHandler? {
        return when (fileName.substring(fileName.lastIndexOf(".") + 1)) {
            LOGCAT_SUFFIX -> {
                LogcatParseHandler()
            }
            else -> LoganParseHandler()
        }
    }
}