package com.redstone.beacon.plugin.simple

import com.redstone.beacon.plugin.*
import com.redstone.beacon.plugin.PluginManager.classLoaders
import com.redstone.beacon.plugin.event.PluginCommandEvent
import com.redstone.beacon.plugin.event.PluginLoadEvent
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.minestom.dependencies.maven.MavenRepository
import net.minestom.server.MinecraftServer
import net.minestom.server.event.EventDispatcher
import taboolib.module.configuration.Configuration
import java.io.File
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.jar.JarFile

/**
 * JavaPluginService provides methods for managing plugins including loading, opening, disabling, and activating plugins.
 * This service also handles retrieving plugin descriptions and checking if files should be ignored.
 *
 * JavaPluginService 提供了管理插件的方法，包括加载、打开、禁用和激活插件
 * 此服务还处理检索插件描述和检查文件是否应被忽略
 */
object JavaPluginService : PluginService {

    /**
     * Retrieves the key identifying this service.
     * 获取标识此服务的键
     *
     * @return A string representing the unique key for this service.
     * 返回一个字符串，表示该服务的唯一键
     */
    override fun getKey(): String {
        return "JavaPluginLoader"
    }

    /**
     * Concurrent map to hold loaded classes.
     * 并发映射用于保存已加载的类
     */
    val classes = ConcurrentHashMap<String, Class<*>>()

    /**
     * Logger instance for logging information and errors.
     * 用于记录信息和错误的日志实例
     */
    val log = ComponentLogger.logger("JVMPluginLoader")

    /**
     * Loads a plugin from the specified file.
     * 从指定文件加载插件
     *
     * @param file The file from which the plugin is to be loaded.
     * 参数file 指定用于加载插件的文件
     * @return The loaded Plugin object or null if the plugin could not be loaded.
     * 返回加载的插件对象，如果无法加载插件则返回null
     */
    override fun loadPlugin(file: File): Plugin? {
        val description = getPluginDescription(file)
        var pluginV: Plugin? = null
        description.let { info ->
            log.info("正在加载${info.name}")
            val dataFolder = Path.of(file.parentFile.path, description.name)
            val main = info.main
            val classLoader = PluginClassLoader(this, MinecraftServer::class.java.classLoader, file)
            // Dependency handling, load dependencies into plugin
            // 依赖处理,把依赖草进去
            PluginManager.pluginResolvedDependency[description.name]?.forEach {
                SimpleDependenciesLoader.loadDependicyToPlugin(description, it, classLoader)
            }

            classLoaders[description.name] = classLoader
            try {
                val clazz = Class.forName(main, true, classLoader)
                if (!BasePlugin::class.java.isAssignableFrom(clazz)) {
                    throw PluginException("no extend BasePlugin")
                }
                try {
                    val pluginClazz = clazz.asSubclass(BasePlugin::class.java) as Class<out BasePlugin>
                    val plugin = pluginClazz.getDeclaredConstructor().newInstance()
                    plugin.init(this, classLoader, description, dataFolder, file)
                    pluginV = plugin
                } catch (e: ClassCastException) {
                    throw PluginException("加载时出现错误，类: ${description.main}")
                } catch (e: InstantiationException) {
                    log.error(
                        "一个在初始化时出现的问题 {}, {}, {}, {}",
                        file,
                        main,
                        description.name,
                        description.version,
                        e
                    )
                }
            } catch (ex: ClassNotFoundException) {
                throw PluginException("无法找到类: ${description.main}")
            }
        }
        val event: PluginLoadEvent = pluginV?.let {
            return@let PluginLoadEvent(pluginV, PluginLoadState.SUCCESS)
        } ?: PluginLoadEvent(null, PluginLoadState.ERROR)
        EventDispatcher.call(event)
        return pluginV
    }

