package com.shopee.logviewer.data

/**
 * author: beitingsu
 * created on: 2020/11/13
 * 过滤信息
 *
 * 如果增加、修改了参数，请务必设置default value，否则初始化时parse Xml时会抛出
 */
data class FilterInfo(
    val name: String = "",
    /** 过滤器自定义名称 */
    var msg: String? = "",
    /** 过滤规则1: [LogInfo].msg.contains(msg)  */
    var tagList: List<String>? = null,
    /** 过滤规则2: tagList.any { it == [LogInfo].tag } */
    val isRegex: Boolean = false
    /** isRegex用于辅助过滤规则1 */
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
            .append("isRegex:").append(isRegex).append("\n")
            .toString()
    }
}