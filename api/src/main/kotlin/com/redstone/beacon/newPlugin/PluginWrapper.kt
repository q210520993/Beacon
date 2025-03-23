package com.redstone.beacon.newPlugin

class PluginWrapper(
    val pluginManager: PluginManager, val pluginDescriptor: Descriptor
) {

    lateinit var pluginFactory: PluginFactory

    lateinit var classLoader: PluginClassLoader
        private set

    lateinit var pluginState: PluginState

    // 插件缓存
    val plugin: Plugin by lazy {
        pluginFactory.create(this)
    }

}