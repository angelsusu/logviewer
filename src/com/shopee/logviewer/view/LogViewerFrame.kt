package com.shopee.logviewer.view

import com.shopee.logviewer.data.FilterInfo
import com.shopee.logviewer.listener.DoubleClickListener
import com.shopee.logviewer.listener.LogMouseListener
import com.shopee.logviewer.util.Utils
import java.awt.BorderLayout
import java.awt.Component
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetAdapter
import java.awt.dnd.DropTargetDropEvent
import java.io.File
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.table.DefaultTableModel


/**
 * author: beitingsu
 * created on: 2020/11/12
 */
class LogViewerFrame {

    private lateinit var mFrame : JFrame

    private lateinit var mFilterList: JList<String>
    private val mTagList = arrayListOf<String>()
    private val mFilterMap = hashMapOf<String, FilterInfo>()

    private val mFilterDialogClickListener = object : ClickListener {
        override fun onClick(clickType: Int, filterInfo: FilterInfo?) {
            when {
                (clickType == ClickType.CLICK_TYPE_OK && null != filterInfo) -> onFilterRecv(filterInfo)
            }
        }
    }

    fun showLogViewer() {
        val frame = JFrame("Android LogViewer") //创建Frame窗口
        mFrame = frame
        frame.layout = BorderLayout() //为Frame窗口设置布局为BorderLayout

        val panel = JPanel()
        panel.layout = BorderLayout()
        panel.add(getFilterPanel(), BorderLayout.NORTH)
        panel.add(getLogContentPanel(), BorderLayout.CENTER)
        frame.add(getFilterTagPanel(), BorderLayout.WEST)
        frame.add(panel, BorderLayout.CENTER)
        frame.isVisible = true
        frame.pack()
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setBounds(300, 300,900, 662)
        supportFileDrag(frame)
    }

    private fun supportFileDrag(component: Component) {
        DropTarget(component, DnDConstants.ACTION_COPY_OR_MOVE, object: DropTargetAdapter() {
            override fun drop(dtde: DropTargetDropEvent?) {
                try {
                    //如果拖入的文件格式受支持
                    if (dtde?.isDataFlavorSupported(DataFlavor.javaFileListFlavor) == true) {
                        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE) //接收拖拽来的数据
                        val fileList = dtde.transferable.getTransferData(DataFlavor.javaFileListFlavor) as? List<File>
                        var temp = ""
                        fileList?.forEach { file ->
                            temp += file.absolutePath + ";\n"
                            JOptionPane.showMessageDialog(null, temp)
                        }
                        dtde.dropComplete(true) //指示拖拽操作已完成
                    } else {
                        dtde?.rejectDrop() //否则拒绝拖拽来的数据
                    }
                } catch (e: Exception) {

                }
            }
        })
    }

    /**
     * 日志过滤、搜索对应布局
     */
    private fun getFilterPanel(): JPanel {
        val jp = JPanel()
        jp.border = EmptyBorder(5, 0, 5, 5) //设置面板的边框
        jp.layout = BorderLayout(0, 0) //设置内容面板为边界布局
        val jtf = JTextField(25)
        val cmb = JComboBox<String>()
        Utils.logLevelList.forEach { item ->
            cmb.addItem(item)
        }
        cmb.addItemListener {
            val text = cmb.selectedItem
            println(text)
        }
        jp.add(jtf, BorderLayout.CENTER)
        jp.add(cmb, BorderLayout.EAST)
        return jp
    }

    /**
     * 过滤器对应的布局
     */
    private fun getFilterTagPanel(): JPanel {
        val jp = JPanel()
        val label = JLabel("Filters:")
        val addButton = JButton("Add tag")
        val delButton = JButton("Delete tag")
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
            showFilterEditDialog()
        }
        delButton.addActionListener {
            val selectItem = mFilterList.selectedValue
            mTagList.remove(selectItem)
            mFilterList.setListData(mTagList.toTypedArray())
        }
        list.addMouseListener(LogMouseListener(object : DoubleClickListener {
            override fun onDoubleClick() {
                val selectItem = list.selectedValue
                val filterInfo = mFilterMap[selectItem]
                showFilterEditDialog(filterInfo)
            }
        }))
        mFilterList = list
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

    private fun showFilterEditDialog(filterInfo: FilterInfo? = null) {
        FilterEditDialog(
                frame = mFrame,
                clickListener = mFilterDialogClickListener,
                filterData = filterInfo
        ).showDialog()
    }

    private fun onFilterRecv(filterInfo: FilterInfo) {
        if (!mFilterMap.containsKey(filterInfo.name)) {
            mTagList.add(filterInfo.name)
        }

        mFilterMap[filterInfo.name] = filterInfo
        mFilterList.setListData(mTagList.toTypedArray())
    }
}