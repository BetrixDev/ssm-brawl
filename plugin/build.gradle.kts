plugins {
    kotlin("jvm") version "2.1.0"
    id("com.gradleup.shadow") version "8.3.5"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
}

group = "dev.betrix"
version = "0.1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://repo.flyte.gg/releases")
    maven { url = uri("https://repo.panda-lang.org/releases") }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("gg.flyte:twilight:1.1.19")
    implementation("dev.rollczi:litecommands-bukkit:3.9.7")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.22.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.22.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("com.michael-bull.kotlin-result:kotlin-result:2.0.1")
}

// Kotlin and Java configuration
val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

// ktlint configuration
ktlint {
    version.set("1.0.1")
    debug.set(false)
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(false)
    
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}

// Task configurations
tasks {
    compileJava {
        options.apply {
            encoding = "UTF-8"
            compilerArgs.addAll(listOf("-parameters", "-Xlint:all", "-Xlint:-serial"))
        }
    }
    
    compileKotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
            freeCompilerArgs.addAll(
                "-Xjsr305=strict",
                "-opt-in=kotlin.RequiresOptIn"
            )
        }
    }
    
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21.6")
        jvmArgs("-Xmx2G", "-Xms1G")
    }
    
    build {
        dependsOn("shadowJar")
    }
    
    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
    
    // Custom task to run all code quality checks
    register("codeQuality") {
        group = "verification"
        description = "Runs all code quality checks (ktlint)"
        dependsOn("ktlintCheck")
    }
    
    // Custom task to apply all code formatting
    register("formatCode") {
        group = "formatting"
        description = "Applies all code formatting (ktlint)"
        dependsOn("ktlintFormat")
    }
    
    // Make check task depend on code quality
    check {
        dependsOn("codeQuality")
    }
    
    // Development build without formatting checks
    register("devBuild") {
        group = "build"
        description = "Builds the plugin without running code quality checks (for development)"
        dependsOn("compileKotlin", "processResources", "shadowJar")
    }
}
