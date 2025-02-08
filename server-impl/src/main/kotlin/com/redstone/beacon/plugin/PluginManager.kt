package com.redstone.beacon.plugin

import com.redstone.beacon.plugin.event.PluginActiveEvent
import com.redstone.beacon.plugin.event.PluginDisableEvent
import com.redstone.beacon.plugin.event.PluginEnableEvent
import com.redstone.beacon.plugin.event.PluginLoadEvent
import com.redstone.beacon.plugin.simple.JvmPluginLoader
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.minestom.dependencies.DependencyGetter
import net.minestom.dependencies.ResolvedDependency
import net.minestom.server.event.EventDispatcher
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object PluginManager : IPluginManager {

    internal val libsFile = File("libs")
    internal val pluginFile = File("plugins")

    val LOGGER = ComponentLogger.logger(PluginManager::class.java)

    var plugins = LinkedHashMap<String, Plugin>()
        private set

    val classLoaders = LinkedList<PluginLoader>()

    val pluginResolvedDependency: ConcurrentHashMap<String, ArrayList<ResolvedDependency>> = ConcurrentHashMap()

    private var dependency = DependencyGetter()

    private var description = ArrayList<PluginDescription>()
    init {
        // JVMPluginLoader注册操作
        classLoaders.addFirst(JvmPluginLoader)

    }

//    enum class State(val value: Int) {
//        DO_NOT_START(-1), // 不启动
//        NOT_STARTED(0),  // 未启动
//        PRE_INIT(1),     // 初始化前
//        INIT(2),         // 初始化
//        POST_INIT(3),     // 初始化后
//        STARTED(4),      // 已启动
//    }


    fun getPluginDescription(file: File) : PluginDescription? {
        classLoaders.forEach {
            if (it.isFileIgnored(file)) {
                try {
                    val description = it.getPluginDescription(file)
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
        classLoaders.forEach {
            if (it.isFileIgnored(file)) {
                try {
                    val plugin = it.loadPlugin(file)
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
//        state = State.PRE_INIT
        if (!pluginFile.exists()) {
            if (!pluginFile.mkdirs()) {
                LOGGER.error("无法找到或创建插件文件夹，插件将不会被加载！")
                return
            }
        }

        if (!libsFile.exists()) {
            if (!libsFile.mkdirs()) {
                LOGGER.error("无法找到或创建插件依赖文件夹，插件将不会被加载！")
                return
            }
        }

        pluginFile.listFiles()?.forEach {
            val des = getPluginDescription(it)
            des?.let { it1 -> description.add(it1) }
        }

        description = topologicalSort(description) as ArrayList<PluginDescription>

        val artifacts = HashMap<String, List<String>>()

        description.forEach {
            //将description的依赖信息库存入DependencyGetter
            dependency.addMavenResolver(it.dependencies!!.repositories)
            //将description的依赖信息资源存入artifacts
            artifacts[it.name] = it.dependencies!!.artifacts
        }

        artifacts.forEach { (pluginName,v) ->
            //加载artifacts
            if (!pluginResolvedDependency.containsKey(pluginName)) {
                pluginResolvedDependency[pluginName] = ArrayList()
            }
            v.forEach {
                // 加入ResolvedDependency，且下载依赖文件
                pluginResolvedDependency[pluginName]?.add(dependency.get(it, libsFile))
            }
        }

        description.forEach {
            loadPlugin(it.originFile!!)
        }

        enablePlugins()

    }

    override fun enablePlugin(plugin: Plugin) {
        if (plugin.enable) {
            LOGGER.error("插件${plugin.origin.name}已经被启用了！")
        }
        plugin.pluginLoader.openPlugin(plugin)
        EventDispatcher.call(PluginEnableEvent(plugin))
    }

    //开启插件
    override fun enablePlugins() {
        plugins.forEach { (_, u) ->
            enablePlugin(u)
        }
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
    //拓扑排序

//    fun topologicalSort(pluginMap: Map<String, PluginDescription>): LinkedHashMap<String, PluginDescription> {
//        val inDegree = mutableMapOf<String, Int>() // 存储每个节点的入度
//        val adjacencyList = mutableMapOf<String, MutableList<String>>() // 存储每个节点的邻居列表
//
//        // 初始化图
//        for (pluginName in pluginMap.keys) {
//            inDegree[pluginName] = 0
//            adjacencyList[pluginName] = mutableListOf()
//        }
//
//        // 构建图
//        for ((pluginName, description) in pluginMap) {
//            val allDependencies = description.depend + description.softDepend + description.loadBefore
//            for (dependency in allDependencies) {
//                if (pluginMap.containsKey(dependency)) {
//                    adjacencyList[dependency]!!.add(pluginName) // 添加边
//                    inDegree[pluginName] = inDegree[pluginName]!! + 1 // 增加入度
//                }
//            }
//        }
//
//        // 找出所有入度为0的节点
//        val zeroInDegreeQueue = ArrayDeque<String>()
//        for ((node, degree) in inDegree) {
//            if (degree == 0) {
//                zeroInDegreeQueue.add(node)
//            }
//        }
//
//        // 执行拓扑排序
//        val sortedOrder = mutableListOf<String>()
//        while (zeroInDegreeQueue.isNotEmpty()) {
//            val node = zeroInDegreeQueue.removeFirst()
//            sortedOrder.add(node)
//
//            // 减少邻居节点的入度
//            for (neighbor in adjacencyList[node]!!) {
//                inDegree[neighbor] = inDegree[neighbor]!! - 1
//                if (inDegree[neighbor] == 0) {
//                    zeroInDegreeQueue.add(neighbor)
//                }
//            }
//        }
//
//        // 检查图中是否有循环
//        if (sortedOrder.size != pluginMap.size) {
//            throw IllegalStateException("检测到插件依赖关系中存在循环！")
//        }
//
//        // 根据排序结果构建新的 LinkedHashMap
//        val sortedPluginMap = LinkedHashMap<String, PluginDescription>()
//        for (pluginName in sortedOrder) {
//            sortedPluginMap[pluginName] = pluginMap[pluginName]!!
//        }
//
//        return sortedPluginMap
//    }


    //激活插件
    override fun activePlugin(plugin: Plugin) {
        if (plugin.active) {
            LOGGER.error("插件${plugin.origin.name}已经被激活了！")
        }
        plugin.pluginLoader.activePlugin(plugin)
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
        plugin.pluginLoader.disablePlugin(plugin)
        EventDispatcher.call(PluginDisableEvent(plugin))
    }


}