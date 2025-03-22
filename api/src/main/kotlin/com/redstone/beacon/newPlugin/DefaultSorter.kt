package com.redstone.beacon.newPlugin

import com.redstone.beacon.utils.Graph

class DefaultSorter: ISorter {

    val versionChecker = DefaultVersionChecker()

    override fun sort(list: List<Descriptor>): List<Descriptor> {
        val descriptorMap = list.associateBy { it.name }
        val graph = Graph<Descriptor>().apply { list.forEach { addNode(it) } }

        list.forEach { descriptor ->
            descriptor.dependencies.forEach { dependency ->
                if (dependency is Dependency.PluginDependency) {
                    handleDependency(descriptor, dependency, descriptorMap, graph, versionChecker)
                }
            }
        }

        return graph.topologicalSort()
    }

    private fun handleDependency(
        current: Descriptor,
        dependency: Dependency.PluginDependency,
        descriptorMap: Map<String, Descriptor>,
        graph: Graph<Descriptor>,
        versionChecker: VersionChecker
    ) {
        // 1. 检查依赖是否存在
        val targetDescriptor = descriptorMap[dependency.pluginId]
        if (targetDescriptor == null) {
            if (!dependency.optional) {
                throw IllegalStateException(
                    "插件 [${current.name}] 的强依赖 [${dependency.pluginId}] 缺失"
                )
            }
            return // 软依赖不存在，直接跳过
        }

        // 2. 检查版本是否匹配
        val checkResult = versionChecker.check(
            type = dependency.versionLimitType,
            expected = dependency.version.toString(),
            actual = targetDescriptor.version.toString()
        )

        // 3. 处理版本检查结果
        if (!checkResult.isSuccess) {
            if (!dependency.optional) {
                throw IllegalStateException(buildVersionErrorMsg(current, dependency, targetDescriptor))
            }
            return // 软依赖版本不匹配，跳过
        }

        // 4. 添加依赖关系到图
        graph.addEdge(from = targetDescriptor, to = current)
    }

    private fun buildVersionErrorMsg(
        current: Descriptor,
        dependency: Dependency.PluginDependency,
        target: Descriptor
    ): String {
        return """
            |插件 [${current.name}] 的强依赖 [${dependency.pluginId}] 版本不满足！
            |要求版本: ${dependency.versionLimitType} ${dependency.version}
            |实际版本: ${target.version}
        """.trimMargin()
    }


}