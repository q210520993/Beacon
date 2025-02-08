package com.redstone.beacon.plugin

import com.redstone.beacon.plugin.simple.PluginClassLoader
import net.minestom.dependencies.ResolvedDependency

interface DependenciesLoader {

    fun loadDependencies(description: PluginDescription, classLoader: PluginClassLoader)
    fun loadDependicyToPlugin(description: PluginDescription, dependency: ResolvedDependency, classLoader: PluginClassLoader)


}