package com.redstone.beacon.newPlugin

import java.net.URL

interface PluginLoader {
    fun isApplicable(url: URL): Boolean
    fun loadPlugin(url: URL, descriptor: Descriptor): ClassLoader?
}