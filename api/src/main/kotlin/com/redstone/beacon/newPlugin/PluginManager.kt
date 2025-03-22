package com.redstone.beacon.newPlugin

import java.nio.file.Path

interface PluginManager {

    val root: Path

    fun getPlugin(): PluginWrapper?

    fun getPlugins(): Map<String, PluginWrapper>

    fun whichPlugin(clazz: Class<*>): PluginWrapper?

    fun getClassLoaders(): Map<String, ClassLoader>

    fun loadPlugins(): Map<String, PluginWrapper>

    fun loadPlugin(name: String): PluginState

    fun disablePlugin(name: String): PluginState

    fun disablePlugins()

    fun enablePlugin(name: String): PluginState

    fun enablePlugins()

}