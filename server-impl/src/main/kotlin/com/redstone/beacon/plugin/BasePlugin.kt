package com.redstone.beacon.plugin

import com.redstone.beacon.plugin.event.PluginCommandEvent
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import net.minestom.server.event.Event
import net.minestom.server.event.EventDispatcher
import net.minestom.server.event.EventNode
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

abstract class BasePlugin: Plugin {

    final override lateinit var origin: PluginDescription
    override lateinit var pluginLoader: PluginLoader
    override lateinit var pluginClassLoader: ClassLoader
    override lateinit var logger: ComponentLogger
    override lateinit var dataDirectory: Path
    override lateinit var originFile: File
    override val dependents: MutableSet<String> = HashSet()
    override var enable: Boolean = false
    override var active: Boolean = false
    override var commands: List<Command> = ArrayList()

    var isInit = false
        private set

    // 初始化插件的相关信息
    fun init(pluginLoader: PluginLoader, classLoader: ClassLoader, description: PluginDescription, dataFolder: Path, file: File) {
        if (!isInit) {
            isInit = true
            this.pluginLoader = pluginLoader
            this.pluginClassLoader = classLoader
            this.origin = description
            this.dataDirectory = dataFolder
            this.originFile = file
            this.logger = ComponentLogger.logger(origin.name)
            preTerminate()
        }
    }

    // 插件启用时的空实现
    override fun onEnable() {}

    // 插件禁用时的空实现
    override fun onDisable() {}

    // 服务器开启时激活插件的空实现
    override fun onActive() {}

    //插件init时
    override fun preTerminate() {}

    override val eventNode: EventNode<Event> by lazy {  EventNode.all(origin.name) }


    override fun getResource(fileName: String): InputStream? {
        return getResource(Paths.get(fileName))
    }

        override fun registryCommand(command: Command) {
        MinecraftServer.getCommandManager().register(command)
        (commands as ArrayList<Command>).add(command)
        EventDispatcher.call(PluginCommandEvent.Registry(this, command))
    }

    // 获取资源文件的输入流
    override fun getResource(target: Path): InputStream? {
        val targetFile = dataDirectory.resolve(target)
        try {
            // 如果目标文件不存在，则从插件包中复制资源到插件数据目录
            if (!Files.exists(targetFile)) {
                savePackagedResource(target)
            }

            return Files.newInputStream(targetFile)
        } catch (ex: IOException) {
            logger.info("读取资源 {} 时失败", target, ex)
            return null
        }
    }

    // 从插件包中获取资源文件
    override fun getPackagedResource(fileName: String): InputStream? {
        try {
            val url = pluginClassLoader.getResource(fileName)
            if (url == null) {
                logger.debug("未找到资源: {}", fileName)
                return null
            }

            return url.openConnection().getInputStream()
        } catch (ex: IOException) {
            logger.debug("加载资源 {} 失败", fileName, ex)
            return null
        }
    }

    // 从插件包中获取资源文件（使用路径形式）
    override fun getPackagedResource(target: Path): InputStream? {
        return getPackagedResource(target.toString().replace('\\', '/'))
    }

    // 将资源文件保存到插件数据目录中，覆盖已存在的文件
    override fun savePackagedResource(fileName: String): Boolean {
        return savePackagedResource(Paths.get(fileName))
    }

    // 将资源文件保存到插件数据目录中，覆盖已存在的文件
    override fun savePackagedResource(target: Path): Boolean {
        val targetFile = dataDirectory.resolve(target)
        try {
            getPackagedResource(target).use { `is` ->
                if (`is` == null) {
                    return false
                }
                // 创建父目录（如果不存在的话）
                Files.createDirectories(targetFile.parent)
                // 将资源文件复制到目标路径，并覆盖已存在的文件
                Files.copy(`is`, targetFile, StandardCopyOption.REPLACE_EXISTING)
                return true
            }
        } catch (ex: IOException) {
            logger.debug("保存资源 {} 时失败", target, ex)
            return false
        }
    }

}
