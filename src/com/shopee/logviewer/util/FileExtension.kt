package com.shopee.logviewer.util

import java.io.Closeable

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
