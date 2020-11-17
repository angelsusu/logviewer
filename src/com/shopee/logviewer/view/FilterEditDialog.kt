package com.shopee.logviewer.view

import com.shopee.logviewer.data.FilterInfo
import java.awt.BorderLayout
import javax.swing.*
import javax.swing.border.EmptyBorder


/**
 * author: beitingsu
 * created on: 2020/11/13
 */
class FilterEditDialog(
        frame: JFrame,
        private val clickListener: ClickListener,
        private val filterData: FilterInfo?
): JDialog(frame) {

    private lateinit var mFilterNameText: JTextField
    private lateinit var mFilterMsgText: JTextField
    private lateinit var mFilterTagList: JList<String>
    private val mTagList = arrayListOf<String>()

    init {
        setSize(615, 500)
        isResizable = false
        setLocationRelativeTo(frame)
        filterData?.tagList?.let { mTagList.addAll(it) }
    }

    fun showDialog() {
        // 创建对话框的内容面板, 在面板内可以根据自己的需要添加任何组件并做任意布局
        val panel = JPanel()
        panel.border = EmptyBorder(5, 5, 5, 5) //设置面板的边框
        panel.layout = BorderLayout(0, 0) //设置内容面板为边界布局
        val filterPanel = JPanel()
        filterPanel.border = EmptyBorder(5, 5, 5, 5) //设置面板的边框
        filterPanel.layout = BorderLayout(0, 0) //设置内容面板为边界布局
        filterPanel.add(getFilterNamePanel(), BorderLayout.NORTH)
        filterPanel.add(getFilterMsgPanel(), BorderLayout.CENTER)
        filterPanel.add(getFilterTagPanel(), BorderLayout.SOUTH)
        panel.add(filterPanel, BorderLayout.NORTH)
        panel.add(getScrollPanel(), BorderLayout.CENTER)
        panel.add(getBtnPanel(), BorderLayout.SOUTH)
        // 设置对话框的内容面板
        contentPane = panel
        // 显示对话框
        isVisible = true
    }

    private fun getFilterNamePanel(): JPanel {
        val panel = JPanel()
        panel.border = EmptyBorder(5, 5, 5, 5) //设置面板的边框
        panel.layout = BorderLayout(0, 0) //设置内容面板为边界布局
        val filterLabel = JLabel("Filter Name")
        filterLabel.border = EmptyBorder(5, 5, 5, 5)
        val jtf = JTextField(25)
        panel.add(filterLabel, BorderLayout.WEST)
        panel.add(jtf, BorderLayout.CENTER)
        jtf.text = filterData?.name
        mFilterNameText = jtf
        return panel
    }

    private fun getFilterMsgPanel(): JPanel {
        val panel = JPanel()
        panel.border = EmptyBorder(5, 5, 5, 5) //设置面板的边框
        panel.layout = BorderLayout(0, 0) //设置内容面板为边界布局
        val filterLabel = JLabel("Filter Message")
        filterLabel.border = EmptyBorder(5, 5, 5, 5)
        val jtf = JTextField(25)
        panel.add(filterLabel, BorderLayout.WEST)
        panel.add(jtf, BorderLayout.CENTER)
        jtf.text = filterData?.msg
        mFilterMsgText = jtf
        return panel
    }

    private fun getFilterTagPanel(): JPanel {
        val panel = JPanel()
        panel.border = EmptyBorder(5, 5, 5, 5) //设置面板的边框
        panel.layout = BorderLayout(0, 0) //设置内容面板为边界布局
        val filterLabel = JLabel("Filter Tag")
        filterLabel.border = EmptyBorder(5, 5, 5, 5)
        val jtf = JTextField(25)
        panel.add(filterLabel, BorderLayout.WEST)
        panel.add(jtf, BorderLayout.CENTER)
        panel.add(getTagControlPanel(jtf), BorderLayout.EAST)
        return panel
    }

    private fun getTagControlPanel(jtf: JTextField): JPanel {
        val panel = JPanel()
        val addButton = JButton("+")
        val delButton = JButton("-")
        panel.add(addButton)
        panel.add(delButton)
        addButton.addActionListener {
            val text = jtf.text
            mTagList.add(text)
            if (!text.isNullOrEmpty()) {
                mFilterTagList.setListData(mTagList.toTypedArray())
            }
            jtf.text = ""
        }
        delButton.addActionListener {
            val selectItem = mFilterTagList.selectedValue
            mTagList.remove(selectItem)
            mFilterTagList.setListData(mTagList.toTypedArray())
        }
        return panel
    }

    private fun getScrollPanel(): JPanel {
        val panel = JPanel()
        panel.border = EmptyBorder(5, 5, 5, 5) //设置面板的边框
        panel.layout = BorderLayout(0, 0) //设置内容面板为边界布局
        val scrollPane = JScrollPane() //创建滚动面板
        val list = JList<String>()
        //限制只能选择一个元素
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        scrollPane.setViewportView(list) //在滚动面板中显示列表
        panel.add(scrollPane, BorderLayout.CENTER) //将面板增加到边界布局中央
        if (filterData?.tagList?.isNullOrEmpty() == false) {
            list.setListData(filterData.tagList?.toTypedArray())
        }
        mFilterTagList = list
        return panel
    }

    private fun getBtnPanel(): JPanel {
        val panel = JPanel()
        panel.border = EmptyBorder(5, 400, 5, 5) //设置面板的边框
        val cancelBtn = JButton("Cancel")
        cancelBtn.addActionListener {
            clickListener.onClick(ClickType.CLICK_TYPE_CANCEL)
            dispose()
        }
        val okBtn = JButton("Ok")
        okBtn.addActionListener {
            clickListener.onClick(ClickType.CLICK_TYPE_OK, getFilterInfo())
            dispose()
        }
        panel.add(cancelBtn)
        panel.add(okBtn)
        return panel
    }

    private fun getFilterInfo(): FilterInfo {
        val name = mFilterNameText.text
        val msg = mFilterMsgText.text
        return FilterInfo(name = name, msg = msg, tagList = mTagList.toList())
    }
}

interface ClickListener {
    fun onClick(clickType: Int, filterInfo: FilterInfo? = null)
}

object ClickType {
    const val CLICK_TYPE_CANCEL = 1
    const val CLICK_TYPE_OK = 2
}