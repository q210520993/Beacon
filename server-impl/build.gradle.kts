import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.0.0"

}

group = "com.redstone.beacon"
version = "1.0-SNAPSHOT"

dependencies {
    testImplementation(kotlin("test"))
    api(project(":api"))
    //terminal
    implementation("org.jline:jline-reader:3.25.0")
    implementation("org.jline:jline-terminal:3.25.0")
    implementation("org.jline:jline-terminal-jna:3.25.0")
    implementation("org.tinylog:tinylog-api:2.7.0")
    implementation("org.tinylog:tinylog-impl:2.7.0")
    implementation("org.tinylog:slf4j-tinylog:2.7.0")
    implementation("org.fusesource.jansi:jansi:2.4.1")
}

tasks.withType<ShadowJar> {
    manifest {
        attributes(
            "Main-Class" to "com.redstone.beacon.MainKt",
            "Multi-Release" to true
        )
    }
    mergeServiceFiles()
    relocate("org.tabooproject.", "com.redstone.taboolib.library.")
    relocate("kotlin.", "com.redstone.libs.kotlin.")
    relocate("org.google.code.gson.", "com.redstone.libs.gson.")
    relocate("kotlinx.", "com.redstone.libs.kotlinx  .")
}

tasks.test {
    useJUnitPlatform()
}