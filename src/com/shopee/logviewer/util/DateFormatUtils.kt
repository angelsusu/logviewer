package com.shopee.logviewer.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * author: beitingsu
 * created on: 2020/11/16
 */
object DateFormatUtils {

    const val DATE_FORMAT_YEAR_TO_SEC = "yyyy-MM-dd HH:mm:ss"

    private val dateFormatMap by lazy {
        hashMapOf(
                DATE_FORMAT_YEAR_TO_SEC to object : ThreadLocal<SimpleDateFormat>() {
                    override fun initialValue(): SimpleDateFormat {
                        return SimpleDateFormat(DATE_FORMAT_YEAR_TO_SEC)
                    }
                }
        )
    }

    @Throws(IllegalArgumentException::class)
    fun getDateToString(milSecond: Long, pattern: String): String {
        val date = Date(milSecond)
        return try {
            val format = dateFormatMap[pattern]?.get()
            format?.format(date) ?: ""
        } catch (e: Exception) {
            throw IllegalArgumentException(e.message)
        }
    }
}