package com.redstone.beacon.plugin

import com.redstone.beacon.plugin.event.PluginActiveEvent
import com.redstone.beacon.plugin.event.PluginDisableEvent
import com.redstone.beacon.plugin.event.PluginEnableEvent
import com.redstone.beacon.plugin.event.PluginLoadEvent
import com.redstone.beacon.plugin.simple.JavaPluginService
import com.redstone.beacon.plugin.simple.PluginClassLoader
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.minestom.dependencies.DependencyGetter
import net.minestom.dependencies.ResolvedDependency
import net.minestom.server.event.EventDispatcher
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object PluginManager : IPluginManager {

    /** 依赖文件夹 */
    internal val libsFile = File("libs")

    /** Logger  */
    val LOGGER = ComponentLogger.logger(PluginManager::class.java)

    /**  所有插件的类加载器实例对象 */
    val classLoaders = ConcurrentHashMap<String, PluginClassLoader>()

    /**  插件列表 */
    val plugins = LinkedHashMap<String, Plugin>()

    /**  所有的插件加载器实例 */
    val pluginLoaders = ConcurrentHashMap<String, PluginService>()

    // 所有的插件依赖文件信息 -> (插件名称, 依赖信息)
    val pluginResolvedDependency: ConcurrentHashMap<String, ArrayList<ResolvedDependency>> = ConcurrentHashMap()

    // 一个依赖处理器
    private var dependency = DependencyGetter()

    // 插件所有的初始化信息
    private var description = ArrayList<PluginDescription>()

    init {
        // JVMPluginLoader注册操作
        pluginLoaders[JavaPluginService.getKey()] = (JavaPluginService)
    }

    fun getPluginDescription(file: File) : PluginDescription? {
        pluginLoaders.forEach { (_, pluginLoader) ->
            if (pluginLoader.isFileIgnored(file)) {
                try {
                    val description = pluginLoader.getPluginDescription(file)
                    return description
                } catch (e: Exception) {
                    e.printStackTrace()
                    return null
                }
            }
        }
        return null
    }

    //加载插件
    override fun loadPlugin(file: File): Plugin? {
        pluginLoaders.forEach { (_, pluginLoader) ->
            if (pluginLoader.isFileIgnored(file)) {
                try {
                    val plugin = pluginLoader.loadPlugin(file)
                    plugins[plugin!!.origin.name] = plugin
                    EventDispatcher.call(PluginLoadEvent(plugin, PluginLoadState.SUCCESS))
                    return plugin
                } catch (e: Exception) {
                    e.printStackTrace()
                    EventDispatcher.call(PluginLoadEvent(null, PluginLoadState.ERROR))
                    return null
                }
            }
        }
        return null
    }

    override fun loadPlugins() {

        // 创建文件
        if (!libsFile.exists()) {
            if (!libsFile.mkdirs()) {
                LOGGER.error("无法找到或创建插件依赖文件夹，插件将不会被加载！")
                return
            }
        }

        // 加载所有插件的信息
        SimplePluginSource.file.listFiles()?.forEach {
            val des = getPluginDescription(it)
            des?.let { it1 -> description.add(it1) }
        }

        // 排序插件的启动顺序
        description = topologicalSort(description) as ArrayList<PluginDescription>

        //处理依赖
        handleDependency()

        //加载插件
        description.forEach {
            loadPlugin(it.originFile!!)
        }

        //启动插件
        enablePlugins()

    }

    override fun enablePlugin(plugin: Plugin) {
        if (plugin.enable) {
            LOGGER.error("插件${plugin.origin.name}已经被启用了！")
        }
        plugin.pluginService.enablePlugin(plugin)
        EventDispatcher.call(PluginEnableEvent(plugin))
    }

    //开启插件
    override fun enablePlugins() {
        plugins.forEach { (_, u) ->
            enablePlugin(u)
        }
    }

    //激活插件
    override fun activePlugin(plugin: Plugin) {
        if (plugin.active) {
            LOGGER.error("插件${plugin.origin.name}已经被激活了！")
        }
        plugin.pluginService.activePlugin(plugin)
        EventDispatcher.call(PluginActiveEvent(plugin))
    }

    override fun activePlugins() {
        plugins.forEach { (_, plugin) ->
            activePlugin(plugin)
        }
    }

    override fun disablePlugins() {
        plugins.forEach { (_, plugin) ->
            disablePlugin(plugin)
        }
    }

    //关闭插件
    override fun disablePlugin(plugin: Plugin) {
        plugin.pluginService.disablePlugin(plugin)
        EventDispatcher.call(PluginDisableEvent(plugin))
    }

    private fun topologicalSort(descriptions: List<PluginDescription>): List<PluginDescription> {
        val inDegree = mutableMapOf<String, Int>() // 存储每个节点的入度
        val adjacencyList = mutableMapOf<String, MutableList<String>>() // 存储每个节点的邻居列表
        val nameToDescription = descriptions.associateBy { it.name } // 名称到描述的映射

        // 初始化图
        for (description in descriptions) {
            inDegree[description.name] = 0
            adjacencyList[description.name] = mutableListOf()
        }

        // 构建图
        for (description in descriptions) {
            val allDependencies = description.depend!! + description.softDepend!! + description.loadBefore
            for (dependency in allDependencies) {
                if (nameToDescription.containsKey(dependency)) {
                    adjacencyList[dependency]!!.add(description.name) // 添加边
                    inDegree[description.name] = inDegree[description.name]!! + 1 // 增加入度
                }
            }
        }

        // 找出所有入度为0的节点
        val zeroInDegreeQueue = ArrayDeque<String>()
        for ((node, degree) in inDegree) {
            if (degree == 0) {
                zeroInDegreeQueue.add(node)
            }
        }

        // 执行拓扑排序
        val sortedOrder = mutableListOf<String>()
        while (zeroInDegreeQueue.isNotEmpty()) {
            val node = zeroInDegreeQueue.removeFirst()
            sortedOrder.add(node)

            // 减少邻居节点的入度
            for (neighbor in adjacencyList[node]!!) {
                inDegree[neighbor] = inDegree[neighbor]!! - 1
                if (inDegree[neighbor] == 0) {
                    zeroInDegreeQueue.add(neighbor)
                }
            }
        }

        // 检查图中是否有循环
        if (sortedOrder.size != descriptions.size) {
            throw IllegalStateException("检测到插件依赖关系中存在循环！")
        }

        // 根据排序结果返回排序后的 PluginDescription 列表
        return sortedOrder.map { nameToDescription[it]!! }
    }

    private fun handleDependency() {
        // 初始化插件的依赖表 --> 插件,所有依赖组成
        val artifacts = HashMap<String, List<String>>()

        description.forEach {
            // 将description的依赖信息库存入一个统一的DependencyGetter
            dependency.addMavenResolver(it.dependencies!!.repositories)
            // 将description的依赖信息资源存入artifacts
            artifacts[it.name] = it.dependencies!!.artifacts
        }

        artifacts.forEach { (pluginName,v) ->
            //加载artifacts
            if (!pluginResolvedDependency.containsKey(pluginName)) {
                pluginResolvedDependency[pluginName] = ArrayList()
            }
            v.forEach {
                // 加入ResolvedDependency，且下载依赖文件
                val resolved = dependency.get(it, libsFile.toPath())
                resolved.printTree(pluginName)
                pluginResolvedDependency[pluginName]?.add(resolved)
            }
        }
    }



}