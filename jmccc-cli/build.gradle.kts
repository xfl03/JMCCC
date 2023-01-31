plugins {
    id("com.github.johnrengelman.shadow")
}

description = "jmccc cli"

dependencies {
    implementation(project(":jmccc"))
    implementation(project(":jmccc-mcdownloader"))
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4")
}
tasks {
    shadowJar {
        manifest {
            attributes("Main-Class" to "jmccc.cli.Main")
        }
        minimize {
            exclude(dependency("commons-logging:commons-logging:.*"))
            exclude(dependency("org.ehcache.modules:ehcache-impl:.*"))
        }
    }
}