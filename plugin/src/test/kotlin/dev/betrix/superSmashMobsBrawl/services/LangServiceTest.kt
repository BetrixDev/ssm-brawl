package dev.betrix.superSmashMobsBrawl.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.string.shouldContain
import org.bukkit.plugin.java.JavaPlugin
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.bukkit.configuration.file.YamlConfiguration
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.nio.file.Files

class LangServiceTest : DescribeSpec({
    lateinit var server: ServerMock
    lateinit var plugin: JavaPlugin

    beforeSpec {
        server = MockBukkit.mock()
        plugin = MockBukkit.createMockPlugin()

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

    describe("LangService") {
        describe("variables") {
            it("interpolates simple variable values") {
                val lang = LangService()
                val text = lang.t("greeting") { "name" to "Alex" }
                text.toString() shouldContain "Hello, Alex!"
            }
        }

        describe("lang references") {
            it("resolves nested lang reference with variables in the key") {
                val lang = LangService()
                val text = lang.t("withRef") {
                    "minigameId" to "test"
                    "name" to "Sam"
                }
                text.toString() shouldContain "Welcome to Test Minigame, Sam!"
            }
        }

        describe("missing keys") {
            it("returns [key] for unknown key") {
                val lang = LangService()
                val text = lang.t("unknown.key") {}
                text.toString() shouldContain "[unknown.key]"
            }
        }

        describe("cycles") {
            it("detects cycles and returns [key]") {
                val lang = LangService()
                lang.t("cycle.a").toString() shouldContain "[cycle.a]"
                lang.t("cycle.b").toString() shouldContain "[cycle.b]"
            }
        }
    }
})