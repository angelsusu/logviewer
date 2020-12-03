package com.shopee.logviewer.view

import com.shopee.logviewer.data.FilterInfo
import com.shopee.logviewer.data.ILogRepository
import com.shopee.logviewer.data.LogInfo
import com.shopee.logviewer.data.LogRepository
import com.shopee.logviewer.filter.CombineFilter
import com.shopee.logviewer.filter.IFilter
import com.shopee.logviewer.filter.MessageFilter
import com.shopee.logviewer.listener.DoubleClickListener
import com.shopee.logviewer.listener.LogKeyListener
import com.shopee.logviewer.listener.LogMouseListener
import com.shopee.logviewer.listener.OnKeyClickListener
import com.shopee.logviewer.parser.ParseFinishListener
import com.shopee.logviewer.util.LogFilterStorage
import com.shopee.logviewer.util.OnFilterLoadedListener
import com.shopee.logviewer.util.Utils
import com.shopee.logviewer.util.Utils.toEnumLevel
import com.shopee.logviewer.util.fastLazy
import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetAdapter
import java.awt.dnd.DropTargetDropEvent
import java.awt.event.ActionListener
import java.awt.event.ItemListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.io.File
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.event.ListSelectionListener
import javax.swing.table.DefaultTableModel


/**
 * author: beitingsu
 * created on: 2020/11/12
 *
 * 如果你跟我一样，running过程中一切换回IDE就crash，试试切换下JDK的版本：https://youtrack.jetbrains.com/issue/JBR-2159
 */
class LogViewerFrame: ILogRepository {

    companion object {
        private const val NO_FILTER_NAME = "All Msg No Filter"
    }

    private val uiFrame by fastLazy {
        buildFrame()
    }

    /** key: [FilterInfo.name] */
    private val uiScrollerJList by fastLazy {
        buildFilterScrollerJList()
    }

    private val mTagList = arrayListOf<String>()
    private val mFilterMap = hashMapOf<String, FilterInfo>()

    /** 更新List的接口统一通过 [ILogRepository] callback */
    private val logRepository = LogRepository(observer = this@LogViewerFrame)
    /** Message 和 Regex的复合过滤器 */
    private val compoundMsgWidget = CompoundMessageWidget(funcAddFilter = logRepository::addFilter)

    private lateinit var mContentTable: JTable
    private val mTableCellRender = LogTableCellRenderer(4)

    private val mOnKeyClickListener = object : OnKeyClickListener {
        override fun onClickSingleLineKey() {
            runCatching {
                val lineNumber = JOptionPane.showInputDialog(uiFrame,"Input Line Number").toInt() - 1
                if (lineNumber < mContentTable.rowCount) {
                    //设置选中的行
                    mContentTable.setRowSelectionInterval(lineNumber, lineNumber)
                    //跳到指定的行
                    val rect = mContentTable.getCellRect(lineNumber, 0, true)
                    mContentTable.scrollRectToVisible(rect)
                }
            }
        }
    }

    fun showLogViewer() {
        supportFileDrag(uiFrame)
        AlertDialog.sFrame = uiFrame

        // restore latest filter tag
        LogFilterStorage.init(listener = object : OnFilterLoadedListener {
            // @UiThread
            override fun onLoaded(filterInfoList: List<FilterInfo>) {
                print("LogFilterStorage.init callback with filterInfoList[${filterInfoList.size}]")

                mTagList.add(0, NO_FILTER_NAME)

                filterInfoList.forEach { filterInfo ->
                    addFilterInfo(filterInfo)
                }

                uiScrollerJList.setListData(mTagList.toTypedArray())
            }

            // @UiThread
            override fun onFailure(e: Throwable?) {
                print("LogFilterStorage.init callback with Throwable:$e")
                AlertDialog.showAlert("Fail to restore Filter tags!")
                mTagList.add(0, NO_FILTER_NAME)
                uiScrollerJList.setListData(mTagList.toTypedArray())
            }
        })
        uiFrame.addKeyListener(LogKeyListener(mOnKeyClickListener))
    }

