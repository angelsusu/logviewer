package com.shopee.logviewer.view

import com.shopee.logviewer.view.MenuClickType.CLICK_TYPE_COPY
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.*

/**
 * author: beitingsu
 * created on: 2020/11/16
 * 右键弹出复制菜单
 */
class PopupCopyMenu(private val component: JTable, private val listener: PopupCopyMenuClickListener? = null) {

    fun init() {
        val jPopupMenuOne = JPopupMenu() //创建jPopupMenuOne对象
        val buttonGroupOne = ButtonGroup()
        //创建单选菜单项，并添加到ButtonGroup对象中
        val copyFile = JRadioButtonMenuItem("copy")
        buttonGroupOne.add(copyFile)
        //将copyFile添加到jPopupMenuOne中
        jPopupMenuOne.add(copyFile)
        //创建监听器对象
        val popupListener: MouseListener = PopupListener(jPopupMenuOne)
        copyFile.addActionListener {
            listener?.onClick(CLICK_TYPE_COPY)
        }
        component.addMouseListener(popupListener)
    }

    inner class PopupListener(var popupMenu: JPopupMenu) : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
            showPopupMenu(e)
        }

        override fun mouseReleased(e: MouseEvent) {
            showPopupMenu(e)
        }

        private fun showPopupMenu(e: MouseEvent) {
            if (e.isPopupTrigger && component.selectedRowCount > 0) {
                //如果当前事件与鼠标事件相关，则弹出菜单
                popupMenu.show(e.component, e.x, e.y)
            }
        }
    }
}

interface PopupCopyMenuClickListener {
    fun onClick(clickType: Int)
}

object MenuClickType {
    const val CLICK_TYPE_COPY = 1
}