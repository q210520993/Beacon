package com.redstone.beacon.newPlugin

import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

open class DefaultPluginLoader(private val pluginManager: PluginManager): PluginLoader {

    override fun isApplicable(url: URL): Boolean {
        val path = Path.of(url.toURI())
        return Files.exists(path) && Files.isRegularFile(path) && path.toString().lowercase(Locale.getDefault()).endsWith(".jar");
    }

    override fun loadPlugin(url: URL, descriptor: Descriptor): ClassLoader {
        val pluginClassLoader = PluginClassLoader(this::class.java.classLoader , pluginManager, descriptor)
        pluginClassLoader.addURL(url)

        return pluginClassLoader
    }

}