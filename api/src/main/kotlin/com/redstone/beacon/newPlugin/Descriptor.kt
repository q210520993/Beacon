package com.redstone.beacon.newPlugin

import com.github.zafarkhaja.semver.Version

interface Descriptor {
    val name: String
    val main: String
    val version: Version
    val dependencies: List<Dependency>
}