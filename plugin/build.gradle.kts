// Super Smash Mobs Brawl Minecraft Plugin
// Gradle build configuration

plugins {
    kotlin("jvm") version "2.1.0"
    id("com.gradleup.shadow") version "8.3.5"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.ncorti.ktfmt.gradle") version "0.20.1"
}

// Project Information
group = "dev.betrix"
version = "0.1.0"
description = "Super Smash Mobs Brawl Minecraft Plugin"

// Repositories
repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://repo.flyte.gg/releases")
    maven("https://repo.panda-lang.org/releases")
}

// Dependencies
dependencies {
    // Minecraft
    compileOnly("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")
    
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    
    // Frameworks
    implementation("gg.flyte:twilight:1.1.19")
    implementation("dev.rollczi:litecommands-bukkit:3.9.7")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.22.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.22.0")
    
    // Utilities
    implementation("com.michael-bull.kotlin-result:kotlin-result:2.0.1")
}

// Kotlin Configuration
kotlin {
    jvmToolchain(21)
}

// Compiler Configuration
tasks.compileKotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-opt-in=kotlin.RequiresOptIn"
        )
    }
}

// Java Compiler Configuration
tasks.compileJava {
    options.apply {
        encoding = "UTF-8"
        compilerArgs.addAll(listOf("-parameters", "-Xlint:all", "-Xlint:-serial"))
    }
}

// Code Formatting Configuration (Prettier-like defaults)
ktfmt {
    // Use Google style as base but customize for prettier-like formatting
    googleStyle()
    
    // Prettier-like configuration
    maxWidth.set(80)           // Prettier default line width
    blockIndent.set(2)         // Prettier default indent (2 spaces)
    continuationIndent.set(2)  // Prettier default continuation indent
    removeUnusedImports.set(true)
}

// Task Configuration
tasks {
    // Resource Processing
    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
    
    // Build Configuration
    build {
        dependsOn("shadowJar")
    }
    
    // Development Server
    runServer {
        minecraftVersion("1.21.6")
        jvmArgs("-Xmx2G", "-Xms1G")
    }
    
    // Custom Tasks
    register("format") {
        group = "formatting"
        description = "Format all Kotlin code"
        dependsOn("ktfmtFormat")
    }
    
    register("formatCheck") {
        group = "verification"
        description = "Check if code is properly formatted"
        dependsOn("ktfmtCheck")
    }
    
    register("devBuild") {
        group = "build"
        description = "Quick build for development (skips formatting checks)"
        dependsOn("compileKotlin", "processResources", "shadowJar")
    }
}
