package com.shopee.logviewer

import java.awt.BorderLayout
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.table.DefaultTableModel


/**
 * author: beitingsu
 * created on: 2020/11/12
 */
class LogViewerFrame {

    fun showLogViewer() {
        val frame = JFrame("Android LogViewer") //创建Frame窗口
        frame.layout = BorderLayout() //为Frame窗口设置布局为BorderLayout

        val panel = JPanel()
        panel.layout = BorderLayout()
        panel.add(getChooseFilePanel(), BorderLayout.NORTH)
        panel.add(getFilterPanel(), BorderLayout.SOUTH)

        frame.add(panel, BorderLayout.NORTH)
        frame.add(getFilterTagPanel(), BorderLayout.WEST)
        frame.add(getLogContentPanel(), BorderLayout.CENTER)
        frame.isVisible = true
        frame.pack()
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    }

    /**
     * 文件选择对应布局
     */
    private fun getChooseFilePanel(): JPanel {
        val button = JButton("choose file")
        val jtf = JTextField(25)
        button.addActionListener {
            val fc = JFileChooser()
            val dialog = fc.showOpenDialog(null) //文件打开对话框
            if (dialog == JFileChooser.APPROVE_OPTION) {
                //正常选择文件
                jtf.text = fc.selectedFile.toString()
            } else {
                //未正常选择文件，如选择取消按钮
                jtf.text = "no file choose"
            }
        }
        val parseButton = JButton("parse file")
        parseButton.addActionListener {
            //todo parse file
        }
        val jp = JPanel()
        jp.add(button)
        jp.add(jtf)
        jp.add(parseButton)
        return jp
    }

    /**
     * 日志过滤、搜索对应布局
     */
    private fun getFilterPanel(): JPanel {
        val jp = JPanel()
        val jtf = JTextField(25)
        val cmb = JComboBox<String>()
        Utils.logLevelList.forEach { item ->
            cmb.addItem(item)
        }
        cmb.addItemListener {
            val text = cmb.selectedItem
            println(text)
        }
        val button = JButton("search")
        jp.add(cmb)
        jp.add(jtf)
        jp.add(button)
        return jp
    }

    /**
     * 过滤器对应的布局
     */
    private fun getFilterTagPanel(): JPanel {
        val jp = JPanel()
        val label = JLabel("Filters:")
        val addButton = JButton("add tag")
        val delButton = JButton("delete tag")
        jp.add(label)
        jp.add(addButton)
        jp.add(delButton)
        val panel = JPanel()
        panel.border = EmptyBorder(0, 5, 5, 5) //设置面板的边框
        panel.layout = BorderLayout(0, 0) //设置内容面板为边界布局
        panel.add(jp, BorderLayout.NORTH)
        val scrollPane = JScrollPane() //创建滚动面板
        panel.add(scrollPane, BorderLayout.CENTER) //将面板增加到边界布局中央
        val list = JList<String>()
        //限制只能选择一个元素
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        scrollPane.setViewportView(list) //在滚动面板中显示列表
        addButton.addActionListener {
            val result = JOptionPane.showInputDialog(panel, "please input tag", "FilterTag", 1)
        }
        return panel
    }

    /**
     * 日志内容对应布局
     */
    private fun getLogContentPanel(): JPanel {
        val contentPane = JPanel() //创建内容面板
        contentPane.border = EmptyBorder(5, 5, 5, 5) //设置面板的边框
        contentPane.layout = BorderLayout(0, 0) //设置内容面板为边界布局

        val table = JTable()
        val scrollPane = JScrollPane(table) //创建滚动面板

        val tableModel = table.model as DefaultTableModel //获得表格模型
        tableModel.rowCount = 0 //清空表格中的数据
        tableModel.setColumnIdentifiers(arrayOf<Any>("Time", "Level", "Tag", "Content")) //设置表头
        table.rowHeight = 30
        table.model = tableModel //应用表格模型

        contentPane.add(scrollPane, BorderLayout.CENTER) //将面板增加到边界布局中央
        return contentPane
    }
}