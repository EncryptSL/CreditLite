plugins {
    kotlin("jvm") version "1.9.23" apply true
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
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly(kotlin("stdlib", "1.9.23"))
    compileOnly("com.zaxxer:HikariCP:5.1.0")
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("org.jetbrains.exposed:exposed-core:0.48.0")
    compileOnly("org.jetbrains.exposed:exposed-jdbc:0.48.0")
    compileOnly("com.github.CodingAir:TradeSystem:v2.5.3")
    compileOnly("com.github.CodingAir:CodingAPI:1.79")

    implementation("org.incendo:cloud-paper:2.0.0-beta.2")
    implementation("org.incendo:cloud-annotations:2.0.0-beta.2")
    implementation("io.github.miniplaceholders:miniplaceholders-kotlin-ext:2.2.3")

    testImplementation(kotlin("test", "1.9.23"))
    testImplementation("com.zaxxer:HikariCP:5.1.0")
    testImplementation("org.xerial:sqlite-jdbc:3.42.0.0")
    testImplementation("org.jetbrains.exposed:exposed-core:0.48.0")
    testImplementation("org.jetbrains.exposed:exposed-jdbc:0.48.0")
    testImplementation("com.squareup.okhttp3:okhttp:4.12.0")
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


        relocate("de.codingair.codingapi", "de.codingair.tradesystem.lib.codingapi")
        minimize {
            relocate("org.incendo.cloud", "incendo-cloud")
        }
    }
}

publishing {
    repositories {
        mavenLocal()
    }
    publications.create<MavenPublication>("libs").from(components["kotlin"])
}