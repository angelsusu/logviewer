package com.shopee.logviewer.parser

import com.shopee.logviewer.data.EnumLogLv
import com.shopee.logviewer.data.LogInfo
import com.shopee.logviewer.util.DateFormatUtils
import com.shopee.logviewer.util.Utils
import com.shopee.logviewer.util.Utils.toEnumLevel
import com.shopee.logviewer.util.safelyRead
import java.io.BufferedReader
import java.io.File
import javax.swing.SwingUtilities

/**
 * author: beitingsu
 * created on: 2020/11/20
 * logcat日志解析
 * 日志格式可能为：
 * <1> 2020-11-13 10:46:33.322 993-1414/? D/AES: AEEIOCTL_RT_MON_Kick IOCTL,cmd= 2147774474, lParam=300.
 * <2> 2020-11-13 10:46:33.322 D/AES: AEEIOCTL_RT_MON_Kick IOCTL,cmd= 2147774474, lParam=300.
 * <3> 11-13 10:46:33.322 993-1414/? D/AES: AEEIOCTL_RT_MON_Kick IOCTL,cmd= 2147774474, lParam=300.
 * <4> 11-13 10:46:33.322 D/AES: AEEIOCTL_RT_MON_Kick IOCTL,cmd= 2147774474, lParam=300.
 * <5> 2020-11-27 14:54:04.402 26484-26530/? I/VivoPush.RequestParams: (26484)initConfig error
                java.io.FileNotFoundException: /storage/emulated/0/pushconfig.txt (No such file or directory)
                at java.io.FileInputStream.open0(Native Method)
                at java.io.FileInputStream.open(FileInputStream.java:200)
 * <6> 2020-11-27 14:54:04.402 I/VivoPush.RequestParams: (26484)initConfig error
            java.io.FileNotFoundException: /storage/emulated/0/pushconfig.txt (No such file or directory)
            at java.io.FileInputStream.open0(Native Method)
            at java.io.FileInputStream.open(FileInputStream.java:200)
   <7> 2020-11-13 10:46:33.322 993-1414 D/AES: AEEIOCTL_RT_MON_Kick IOCTL,cmd= 2147774474, lParam=300.
   <8> 2020-11-13 10:46:33.322 com.shopee.driver D/AES: AEEIOCTL_RT_MON_Kick IOCTL,cmd= 2147774474, lParam=300.
 */
class LogcatParseHandler : ILogParseHandler {

    private val mLogInfoParser = arrayListOf(
            //支持格式<1><5>
            LogcatParserWithFullInfo(DateFormatUtils.DATE_FORMAT_YEAR_TO_MILL),

            //支持格式<3><5>
            LogcatParserWithFullInfo(DateFormatUtils.DATE_FORMAT_MONTH_TO_MILL),

            //支持格式<7>
            LogcatParserWithoutPackageName(DateFormatUtils.DATE_FORMAT_YEAR_TO_MILL),

            //支持格式<7>
            LogcatParserWithoutPackageName(DateFormatUtils.DATE_FORMAT_MONTH_TO_MILL),

            //支持格式<8>
            LogcatParserWithoutPid(DateFormatUtils.DATE_FORMAT_YEAR_TO_MILL),

            //支持格式<8>
            LogcatParserWithoutPid(DateFormatUtils.DATE_FORMAT_MONTH_TO_MILL),

            //支持格式<2><6>
            LogcatParserWithoutProcess(DateFormatUtils.DATE_FORMAT_YEAR_TO_MILL),

            //支持格式<4><6>
            LogcatParserWithoutProcess(DateFormatUtils.DATE_FORMAT_MONTH_TO_MILL)

    )

    override fun parse(logFile: File, parseFinishListener: ParseFinishListener?) {
        val listInfo = arrayListOf<LogInfo>()
        Thread().run {
            logFile.safelyRead { reader: BufferedReader ->
                while (true) {
                    val logStr = reader.readLine()
                    if (logStr == null) {
                        break
                    } else {
                        listInfo.add(parseToLogInfo(logStr))
                    }
                }
            }
        }
        //回调UI
        SwingUtilities.invokeLater {
            parseFinishListener?.onParseFinish(listInfo)
        }
    }

    private fun parseToLogInfo(logStr: String): LogInfo {
        mLogInfoParser.forEach { parser ->
            val logInfo = parser.parse(logStr)
            if (logInfo != null) {
                return logInfo
            }
        }
        //返回默认结构
        return LogInfo("", "", "", "", Utils.DEFAULT_LOG_STR_LEVEL, EnumLogLv.V, logStr)
    }
}

