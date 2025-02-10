package com.redstone.beacon.plugin

import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.minestom.server.command.builder.Command
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import java.io.File
import java.io.InputStream
import java.nio.file.Path

interface Plugin {

    var active: Boolean
    var enable: Boolean

    var commands: List<Command>

    /**
     * 一个可修改的依赖插件列表
     */
    val dependents: MutableSet<String>

    /**
     * 获取插件加载器
     */
    val pluginService: PluginService

    /**
     * 插件启用时调用
     */
    fun onEnable()

    /**
     * 插件激活时调用
     */
    fun onActive()

    /**
     * 插件终止前调用
     */
    fun preTerminate()

    /**
     * 插件禁用时调用
     */
    fun onDisable()

    /**
     * 获取插件的类加载器
     */
    val pluginClassLoader: ClassLoader

    /**
     * 获取插件的原始信息
     */
    val origin: PluginDescription

    /**
     * 获取插件的日志记录器
     */
    val logger: ComponentLogger

    /**
     * 获取插件的事件节点
     */
    val eventNode: EventNode<Event>

    /**
     * 获取插件的源文件
     */
    val originFile: File

    /**
     * 获取插件的数据目录
     */
    val dataDirectory: Path

    /**
     * 从扩展目录中获取资源文件，如果在扩展目录中不存在该文件，则从 JAR 包中获取
     * 调用者需要负责关闭返回的 [InputStream]
     *
     * @param fileName 要读取的文件名
     * @return 文件内容，如果读取文件时出现问题则返回 null
     */
    fun getResource(fileName: String): InputStream?

    /**
     * 从扩展目录中获取资源文件，如果在扩展目录中不存在该文件，则从 JAR 包中获取
     * 调用者需要负责关闭返回的 [InputStream]
     *
     * @param target 要读取的文件路径
     * @return 文件内容，如果读取文件时出现问题则返回 null
     */
    fun getResource(target: Path): InputStream?

    /**
     * 从插件 JAR 包内部获取资源文件
     * 调用者需要负责关闭返回的 [InputStream]
     *
     * @param fileName 要读取的文件名
     * @return 文件内容，如果读取文件时出现问题则返回 null
     */
    fun getPackagedResource(fileName: String): InputStream?

    /**
     * 从插件 JAR 包内部获取资源文件
     * 调用者需要负责关闭返回的 [InputStream]
     *
     * @param target 要读取的文件路径
     * @return 文件内容，如果读取文件时出现问题则返回 null
     */
    fun getPackagedResource(target: Path): InputStream?

    /**
     * 将资源文件复制到扩展目录，替换任何已存在的文件
     *
     * @param fileName 要保存的资源文件名
     * @return 如果资源文件成功保存则返回 true，失败则返回 false
     */
    fun savePackagedResource(fileName: String): Boolean

    /**
     * 将资源文件复制到扩展目录，替换任何已存在的文件
     *
     * @param target 要保存的资源文件路径
     * @return 如果资源文件成功保存则返回 true，失败则返回 false
     */
    fun savePackagedResource(target: Path): Boolean

    fun registryCommand(command: Command)

}
