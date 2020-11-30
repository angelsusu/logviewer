package com.shopee.logviewer

import com.shopee.logviewer.util.Utils
import com.shopee.logviewer.util.safelyWrite
import java.io.File
import java.util.*


/**
 * author: beitingsu
 * created on: 2020/11/30
 * 收集异常信息
 */
class CrashHandler : Thread.UncaughtExceptionHandler {
    private val crashFile = Utils.DIR_NAME + "/crash.txt"

    override fun uncaughtException(t: Thread?, e: Throwable?) {
        val file = File(crashFile)
        if (!file.exists()) {
            file.createNewFile()
        }
        file.safelyWrite({ bufferedWriter ->
            e?.let {
                bufferedWriter.write(getExceptionStack(it, hashSetOf(), 0))
            }
        })
        Thread.setDefaultUncaughtExceptionHandler(null)
        e?.let {
            throw e
        }
    }


    private fun getExceptionStack(e: Throwable, set: HashSet<String?>, num: Int): String {
        var num = num
        val stackTraceElements = e.stackTrace
        var prefix = ""
        prefix = if (num == 0) {
            "Exception in thread " + "\"" + Thread.currentThread().name + "\" "
        } else {
            "Caused by: "
        }
        var result = "$prefix$e\n"
        val lenth = stackTraceElements.size - 1
        for (i in 0..lenth) {
            val err = stackTraceElements[i].className + "." + stackTraceElements[i].methodName + "(" + stackTraceElements[i].fileName + "." + stackTraceElements[i].lineNumber + ")"
            if (set.contains(err)) {
                continue
            }
            set.add(err)
            result = "$result\tat $err\n"
        }
        val t = e.cause
        var cause = ""
        if (t != null) {
            num++
            cause = getExceptionStack(t, set, num)
        }
        return result + cause
    }


}