package com.shopee.logviewer

import com.shopee.logviewer.view.LogViewerFrame

/**
 * author: beitingsu
 * created on: 2020/11/12
 * 主函数入口
 */
fun main() {
    LogViewerFrame().showLogViewer()

    Thread.setDefaultUncaughtExceptionHandler(CrashHandler())
}