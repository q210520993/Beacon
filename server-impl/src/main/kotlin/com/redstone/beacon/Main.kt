package com.redstone.beacon

import com.redstone.beacon.DataObject.minestomData
import com.redstone.beacon.plugin.BeaconHook
import com.redstone.beacon.terminal.EasyTerminal
import net.minestom.server.MinecraftServer
import net.minestom.server.extras.bungee.BungeeCordProxy
import net.minestom.server.extras.lan.OpenToLAN
import net.minestom.server.extras.velocity.VelocityProxy
import net.minestom.server.utils.time.TimeUnit
import java.lang.String
import java.time.Duration

fun main() {

    //初始化服务器信息
    DataObject.setData()
    val networkData: MinestomData.Network = minestomData.network
    val proxyData: MinestomData.Proxy = minestomData.proxy
    val serverData: MinestomData.Server = minestomData.server
    //第一步，设置Property，启动terminal
    EasyTerminal.start()
    setProperty()
    //第二步开服与初始化插件系统实例
    val server = MinecraftServer.init()
    val beacon = BeaconHook(server)
    //第三步，进行proxy处理
    proxyHandle(proxyData, networkData)
    //第四步，启动性能测试
    runBenchMark(serverData)
    //第五步，启动插件！启动服务器！
    beacon.start()
    server.start(networkData.ip, networkData.port)
    //第六步，激活插件！设立一个终端关闭的任务
    beacon.active()
    MinecraftServer.getSchedulerManager().buildShutdownTask {
        EasyTerminal.stop()
    }
}
//第三步 启动跑跑基准测试
private fun runBenchMark(serverData: MinestomData.Server) {
    if (serverData.benchmark) {
        MinecraftServer.getBenchmarkManager().enable(Duration.of(10, TimeUnit.SECOND))
    }
}
//第二步
private fun proxyHandle(proxyData: MinestomData.Proxy, networkData: MinestomData.Network) {
    if (networkData.openToLan) {
        OpenToLAN.open()
    }
    if (proxyData.enable) {
        val proxyType: kotlin.String = proxyData.type

        if (proxyType.equals("velocity", ignoreCase = true)) {
            VelocityProxy.enable(proxyData.secret)
        } else if (proxyType.equals("bungeecord", ignoreCase = true)) {
            BungeeCordProxy.enable()
        }
    }
}
//第一步
private fun setProperty() {
    //设置服务器每秒的tps
    System.setProperty("minestom.tps",
        String.valueOf(minestomData.server.ticksPerSecond)
    )

    System.setProperty("minestom.chunk-view-distance",
        String.valueOf(minestomData.server.chunkViewDistance)
    )

    System.setProperty("minestom.entity-view-distance",
        String.valueOf(minestomData.server.entityViewDistance)
    )
}

