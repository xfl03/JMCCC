dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

pluginManagement {
    plugins {
        id("com.github.johnrengelman.shadow") version "7.1.2"
    }
}

rootProject.name = "jmccc-parent"
include(":jmccc-yggdrasil-authenticator")
include(":jmccc-mojang-api")
include(":jmccc")
include(":jmccc-mcdownloader")
include(":jmccc-cli")
