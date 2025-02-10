plugins {
    kotlin("jvm")
}

group = "com.redstone.beacon"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    // Add shrinkwrap resolver
    api("org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-depchain:3.1.4")
    compileOnly("org.slf4j:slf4j-api:2.0.7")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}