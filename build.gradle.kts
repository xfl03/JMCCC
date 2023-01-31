plugins {
    base
}

version = "3.0.2"

subprojects {
    //Real subproject DSL is located at `buildSrc/src/main/kotlin/dev.3-3.jmccc.gradle.kts`
    apply(plugin = "dev.3-3.jmccc")
}