plugins {
    id("java-library")
    id("maven-publish")
}


java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val projectMainName = "klanguage"
group = "net.kettlemc.klanguage"
version = "1.0.0"

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.varoplugin.de/repository/maven-public/")
    }
    maven {
        url = uri("https://s01.oss.sonatype.org/content/repositories/iogithubalmighty-satan-1010")
    }
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.1")
    implementation("io.github.almighty-satan.slams:slams-minimessage:1.0.0")
    implementation("io.github.almighty-satan.slams:slams-parser-jackson:1.0.0")
    implementation("io.github.almighty-satan.jaskl:jaskl-hocon:1.4.1")
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.compileJava {
    options.encoding = "UTF-8"
}

tasks.javadoc {
    options.encoding = "UTF-8"
}

tasks.processResources {
    outputs.upToDateWhen { false }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

configurations {
    create("internal")
    configurations.getByName("api").extendsFrom(configurations.getByName("internal"))
}

tasks.jar {
    from({
        configurations.getByName("internal").map { if (it.isDirectory) it else zipTree(it) }
    })
    archiveFileName.set("${projectMainName}-${project.name}-${project.version}.jar")
}

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["java"])
            pom {
                name.set("kLanguage")
                description.set("Simple api for allowing players to choose their language")
                url.set("https://github.com/KettleMC-Network/kLanguage")
                licenses {
                    license {
                        name.set("GNU Lesser General Public License v2.1")
                        url.set("https://opensource.org/license/lgpl-2-1/")
                    }
                }
                developers {
                    developer {
                        name.set("LeStegii")
                        url.set("https://github.com/LeStegii")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/KettleMC-Network/kLanguage.git")
                    developerConnection.set("scm:git:ssh://github.com:KettleMC-Network/kLanguage.git")
                    url.set("https://github.com/KettleMC-Network/kLanguage")
                }
            }
            artifactId = "klanguage-${project.name}"
        }
        repositories {
            maven {
                setUrl("https://repo.kettlemc.net/repository/maven-releases/")
                credentials {
                    username = System.getenv("repouser")
                    password = System.getenv("repopassword")
                }
            }
        }
    }
}