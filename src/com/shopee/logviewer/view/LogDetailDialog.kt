package com.shopee.logviewer.view

import com.shopee.logviewer.data.LogInfo
import java.awt.Component
import java.awt.Dimension
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
            setContentView(frame = frame)
        }

        private fun setContentView(frame: JFrame) {
            setLocationRelativeTo(frame)
            setSize(300, 200)

            contentPane = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS).apply {
                    add(JPanel().apply {
                        alignmentX = Component.LEFT_ALIGNMENT
                        border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
                        add(Box.createHorizontalGlue())
                        add(JLabel("TAG:"))
                        add(Box.createRigidArea(Dimension(5, 0)))
                        add(JLabel(logInfo.tag))
                    })

                    add(JPanel().apply {
                        alignmentX = Component.LEFT_ALIGNMENT
                        border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
                        add(Box.createHorizontalGlue())
                        add(JLabel("TIME:"))
                        add(Box.createRigidArea(Dimension(5, 0)))
                        add(JLabel(logInfo.time))
                    })

                    add(JPanel().apply {
                        alignmentX = Component.LEFT_ALIGNMENT
                        border = BorderFactory.createEmptyBorder(0, 0, 0, 0)
                        add(Box.createHorizontalGlue())
                        add(JLabel("LEVEL:"))
                        add(Box.createRigidArea(Dimension(5, 0)))
                        add(JLabel(logInfo.strLevel))
                    })

                    add(JTextArea(logInfo.content).apply {
                        alignmentX = Component.LEFT_ALIGNMENT
                        lineWrap = true
                        isEditable = false
                    })
                }
            }
        }

        fun showDialog() {
            this.isVisible = true
        }

        fun dismissDialog() {
            this.isVisible = false
        }

    }
}

