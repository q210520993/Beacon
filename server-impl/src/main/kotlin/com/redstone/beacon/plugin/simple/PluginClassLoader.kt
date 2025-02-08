package com.redstone.beacon.plugin.simple

import com.redstone.beacon.plugin.simple.JvmPluginLoader
import java.io.File
import java.net.URL
import java.net.URLClassLoader

class PluginClassLoader(val loader: JvmPluginLoader, parent: ClassLoader, val file: File) : URLClassLoader(arrayOf(file.toURI().toURL()), parent) {

    val classes = HashMap<String ,Class<*>>()

    var global = true

    public override fun addURL(url: URL) {
        super.addURL(url)
    }

    public override fun findClass(name: String): Class<*> {

        var clazz = classes[name]
        if (clazz == null) {
            if (global) {
                clazz = loader.classes[name]
            }
            if (clazz == null) {
                clazz = super.findClass(name)
                if (clazz != null) {
                    loader.classes[name] = clazz
                }
            }
            classes[name] = clazz!!
        }
        return clazz

    }

}