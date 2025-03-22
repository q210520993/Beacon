//package com.redstone.beacon.newPlugin
//
//import com.github.zafarkhaja.semver.Version
//import com.redstone.beacon.Beacon
//import com.redstone.beacon.newPlugin.event.PluginLoadEvent
//import com.redstone.beacon.plugin.Dependencies
//import com.redstone.beacon.plugin.PluginException
//import com.redstone.beacon.utils.safe
//import net.minestom.dependencies.maven.MavenRepository
//import net.minestom.server.MinecraftServer
//import net.minestom.server.event.EventDispatcher
//import taboolib.module.configuration.Configuration
//import java.io.File
//import java.net.URL
//import java.nio.file.Path
//import java.util.*
//import java.util.jar.JarFile
//
//object SimpleService: PluginService() {
//    override fun isIgnored(url: URL): Boolean {
//        val file = File(url.toURI())
//        return file.extension == "jar"
//    }
//
//    override fun initDescription(url: URL): PluginDescription {
//        val file = File(url.toURI())
//        // 初始化信息
//        val description: PluginDescription
//        // 封装
//        val jar = JarFile(file)
//        // 得到文件
//        val entry = jar.getJarEntry("plugin.yml") ?: jar.getJarEntry("plugin.conf")
//        val stream = jar.getInputStream(entry)
//        // 转换成configuration
//        val config = Configuration.loadFromInputStream(
//            stream,
//            Configuration.getTypeFromExtension(entry.name.replace("plugin.", ""))
//        )
//        // 开始生成
//        val d = object : PluginDescription {
//            override val id: String = config.getString("name") ?: throw PluginException("no plugin Name")
//            override val dependencies: List<String> = emptyList()
//            override val dependencyRepositories = run<Dependencies> {
//                val dependenciesConfig = config.getConfigurationSection("dependencies") ?: return@run Dependencies()
//                val dependencies = Dependencies()
//                val repositoriesC = dependenciesConfig.getMapList("repositories")
//                val repos = LinkedList<MavenRepository>()
//                repositoriesC.forEach {
//                    repos.add(MavenRepository(it["name"] as String, it["url"] as String))
//                }
//                dependencies.repositories = repos
//                dependencies.artifacts = dependenciesConfig.getStringList("artifacts")
//                return@run dependencies
//            }
//            override val mainClass: String = config.getString("main") ?: throw PluginException("no plugin Main")
//            override val optionalDependencies: List<String> = emptyList()
//            override val sourceUrl: URL = file.toURI().toURL()
//            override val version: Version = Version.parse("1.0.0")
//            override val pluginService: PluginService = SimpleService
//        }
//
//        description = d
//        return description
//    }
//
//    override fun createClassLoader(pluginDescription: PluginDescription, parent: ClassLoader): ClassLoader {
//        val dependencyClassLoader = DependencyClassLoader(arrayOf(), MinecraftServer::class.java.classLoader)
//        Beacon.pluginmanager.dependencyMap[pluginDescription.id]!!.dependencies.forEach { _, u ->
//            dependencyClassLoader.addURL(u.contentsLocation)
//        }
//        val classLoader = PluginClassLoader(arrayOf(pluginDescription.sourceUrl), parent)
//        return classLoader
//    }
//
//    override fun loadPlugin(
//        pluginDescription: PluginDescription
//    ): Plugin? {
//        val plugin = safe {
//            val classLoader = createClassLoader(pluginDescription, MinecraftServer::class.java.classLoader)
//            val clazz = (classLoader as PluginClassLoader).loadClass(pluginDescription.mainClass)
//            val dataFolder = Path.of(pluginDescription.id, pluginDescription.id)
//            val plugin = clazz.getDeclaredConstructor().newInstance() as BasePlugin
//            plugin.init(this, classLoader, pluginDescription, dataFolder)
//            return@safe plugin
//        }
//        val event: PluginLoadEvent = plugin?.let {
//            return@let PluginLoadEvent(plugin)
//        } ?: PluginLoadEvent(null)
//        EventDispatcher.call(event)
//        return plugin
//    }
//
//    override fun onPlugin(plugin: Plugin) {
//        plugin
//    }
//
//    override fun onLoad(plugin: Plugin) {
//        TODO("Not yet implemented")
//    }
//
//    override fun onActive(plugin: Plugin) {
//        TODO("Not yet implemented")
//    }
//}