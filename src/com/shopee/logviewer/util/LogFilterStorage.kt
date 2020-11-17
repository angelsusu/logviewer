package com.shopee.logviewer.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.shopee.logviewer.data.FilterInfo
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.ConcurrentLinkedQueue
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

    private var mFilterInfoList = ConcurrentLinkedQueue<FilterInfo>()
    private val mThreadPool = newCachedThreadPool()

    private val mListeners = ConcurrentLinkedQueue<OnFilterLoadedListener>()

    fun init() {
        mFilterInfoList.clear()
        parseXmlToJson()
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

    private fun parseXmlToJson() {
        mThreadPool.execute {
            val file = File(DIR_NAME + FILE_NAME)
            safelyCreate { FileInputStream(file) }?.safelyUse { input ->
                val mapper = XmlMapper()
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                val listInfo = mapper.readValue<List<FilterInfo>>(input, object : TypeReference<List<FilterInfo>>() {})
                mFilterInfoList.addAll(listInfo)
                notifyListeners()
            }
        }
    }

    private fun notifyListeners() {
        SwingUtilities.invokeLater {
            mListeners.forEach { listener ->
                listener.onLoaded(mFilterInfoList.toList())
            }
        }
    }

    fun addListener(listener: OnFilterLoadedListener) {
        mListeners.add(listener)
    }

    fun removeListener(listener: OnFilterLoadedListener) {
        mListeners.remove(listener)
    }

    fun addFilterInfo(filterInfo: FilterInfo) {
        mFilterInfoList.find { it.name == filterInfo.name }?.let {
            it.msg = filterInfo.msg
            it.tagList = filterInfo.tagList
        } ?: mFilterInfoList.add(filterInfo)
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
}