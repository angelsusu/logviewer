package com.shopee.logviewer.util

import com.google.gson.Gson

/**
 * author: beitingsu
 * created on: 2020/11/12
 */
object Utils {
    val logLevelMap = hashMapOf(
            "Verbose" to 2,
            "Debug" to 3,
            "Info" to 4,
            "Warning" to 5,
            "Error" to 6
    )

    val logLevelConvertMap = hashMapOf(
            2 to "Verbose",
            3 to "Debug",
            4 to "Info",
            5 to "Warning",
            6 to "Error"
    )

    val logLevelList = arrayListOf("Verbose", "Debug", "Info", "Warning", "Error")

    val GSON = Gson()
}

object LogEncrypt {
    const val ENCRYPT_KEY = "0123456789012345"
    const val ENCRYPT_IV = "0123456789012345"
}