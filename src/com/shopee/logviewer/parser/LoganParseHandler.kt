package com.shopee.logviewer.parser

import com.shopee.logviewer.data.LogInfo
import com.shopee.logviewer.data.ParseLoganInfo
import com.shopee.logviewer.util.*
import com.shopee.logviewer.util.Utils.GSON
import com.shopee.logviewer.util.Utils.toEnumLevel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.swing.SwingUtilities

/**
 * author: beitingsu
 * created on: 2020/11/16
 * logan日志解密
 * 解密后的日志格式为：{"c":"|LoganService|LoganTest| logger test in main process:0","f":3,"l":1590118408849,"n":"Thread-6","i":10047,"m":false}
 */
class LoganParseHandler : ILogParseHandler {

    private val mParser = LoganParser(LogEncrypt.ENCRYPT_KEY.toByteArray(), LogEncrypt.ENCRYPT_IV.toByteArray())

    override fun parse(logFile: File, parseFinishListener: ParseFinishListener?) {
        Thread().run {
            //已经是解密后的文件
            if (logFile.name.substring(logFile.name.lastIndexOf(".") + 1) == "log") {
                onParseFinish(logFile.absolutePath, parseFinishListener)
            } else {
                val outFileName = logFile.absolutePath + "_parse.txt"
                safelyCreate { FileInputStream(File(logFile.absolutePath)) }?.safelyUse { input ->
                    safelyCreate { FileOutputStream(File(outFileName)) }?.safelyUse { output ->
                        mParser.parse(input, output)
                        onParseFinish(outFileName, parseFinishListener)
                    }
                }
            }
        }
    }

    private fun onParseFinish(outFileName: String, parseFinishListener: ParseFinishListener?) {
        //解析为结构化数据
        val listInfo = arrayListOf<LogInfo>()
        val file = File(outFileName)
        file.safelyRead { bufferReader ->
            while (true) {
                val logStr = bufferReader.readLine()
                if (logStr == null) {
                    break
                } else {
                    parseToLogInfo(logStr)?.let {
                        listInfo.add(it)
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
        val parseLogInfo = try {
            GSON.fromJson(logStr, ParseLoganInfo::class.java)
        } catch (except: Exception) {
            null
        }
        return try {
            parseLogInfo?.let { parseLogInfo ->
                if (!parseLogInfo.content.contains("LoganService")) {
                    return null
                }
                val time = DateFormatUtils.getDateToString(parseLogInfo.timestamp,
                        DateFormatUtils.DATE_FORMAT_YEAR_TO_MILL)
                val pid = parseLogInfo.threadName
                val tid = parseLogInfo.threadId.toString()
                val index = parseLogInfo.content.indexOf(" ")
                val tag = parseLogInfo.content.substring(14, index - 1) //不包含"|LoganService|"
                val content = parseLogInfo.content.substring(index + 1)
                val enumLevel = parseLogInfo.level.toEnumLevel()
                val strLevel = Utils.logLevelConvertMap.getOrDefault(enumLevel, Utils.DEFAULT_LOG_STR_LEVEL)
                LogInfo(time = time, tag = tag,pid = pid, tid = tid, strLevel = strLevel, enumLevel = enumLevel, content = content)
            }
        } catch (exception: Exception) {
            null
        }
    }
}

interface ParseFinishListener {
    //回调在主线程
    fun onParseFinish(logInfo: List<LogInfo>)
}