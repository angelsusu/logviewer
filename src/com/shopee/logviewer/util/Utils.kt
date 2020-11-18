package com.shopee.logviewer.util

import com.google.gson.Gson
import com.shopee.logviewer.data.EnumLogLv
import java.awt.Color

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

    const val DEFAULT_LOG_STR_LEVEL = V

    val logLevelMap = mapOf(
        V to EnumLogLv.V,
        D to EnumLogLv.D,
        I to EnumLogLv.I,
        W to EnumLogLv.W,
        E to EnumLogLv.E
    )

    val logLevelConvertMap = mapOf(
        EnumLogLv.V to V,
        EnumLogLv.D to D,
        EnumLogLv.I to I,
        EnumLogLv.W to W,
        EnumLogLv.E to E
    )

    val LOG_STR_LEVELS = arrayOf(V, D, I, W, E)
    private val LOG_ENUM_LEVELS = arrayOf(EnumLogLv.V, EnumLogLv.D, EnumLogLv.I, EnumLogLv.W, EnumLogLv.E)

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
            E to Color(255,0,6)
    )
}

object LogEncrypt {
    const val ENCRYPT_KEY = "0123456789012345"
    const val ENCRYPT_IV = "0123456789012345"
}