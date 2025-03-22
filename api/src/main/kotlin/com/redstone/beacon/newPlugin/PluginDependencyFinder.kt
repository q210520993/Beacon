package com.redstone.beacon.newPlugin

interface PluginDependencyFinder {

    fun findDependency(map: Map<String, Any?>): Dependency?

}