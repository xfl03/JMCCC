plugins {
    id("com.github.johnrengelman.shadow")
}

description = "jmccc cli"

dependencies {
    implementation(project(":jmccc"))
    implementation(project(":jmccc-mcdownloader"))
    implementation(project(":jmccc-microsoft-authenticator"))
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4")
    implementation("com.google.code.gson:gson:2.10.1")
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