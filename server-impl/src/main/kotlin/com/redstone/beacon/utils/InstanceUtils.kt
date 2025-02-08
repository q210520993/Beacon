package com.redstone.beacon.utils

import net.minestom.server.instance.Chunk
import net.minestom.server.instance.Instance
import net.minestom.server.instance.LightingChunk
import net.minestom.server.utils.chunk.ChunkUtils
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

object InstanceUtils {
    val logger = LoggerFactory.getLogger("InstanceUtils")
    @JvmStatic
    fun instanceLighting(instance: Instance, range: Int) {
        val chunks = ArrayList<CompletableFuture<Chunk>>()

        CompletableFuture.runAsync {
            //堵塞线程
            CompletableFuture.allOf(*chunks.toTypedArray()).join()
            val nanoTime1 = System.nanoTime()
            //点亮！
            LightingChunk.relight(instance, instance.chunks)
            val nanoTime2 = System.nanoTime()
        }.thenRun {
            instance.saveChunksToStorage()
        }
    }
}