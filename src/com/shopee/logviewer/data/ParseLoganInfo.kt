package com.shopee.logviewer.data

import com.google.gson.annotations.SerializedName

/**
 * author: beitingsu
 * created on: 2020/11/16
 * logan解密后的日志信息
 */
data class ParseLoganInfo(
        @SerializedName("c")
        val content: String = "",

        @SerializedName("f")
        val level: Int = 0,

        @SerializedName("l")
        val timestamp: Long = 0L,

        @SerializedName("n")
        val threadName: String = "",

        @SerializedName("i")
        val threadId: Int = 0,

        @SerializedName("m")
        val isInMainThread: Boolean = false
)