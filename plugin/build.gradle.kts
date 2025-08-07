plugins {
    kotlin("jvm") version "2.2.0-RC2"
    kotlin("plugin.serialization") version "2.2.0"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.ncorti.ktfmt.gradle") version "0.23.0"
}

group = "dev.betrix"

version = "0.1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") { name = "papermc-repo" }
    maven("https://oss.sonatype.org/content/groups/public/") { name = "sonatype" }
    maven("https://repo.flyte.gg/releases")
    maven { url = uri("https://repo.panda-lang.org/releases") }
    maven("https://repo.md-5.net/content/groups/public/") {
        content { includeGroup("me.libraryaddict.disguises") }
    }
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
    compileOnly("me.libraryaddict.disguises:libsdisguises:11.0.6")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("com.charleskorn.kaml:kaml:0.85.0")
    implementation("com.squareup.okio:okio:3.10.2")
    implementation("io.insert-koin:koin-core:3.5.6")
    
    // Test dependencies
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("io.kotest:kotest-property:5.8.0")
    testImplementation("com.github.seeseemelk:MockBukkit-v1.21:3.149.0")
    testImplementation("io.insert-koin:koin-test:3.5.6")
    testImplementation("io.mockk:mockk:1.13.8")
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21.6")
    }

    compileJava { options.compilerArgs.add("-parameters") }

    build { dependsOn("shadowJar") }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") { expand(props) }
    }

    ktfmt { kotlinLangStyle() }
    
    test {
        useJUnitPlatform()
        // Allow tests to run even if main compilation has issues
        dependsOn("compileTestKotlin")
    }
    
    compileTestKotlin {
        // Make test compilation independent of main source compilation
        enabled = true
    }
    
    register<JavaExec>("runLangServiceTest") {
        description = "Run the simple LangService test"
        classpath = sourceSets["test"].runtimeClasspath
        mainClass.set("dev.betrix.superSmashMobsBrawl.services.SimpleLangServiceTest")
        dependsOn("compileTestKotlin")
    }
}

val targetJavaVersion = 21

kotlin { jvmToolchain(targetJavaVersion) }
