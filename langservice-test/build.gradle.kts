plugins {
    kotlin("jvm") version "2.2.0-RC2"
    application
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation("io.papermc.paper:paper-api:1.21.5-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    // Test dependencies
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("io.kotest:kotest-property:5.8.0")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("TestLangServiceKt")
}

kotlin {
    jvmToolchain(21)
}