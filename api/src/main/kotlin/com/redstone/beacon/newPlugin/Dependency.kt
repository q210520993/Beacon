package com.redstone.beacon.newPlugin

import com.github.zafarkhaja.semver.Version

sealed class Dependency {

    abstract fun getClassLoader(): ClassLoader?

    data class PluginDependency(
        val pluginManager: PluginManager,
        val pluginId: String,
        val version: Version,
        val versionLimitType: VersionCheckType,
        val optional: Boolean = true
    ) : Dependency() {
        override fun getClassLoader(): ClassLoader? {
            return pluginManager.getClassLoaders()[pluginId]
        }
    }

    // 支持多仓库和依赖项的 Maven 依赖
    data class MavenDependency(
        val pluginManager: PluginManager,
        val repositories: List<MavenRepository> = emptyList(),
        val artifacts: List<String> = emptyList(),
        val resolverName: String
    ) : Dependency() {
        override fun getClassLoader(): ClassLoader? {
            TODO("Not yet implemented")
        }
    }

    companion object {

        data class MavenRepository(val name: String, val url: String)
    }

}

