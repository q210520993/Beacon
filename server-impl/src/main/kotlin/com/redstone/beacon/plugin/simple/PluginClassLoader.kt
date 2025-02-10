package com.redstone.beacon.plugin.simple

import java.io.File
import java.net.URL
import java.net.URLClassLoader

class PluginClassLoader(
    val loader: JavaPluginService,
    parent: ClassLoader,
    file: File
) : URLClassLoader(arrayOf(file.toURI().toURL()), parent) {
    private val classes = HashMap<String ,Class<*>>()
    private var global = true

    /**
     * 加载类的方法。Attempts to load a class.
     * @param name 类的名字。The name of the class.
     * @param resolve 是否解析类。Whether to resolve the class.
     * @return 已加载的类。The loaded class.
     */
    @Synchronized
    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        var clazz = findLoadedClass(name)
        if (clazz == null) { //看类是否被加载过
            clazz = classes[name]
            if (clazz == null) {
                clazz = try {
                    findClass(name)
                } catch (e: ClassNotFoundException) {
                    parent.loadClass(name) //从系统层加载
                }
            }
            classes[name] = clazz // 添加到本地缓存
        }
        if (resolve) {
            resolveClass(clazz) // 解析类
        }
        return clazz
    }

    @Synchronized
    public override fun findClass(name: String): Class<*> {
        var clazz = classes[name]
        if (clazz != null) return clazz

        try {
            clazz = super.findClass(name)

            if (clazz != null) {
                loader.classes[name] = clazz
            }
        } catch (e: ClassNotFoundException) {
            clazz = JavaPluginService.classes[name]
            if (clazz != null) {
                classes[name] = clazz
                println("Class found and added to classes: $clazz")
            } else {
                throw ClassNotFoundException("Class not found: $name")
            }
        }
        return clazz!!

    }

    /**
    * 这是bukkit的findClass，其目的最主要的是为了插件间类的沟通
    * */
    @SuppressWarnings
    fun bukkitFindClass(name: String): Class<*> {
        var clazz = classes[name]
        if (clazz == null) {
            if (global) {
                clazz = JavaPluginService.getClassByName(name, setOf(this))
            }
            classes[name] = clazz!!
        }
        return clazz
    }

    public override fun addURL(url: URL) {
        super.addURL(url)
    }


}