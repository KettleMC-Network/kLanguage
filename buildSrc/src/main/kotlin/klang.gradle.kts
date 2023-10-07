plugins {
    id("java-library")
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