    private fun buildFrame(): JFrame = JFrame("Android LogViewer").also { frame ->
        frame.layout = BorderLayout() //为Frame窗口设置布局为BorderLayout
        frame.add(JPanel().also { panel ->
            panel.layout = BorderLayout()
            panel.add(filterPanel, BorderLayout.NORTH)
            panel.add(getLogContentPanel(), BorderLayout.CENTER)
        }, BorderLayout.CENTER)
        frame.add(filterTagPanel, BorderLayout.WEST)
        frame.isFocusable = true
        frame.isVisible = true
        frame.pack()
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setBounds(300, 300,900, 662)
        frame.requestFocusInWindow()
    }

    /** 左侧Filter Scroller Content Adapter */
    private fun buildFilterScrollerJList(): JList<String> = JList<String>().also { list ->
        // 限制只能选择一个元素
        list.selectionMode = ListSelectionModel.SINGLE_SELECTION
        // 选择监听器
        list.addListSelectionListener(sFilterSelectListener)
        // 双击查看详情
        list.addMouseListener(LogMouseListener(object : DoubleClickListener {
            override fun onDoubleClick() {
                val selectItem = list.selectedValue ?: return
                val filterInfo = mFilterMap[selectItem] ?: return
                showFilterEditDialog(filterInfo)
            }
        }))

        list.setListData(mTagList.toTypedArray())
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
                            val parseHandler = LogParseHandlerFactory.getParseHandler(file.name)
                            parseHandler?.parse(file, sLogParserListener)
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
    private val filterPanel: JPanel
        get() = JPanel().also { jp ->
            jp.border = EmptyBorder(0, 0, 0, 0) //设置面板的边框
            jp.layout = FlowLayout(FlowLayout.CENTER)

            jp.add(compoundMsgWidget.msgFilterField)
            jp.add(compoundMsgWidget.regexCheckbox)
            jp.add(logLevelBox)
        }

    /** 日志等级过滤器 */
    private val logLevelBox: JComboBox<String>
        get() = JComboBox<String>().also { box ->
            Utils.LOG_STR_LEVELS.forEach { item ->
                box.addItem(item)
            }

            box.addItemListener(sLogLevelSelector)
        }

    /**
     * 过滤器对应的布局
     */
    private val filterTagPanel: JPanel
        get() = JPanel().apply {
            border = EmptyBorder(0, 5, 5, 5) //设置面板的边框
            layout = BorderLayout(0, 0) //设置内容面板为边界布局
            add(filterTagBtnPanel, BorderLayout.NORTH)
            add(filterTagScroller, BorderLayout.CENTER) // 创建滚动面板，将面板增加到边界布局中央
        }

    /** 左侧Filter Scroller列表 */
    private val filterTagScroller: JScrollPane
        get() = JScrollPane().also { scrollPane ->
            scrollPane.setViewportView(uiScrollerJList) //在滚动面板中显示列表
            uiScrollerJList.addKeyListener(LogKeyListener(mOnKeyClickListener))
        }

    /** 左上角Filter操作区域 */
    private val filterTagBtnPanel: JPanel
        get() = JPanel().also { jp ->
            jp.add(buildFilterTagProcessBtn(btnName = "Add", actionListener = ActionListener { showFilterEditDialog() }))
            jp.add(buildFilterTagProcessBtn(btnName = "Delete", actionListener = sTagMsgFilterDeleteListener))
            jp.add(buildFilterTagProcessBtn(btnName = "Clear", actionListener = sTagMsgFilterClearListener))
        }

    private fun buildFilterTagProcessBtn(btnName: String, actionListener: ActionListener?): JButton {
        return JButton(btnName).also { btn ->
            btn.addActionListener(actionListener)
        }
    }

    /**
     * 日志内容对应布局
     */
    private fun getLogContentPanel(): JPanel {
        val contentPane = JPanel() //创建内容面板
        contentPane.border = EmptyBorder(5, 5, 5, 5) //设置面板的边框
        contentPane.layout = BorderLayout(0, 0) //设置内容面板为边界布局
        val model = object : DefaultTableModel() {
            override fun isCellEditable(row: Int, column: Int): Boolean {
                //设置为不可编辑
                return false
            }
        }
        val table = JTable(model)
        val scrollPane = JScrollPane(table) //创建滚动面板
        val tableModel = table.model as DefaultTableModel //获得表格模型
        tableModel.rowCount = 0 //清空表格中的数据
        val columnNames = arrayOf<Any>("Index", "Time", "pid", "tid", "Level", "Tag", "Content")
        tableModel.setColumnIdentifiers(columnNames) //设置表头
        table.rowHeight = 30
        table.model = tableModel //应用表格模型
        table.setShowGrid(true)
        table.gridColor = Color.lightGray
        contentPane.add(scrollPane, BorderLayout.CENTER) //将面板增加到边界布局中央
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
        // 设置表格列的单元格渲染器
        for (index in columnNames.indices) {
            val tableColumn = table.getColumn(columnNames[index])
            tableColumn.cellRenderer = mTableCellRender
        }
        Utils.adjustColumnWidth(table)
        initPopupCopyMenu(table)
        table.addKeyListener(LogKeyListener(mOnKeyClickListener))
        table.addMouseListener(LogTableMouseListener(jTable = table))
        mContentTable = table
        return contentPane
    }

    private fun showFilterEditDialog(filterInfo: FilterInfo? = null) {
        FilterEditDialog(
                frame = uiFrame,
                clickListener = sFilterDialogClickListener,
                filterData = filterInfo
        ).showDialog()
    }

    /** 获取当前highlight的[FilterInfo] */
    private fun getHighlightFilter(): FilterInfo? {
        val filterName = uiScrollerJList.selectedValue ?: return null

        if (filterName.isBlank() || filterName == NO_FILTER_NAME) return null

        return mFilterMap[filterName]
    }

    /** Log解密回调监听器 */
    private val sLogParserListener = object : ParseFinishListener {
        override fun onParseFinish(logInfo: List<LogInfo>) {
            // 更新元数据
            logRepository.updateMeta(logInfo)

            val filter = getHighlightFilter() ?: run {
                refreshLogTables(logInfo)
                return
            }

            logRepository.addFilter(filter)
        }
    }

    /** Filter添加Dialog监听器 */
    private val sFilterDialogClickListener = object : ClickListener {
        override fun onClick(clickType: Int, filterInfo: FilterInfo?) {
            when {
                (clickType == ClickType.CLICK_TYPE_OK && null != filterInfo) -> onFilterAddRecv(filterInfo)
            }
        }
    }

    /** 左侧Filter栏点击触发器 */
    private val sFilterSelectListener = ListSelectionListener {
        val filterInfo = getHighlightFilter()

        filterInfo ?: run {
            logRepository.removeFilters()
            return@ListSelectionListener
        }

        print("filter:\n$filterInfo")
        logRepository.addFilter(filterInfo)
    }

    /** 日志等级RadioButton */
    private val sLogLevelSelector = ItemListener { event ->
        val selectLevel = event.item as? String
        print("new select level[$selectLevel]")

        if (!selectLevel.isNullOrEmpty()) {
            logRepository.addFilter(selectLevel.toEnumLevel())
        }
    }

    /** 自定义过滤条件删除后点击事件 */
    private val sTagMsgFilterDeleteListener = ActionListener {
        val filterName: String = uiScrollerJList.selectedValue ?: return@ActionListener

        if (filterName == NO_FILTER_NAME) {
            return@ActionListener
        }

        print("FilterDeleteListener >>> filterName[$filterName]")
        if (!mTagList.remove(filterName)) {
            return@ActionListener
        }

        // 删除侧边栏UI Tag
        uiScrollerJList.setListData(mTagList.toTypedArray())

        val filterInfo = mFilterMap[filterName] ?: return@ActionListener

        // 删除本地存储
        LogFilterStorage.deleteFilterInfo(filterInfo)

        // 删除对应的FilterInfo，并重新开始过滤
        mFilterMap.remove(filterName)
        logRepository.removeFilter(filterInfo)
    }

    inner class LogTableMouseListener(
        private val jTable: JTable
    ) : MouseListener {

        override fun mousePressed(evt: MouseEvent?) {
            evt ?: return

            if (2 == evt.clickCount) {
                val rowIndex = jTable.rowAtPoint(evt.point)
                val logInfo = logRepository.getFromFilterLogs(rowIndex)

                if (-1 != rowIndex && null != logInfo) {
                    LogDetailDialog.showLogDetail(frame = uiFrame, logInfo = logInfo)
                } else {
                    print("invalid index[$rowIndex] or logInfo.isNull[${null == logInfo}]")
                }
            }
        }

        override fun mouseReleased(e: MouseEvent?) {}

        override fun mouseEntered(e: MouseEvent?) {}

        override fun mouseClicked(e: MouseEvent?) {}

        override fun mouseExited(e: MouseEvent?) {}
    }

    /** [ILogRepository] */
    override fun onFilterResult(lastFilter: IFilter?, result: List<LogInfo>?) {
        refreshLogTables(logInfo = result)

        //highlight 文本信息
        val msg = when (lastFilter) {
            is CombineFilter -> lastFilter.filterInfo.msg
            is MessageFilter -> lastFilter.message
            else -> ""
        } ?: return

        highlightMsg(msg)
    }

    /** 添加新的Filter */
    private fun onFilterAddRecv(filterInfo: FilterInfo) {
        addFilterInfo(filterInfo)
        uiScrollerJList.setListData(mTagList.toTypedArray())
        uiScrollerJList.selectedIndex = 1

        // 以最新的filter进行过滤
        logRepository.addFilter(filterInfo)

        // 存储新的filterInfo
        LogFilterStorage.addFilterInfo(filterInfo)
    }

    private fun addFilterInfo(filterInfo: FilterInfo) {
        if (!mFilterMap.containsKey(filterInfo.name)) {
            //首位为no filter
            mTagList.add(1, filterInfo.name)
        }

        mFilterMap[filterInfo.name] = filterInfo
        logRepository.addFilter(filterInfo)
    }

    /** 统一更新Filtered Log的入口 */
    private fun refreshLogTables(logInfo: List<LogInfo>?) {
        if (!::mContentTable.isInitialized) {
            return
        }
        val tableModel = mContentTable.model as DefaultTableModel
        tableModel.rowCount = 0

        logInfo ?: return

        for (index in logInfo.indices) {
            val info = logInfo[index]
            tableModel.addRow(arrayOf(index + 1, info.time, info.pid, info.tid, info.strLevel, info.tag, info.content))
        }
    }

    private fun initPopupCopyMenu(component: JTable) {
        val menu = PopupCopyMenu(component, object : PopupCopyMenuClickListener {
            override fun onClick(clickType: Int) {
                if (clickType == MenuClickType.CLICK_TYPE_COPY) {
                    //copy选中数据
                    val rowCount = mContentTable.selectedRows
                    val strBuilder= StringBuilder()
                    for (index in rowCount.indices) {
                        val row = rowCount[index]
                        val time = mContentTable.getValueAt(row, 1) as String
                        val pid = mContentTable.getValueAt(row, 2) as String
                        val tid = mContentTable.getValueAt(row, 3) as String
                        val strLevel = mContentTable.getValueAt(row, 4) as String
                        val tag = mContentTable.getValueAt(row, 5) as String
                        val content = mContentTable.getValueAt(row, 6) as String

                        strBuilder.append(time)
                                .append(":")
                                .append(pid)
                                .append(":")
                                .append(tid)
                                .append(":")
                                .append(strLevel)
                                .append(":")
                                .append(tag)
                                .append(":")
                                .append(content)
                                .append('\n')
                    }
                    val cb = Toolkit.getDefaultToolkit().systemClipboard
                    val trans = StringSelection(strBuilder.toString())
                    cb.setContents(trans, null)
                }
            }
        })
        menu.init()
    }

    /** 自定义过滤条件清除后点击事件 */
    private val sTagMsgFilterClearListener = ActionListener {
        mTagList.clear()
        mTagList.add(NO_FILTER_NAME)
        mFilterMap.clear()
        uiScrollerJList.setListData(mTagList.toTypedArray())
        LogFilterStorage.clear()

        logRepository.removeFilters()
    }

    private fun highlightMsg(highlightMsg: String) {
        mTableCellRender.highlightMsg = highlightMsg
        mContentTable.revalidate()
    }
}