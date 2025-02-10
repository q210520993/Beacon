package com.redstone.beacon.plugin

import java.io.File
import java.nio.file.Path

interface PluginSource {
    fun getPath() : Path
    fun createFolder() : File
}