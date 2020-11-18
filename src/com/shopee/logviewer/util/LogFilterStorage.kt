package com.shopee.logviewer.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.shopee.logviewer.data.FilterInfo
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors.newCachedThreadPool
import javax.swing.SwingUtilities


/**
 * author: beitingsu
 * created on: 2020/11/17
 * 负责过滤器存储
 */
object LogFilterStorage {

    private val HOME_PATH = System.getProperty("user.home")
    private val DIR_NAME = "$HOME_PATH/FoodyLogViewer"
    private const val FILE_NAME = "/filterInfo.xml"

    private var mFilterInfoList = CopyOnWriteArrayList<FilterInfo>()
    private val mThreadPool = newCachedThreadPool()

    fun init(listener: OnFilterLoadedListener?) {
        mFilterInfoList.clear()
        parseXmlToJson(listener)
    }

    private fun parseJsonToXml() {
        mThreadPool.execute {
            val module = JacksonXmlModule()
            val mapper = XmlMapper(module)
            val xml = mapper.writeValueAsString(mFilterInfoList)
            val file = File(DIR_NAME)
            if (!file.exists()) {
                file.mkdirs()
            }
            val xmlFile = File(file.absolutePath + FILE_NAME)
            if (!xmlFile.exists()) {
                xmlFile.createNewFile()
            }
            xmlFile.safelyWrite({ bufferedWriter ->
                bufferedWriter.write(xml)
            }, false)
        }
    }

    private fun parseXmlToJson(listener: OnFilterLoadedListener?) = mThreadPool.execute {
        val file = File(DIR_NAME + FILE_NAME)
        if (!file.exists() || !file.isFile) {
            print("parseXmlToJson() >>> tag cache dont exists")
            SwingUtilities.invokeLater {
                listener?.onLoaded(listOf())
            }
            return@execute
        }

        val fisResult = runCatching {
            FileInputStream(file)
        }

        val fis = fisResult.getOrNull()

        if (fisResult.isFailure || null == fis) {
            SwingUtilities.invokeLater {
                listener?.onFailure(fisResult.exceptionOrNull())
            }
            return@execute
        }

        val parseResult = runCatching {
            fis.use { input ->
                val mapper = XmlMapper()
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                val listInfo = mapper.readValue<List<FilterInfo>>(input, object : TypeReference<List<FilterInfo>>() {})
                mFilterInfoList.addAll(listInfo)
            }
        }

        if (parseResult.isSuccess) {
            SwingUtilities.invokeLater {
                listener?.onLoaded(mFilterInfoList.toList())
            }
        } else {
            SwingUtilities.invokeLater {
                listener?.onFailure(parseResult.exceptionOrNull())
            }
        }
    }

    fun addFilterInfo(filterInfo: FilterInfo) {
        mFilterInfoList.find { it.name == filterInfo.name }?.let {
            it.msg = filterInfo.msg
            it.tagList = filterInfo.tagList
        } ?: mFilterInfoList.add(0, filterInfo)
        parseJsonToXml()
    }

    fun deleteFilterInfo(filterInfo: FilterInfo) {
        mFilterInfoList.remove(filterInfo)
        parseJsonToXml()
    }

    fun clear() {
        mFilterInfoList.clear()
        val file = File(DIR_NAME + FILE_NAME)
        if (file.exists()) {
            file.delete()
        }
    }
}

interface OnFilterLoadedListener {

    fun onLoaded(filterInfoList: List<FilterInfo>)

    fun onFailure(e: Throwable?)

}