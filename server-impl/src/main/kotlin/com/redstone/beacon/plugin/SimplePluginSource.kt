package com.redstone.beacon.plugin

import com.redstone.beacon.plugin.PluginManager.LOGGER
import java.io.File
import java.nio.file.Path

object SimplePluginSource: PluginSource {
    var file: File

    init {
        file = createFolder()
    }

    override fun getPath(): Path {
        return Path.of("plugins")
    }

    override fun createFolder(): File {
        val pluginFile = getPath().toFile()
        if (!pluginFile.exists()) {
            if (!pluginFile.mkdirs()) {
                LOGGER.error("无法找到或创建插件文件夹，插件将不会被加载！")
            }
        }
        return pluginFile
    }

}