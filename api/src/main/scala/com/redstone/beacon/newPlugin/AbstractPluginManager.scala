package com.redstone.beacon.newPlugin

import com.redstone.beacon.newPlugin.AbstractPluginManager.LOGGER
import org.slf4j.LoggerFactory

import java.io.File
import java.util
import java.util.concurrent.ConcurrentHashMap
import scala.util.Try
import scala.util.control.Breaks.{break, breakable}

abstract class AbstractPluginManager extends PluginManager {
  // 使用 Option 或懒加载
  protected var pluginDescriptorFinder: Option[DescriptionFinder] = None
  protected var plugins: Option[util.Map[String, PluginWrapper]] = None
  private val notSortedPlugins = new util.ArrayList[String]()
  private val pluginDescrptors  = new util.HashMap[String, Descriptor]()
  protected var versionChecker: Option[VersionChecker] = None

  // 抽象方法
  def createSorter(): ISorter
  def createPluginDescriptorFinder(): DescriptionFinder

  override def getPlugins: util.Map[String, PluginWrapper] = {
    plugins.getOrElse(new ConcurrentHashMap[String, PluginWrapper]()).asInstanceOf
  }

  // 初始化方法信息
  protected def initialize(): Unit = {
    Try {
      pluginDescriptorFinder = Some(createPluginDescriptorFinder())
      plugins = Some(new ConcurrentHashMap[String, PluginWrapper]())
      versionChecker = Some(new DefaultVersionChecker)
    }.recover {
      case ex: Exception =>
        ex.printStackTrace()
    }
  }

  override def loadPlugin(name: String): PluginState = ???

  override def loadPlugins(): util.Map[String, PluginWrapper] = {
    val file = new File(getRoot.toUri)
    val map = ConcurrentHashMap[String, PluginWrapper]
    // 创建文件
    if (!file.exists()) {
      if (!file.mkdirs()) {
        LOGGER.error("无法找到或创建插件依赖文件夹，插件将不会被加载！")
        return map
      }
    }
    file.listFiles().foreach(v=> {
      breakable {
        val descriptor = Option(pluginDescriptorFinder.get.find(v.toURI.toURL))
        if(descriptor.isEmpty) {
          throw new PluginException("wrong plugin descriptor")
          break
        }
        notSortedPlugins.add(descriptor.get.getName)
        pluginDescrptors.put(descriptor.get.getName, descriptor.get)
      }
    })

  }

  def getVersionChecker(): VersionChecker = {
    versionChecker.get
  }

}

object AbstractPluginManager {
  protected val LOGGER = LoggerFactory.getLogger("PluginManager")
}