package com.redstone.beacon.provider

import net.minestom.server.entity.Player
import net.minestom.server.network.player.GameProfile
import net.minestom.server.network.player.PlayerConnection

/**
*   此接口表示一个未实现的Player初始化接口，你需要自行实现它，不然会得到警告
*/
interface PlayerProvider : Provider{

    /**
     * 初始化玩家的对象所形成的对象
     */
    fun createPlayer(p0: PlayerConnection, p1: GameProfile): Player

    /**
     * 在玩家初始化之前调用，它可用于在任何连接之前踢玩家或更改他的最终用户名/uuid
     */
    fun preLoginSetter(p0: GameProfile): GameProfile

    override fun getFieldName(): String {
        return "playerProvider"
    }

}