dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

pluginManagement {
    plugins {
        id("com.github.johnrengelman.shadow") version "7.1.2"
        id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    }
}

rootProject.name = "JMCCC"
include(":jmccc-yggdrasil-authenticator")
include(":jmccc-mojang-api")
include(":jmccc")
include(":jmccc-mcdownloader")
include(":jmccc-cli")
include("jmccc-microsoft-authenticator")
