package com.redstone.beacon.newPlugin

open class Plugin(val pluginWrapper: PluginWrapper) {

    /**
     * 服务器初始化前调用
     * 这里可以随便mixin Minestom
     */
    fun onPreServerInit() {}

    /**
     * 插件加载时调用
     */
    fun onPostServerInit() {}

    /**
     * 插件激活时调用
     */
    fun onActive() {}

    /**
     * 插件禁用时调用
     */
    fun onDisable() {}

}
