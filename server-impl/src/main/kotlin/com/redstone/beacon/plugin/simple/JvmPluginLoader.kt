package com.redstone.beacon.plugin.simple

import com.redstone.beacon.plugin.*
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

object JvmPluginLoader: PluginLoader {

    val classes = ConcurrentHashMap<String, Class<*>>()

    val log = ComponentLogger.logger("JVMPluginLoader")

    val classLoaders = ConcurrentHashMap<String, PluginClassLoader>()

    override fun loadPlugin(file: File): Plugin? {
        val description = getPluginDescription(file)
        var pluginV: Plugin? = null
        description.let { info ->
            log.info("正在加载${info.name}")
            val dataFolder = Path.of(file.parentFile.path, description.name)
            val main = info.main
            val classLoader = PluginClassLoader(this, this.javaClass.classLoader, file)
            //依赖处理
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
                    plugin.init(this, classLoader, description, dataFolder ,file)
                    pluginV = plugin
                }catch (e: ClassCastException) {
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
        }?: PluginLoadEvent(null, PluginLoadState.ERROR)
        EventDispatcher.call(event)
        return pluginV
    }

    override fun getPluginDescription(file: File): PluginDescription {
        var description: PluginDescription? = null
        val jar = JarFile(file)
        val entry = jar.getJarEntry("plugin.yml") ?: jar.getJarEntry("plugin.conf")
        val stream = jar.getInputStream(entry)
        val config = Configuration.loadFromInputStream(
            stream,
            Configuration.getTypeFromExtension(entry.name.replace("plugin.", ""))
        )
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

    override fun openPlugin(plugin: Plugin) {
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

    override fun disablePlugin(plugin: Plugin) {
        if (!plugin.enable) {
            log.error("${plugin.origin.name}已经被禁用了！")
            return
        }
        if(plugin.enable) {

            plugin.onDisable()
            plugin.enable = false
            MinecraftServer.getGlobalEventHandler().removeChild(plugin.eventNode)
            plugin.commands.forEach {
                MinecraftServer.getCommandManager().unregister(it)
                EventDispatcher.call(PluginCommandEvent.UnRegister(plugin, it))
            }

        }
    }

    override fun activePlugin(plugin: Plugin) {
        if (plugin.active) {
            log.error("${plugin.origin.name}已经被激活了！")
            return
        }
        plugin.onActive()
        plugin.active = true
    }

    fun getClassByName(name: String) : Class<*>? {
        var clazz1 = classes[name]
        if (clazz1 != null) {
            return clazz1
        } else {
            classLoaders.forEach { (_, v) ->
                try {
                    clazz1 = v.findClass(name)
                } catch (e: ClassNotFoundException) {}
                if (clazz1 != null) {
                    return clazz1
                }
            }
        }
        return null
    }

    override fun isFileIgnored(file: File): Boolean {
        return file.extension == "jar"
    }

}