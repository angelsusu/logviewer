package com.shopee.logviewer.view

import com.fasterxml.jackson.databind.ObjectMapper
import com.shopee.logviewer.data.FilterInfo
import com.shopee.logviewer.data.ILogRepository
import com.shopee.logviewer.data.LogInfo
import com.shopee.logviewer.data.LogRepository
import com.shopee.logviewer.filter.CombineFilter
import com.shopee.logviewer.filter.IFilter
import com.shopee.logviewer.filter.MessageFilter
import com.shopee.logviewer.listener.DoubleClickListener
import com.shopee.logviewer.listener.LogMouseListener
import com.shopee.logviewer.util.*
import com.shopee.logviewer.util.Utils.toEnumLevel
import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetAdapter
import java.awt.dnd.DropTargetDropEvent
import java.awt.event.ActionListener
import java.awt.event.ItemListener
import java.io.File
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.event.ListSelectionListener
import javax.swing.table.DefaultTableModel


/**
 * author: beitingsu
 * created on: 2020/11/12
 *
 * 如果你跟我一样，running过程中一切换回IDE就crash，试试切换下JDK的版本：https://youtrack.jetbrains.com/issue/JBR-2159
 */
class LogViewerFrame: ILogRepository {

    private val uiFrame = fastLazy {
        buildFrame()
    }

    /** key: [FilterInfo.name] */
    private val uiScrollerJList = fastLazy {
        buildFilterScrollerJList()
    }

    private val mTagList = arrayListOf<String>()
    private val mFilterMap = hashMapOf<String, FilterInfo>()

    /** 更新List的接口统一通过 [ILogRepository] callback */
    private val logRepository = LogRepository(observer = this@LogViewerFrame)

    private lateinit var mContentTable: JTable
    private val mTableCellRender = LogTableCellRenderer()

    init {
        LogFilterStorage.init()
        LogFilterStorage.addListener(object : OnFilterLoadedListener {
            override fun onLoaded(filterInfoList: List<FilterInfo>) {
                filterInfoList.forEach { filterInfo ->
                    addFilterInfo(filterInfo)
                }

                uiScrollerJList.value.setListData(mTagList.toTypedArray())
            }
        })
    }

    fun showLogViewer() {
        supportFileDrag(uiFrame.value)
    }

