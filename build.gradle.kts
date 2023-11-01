plugins {
    kotlin("jvm") version "1.9.10" apply true
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("maven-publish")
}

group = "com.github.encryptsl.credit"
version = providers.gradleProperty("plugin_version").get()
description = providers.gradleProperty("plugin_description").get()

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
    compileOnly(kotlin("stdlib", "1.9.10"))
    compileOnly("me.lokka30:treasury-api:1.2.1")
    compileOnly("com.zaxxer:HikariCP:5.0.1")
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("org.jetbrains.exposed:exposed-core:0.44.0")
    compileOnly("org.jetbrains.exposed:exposed-jdbc:0.44.0")
    compileOnly("com.squareup.okhttp3:okhttp:4.10.0")

    implementation("org.bstats:bstats-bukkit:3.0.1")
    implementation("cloud.commandframework:cloud-paper:1.8.3")
    implementation("cloud.commandframework:cloud-annotations:1.8.3")

    testImplementation(kotlin("test"))
    testImplementation("com.zaxxer:HikariCP:5.0.1")
    testImplementation("org.xerial:sqlite-jdbc:3.42.0.0")
    testImplementation("org.jetbrains.exposed:exposed-core:0.42.0")
    testImplementation("org.jetbrains.exposed:exposed-jdbc:0.42.0")
    testImplementation("com.squareup.okhttp3:okhttp:4.10.0")
}

tasks {

    build {
        dependsOn(shadowJar)
    }

    test {
        useJUnitPlatform()
    }

    processResources {
        filesMatching("plugin.yml") {
            expand(project.properties)
        }
    }

    shadowJar {
        archiveFileName.set("${providers.gradleProperty("plugin_name").get()}-${providers.gradleProperty("plugin_version").get()}.jar")
        minimize {
            relocate("org.bstats", "encryptsl.cekuj.net.api.bstats")
            relocate("cloud.commandframework", "encryptsl.cekuj.net.cloud")
        }
    }
}

publishing {
    repositories {
        mavenLocal()
    }
    publications.create<MavenPublication>("libs").from(components["kotlin"])
}