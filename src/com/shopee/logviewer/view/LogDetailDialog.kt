package com.shopee.logviewer.view

import com.shopee.logviewer.data.LogInfo
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JPanel

/**
 * @Author junzhang
 * @Time 2020/11/18
 */
object LogDetailDialog {

    fun showLogDetail(frame: JFrame, logInfo: LogInfo) {
        LogDetailDialogBuilder()
            .setFrame(frame)
            .setLogInfo(logInfo)
            .build()
            ?.showDialog()
    }

    private class LogDetailDialogBuilder {

        private var frame: JFrame? = null
        private var logInfo: LogInfo? = null

        fun setFrame(frame: JFrame) = apply { this.frame = frame }
        fun setLogInfo(logInfo: LogInfo) = apply { this.logInfo = logInfo }

        fun build(): LogDetailDialogImpl? {
            val f = frame ?: return null
            val l = logInfo ?: return null

            return LogDetailDialogImpl(f, l)
        }
    }

    private class LogDetailDialogImpl(
        frame: JFrame,
        private val logInfo: LogInfo
    ): JDialog(frame) {

        init {
            setContentView()
        }

        private fun setContentView() {
            setSize(500, 500)
            isResizable = false
            contentPane = JPanel()
        }

        fun showDialog() {
            this.isVisible = true
        }

        fun dismissDialog() {
            this.isVisible = false
        }

    }
}

