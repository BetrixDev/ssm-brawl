package dev.betrix.superSmashMobsBrawl.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.nio.file.Files
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent

class LangServiceTest :
    DescribeSpec({
        lateinit var server: ServerMock
        lateinit var plugin: JavaPlugin

        beforeSpec {
            server = MockBukkit.mock()
            plugin = MockBukkit.createMockPlugin()

            val dataFolder = plugin.dataFolder.toPath()
            Files.createDirectories(dataFolder.resolve("data/lang"))
            Files.createDirectories(dataFolder.resolve("data"))

            val enPath = dataFolder.resolve("data/lang/en.yml")
            val yml = YamlConfiguration()
            yml.set("greeting", "<green>Hello, {name}!")
            yml.set("farewell", "<red>Bye, {name}!")
            yml.set("minigames.test.name", "Test Minigame")
            yml.set("withRef", "Welcome to {lang:minigames.{minigameId}.name}, {name}!")
            yml.set("cycle.a", "{lang:cycle.b}")
            yml.set("cycle.b", "{lang:cycle.a}")
            // Color-related keys for testing
            yml.set("colors.primary", "<primary>Hello")
            yml.set("colors.text", "<text>World")
            yml.set("colors.gradient", "<gradient:accent:ui-shadow>Colorful</gradient>")
            yml.save(enPath.toFile())

            // Provide a theme with tokens so ThemeService can resolve them in tests
            val themePath = dataFolder.resolve("data/theme.yml")
            val theme = YamlConfiguration()
            theme.set("tokens.primary", "#8bd5ff")
            theme.set("tokens.text", "#e8edff")
            theme.set("tokens.accent", "#cba6f7")
            theme.set("tokens.ui-shadow", "#585b70")
            theme.save(themePath.toFile())

            startKoin {
                modules(
                    module {
                        single { plugin as JavaPlugin }
                        single(createdAtStart = true) { ThemeService() }
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
                    val text =
                        lang.t("withRef") {
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

            describe("colors") {
                fun flattenTextComponents(component: Component): List<TextComponent> {
                    val list = mutableListOf<TextComponent>()
                    fun traverse(node: Component) {
                        if (node is TextComponent && node.content().isNotEmpty()) {
                            list.add(node)
                        }
                        for (child in node.children()) {
                            traverse(child)
                        }
                    }
                    traverse(component)
                    return list
                }

                it("applies token tag color for <primary>") {
                    val lang = LangService()
                    val comp = lang.t("colors.primary")
                    val segments = flattenTextComponents(comp)
                    segments.size shouldBe 1
                    segments.first().content() shouldBe "Hello"
                    segments.first().color()!!.asHexString() shouldBe "#8bd5ff"
                }

                it("applies token tag color for <text>") {
                    val lang = LangService()
                    val comp = lang.t("colors.text")
                    val segments = flattenTextComponents(comp)
                    segments.size shouldBe 1
                    segments.first().content() shouldBe "World"
                    segments.first().color()!!.asHexString() shouldBe "#e8edff"
                }

                it("expands tokens inside gradient arguments and applies gradient colors") {
                    val lang = LangService()
                    val comp = lang.t("colors.gradient")
                    val segments = flattenTextComponents(comp)
                    // Expect at least 2 segments due to gradient splitting text
                    assert(segments.isNotEmpty())
                    // First character should start with accent color, last should end with ui-shadow color
                    val firstColor = segments.first().color()!!.asHexString()
                    val lastColor = segments.last().color()!!.asHexString()
                    firstColor shouldBe "#cba6f7"
                    lastColor shouldBe "#585b70"
                }
            }
        }
    })
