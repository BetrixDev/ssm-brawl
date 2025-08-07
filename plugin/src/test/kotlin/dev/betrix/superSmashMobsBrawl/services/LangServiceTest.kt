package dev.betrix.superSmashMobsBrawl.services

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import org.bukkit.plugin.java.JavaPlugin
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.bukkit.configuration.file.YamlConfiguration
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.nio.file.Files

class LangServiceTest : StringSpec({
    lateinit var server: ServerMock
    lateinit var plugin: JavaPlugin

    beforeSpec {
        server = MockBukkit.mock()
        // Create a minimal mock plugin jarless
        plugin = MockBukkit.createMockPlugin()

        // Prepare lang files under plugin data folder
        val dataFolder = plugin.dataFolder.toPath()
        Files.createDirectories(dataFolder.resolve("data/lang"))

        val enPath = dataFolder.resolve("data/lang/en.yml")
        val yml = YamlConfiguration()
        yml.set("greeting", "<green>Hello, {name}!")
        yml.set("farewell", "<red>Bye, {name}!")
        yml.set("minigames.test.name", "Test Minigame")
        yml.set("withRef", "Welcome to {lang:minigames.{minigameId}.name}, {name}!")
        yml.set("cycle.a", "{lang:cycle.b}")
        yml.set("cycle.b", "{lang:cycle.a}")
        yml.save(enPath.toFile())

        // Start Koin with JavaPlugin binding
        startKoin {
            modules(
                module {
                    single { plugin as JavaPlugin }
                }
            )
        }
    }

    afterSpec {
        GlobalContext.stopKoin()
        MockBukkit.unmock()
    }

    "interpolates simple variable values" {
        val lang = LangService()
        val text = lang.t("greeting") { "name" to "Alex" }
        text.toString() shouldContain "Hello, Alex!"
    }

    "resolves nested lang reference with variables in the key" {
        val lang = LangService()
        val text = lang.t("withRef") {
            "minigameId" to "test"
            "name" to "Sam"
        }
        val str = text.toString()
        str shouldContain "Welcome to Test Minigame, Sam!"
    }

    "returns [key] for unknown key" {
        val lang = LangService()
        val text = lang.t("unknown.key") {}
        text.toString() shouldContain "[unknown.key]"
    }

    "detects cycles and returns [key]" {
        val lang = LangService()
        val a = lang.t("cycle.a").toString()
        val b = lang.t("cycle.b").toString()
        a shouldContain "[cycle.a]"
        b shouldContain "[cycle.b]"
    }
})