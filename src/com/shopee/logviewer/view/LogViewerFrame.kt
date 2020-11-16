package com.shopee.logviewer.view

import com.shopee.logviewer.data.FilterInfo
import com.shopee.logviewer.data.ILogRepository
import com.shopee.logviewer.data.LogRepository
import com.shopee.logviewer.data.LogInfo
import com.shopee.logviewer.listener.DoubleClickListener
import com.shopee.logviewer.listener.LogMouseListener
import com.shopee.logviewer.util.LogParserHandler
import com.shopee.logviewer.util.ParseFinishListener
import com.shopee.logviewer.util.Utils
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetAdapter
import java.awt.dnd.DropTargetDropEvent
import java.io.File
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.event.ListSelectionListener
import javax.swing.table.DefaultTableModel


/**
 * author: beitingsu
 * created on: 2020/11/12
 */
class LogViewerFrame: ILogRepository {

    private lateinit var mFrame : JFrame

    private lateinit var uiFilterList: JList<String>
    private val mTagList = arrayListOf<String>()
    private val mFilterMap = hashMapOf<String, FilterInfo>()

    /** 更新List的接口统一通过 [ILogRepository] callback */
    private val logRepository = LogRepository(observer = this@LogViewerFrame)

    private lateinit var mContentTable: JTable

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
                        val fileSize = fileList?.size ?: 0
                        if (fileSize > 1) {
                            dtde.rejectDrop() //否则拒绝拖拽来的数据
                        } else {
                            val file = fileList?.get(0) ?: return
                            sLogParserHandler.parse(file)
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

        addButton.addActionListener {
            showFilterEditDialog()
        }

        delButton.addActionListener {
            val selectItem = uiFilterList.selectedValue
            mTagList.remove(selectItem)
            uiFilterList.setListData(mTagList.toTypedArray())
        }

        //在滚动面板中显示列表
        scrollPane.setViewportView(JList<String>().also { list ->
            // 限制只能选择一个元素
            list.selectionMode = ListSelectionModel.SINGLE_SELECTION
            // 选择监听器
            list.addListSelectionListener(sFilterSelectListener)
            // 双击查看详情
            list.addMouseListener(LogMouseListener(object : DoubleClickListener {
                override fun onDoubleClick() {
                    val selectItem = list.selectedValue
                    val filterInfo = mFilterMap[selectItem]
                    showFilterEditDialog(filterInfo)
                }
            }))

            uiFilterList = list
        })

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
        table.setShowGrid(true)
        table.gridColor = Color.lightGray
        contentPane.add(scrollPane, BorderLayout.CENTER) //将面板增加到边界布局中央
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
        initPopupCopyMenu(table)
        mContentTable = table
        return contentPane
    }

    private fun showFilterEditDialog(filterInfo: FilterInfo? = null) {
        FilterEditDialog(
                frame = mFrame,
                clickListener = sFilterDialogClickListener,
                filterData = filterInfo
        ).showDialog()
    }

    /** 获取当前highlight的[FilterInfo] */
    private fun getHighlightFilter(): FilterInfo? {
        val filterName = uiFilterList.selectedValue ?: return null

        if (filterName.isBlank()) return null

        return mFilterMap[filterName]
    }

    /** Log解密回调 */
    private val sLogParserHandler = LogParserHandler(object : ParseFinishListener {
        override fun onParseFinish(logInfo: List<LogInfo>) {
            // 更新元数据
            logRepository.updateMeta(logInfo)

            val filter = getHighlightFilter() ?: run {
                refreshLogTables(logInfo)
                return
            }

            logRepository.filter(filter)
        }
    })

    /** Filter添加Dialog监听器 */
    private val sFilterDialogClickListener = object : ClickListener {
        override fun onClick(clickType: Int, filterInfo: FilterInfo?) {
            when {
                (clickType == ClickType.CLICK_TYPE_OK && null != filterInfo) -> onFilterAddRecv(filterInfo)
            }
        }
    }

    /** 左侧Filter栏点击触发器 */
    private val sFilterSelectListener = ListSelectionListener { event ->
        if (event.firstIndex != event.lastIndex) {
            print("filter select index didn't support multi select:[${event.firstIndex} - ${event.lastIndex}]")
            return@ListSelectionListener
        }

        val filter = getHighlightFilter() ?: return@ListSelectionListener
        print("filter:\n$filter")
        logRepository.filter(filter)
    }

    /** [ILogRepository] */
    override fun onFilterResult(result: List<LogInfo>?) {
        refreshLogTables(logInfo = result)
    }

    /** 添加新的Filter */
    private fun onFilterAddRecv(filterInfo: FilterInfo) {
        if (!mFilterMap.containsKey(filterInfo.name)) {
            mTagList.add(filterInfo.name)
        }

        mFilterMap[filterInfo.name] = filterInfo
        uiFilterList.setListData(mTagList.toTypedArray())

        // 以最新的filter进行过滤
        logRepository.filter(filterInfo)
    }

    /** 统一更新Filtered Log的入口 */
    private fun refreshLogTables(logInfo: List<LogInfo>?) {
        val tableModel = mContentTable.model as DefaultTableModel
        tableModel.rowCount = 0

        logInfo ?: return

        logInfo.forEach { info ->
            tableModel.addRow(arrayOf<Any>(info.time, info.level, info.tag, info.content))
        }
    }

    private fun initPopupCopyMenu(component: JTable) {
        val menu = PopupCopyMenu(component, object : PopupCopyMenuClickListener {
            override fun onClick(clickType: Int) {
                if (clickType == MenuClickType.CLICK_TYPE_COPY) {
                    //copy选中数据
                    val rowCount = mContentTable.selectedRows
                    val listInfo = arrayListOf<LogInfo>()
                    for (index in rowCount.indices) {
                        val time = mContentTable.getValueAt(index, 0) as String
                        val level = mContentTable.getValueAt(index, 1) as String
                        val tag = mContentTable.getValueAt(index, 2) as String
                        val content = mContentTable.getValueAt(index, 3) as String
                        val logInfo = LogInfo(time, tag, level, content)
                        listInfo.add(logInfo)
                    }
                    val cb = Toolkit.getDefaultToolkit().systemClipboard
                    val trans = StringSelection(Utils.GSON.toJson(listInfo))
                    cb.setContents(trans, null)
                }
            }
        })
        menu.init()
    }
}