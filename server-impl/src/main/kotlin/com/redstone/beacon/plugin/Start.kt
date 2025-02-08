package com.redstone.beacon.plugin

import net.minestom.server.MinecraftServer

class BeaconHook(val process: MinecraftServer) {

    fun start() {
        PluginManager.loadPlugins()

        MinecraftServer.getSchedulerManager().buildShutdownTask {
            close()
        }

    }

    fun active() {
        PluginManager.activePlugins()
    }

    private fun close() {
        PluginManager.disablePlugins()
    }


}