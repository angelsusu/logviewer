package com.shopee.logviewer.view

import java.awt.Frame
import javax.swing.JOptionPane

/**
 * @Author junzhang
 * @Time 2020/11/18
 *
 * 提示弹窗
 */
object AlertDialog {

    var sFrame: Frame? = null

    fun showInfo(msg: String?) {
        val frame = sFrame ?: return
        JOptionPane.showMessageDialog(frame, msg, "INFO.", JOptionPane.INFORMATION_MESSAGE)
    }

    fun showAlert(msg: String?) {
        val frame = sFrame ?: return
        JOptionPane.showMessageDialog(frame, msg, "WARNING.", JOptionPane.WARNING_MESSAGE)
    }

}

