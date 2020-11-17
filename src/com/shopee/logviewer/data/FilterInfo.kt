package com.shopee.logviewer.data

/**
 * author: beitingsu
 * created on: 2020/11/13
 * 过滤信息
 */
data class FilterInfo(
    val name: String,
    /** 过滤器自定义名称 */
    val msg: String? = "",
    /** 过滤规则1: [LogInfo].msg.contains(msg)  */
    val tagList: List<String>? = null
    /** 过滤规则2: tagList.any { it == [LogInfo].tag } */
) {

    override fun toString(): String {
        return StringBuilder()
            .append("name:").append(name).append("\n")
            .append("msg:").append(msg).append("\n")
            .also { sb ->
                tagList?.forEachIndexed { index, tag ->
                    sb.append("tag[").append(index).append("]").append(tag).append("\n")
                }
            }
            .toString()
    }
}