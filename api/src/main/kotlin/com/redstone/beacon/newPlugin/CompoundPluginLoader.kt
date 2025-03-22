package com.redstone.beacon.newPlugin

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL

class CompoundPluginLoader: PluginLoader {

    private val loaders = ArrayList<PluginLoader>()

    override fun isApplicable(url: URL): Boolean {
        for (loader in loaders) {
            if (loader.isApplicable(url)) {
                return true
            }
        }

        return false
    }

    override fun loadPlugin(url: URL, descriptor: Descriptor): ClassLoader {
        for (loader in loaders) {
            if (loader.isApplicable(url)) {
                logger.debug("'{}' is applicable for plugin '{}'", loader, url)
                try {
                    val classLoader = loader.loadPlugin(url, descriptor)
                    if (classLoader != null) {
                        return classLoader
                    }
                } catch (e: Exception) {
                    // log the exception and continue with the next loader
                    logger.error(e.message) // ?!
                }
            } else {
                logger.debug("'{}' is not applicable for plugin '{}'", loader, url)
            }
        }

        throw RuntimeException("No PluginLoader for plugin ${descriptor.name}")
    }

    fun addLoader(loader: PluginLoader): CompoundPluginLoader {
        loaders.add(loader)
        return this
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger("CompoundPluginLoader")
    }

}