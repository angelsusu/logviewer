package com.shopee.logviewer.view


import com.shopee.logviewer.util.Utils.levelTextColorMap
import java.awt.Color
import java.awt.Component
import javax.swing.JTable
import javax.swing.JTextPane
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.text.StyleConstants


/**
 * author: beitingsu
 * created on: 2020/11/17
 * 表格渲染类，处理文本高亮、背景色逻辑
 */
class LogTableCellRenderer(private val levelColumn: Int) : DefaultTableCellRenderer() {

    var highlightMsg = ""

    private var mTextColor: Color? = null

    companion object {
        private const val NORMAL_STYLE = "normal"
        private const val HIGHLIGHT_STYLE = "highlight"
    }

    override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
        return table?.let {
            val level = table.getValueAt(row, levelColumn) as? String
            mTextColor = levelTextColorMap[level]
            foreground = mTextColor  //设置文字颜色
            if (column == (table.columnCount - 1)) {
                createContentPanel(value, isSelected)
            } else {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
            }
        } ?: super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
    }

    private fun createContentPanel(value: Any?, isSelected: Boolean): JTextPane {
        val content = value as? String
        val textPane = JTextPane()
        val def = textPane.styledDocument.addStyle(null, null)
        val normal = textPane.addStyle(NORMAL_STYLE, def)
        val highlightStyle = textPane.addStyle(HIGHLIGHT_STYLE, normal)
        StyleConstants.setForeground(highlightStyle, Color.BLACK)
        if (isSelected) {
            textPane.background = Color(7, 73, 217)
            StyleConstants.setForeground(normal, Color.WHITE)
        } else {
            textPane.background = Color.WHITE
            StyleConstants.setForeground(normal, mTextColor)
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
                    //println("LogTableCellRenderer:$normalText:$highlightText:$splitText")
                }
            }
        }
    }

    private fun setTextNormal(textPane: JTextPane, content: String?) {
        textPane.document.insertString(textPane.document.length, content, textPane.getStyle(NORMAL_STYLE))
    }

    private fun setTextHighlight(textPane: JTextPane, content: String?) {
        textPane.document.insertString(textPane.document.length, content, textPane.getStyle(HIGHLIGHT_STYLE))
    }
}