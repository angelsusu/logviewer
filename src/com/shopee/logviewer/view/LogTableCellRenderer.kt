package com.shopee.logviewer.view

import com.shopee.logviewer.data.FilterInfo
import java.awt.Color
import java.awt.Component
import javax.swing.JTable
import javax.swing.JTextPane
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.text.StyleConstants


/**
 * author: beitingsu
 * created on: 2020/11/17
 * 表格渲染类，处理文本高亮
 */
class LogTableCellRenderer : DefaultTableCellRenderer() {

    var highlightMsg = ""

    override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
        val content = value as? String
        val textPane = JTextPane()
        val def = textPane.styledDocument.addStyle(null, null)
        val normal = textPane.addStyle("normal", def)
        val style = textPane.addStyle("red", normal)
        val selectedStyle = textPane.addStyle("white", def)
        StyleConstants.setForeground(selectedStyle, Color.WHITE)
        StyleConstants.setForeground(style, Color.RED)
        if (isSelected) {
            textPane.background = Color(7,73,217)
            textPane.setParagraphAttributes(selectedStyle, true)
        } else {
            textPane.background = Color.WHITE
            textPane.setParagraphAttributes(normal, true)
        }
        content?.let {
            highlightMsg(textPane, content)
        } ?: setTextNormal(textPane, content)
        return textPane
    }

    private fun highlightMsg(textPane: JTextPane, content: String) {
        if (highlightMsg.isEmpty()) {
            setTextNormal(textPane, content)
        } else {
            var splitText = content
            while (true) {
                val index = splitText.indexOf(highlightMsg, ignoreCase = true)
                if (index == -1) {
                    setTextNormal(textPane, splitText)
                    break
                } else {
                    var normalText = ""
                    var highlightText = ""
                    if (index > 0) {
                        normalText = splitText.substring(0, index)
                        splitText = splitText.substring(index)
                        setTextNormal(textPane, normalText)
                    } else {
                        highlightText = splitText.substring(0, highlightMsg.length)
                        splitText = splitText.substring(highlightMsg.length)
                        setTextHighlight(textPane, highlightText)
                    }
                    println("LogTableCellRenderer:$normalText:$highlightText:$splitText")
                }
            }
        }
    }

    private fun setTextNormal(textPane: JTextPane, content: String?) {
        textPane.document.insertString(textPane.document.length, content, textPane.getStyle("normal"))
    }

    private fun setTextHighlight(textPane: JTextPane, content: String?) {
        textPane.document.insertString(textPane.document.length, content, textPane.getStyle("red"))
    }
}