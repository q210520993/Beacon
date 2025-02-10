plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "Beacon"

include("api", "server-impl")
include("dependency")
