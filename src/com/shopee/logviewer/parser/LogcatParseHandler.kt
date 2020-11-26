package com.shopee.logviewer.parser

import com.shopee.logviewer.data.LogInfo
import com.shopee.logviewer.util.DateFormatUtils
import com.shopee.logviewer.util.Utils
import com.shopee.logviewer.util.Utils.toEnumLevel
import com.shopee.logviewer.util.safelyRead
import java.io.BufferedReader
import java.io.File
import java.lang.Exception
import javax.swing.SwingUtilities

/**
 * author: beitingsu
 * created on: 2020/11/20
 * logcat日志解析
 * 日志格式为：2020-11-13 10:46:33.322 993-1414/? D/AES: AEEIOCTL_RT_MON_Kick IOCTL,cmd= 2147774474, lParam=300.
 */
class LogcatParseHandler : ILogParseHandler {

    override fun parse(logFile: File, parseFinishListener: ParseFinishListener?) {
        val listInfo = arrayListOf<LogInfo>()
        Thread().run {
            logFile.safelyRead { reader: BufferedReader ->
                while (true) {
                    val logStr = reader.readLine()
                    if (logStr == null) {
                        break
                    } else {
                        parseToLogInfo(logStr)?.let {
                            listInfo.add(it)
                        }
                    }
                }
            }
        }
        //回调UI
        SwingUtilities.invokeLater {
            parseFinishListener?.onParseFinish(listInfo)
        }
    }

    private fun parseToLogInfo(logStr: String): LogInfo? {
        return try {
            val timeLength = DateFormatUtils.DATE_FORMAT_YEAR_TO_MILL.length
            val firstBlankIndex = logStr.indexOf(" ", timeLength + 1)
            val firstColonIndex = logStr.indexOf(":", firstBlankIndex + 1)

            val time = logStr.substring(0, timeLength)
            if (DateFormatUtils.getFormatTime(time, DateFormatUtils.DATE_FORMAT_YEAR_TO_MILL) == 0L) {
                return null
            }
            val tag = logStr.substring(firstBlankIndex + 3, firstColonIndex)
            val content = logStr.substring(firstColonIndex + 1)
            val enumLevel = logStr.substring(firstBlankIndex + 1, firstBlankIndex + 2).toEnumLevel()
            val strLevel = Utils.logLevelConvertMap.getOrDefault(enumLevel, Utils.DEFAULT_LOG_STR_LEVEL)

            LogInfo(time = time, tag = tag, strLevel = strLevel, enumLevel = enumLevel, content = content)
        } catch (exception: Exception) {
            null
        }
    }
}