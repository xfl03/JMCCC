plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    testImplementation("junit:junit:4.13.1")
}

group = "dev.3-3"
version = rootProject.version

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
    withJavadocJar()
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
    javadoc {
        if (JavaVersion.current().isJava9Compatible) {
            (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
        }
        options {
            encoding = "UTF-8"
            (this as CoreJavadocOptions).addStringOption("Xdoclint:none", "-quiet")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            if (tasks.findByName("shadowJar") != null) {
                artifact(tasks["shadowJar"])
            }
            pom {
                description.set("JMCCC is a powerful open-source library for launching and downloading Minecraft.")
                url.set("https://github.com/xfl03/JMCCC")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://github.com/xfl03/JMCCC/LICENSE")
                        distribution.set("repo")
                        comments.set(
                            """
                            The MIT License (MIT)
            
                            Copyright (c) 2016-2023 yushijinhun, xfl03 and contributors
            
                            Permission is hereby granted, free of charge, to any person obtaining a copy
                            of this software and associated documentation files (the "Software"), to deal
                            in the Software without restriction, including without limitation the rights
                            to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
                            copies of the Software, and to permit persons to whom the Software is
                            furnished to do so, subject to the following conditions:
            
                            The above copyright notice and this permission notice shall be included in all
                            copies or substantial portions of the Software.
            
                            THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
                            IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
                            FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
                            AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
                            LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
                            OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
                            SOFTWARE.
                        """.trimIndent()
                        )
                    }
                }
                developers {
                    developer {
                        id.set("darkyoooooo")
                        name.set("Qiyun Zhou")
                        email.set("2569766005@qq.com")
                        url.set("https://github.com/darkyoooooo")
                    }
                    developer {
                        id.set("yushijinhun")
                        name.set("Haowei Wen")
                        email.set("yushijinhun@gmail.com")
                        url.set("https://github.com/yushijinhun")
                    }
                    developer {
                        id.set("xfl03")
                        name.set("xfl03")
                        email.set("moe@3-3.dev")
                        url.set("https://github.com/xfl03")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/xfl03/JMCCC.git")
                    developerConnection.set("scm:git:git@github.com:xfl03/JMCCC.git")
                    tag.set("master")
                    url.set("https://github.com/xfl03/JMCCC")
                }
                issueManagement {
                    system.set("GitHub Issues")
                    url.set("https://github.com/xfl03/JMCCC/issues")
                }
            }
        }
    }
    repositories {
        maven {
            name = "ossrh"
            val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            credentials {
                username = System.getenv("OSSRH_USERNAME")
                password = System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}