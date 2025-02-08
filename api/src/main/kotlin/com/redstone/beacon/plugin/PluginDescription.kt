package com.redstone.beacon.plugin

import java.io.File
import java.net.URL
import java.util.LinkedList

abstract class PluginDescription {
    abstract val name: String
    abstract val main: String
    abstract val version: String
    abstract val dependencies: Dependencies?
    abstract val depend: List<String>?
    abstract val softDepend: List<String>?
    abstract val authors: List<String>?
    abstract val loadBefore: List<String>?
    abstract val originFile: File?
    val dependenciesFiles = LinkedList<URL>()
}