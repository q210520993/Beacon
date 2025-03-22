package com.redstone.beacon.newPlugin

interface PluginFactory {
    fun create(pluginWrapper: PluginWrapper): Plugin
}