//time 993-1414/? D/AES: AEEIOCTL_RT_MON_Kick IOCTL,cmd= 2147774474, lParam=300.
class LogcatParserWithFullInfo(private val dateFormat: String) : ILogInfoParser {
    override fun parse(logStr: String): LogInfo? {
        return try {
            val timeLength = dateFormat.length
            val firstBlankIndex = logStr.indexOf(" ", timeLength + 1)
            val firstColonIndex = logStr.indexOf(":", firstBlankIndex + 1)

            val time = logStr.substring(0, timeLength)
            if (DateFormatUtils.getFormatTime(time, dateFormat) == 0L) {
                return null
            }
            val level = logStr.substring(timeLength + 1, timeLength + 2)
            if (Utils.logLevelMap.containsKey(level)) {
                return null
            }
            val tag = logStr.substring(firstBlankIndex + 3, firstColonIndex)
            val content = logStr.substring(firstColonIndex + 1)
            val enumLevel = logStr.substring(firstBlankIndex + 1, firstBlankIndex + 2).toEnumLevel()
            val strLevel = Utils.logLevelConvertMap.getOrDefault(enumLevel, Utils.DEFAULT_LOG_STR_LEVEL)

            val idIndex = logStr.indexOf("-", timeLength + 1)
            val pid = if (idIndex > firstBlankIndex) {
                ""
            } else {
                val slash = logStr.indexOf("/", idIndex)
                logStr.substring(timeLength + 1, idIndex) + "-" +  logStr.substring(slash + 1, firstBlankIndex)
            }
            val tid = if (idIndex > firstBlankIndex) {
                ""
            } else {
                val slash = logStr.indexOf("/", idIndex)
                logStr.substring(idIndex + 1, slash)
            }
            LogInfo(time = time, tag = tag, pid = pid, tid = tid, strLevel = strLevel, enumLevel = enumLevel, content = content)
        } catch (exception: Exception) {
            null
        }
    }
}

//time D/AES: AEEIOCTL_RT_MON_Kick IOCTL,cmd= 2147774474, lParam=300.
class LogcatParserWithoutProcess(private val dateFormat: String) : ILogInfoParser {
    override fun parse(logStr: String): LogInfo? {
        return try {
            val timeLength = dateFormat.length
            val firstSymbolIndex = logStr.indexOf("/", timeLength + 1)
            val firstColonIndex = logStr.indexOf(":", firstSymbolIndex + 1)

            val time = logStr.substring(0, timeLength)
            if (DateFormatUtils.getFormatTime(time, dateFormat) == 0L) {
                return null
            }
            val tag = logStr.substring(firstSymbolIndex + 1, firstColonIndex)
            val content = logStr.substring(firstColonIndex + 1)
            val enumLevel = logStr.substring(firstSymbolIndex - 1, firstSymbolIndex).toEnumLevel()
            val strLevel = Utils.logLevelConvertMap.getOrDefault(enumLevel, Utils.DEFAULT_LOG_STR_LEVEL)

            LogInfo(time = time, tag = tag, strLevel = strLevel, enumLevel = enumLevel, content = content)
        } catch (exception: Exception) {
            null
        }
    }
}


//time 993-1414 D/AES: AEEIOCTL_RT_MON_Kick IOCTL,cmd= 2147774474, lParam=300.
class LogcatParserWithoutPackageName(private val dateFormat: String) : ILogInfoParser {
    override fun parse(logStr: String): LogInfo? {
        return try {
            val timeLength = dateFormat.length
            val firstBlankIndex = logStr.indexOf(" ", timeLength + 1)
            val firstColonIndex = logStr.indexOf(":", firstBlankIndex + 1)

            val time = logStr.substring(0, timeLength)
            if (DateFormatUtils.getFormatTime(time, dateFormat) == 0L) {
                return null
            }
            val level = logStr.substring(timeLength + 1, timeLength + 2)
            if (Utils.logLevelMap.containsKey(level)) {
                return null
            }
            val tag = logStr.substring(firstBlankIndex + 3, firstColonIndex)
            val content = logStr.substring(firstColonIndex + 1)
            val enumLevel = logStr.substring(firstBlankIndex + 1, firstBlankIndex + 2).toEnumLevel()
            val strLevel = Utils.logLevelConvertMap.getOrDefault(enumLevel, Utils.DEFAULT_LOG_STR_LEVEL)
            val idIndex = logStr.indexOf("-", timeLength + 1)
            val pid = if (idIndex > firstBlankIndex) {
                ""
            } else {
                logStr.substring(timeLength + 1, idIndex)
            }
            val tid = if (idIndex > firstBlankIndex) {
                ""
            } else {
                logStr.substring(idIndex + 1, firstBlankIndex)
            }
            LogInfo(time = time, tag = tag, pid = pid, tid = tid, strLevel = strLevel, enumLevel = enumLevel, content = content)
        } catch (exception: Exception) {
            null
        }
    }
}

//time packageName D/AES: AEEIOCTL_RT_MON_Kick IOCTL,cmd= 2147774474, lParam=300.
class LogcatParserWithoutPid(private val dateFormat: String) : ILogInfoParser {
    override fun parse(logStr: String): LogInfo? {
        return try {
            val timeLength = dateFormat.length
            val firstBlankIndex = logStr.indexOf(" ", timeLength + 1)
            val firstColonIndex = logStr.indexOf(":", firstBlankIndex + 1)

            val time = logStr.substring(0, timeLength)
            if (DateFormatUtils.getFormatTime(time, dateFormat) == 0L) {
                return null
            }
            val level = logStr.substring(timeLength + 1, timeLength + 2)
            if (Utils.logLevelMap.containsKey(level)) {
                return null
            }
            val tag = logStr.substring(firstBlankIndex + 3, firstColonIndex)
            val content = logStr.substring(firstColonIndex + 1)
            val enumLevel = logStr.substring(firstBlankIndex + 1, firstBlankIndex + 2).toEnumLevel()
            val strLevel = Utils.logLevelConvertMap.getOrDefault(enumLevel, Utils.DEFAULT_LOG_STR_LEVEL)
            val pid = logStr.substring(timeLength + 1, firstBlankIndex)
            LogInfo(time = time, tag = tag, pid = pid, tid = "", strLevel = strLevel, enumLevel = enumLevel, content = content)
        } catch (exception: Exception) {
            null
        }
    }
}

interface ILogInfoParser {
    fun parse(logStr: String): LogInfo?
}