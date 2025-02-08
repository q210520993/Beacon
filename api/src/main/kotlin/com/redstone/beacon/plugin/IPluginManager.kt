package com.redstone.beacon.plugin

import java.io.File

interface IPluginManager {
    //加载插件
    fun loadPlugin(file: File): Plugin?

    fun loadPlugins()

    fun enablePlugin(plugin: Plugin)

    fun disablePlugin(plugin: Plugin)

    fun enablePlugins()
    fun disablePlugins()
    fun activePlugin(plugin: Plugin): Plugin?
    fun activePlugins()
}