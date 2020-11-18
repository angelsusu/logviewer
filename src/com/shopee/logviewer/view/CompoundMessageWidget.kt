package com.shopee.logviewer.view

import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import javax.swing.JCheckBox
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * @Author junzhang
 * @Time 2020/11/18
 *
 * Message 和 Regex的复合过滤器
 */
class CompoundMessageWidget(
    private val funcAddFilter: (String?, Boolean) -> Unit
): DocumentListener, ItemListener {

    /** 文本过滤EditText */
    val msgFilterField: JTextField = JTextField(35).also {
        it.document.addDocumentListener(this)
    }

    /** 正则表达式Checkbox */
    val regexCheckbox: JCheckBox = JCheckBox("Regex").also {
        it.addItemListener(this)
    }

    /** 文本过滤条件EditText Listener */
    override fun changedUpdate(e: DocumentEvent?) {
        addFilter()
    }

    override fun insertUpdate(e: DocumentEvent?) {
        addFilter()
    }

    override fun removeUpdate(e: DocumentEvent?) {
        addFilter()
    }

    /** 正则表达式开关切换 */
    override fun itemStateChanged(event: ItemEvent?) {
        when (event?.stateChange) {
            ItemEvent.SELECTED -> addFilter()
            ItemEvent.DESELECTED -> addFilter()
        }
    }

    private fun addFilter() {
        funcAddFilter.invoke(msgFilterField.text, regexCheckbox.isSelected)
    }
}