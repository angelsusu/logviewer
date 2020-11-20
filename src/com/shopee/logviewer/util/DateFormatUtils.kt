package com.shopee.logviewer.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * author: beitingsu
 * created on: 2020/11/16
 */
object DateFormatUtils {

    const val DATE_FORMAT_YEAR_TO_MILL = "yyyy-MM-dd HH:mm:ss.SSS"

    private val dateFormatMap by lazy {
        hashMapOf(
                DATE_FORMAT_YEAR_TO_MILL to object : ThreadLocal<SimpleDateFormat>() {
                    override fun initialValue(): SimpleDateFormat {
                        return SimpleDateFormat(DATE_FORMAT_YEAR_TO_MILL)
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