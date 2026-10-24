plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "connected-games-services"

include("gateway", "identity", "catalog", "match", "tournament", "stats", "architecture-rules")
