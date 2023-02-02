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
    implementation("org.slf4j:slf4j-nop:1.7.36")
}
tasks {
    shadowJar {
        manifest {
            attributes(
                "Main-Class" to "jmccc.cli.Main",
                "Specification-Title" to "JMCCC CLI",
                "Specification-Version" to "1",
                "Specification-Vendor" to "xfl03",
                "Implementation-Title" to "JMCCC CLI",
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "xfl03"
            )
        }
        minimize {
            exclude(dependency("commons-logging:commons-logging:.*"))
            exclude(dependency("org.ehcache.modules:ehcache-impl:.*"))
            exclude(dependency("org.slf4j:.*:.*"))
        }
    }
}