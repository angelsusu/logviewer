package com.shopee.logviewer.listener

import java.awt.event.KeyEvent
import java.awt.event.KeyListener

/**
 * author: beitingsu
 * created on: 2020/11/18
 */
class LogKeyListener(private val keyClickListener: OnKeyClickListener? = null) : KeyListener {

    private var mLastKeyCode = 0

    override fun keyTyped(e: KeyEvent?) {

    }

    override fun keyPressed(e: KeyEvent?) {
        //cmd+b
        if (mLastKeyCode == KeyEvent.VK_META && e?.keyCode == KeyEvent.VK_B) {
            keyClickListener?.onClickSingleLineKey()
        } else {
            mLastKeyCode = e?.keyCode ?: 0
        }
    }

    override fun keyReleased(e: KeyEvent?) {

    }
}

interface OnKeyClickListener {
    //单行定位
    fun onClickSingleLineKey()
}