    private fun buildFrame(): JFrame = JFrame("Android LogViewer").also { frame ->
        frame.layout = BorderLayout() //为Frame窗口设置布局为BorderLayout
        frame.add(JPanel().also { panel ->
            panel.layout = BorderLayout()
            panel.add(filterPanel, BorderLayout.NORTH)
            panel.add(getLogContentPanel(), BorderLayout.CENTER)
        }, BorderLayout.CENTER)

        frame.add(filterTagPanel, BorderLayout.WEST)
        frame.isVisible = true
        frame.pack()
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setBounds(300, 300,900, 662)
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
    private val filterPanel: JPanel
        get() = JPanel().also { jp ->
            jp.border = EmptyBorder(5, 0, 5, 5) //设置面板的边框
            jp.layout = BorderLayout(0, 0) //设置内容面板为边界布局

            jp.add(msgFilterField, BorderLayout.CENTER)
            jp.add(logLevelBox, BorderLayout.EAST)
        }

    private val msgFilterField: JTextField
        get() = JTextField(25).also { field ->
            field.document.addDocumentListener(MsgFilterEditListener(field))
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
            scrollPane.setViewportView(uiScrollerJList.value) //在滚动面板中显示列表
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
        val table = JTable()
        val scrollPane = JScrollPane(table) //创建滚动面板
        val tableModel = table.model as DefaultTableModel //获得表格模型
        tableModel.rowCount = 0 //清空表格中的数据
        val columnNames = arrayOf<Any>("Time", "Level", "Tag", "Content")
        tableModel.setColumnIdentifiers(arrayOf<Any>("Time", "Level", "Tag", "Content")) //设置表头
        table.rowHeight = 30
        table.model = tableModel //应用表格模型
        table.setShowGrid(true)
        table.gridColor = Color.lightGray
        contentPane.add(scrollPane, BorderLayout.CENTER) //将面板增加到边界布局中央
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
        val tableColumn = table.getColumn(columnNames[columnNames.size - 1])
        // 设置表格列的单元格渲染器
        tableColumn.cellRenderer = mTableCellRender
        initPopupCopyMenu(table)
        mContentTable = table
        return contentPane
    }

    private fun showFilterEditDialog(filterInfo: FilterInfo? = null) {
        FilterEditDialog(
                frame = uiFrame.value,
                clickListener = sFilterDialogClickListener,
                filterData = filterInfo
        ).showDialog()
    }

    /** 获取当前highlight的[FilterInfo] */
    private fun getHighlightFilter(): FilterInfo? {
        val filterName = uiScrollerJList.value.selectedValue ?: return null

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

            logRepository.addFilter(filter)
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
    private val sFilterSelectListener = ListSelectionListener {
        val filter = getHighlightFilter() ?: return@ListSelectionListener
        print("filter:\n$filter")
        logRepository.addFilter(filter)
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
        val filterName: String = uiScrollerJList.value.selectedValue ?: return@ActionListener

        print("FilterDeleteListener >>> filterName[$filterName]")
        if (!mTagList.remove(filterName)) {
            return@ActionListener
        }

        // 删除侧边栏UI Tag
        uiScrollerJList.value.setListData(mTagList.toTypedArray())

        val filterInfo = mFilterMap[filterName] ?: return@ActionListener

        // 删除本地存储
        LogFilterStorage.deleteFilterInfo(filterInfo)

        // 删除对应的FilterInfo，并重新开始过滤
        mFilterMap.remove(filterName)
        logRepository.removeFilter(filterInfo)
    }

    /** [ILogRepository] */
    override fun onFilterResult(lastFilter: IFilter?, result: List<LogInfo>?) {
        refreshLogTables(logInfo = result)

        //highlight 文本信息
        val highLightMsg = when (lastFilter) {
            is CombineFilter -> {
                lastFilter.filterInfo.msg
            }
            is MessageFilter -> {
                lastFilter.message
            }
            else -> {
                ""
            }
        }
        if (!highLightMsg.isNullOrEmpty()) {
            highlightMsg(highLightMsg)
        }
    }

    /** 添加新的Filter */
    private fun onFilterAddRecv(filterInfo: FilterInfo) {
        addFilterInfo(filterInfo)
        uiScrollerJList.value.setListData(mTagList.toTypedArray())

        // 以最新的filter进行过滤
        logRepository.addFilter(filterInfo)

        // 存储新的filterInfo
        LogFilterStorage.addFilterInfo(filterInfo)
    }

    private fun addFilterInfo(filterInfo: FilterInfo) {
        if (!mFilterMap.containsKey(filterInfo.name)) {
            mTagList.add(filterInfo.name)
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

        logInfo.forEach { info ->
            tableModel.addRow(arrayOf<Any>(info.time, info.strLevel, info.tag, info.content))
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
                        val row = rowCount[index]
                        val time = mContentTable.getValueAt(row, 0) as String
                        val strLevel = mContentTable.getValueAt(row, 1) as String
                        val tag = mContentTable.getValueAt(row, 2) as String
                        val content = mContentTable.getValueAt(row, 3) as String

                        listInfo.add(LogInfo(
                            time = time,
                            tag = tag,
                            enumLevel = strLevel.toEnumLevel(),
                            strLevel = strLevel,
                            content = content
                        ))
                    }
                    val cb = Toolkit.getDefaultToolkit().systemClipboard
                    val mapper = ObjectMapper()
                    val trans = StringSelection(mapper.writeValueAsString(listInfo))
                    cb.setContents(trans, null)
                }
            }
        })
        menu.init()
    }

    /** 自定义过滤条件清除后点击事件 */
    private val sTagMsgFilterClearListener = ActionListener {
        mTagList.clear()
        mFilterMap.clear()
        uiScrollerJList.value.setListData(mTagList.toTypedArray())
        LogFilterStorage.clear()

        logRepository.removeFilters()
    }

    /** 文本过滤条件EditText Listener */
    inner class MsgFilterEditListener(private val textField: JTextField): DocumentListener {

        override fun changedUpdate(e: DocumentEvent?) { logRepository.addFilter(textField.text) }

        override fun insertUpdate(e: DocumentEvent?) { logRepository.addFilter(textField.text) }

        override fun removeUpdate(e: DocumentEvent?) { logRepository.addFilter(textField.text) }
    }

    private fun highlightMsg(highlightMsg: String) {
        mTableCellRender.highlightMsg = highlightMsg
        mContentTable.revalidate()
    }
}