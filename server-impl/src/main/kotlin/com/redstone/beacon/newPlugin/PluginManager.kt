//package com.redstone.beacon.newPlugin
//
//import com.redstone.beacon.plugin.PluginManager.LOGGER
//import com.redstone.beacon.utils.Graph
//import com.redstone.beacon.utils.safe
//import net.minestom.dependencies.DependencyGetter
//import java.net.URL
//import java.nio.file.Path
//import java.util.concurrent.ConcurrentHashMap
//
//open class BasePluginManager(private val originFiles: Path) : PluginManager {
//
//    // 依赖
//    var dependencyMap = ConcurrentHashMap<String, DependencyGetter>()
//
//    private val initInfo = ConcurrentHashMap<String, PluginDescription>()
//
//    // 这玩意是预设注册的PluginService
//    val pluginServices = ConcurrentHashMap<String, PluginService>()
//
//    init {
//        val pluginFile = originFiles.toFile()
//        if (!pluginFile.exists()) {
//            if (!pluginFile.mkdirs()) {
//                LOGGER.error("无法找到或创建插件文件夹，插件将不会被加载！")
//            }
//        }
//    }
//
//    fun loadPlugins() {
//        getDesciptionAndSort()
//        downloadDepenenciesAndSave()
//    }
//
//
//    override fun loadPlugin(url: URL): Plugin? {
//        return safe {
//            val description = getDescription(url) ?: return@safe null
//            val plugin = description.pluginService.loadPlugin(description) ?: return@safe null
//            plugins[description.id] = plugin
//            return@safe plugin
//        }
//
//    }
//
//    override fun findPlugin(pluginID: String): Plugin? {
//        return safe {
//            return@safe plugins[pluginID]
//        }
//    }
//
//    override fun unloadPlugin(pluginID: String) {
//    }
//
//    private fun getDesciptionAndSort() {
//        val file = originFiles.toFile()
//        val originDescriptions = ArrayList<PluginDescription>()
//        file.listFiles()?.forEach { a ->
//            try {
//                val info = getDescription(a.toURI().toURL()) ?: return@forEach
//                originDescriptions.add(info)
//            }catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//
//        val last = topologicalSort(originDescriptions)
//        last.forEach {
//            initInfo[it.id] = it
//        }
//    }
//
//    private fun downloadDepenenciesAndSave() {
//        // 初始化插件的依赖表 --> 插件,所有依赖组成
//        val artifacts = HashMap<String, List<String>>()
//
//        //先为所有的插件初始化一个dependencyGetter
//        initInfo.forEach { (_,v) ->
//            if (v.dependencyRepositories.artifacts.isNotEmpty()) {
//                dependencyMap[v.id] = DependencyGetter()
//            }
//        }
//
//        //开始初始化所有的依赖表
//        initInfo.forEach{ (_,v) ->
//            // 将description的依赖信息库存入DependencyGetter
//            // 将description的依赖信息资源存入artifacts
//            dependencyMap[v.id]?.addMavenResolver(v.dependencyRepositories.repositories) ?: return@forEach
//            artifacts[v.id] = v.dependencyRepositories.artifacts
//        }
//
//        //下载 run！
//        artifacts.forEach { (pluginName,v) ->
//            // 得到相对应的depenency
//            val getter = dependencyMap[pluginName] ?: return@forEach
//            v.forEach {
//                getter.get(it,
//                    Path.of(
//                        System.getProperty("Beacon.PluginManager.LibsFile", "libs")
//                    ),
//                    true
//                )
//            }
//        }
//
//    }
//
//    private fun getDescription(url: URL): PluginDescription? {
//        return safe {
//            pluginServices.forEach {(_,v) ->
//                // 先看看预设中有没有对该插件进行适配的
//                if (v.isIgnored(url)) {
//                    return@safe v.initDescription(url)
//                }
//            }
//            // 如果没有，则使用默认的
//            return@safe SimpleService.initDescription(url)
//        }
//    }
//
//    private fun topologicalSort(descriptions: List<PluginDescription>): List<PluginDescription> {
//        // 1. 创建图结构
//        val graph = Graph<PluginDescription>()
//        val nameToDesc = descriptions.associateBy { it.id }
//
//        // 2. 构建图节点和边
//        descriptions.forEach { plugin ->
//            graph.addNode(plugin)
//
//            // 合并所有依赖类型（硬依赖 + 软依赖）
//            val allDependencies = plugin.dependencies + plugin.optionalDependencies
//
//            allDependencies.forEach { depName ->
//                // 查找被依赖的插件
//                val depPlugin = nameToDesc[depName]
//                depPlugin?.let {
//                    // 添加边：被依赖的插件 -> 当前插件
//                    // 表示被依赖插件需要先加载
//                    graph.addEdge(from = depPlugin, to = plugin)
//                }
//            }
//        }
//
//        // 3. 执行拓扑排序（自动检测循环依赖）
//        return graph.topologicalSort()
//    }
//
//}