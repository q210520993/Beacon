package com.redstone.beacon.plugin

class PluginException: RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}