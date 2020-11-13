package com.shopee.logviewer.listener

import java.awt.event.MouseEvent
import java.awt.event.MouseListener

/**
 * author: beitingsu
 * created on: 2020/11/13
 */
class LogMouseListener(private val doubleClickListener: DoubleClickListener? = null) : MouseListener {
    override fun mouseReleased(e: MouseEvent?) {

    }

    override fun mouseEntered(e: MouseEvent?) {

    }

    override fun mouseClicked(e: MouseEvent?) {
        if (e?.clickCount == 2) {
            doubleClickListener?.onDoubleClick()
        }
    }

    override fun mouseExited(e: MouseEvent?) {

    }

    override fun mousePressed(e: MouseEvent?) {

    }
}

interface DoubleClickListener {
    fun onDoubleClick()
}