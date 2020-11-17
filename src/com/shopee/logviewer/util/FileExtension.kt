package com.shopee.logviewer.util

import java.io.*

/**
 * author: beitingsu
 * created on: 2020/11/12
 */
inline fun <T> safelyCreate(crossinline creator: () -> T?): T? = run {
    try {
        creator.invoke()
    } catch (e: Throwable) {
        null
    }
}

inline fun <T : Closeable?, R> T.safelyUse(block: (T) -> R): R? {
    return try {
        block(this)
    } catch (e: Throwable) {
        null
    } finally {
        try {
            this?.close()
        } catch (closeException: Throwable) {

        }
    }
}

fun <T> File.safelyRead(block: (reader: BufferedReader) -> T): T? {
    return safelyCreate {
        BufferedReader(InputStreamReader(FileInputStream(this)))
    }?.safelyUse { reader ->
        block.invoke(reader)
    }
}

fun File.safelyWrite(block: (writer: BufferedWriter) -> Unit, isAppend: Boolean = true) {
    safelyCreate {
        BufferedWriter(OutputStreamWriter(FileOutputStream(this, isAppend)))
    }?.safelyUse { writer ->
        block.invoke(writer)
        writer.flush()
    }
}
