package com.redstone.beacon.plugin.simple

import com.redstone.beacon.plugin.DependenciesLoader
import com.redstone.beacon.plugin.PluginDescription
import com.redstone.beacon.plugin.PluginManager
import net.minestom.dependencies.DependencyGetter
import net.minestom.dependencies.ResolvedDependency
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object SimpleDependenciesLoader: DependenciesLoader {
    private val log: Logger = LoggerFactory.getLogger(SimpleDependenciesLoader::class.java)

    override fun loadDependencies(description: PluginDescription, classLoader: PluginClassLoader) {
        val dependency = DependencyGetter()
        dependency.addMavenResolver(description.dependencies!!.repositories)
        description.dependencies!!.artifacts.forEach {
            val resolver = dependency.get(it, PluginManager.libsFile.toPath())
            loadDependicyToPlugin(description, resolver, classLoader)
            log.trace("依赖：{}", resolver)
        }
        description.dependencies
    }

    override fun loadDependicyToPlugin(
        description: PluginDescription,
        dependency: ResolvedDependency,
        classLoader: PluginClassLoader
    ) {
        val location = dependency.contentsLocation
        description.dependenciesFiles.add(location)
        classLoader.addURL(location)
        log.trace(
            "添加依赖 {} 到插件 {} 类路径",
            location.toExternalForm(),
            description.main
        )

        // 递归添加完整的依赖树
        if (dependency.subdependencies.isNotEmpty()) {
            log.trace("依赖 {} 有子依赖，正在添加...", location.toExternalForm())
            for (sub in dependency.subdependencies) {
                loadDependicyToPlugin(description, sub, classLoader)
            }
            log.trace("依赖 {} 的子依赖已添加", location.toExternalForm())
        }
    }
}