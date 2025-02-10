plugins {
    `java-library`
    id("java")
    id("io.github.goooler.shadow") version ("8.1.2")
    kotlin("jvm") version "2.0.0"
}

buildscript {
    repositories {
        mavenCentral()
        maven("https://plugins.gradle.org/m2/")
    }
}


allprojects {

    repositories {
        mavenLocal()
        maven("https://jitpack.io")
        maven("https://repo.tabooproject.org/repository/releases/")
        mavenCentral()
    }

    group = "com.redstone.beacon"
    version = "1.0"
}

subprojects {
    plugins.apply("java")
    plugins.apply("java-library")
    plugins.apply("io.github.goooler.shadow")

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked"))
    }

    tasks {
        compileJava {
            options.encoding = "UTF-8"
            options.compilerArgs.add("-Xpkginfo:always")
            java.sourceCompatibility = JavaVersion.VERSION_21
            java.targetCompatibility = JavaVersion.VERSION_21
        }
    }


}

//repositories {
//    mavenCentral()
//}
//
//dependencies {
//    testImplementation(kotlin("test"))
//}
//
//tasks.test {
//    useJUnitPlatform()
//}