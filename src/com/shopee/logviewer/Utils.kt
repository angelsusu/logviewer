package com.shopee.logviewer

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

    val logLevelList = arrayListOf("Verbose", "Debug", "Info", "Warning", "Error")
}

object LogEncrypt {
    const val ENCRYPT_KEY = "0123456789012345"
    const val ENCRYPT_IV = "0123456789012345"
}