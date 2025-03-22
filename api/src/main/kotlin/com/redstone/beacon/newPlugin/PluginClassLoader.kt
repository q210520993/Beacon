package com.redstone.beacon.newPlugin

import com.redstone.beacon.utils.safe
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.*

//strategy为加载顺序
open class PluginClassLoader(
    arrayURL: Array<URL>, parent: ClassLoader, val pluginManager: PluginManager, val strategy: List<DependencySource>, val descriptor: Descriptor
): URLClassLoader(arrayURL, parent) {

    val dependenciesClassLoaders = ArrayList<ClassLoader>()

    companion object {
        private val logger: Logger = LoggerFactory.getLogger("PluginClassLoader")
    }

    constructor(parent: ClassLoader, pluginManager: PluginManager, descriptor: Descriptor): this(emptyArray(), parent, pluginManager,
        listOf(DependencySource.PLUGIN, DependencySource.MAVEN,DependencySource.DEPENDENCIES, DependencySource.APPLICATION), descriptor
    )

    fun addFile(file: File) {
        safe {
            addURL(file.canonicalFile.toURI().toURL())
        }
    }

    fun findResourceFromDependency(name: String): URL? {
        logger.debug("Serach resource from plugin dependency '{}'", name)
        for (loader in dependenciesClassLoaders) {
            val url: URL? = loader.getResource(name)
            if (Objects.nonNull(url)) {
                return url
            }
        }
        return null
    }

}