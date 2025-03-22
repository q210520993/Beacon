package com.redstone.beacon.utils


/**
 * @author Clok
 * @Date 2025/2/22 15:24
*/
class Graph<T : Any> {
    // 使用 LinkedHashMap 保持插入顺序
    private val adjacencyList = linkedMapOf<T, MutableList<T>>()
    private val inDegree = linkedMapOf<T, Int>()

    /**
     * 添加节点
     * @param node 图节点（插件描述）
     */
    fun addNode(node: T) {
        adjacencyList.getOrPut(node) { mutableListOf() }
        inDegree.getOrPut(node) { 0 }
    }

    /**
     * 添加有向边
     * @param from 边的起点（被依赖方）
     * @param to 边的终点（依赖方）
     */
    fun addEdge(from: T, to: T) {
        adjacencyList.getOrPut(from) { mutableListOf() }.add(to)
        inDegree[to] = inDegree.getOrDefault(to, 0) + 1
    }

    /**
     * 执行kahn拓扑排序
     * @return 排序后的节点列表
     * @throws IllegalStateException 如果存在循环依赖
     */
    fun topologicalSort(): List<T> {
        val queue = ArrayDeque<T>().apply {
            addAll(inDegree.filter { it.value == 0 }.keys)
        }
        val result = mutableListOf<T>()

        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            result.add(node)

            // 处理所有邻接节点
            adjacencyList[node]?.forEach { neighbor ->
                inDegree[neighbor] = inDegree[neighbor]!! - 1
                if (inDegree[neighbor] == 0) {
                    queue.add(neighbor)
                }
            }
        }

        // 检查循环依赖
        if (result.size != adjacencyList.size) {
            val cyclicNodes = adjacencyList.keys - result.toSet()
            throw IllegalStateException("发现循环依赖: ${cyclicNodes.joinToString()}")
        }

        return result
    }


}