package com.shopee.logviewer

import java.awt.BorderLayout
import javax.swing.*
import javax.swing.border.EmptyBorder


/**
 * author: beitingsu
 * created on: 2020/11/13
 */
class FilterEditDialog(frame: JFrame): JDialog(frame) {

    init {
        setSize(615, 500)
        isResizable = false
        setLocationRelativeTo(frame)
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
        return panel
    }

    private fun getFilterTagPanel(): JPanel {
        val panel = JPanel()
        panel.border = EmptyBorder(5, 5, 5, 5) //设置面板的边框
        panel.layout = BorderLayout(0, 0) //设置内容面板为边界布局
        val filterLabel = JLabel("Filter Tag")
        filterLabel.border = EmptyBorder(5, 5, 5, 5)
        val jtf = JTextField(25)
        val addButton = JButton("+")
        panel.add(filterLabel, BorderLayout.WEST)
        panel.add(jtf, BorderLayout.CENTER)
        panel.add(addButton, BorderLayout.EAST)
        addButton.addActionListener {

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
        return panel
    }

    private fun getBtnPanel(): JPanel {
        val panel = JPanel()
        panel.border = EmptyBorder(5, 400, 5, 5) //设置面板的边框
        val cancelBtn = JButton("Cancel")
        cancelBtn.addActionListener {
            dispose()
        }
        val okBtn = JButton("Ok")
        okBtn.addActionListener {
            //todo
            dispose()
        }
        panel.add(cancelBtn)
        panel.add(okBtn)
        return panel
    }

}