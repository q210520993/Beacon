package com.redstone.beacon.plugin

import java.io.File

/**
 * Interface for managing plugins with capabilities to load, open, activate, disable, and get descriptions of plugins.
 * This interface also provides functionality to check if a file should be ignored.
 * 此接口为插件的一条龙服务
 * 接口用于管理插件，功能包括加载、打开、激活、禁用和获取插件插件
 * 该接口还提供检查文件是否应被忽略的功能
 */
interface PluginService {

    /**
     * Retrieves the key identifying this service.
     * 获取标识此服务的键。
     *
     * @return A string representing the unique key for this service.
     * 返回一个字符串，表示该服务的唯一键。
     */
    fun getKey(): String

    /**
     * Loads a plugin from the specified file.
     * 从指定文件加载插件。
     *
     * @param file The file from which the plugin is to be loaded.
     * 参数file 指定用于加载插件的文件。
     * @return The loaded Plugin object or null if the plugin could not be loaded.
     * 返回加载的插件对象，如果无法加载插件则返回null。
     */
    fun loadPlugin(file: File): Plugin?

    /**
     * Retrieves the description of a plugin from the specified file.
     * 从指定文件获取插件的描述。
     *
     * @param file The file from which the plugin description is to be retrieved.
     * 参数file 指定用于获取插件描述的文件。
     * @return A PluginDescription object containing metadata about the plugin, or null if the description could not be retrieved.
     * 返回包含插件元数据的PluginDescription对象，如果无法获取描述则返回null。
     */
    fun getPluginDescription(file: File): PluginDescription?

    /**
     * Opens (initializes) the specified plugin.
     * 打开（初始化）指定的插件。
     *
     * @param plugin The plugin to be opened.
     * 参数plugin 要打开的插件。
     */
    fun enablePlugin(plugin: Plugin)

    /**
     * Disables the specified plugin.
     * 禁用指定的插件。
     *
     * @param plugin The plugin to be disabled.
     * 参数plugin 要禁用的插件。
     */
    fun disablePlugin(plugin: Plugin)

    /**
     * Activates the specified plugin.
     * 激活指定的插件。
     *
     * @param plugin The plugin to be activated.
     * 参数plugin 要激活的插件。
     */
    fun activePlugin(plugin: Plugin)

    /**
     * Checks if the specified file should be ignored by the service.
     * 检查该服务是否应忽略指定的文件。
     *
     * @param file The file to check.
     * 参数file 要检查的文件。
     * @return True if the file should be ignored, false otherwise.
     * 如果该文件应被忽略返回true，否则返回false。
     */
    fun isFileIgnored(file: File): Boolean
}
