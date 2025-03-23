package com.redstone.beacon.newPlugin

import com.redstone.beacon.newPlugin.AbstractPluginManager.LOGGER
import org.slf4j.LoggerFactory

import java.io.File
import java.util
import java.util.concurrent.ConcurrentHashMap
import scala.util.Try
import scala.util.control.Breaks.{break, breakable}


object AbstractPluginManager {
  private val LOGGER = LoggerFactory.getLogger("PluginManager")
}

abstract class AbstractPluginManager extends PluginManager {
  // 使用 Option 相当于kotlin的空处理，但是比kotlin的好用
  protected var pluginDescriptorFinder: Option[DescriptionFinder] = None
  protected var plugins: Option[util.Map[String, PluginWrapper]] = None
  protected var sorter: Option[ISorter] = None
  protected var versionChecker: Option[VersionChecker] = None
  protected var mavenResolver: Option[MavenResolver] = None
  protected var pluginFactory: Option[PluginFactory] = None
  protected var pluginLoader: Option[PluginLoader] = None

  private val notSortedPlugins = new util.ArrayList[String]()
  private val pluginDescriptors  = new util.HashMap[String, Descriptor]()
  private var sortResult: Option[SortResult] = None

  // 抽象方法
  def createSorter(): ISorter
  def createPluginDescriptorFinder(): DescriptionFinder
  def createMavenResolver(): MavenResolver
  def createversionChecker(): VersionChecker
  def createPluginFactory(): PluginFactory
  def createPluginClassLoader(): PluginLoader


  override def getPlugins: util.Map[String, PluginWrapper] = {
    plugins.getOrElse(new ConcurrentHashMap[String, PluginWrapper]())
  }

  // 初始化方法信息
  protected def initialize(): Unit = {
    Try {
      pluginDescriptorFinder = Some(createPluginDescriptorFinder())
      plugins = Some(new ConcurrentHashMap[String, PluginWrapper]())
      sorter = Some(createSorter())
      mavenResolver = Some(createMavenResolver())
      versionChecker = Some(createversionChecker())
      pluginFactory = Some(createPluginFactory())
      pluginLoader = Some(createPluginClassLoader())
    }.recover {
      case ex: Exception =>
        ex.printStackTrace()
    }
  }

  override def loadPlugin(name: String): PluginState = ???

  override def loadPlugins(): util.Map[String, PluginWrapper] = {
    val file = new File(getRoot.toUri)
    // 创建文件
    if (!file.exists()) {
      if (!file.mkdirs()) {
        throw new PluginException("无法找到或创建插件依赖文件夹，插件将不会被加载！")
      }
    }

    initDescriptors(file)
    initPluginWrappers()
    downloadMaven()
    getPlugins()

  }

  // 包装插件
  protected def createPluginWrapper(descriptor: Descriptor): PluginWrapper = {
    LOGGER.debug("正在包装插件: {}", descriptor.getName)
    val wrapper = new PluginWrapper(this, descriptor)
    wrapper.setPluginFactory(getPluginFactory())
    wrapper.setPluginState(PluginState.CREATED)
    wrapper
  }

  // First
  private def initDescriptors(file: File): Unit = {
    file.listFiles().foreach(v=> {
      breakable {
        val descriptor = Option(pluginDescriptorFinder.get.find(v.toURI.toURL))
        if(descriptor.isEmpty) {
          throw new PluginException("wrong plugin descriptor")
          break
        }
        notSortedPlugins.add(descriptor.get.getName)
        pluginDescriptors.put(descriptor.get.getName, descriptor.get)
      }
    })
    val arrayList = new util.ArrayList[Descriptor]()

    notSortedPlugins.forEach(a=>{arrayList.add(pluginDescriptors.get(a))})

    val result = getSorter().sort(arrayList)
    sortResult = Some(result)
  }

  // initPluginWrappers
  private def initPluginWrappers(): Unit = {
    pluginDescriptors.forEach((a,v) => {
      plugins.get.put(a, createPluginWrapper(v))
    })
  }


  private def downloadMaven(): Unit = {
    // 尝试下载依赖
    Try {
      plugins.get.forEach((_,v) =>{
        getMavenResolver().download(v)
      })
    }.recover {
      case Ex: Exception =>
        Ex.printStackTrace()
    }

  }

  def getVersionChecker(): VersionChecker = {
    versionChecker.get
  }

  def getPluginFactory(): PluginFactory = {
    pluginFactory.get
  }

  def getSorter(): ISorter = {
    sorter.get
  }

  def getMavenResolver(): MavenResolver = {
    mavenResolver.get
  }

}