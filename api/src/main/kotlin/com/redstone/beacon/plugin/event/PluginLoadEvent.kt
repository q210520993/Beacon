package com.redstone.beacon.plugin.event

import com.redstone.beacon.plugin.Plugin
import com.redstone.beacon.plugin.PluginLoadState

class PluginLoadEvent(val plugin: Plugin?, state: PluginLoadState) : PluginEvent