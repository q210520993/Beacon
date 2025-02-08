package com.redstone.beacon.plugin.event

import com.redstone.beacon.plugin.Plugin
import net.minestom.server.command.builder.Command

class PluginCommandEvent {
    class Registry(val plugin: Plugin, val command: Command) : PluginEvent
    class UnRegister(val plugin: Plugin, val command: Command) : PluginEvent
}