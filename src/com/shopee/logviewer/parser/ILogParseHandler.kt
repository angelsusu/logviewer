package com.shopee.logviewer.parser

import java.io.File

/**
 * author: beitingsu
 * created on: 2020/11/20
 */
interface ILogParseHandler {

    /**
     * 解密文件
     */
    fun parse(logFile: File, parseFinishListener: ParseFinishListener? = null)
}