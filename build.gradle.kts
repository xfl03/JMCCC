plugins {
    base
    id("io.github.gradle-nexus.publish-plugin")
}

group = "dev.3-3"
version = "3.1.1-SNAPSHOT"

subprojects {
    //Real subproject DSL is located at `buildSrc/src/main/kotlin/dev.3-3.jmccc.gradle.kts`
    apply(plugin = "dev.3-3.jmccc")
}

nexusPublishing {
    repositories {
        sonatype {
            // https://s01.oss.sonatype.org/#stagingRepositories
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("OSSRH_USERNAME"))
            password.set(System.getenv("OSSRH_PASSWORD"))
        }
    }
}