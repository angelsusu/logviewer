package com.shopee.logviewer.view

import com.shopee.logviewer.data.LogInfo
import java.awt.*
import javax.swing.*


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
            setContentView(parentFrame = frame)
        }

        private fun setContentView(parentFrame: JFrame) {
            setLocationRelativeTo(parentFrame)
            this.setSize(300, 200)

            this.contentPane = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS).apply {
                    alignmentX = Component.LEFT_ALIGNMENT
                    border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
                }

                add(buildPanel("TAG:", logInfo.tag))
                add(buildPanel("TIME:", logInfo.time))
                add(buildPanel("LEVEL:", logInfo.strLevel))
                add(JTextArea(logInfo.content).apply {
                    lineWrap = true
                    isEditable = false
                })
            }
        }

        private fun buildPanel(title: String?, msg: String?): JPanel = JPanel().apply {
            border = BorderFactory.createEmptyBorder(0, 0, 0, 0)

            add(Box.createHorizontalGlue())
            add(JLabel(title))
            add(Box.createRigidArea(Dimension(5, 0)))
            add(JLabel(msg))
        }

        fun showDialog() {
            this.isVisible = true
        }

        fun dismissDialog() {
            this.isVisible = false
        }

    }
}

