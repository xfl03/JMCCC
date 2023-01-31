dependencies {
    api(project(":jmccc"))
    implementation("org.tukaani:xz:1.9")
    implementation("org.apache.httpcomponents:httpasyncclient:4.1.5")
    implementation("org.ehcache.modules:ehcache-impl:3.10.8")
    implementation("javax.cache:cache-api:1.1.1")
    implementation("org.apache.commons:commons-compress:1.22")
    implementation("org.ow2.asm:asm:9.4")
}

description = "jmccc mcdownloader"

java {
    withJavadocJar()
}
