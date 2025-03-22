package com.redstone.beacon.newPlugin

import com.redstone.beacon.utils.safe
import net.minestom.dependencies.DependencyGetter
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

open class MavenResolver(
    private val pluginManager: PluginManager,
    private val targetPath: Path
): ConcurrentHashMap<String, DependencyGetter>() {

    override fun get(key: String): DependencyGetter {

        val value = super.get(key)
        if (value != null) return value

        if (key !in pluginManager.loadPlugins()) {
            throw IllegalArgumentException("Plugin with key '$key' is not loaded or does not exist.")
        }

        val getter = DependencyGetter()
        set(key, getter)
        return getter
    }

    // 下载
    fun download(pluginWrapper: PluginWrapper) {
        safe {
            pluginWrapper.pluginDescriptor.dependencies.forEach {
                if (it is Dependency.MavenDependency) {
                    this[pluginWrapper.pluginDescriptor.name].get(it.artifacts, targetPath)
                }
            }
        }
    }

    fun getClassLoader(name: String): ClassLoader {
        return this[name].classLoader
    }

    // 这是在初始化完PluginWrapper直接把依赖项注入进去
    fun injectDependencyToPlugin(pluginWrapper: PluginWrapper) {
        pluginWrapper.classLoader.dependenciesClassLoaders.add(
            getClassLoader(pluginWrapper.pluginDescriptor.name)
        )
    }

}