    /**
     * Retrieves the description of a plugin from the specified file.
     * 从指定文件获取插件的描述
     *
     * @param file The file from which the plugin description is to be retrieved.
     * 参数file 指定用于获取插件描述的文件
     * @return A PluginDescription object containing metadata about the plugin, or null if the description could not be retrieved.
     * 返回包含插件元数据的PluginDescription对象，如果无法获取描述则返回null
     */
    override fun getPluginDescription(file: File): PluginDescription {
        // 初始化信息
        val description: PluginDescription?
        // 封装
        val jar = JarFile(file)
        // 得到文件
        val entry = jar.getJarEntry("plugin.yml") ?: jar.getJarEntry("plugin.conf")
        val stream = jar.getInputStream(entry)
        // 转换成configuration
        val config = Configuration.loadFromInputStream(
            stream,
            Configuration.getTypeFromExtension(entry.name.replace("plugin.", ""))
        )
        // 开始生成
        val d = object : PluginDescription() {
            override val name: String = config.getString("name") ?: throw PluginException("no plugin Name")
            override val dependencies: Dependencies = run<Dependencies> {
                val dependenciesConfig = config.getConfigurationSection("dependencies") ?: return@run Dependencies()
                val dependencies = Dependencies()
                val repositoriesC = dependenciesConfig.getMapList("repositories")
                val repos = LinkedList<MavenRepository>()
                repositoriesC.forEach {
                    repos.add(MavenRepository(it["name"] as String, it["url"] as String))
                }
                dependencies.repositories = repos
                dependencies.artifacts = dependenciesConfig.getStringList("artifacts")
                return@run dependencies
            }
            override val originFile: File = file
            override val authors: List<String> = config.getStringList("authors")
            override val depend: List<String> = config.getStringList("depend")
            override val softDepend: List<String> = config.getStringList("softDepend")
            override val main: String = config.getString("main") ?: throw PluginException("no plugin Main")
            override val version: String = config.getString("version") ?: throw PluginException("no plugin Version")
            override val loadBefore: List<String> = config.getStringList("loadBefore")
        }
        description = d
        description.dependenciesFiles.add(file.toURI().toURL())
        return description
    }

    /**
     * Opens (initializes) the specified plugin.
     * 启用指定的插件
     *
     * @param plugin The plugin to be opened.
     * 参数plugin 要启用的插件
     */
    override fun enablePlugin(plugin: Plugin) {
        if (plugin.enable) {
            log.error("${plugin.origin.name}已经被启动了！无法再次启动")
            return
        }
        try {
            plugin.onEnable()
            plugin.enable = true
            MinecraftServer.getGlobalEventHandler().addChild(plugin.eventNode)
        } catch (e: Exception) {
            log.error("${plugin.origin.name}在启动过程中出现了一些问题, $e")
        }
    }

    /**
     * Disables the specified plugin.
     * 禁用指定的插件
     *
     * @param plugin The plugin to be disabled.
     * 参数plugin 要禁用的插件
     */
    override fun disablePlugin(plugin: Plugin) {
        if (!plugin.enable) {
            log.error("${plugin.origin.name}已经被禁用了！")
            return
        }
        if (plugin.enable) {
            plugin.onDisable()
            plugin.enable = false
            MinecraftServer.getGlobalEventHandler().removeChild(plugin.eventNode)
            plugin.commands.forEach {
                MinecraftServer.getCommandManager().unregister(it)
                EventDispatcher.call(PluginCommandEvent.UnRegister(plugin, it))
            }
        }
    }

    /**
     * Activates the specified plugin.
     * 激活指定的插件
     *
     * @param plugin The plugin to be activated.
     * 参数plugin 要激活的插件
     */
    override fun activePlugin(plugin: Plugin) {
        if (plugin.active) {
            log.error("${plugin.origin.name}已经被激活了！")
            return
        }
        plugin.onActive()
        plugin.active = true
    }

    /**
     * Unsafe
     * Retrieves a class by its name, checking multiple class loaders.
     * 通过类名检索类，检查多个类加载器
     *
     * @param name The name of the class to retrieve.
     * 参数name 要检索的类名
     * @param include A set of class loaders to include in the search.
     * 参数ignore 要包含在搜索中的类加载器集合
     * @return The class object if found, or null if not found.
     * 返回类对象（如果找到），否则返回null
     */
    @SuppressWarnings
    fun getClassByName(name: String, ignore: Set<ClassLoader>): Class<*>? {
        var clazz1 = classes[name]
        if (clazz1 != null) {
            return clazz1
        } else {
            classLoaders.forEach { (_, v) ->
                if (ignore.contains(v)) return@forEach
                try {
                    clazz1 = v.findClass(name)
                } catch (_: ClassNotFoundException) {}
                if (clazz1 != null) {
                    return clazz1
                }
            }
        }
        return null
    }

    /**
     * Checks if the specified file should be ignored by the service.
     * 检查该服务是否应忽略指定的文件
     *
     * @param file The file to check.
     * 参数file 要检查的文件
     * @return True if the file should be ignored, false otherwise.
     * 如果该文件应被忽略返回true，否则返回false
     */
    override fun isFileIgnored(file: File): Boolean {
        return file.extension == "jar"
    }
}
