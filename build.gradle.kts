import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.0.0" apply true
    id("io.github.goooler.shadow") version "8.1.8"
    id("maven-publish")
}

group = "com.github.encryptsl"
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
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly(kotlin("stdlib", "2.0.0"))
    compileOnly("com.zaxxer:HikariCP:5.1.0")
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("org.jetbrains.exposed:exposed-core:0.52.0")
    compileOnly("org.jetbrains.exposed:exposed-jdbc:0.52.0")
    compileOnly("org.jetbrains.exposed:exposed-kotlin-datetime:0.52.0")
    compileOnly("com.github.CodingAir:TradeSystem:v2.5.3")
    compileOnly("com.github.CodingAir:CodingAPI:1.79")

    implementation("com.github.encryptsl:KMonoLib:1.0.1")
    implementation("io.github.miniplaceholders:miniplaceholders-kotlin-ext:2.2.3")

    testImplementation(kotlin("test", "2.0.0"))
    testImplementation("com.zaxxer:HikariCP:5.1.0")
    testImplementation("org.xerial:sqlite-jdbc:3.42.0.0")
    testImplementation("org.jetbrains.exposed:exposed-core:0.52.0")
    testImplementation("org.jetbrains.exposed:exposed-jdbc:0.52.0")
    testImplementation("com.squareup.okhttp3:okhttp:4.12.0")
}

sourceSets {
    getByName("main") {
        java {
            srcDir("src/main/java")
        }
        kotlin {
            srcDir("src/main/kotlin")
        }
    }
}

tasks {

    build {
        dependsOn(shadowJar)
    }

    test {
        useJUnitPlatform()
    }

    processResources {
        filesMatching(listOf("plugin.yml", "paper-plugin.yml")) {
            expand(project.properties)
        }
    }

    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
        options.compilerArgs.add("-Xlint:deprecation")
    }
    compileKotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    }

    shadowJar {
        archiveFileName.set("${providers.gradleProperty("plugin_name").get()}-${providers.gradleProperty("plugin_version").get()}.jar")
        manifest {
            attributes["paperweight-mappings-namespace"] = "spigot"
        }
        relocate("de.codingair.codingapi", "de.codingair.tradesystem.lib.codingapi")
        minimize {
            relocate("com.github.encryptsl.kmono.lib", "kmono-lib")
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