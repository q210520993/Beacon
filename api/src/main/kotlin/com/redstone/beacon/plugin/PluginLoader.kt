package com.redstone.beacon.plugin

import java.io.File

interface PluginLoader {


    fun loadPlugin(file: File): Plugin?

    fun getPluginDescription(file: File): PluginDescription?

    fun openPlugin(plugin: Plugin)

    fun disablePlugin(plugin: Plugin)

    fun activePlugin(plugin: Plugin)

    fun isFileIgnored(file: File): Boolean

}