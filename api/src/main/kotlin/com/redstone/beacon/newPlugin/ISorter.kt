package com.redstone.beacon.newPlugin

interface ISorter {

    fun sort(list: List<Descriptor>): List<Descriptor>

}

class Result {
    // 这个是强依赖缺失列表 插件名称 -> 依赖信息
    val wrongDependencies = HashMap<String, List<Dependency.PluginDependency>>()
    // 这个是版本依赖缺失列表 插件名称 -> 依赖信息
    val wrongVersion = HashMap<String, List<Dependency.PluginDependency>>()
    // 正确处理后的
    val sortedPlugins = ArrayList<String>()
}