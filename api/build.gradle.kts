plugins {
    kotlin("jvm") version "2.0.0"
}

group = "com.redstone.beacon"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.tabooproject.org/repository/releases/")
}

dependencies {
    api("net.minestom:minestom-snapshots:9803f2bfe3")
    api("com.github.Minestom:DependencyGetter:v1.0.1")

    //都是为了taboolib的configuration
    api("org.yaml:snakeyaml:2.2")
    api("com.typesafe:config:1.4.3")
    api("com.electronwill.night-config:core:3.6.7")
    api("com.electronwill.night-config:toml:3.6.7")
    api("com.electronwill.night-config:json:3.6.7")
    api("com.electronwill.night-config:hocon:3.6.7")
    api("com.electronwill.night-config:core-conversion:6.0.0")
    api("org.tabooproject.reflex:analyser:1.0.23")
    api("org.tabooproject.reflex:fast-instance-getter:1.0.23")
    api("org.tabooproject.reflex:reflex:1.0.23") // 需要 analyser 模块
    api("com.github.Minestom:DependencyGetter:v1.0.1")
    api("com.google.guava:guava:32.0.0-android")
    api("com.google.code.gson:gson:2.11.0")

}

tasks.test {
    useJUnitPlatform()
}
