package com.shopee.logviewer.util

import com.google.gson.Gson
import com.shopee.logviewer.data.EnumLogLv
import java.awt.Color
import javax.swing.JTable

/**
 * author: beitingsu
 * created on: 2020/11/12
 */
object Utils {

    private const val V = "Verbose"
    private const val D = "Debug"
    private const val I = "Info"
    private const val W = "Warning"
    private const val E = "Error"
    private const val A = "Assert"

    private const val LOGCAT_V = "V"
    private const val LOGCAT_D = "D"
    private const val LOGCAT_I = "I"
    private const val LOGCAT_W = "W"
    private const val LOGCAT_E = "E"
    private const val LOGCAT_A = "A"

    const val DEFAULT_LOG_STR_LEVEL = V

    val logLevelMap = mapOf(
            V to EnumLogLv.V,
            D to EnumLogLv.D,
            I to EnumLogLv.I,
            W to EnumLogLv.W,
            E to EnumLogLv.E,
            A to EnumLogLv.A,
            LOGCAT_V to EnumLogLv.V,
            LOGCAT_D to EnumLogLv.D,
            LOGCAT_I to EnumLogLv.I,
            LOGCAT_W to EnumLogLv.W,
            LOGCAT_E to EnumLogLv.E,
            LOGCAT_A to EnumLogLv.A
    )

    val logLevelConvertMap = mapOf(
            EnumLogLv.V to V,
            EnumLogLv.D to D,
            EnumLogLv.I to I,
            EnumLogLv.W to W,
            EnumLogLv.E to E,
            EnumLogLv.A to A
    )

    val LOG_STR_LEVELS = arrayOf(V, D, I, W, E, A)
    private val LOG_ENUM_LEVELS = arrayOf(EnumLogLv.V, EnumLogLv.D, EnumLogLv.I, EnumLogLv.W, EnumLogLv.E, EnumLogLv.A)

    val GSON = Gson()

    fun String.toEnumLevel(): EnumLogLv {
        return logLevelMap.getOrDefault(this, EnumLogLv.V)
    }

    fun Int.toEnumLevel(): EnumLogLv {
        return LOG_ENUM_LEVELS.firstOrNull {
            it.value == this@toEnumLevel
        } ?: EnumLogLv.V
    }

    val levelTextColorMap = mapOf(
            V to Color(187,187,187),
            D to Color(0,112,187),
            I to Color(72,187,49),
            W to Color(187,187,35),
            E to Color(255,0,6),
            A to Color(255,107,104)
    )

    //调整行宽
    fun adjustColumnWidth(table: JTable) {
        val header = table.tableHeader
        val rowCount = table.rowCount
        val columns = table.columnModel.columns
        while (columns.hasMoreElements()) {
            val column = columns.nextElement()
            val col = header.columnModel.getColumnIndex(column.identifier)
            var width = table.tableHeader.defaultRenderer
                    .getTableCellRendererComponent(table,column.identifier, false, false, -1, col).preferredSize.getWidth()
            for (row in 0 until rowCount) {
                val preferedWidth = table.getCellRenderer(row, col)
                        .getTableCellRendererComponent(table,
                                table.getValueAt(row, col), false, false, row,col).preferredSize.getWidth()
                width = width.coerceAtLeast(preferedWidth)
            }
            header.resizingColumn = column // 此行很重要
            column.width = (width + table.intercellSpacing.width).toInt()
        }
    }
}

object LogEncrypt {
    const val ENCRYPT_KEY = "0123456789012345"
    const val ENCRYPT_IV = "0123456789012345"
}