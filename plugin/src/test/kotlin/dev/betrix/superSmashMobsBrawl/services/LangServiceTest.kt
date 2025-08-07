package dev.betrix.superSmashMobsBrawl.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.configuration.file.YamlConfiguration
import java.io.StringReader

class LangServiceTest : DescribeSpec({

    fun createTestLangService(): LangService {
        // Create YAML configuration directly from string to avoid file path issues
        val testYamlContent = """
simple:
  greeting: "Hello, World!"
  with_variable: "Hello, {name}!"
  with_number: "You have {count} items"

references:
  nested_reference: "Welcome to {lang:simple.greeting}"
  complex_reference: "Player {name} says: {lang:simple.with_variable}"
  variable_in_reference: "Game: {lang:games.{gameId}.name}"

games:
  test_game:
    name: "Test Game"
    description: "A game for testing"
  another_game:
    name: "Another Game"
    description: "Another test game"

cycles:
  cycle_a: "This references {lang:cycles.cycle_b}"
  cycle_b: "This references {lang:cycles.cycle_a}"

missing:
  partial: "This references {lang:does.not.exist}"

complex:
  multi_var: "Player {player} has {count} {item_type} in {location}"
  multi_ref: "Status: {lang:simple.greeting} - {lang:games.{gameType}.name}"

edge_cases:
  empty_placeholder: "This has {}"
  nested_braces: "This has {{nested}} braces"
  special_chars: "Special chars: !@#$%^&*()"
  unicode: "Unicode: 你好 🌍"
        """.trimIndent()
        
        val yamlConfig = YamlConfiguration()
        yamlConfig.load(StringReader(testYamlContent))
        return LangService(yamlConfig)
    }

    fun Component.toPlainText(): String = PlainTextComponentSerializer.plainText().serialize(this)

    describe("LangService t() function") {

        context("simple translations") {
            val langService = createTestLangService()

            it("should return simple greeting without variables") {
                val result = langService.t("simple.greeting")
                result.toPlainText() shouldBe "Hello, World!"
            }

            it("should handle single variable substitution") {
                val result = langService.t("simple.with_variable") {
                    "name" to "Alice"
                }
                result.toPlainText() shouldBe "Hello, Alice!"
            }

            it("should handle numeric variable substitution") {
                val result = langService.t("simple.with_number") {
                    "count" to 42
                }
                result.toPlainText() shouldBe "You have 42 items"
            }

            it("should leave placeholder unchanged when variable is missing") {
                val result = langService.t("simple.with_variable")
                result.toPlainText() shouldBe "Hello, {name}!"
            }
        }

        context("language references") {
            val langService = createTestLangService()

            it("should resolve simple language references") {
                val result = langService.t("references.nested_reference")
                result.toPlainText() shouldBe "Welcome to Hello, World!"
            }

            it("should resolve complex language references with variables") {
                val result = langService.t("references.complex_reference") {
                    "name" to "Bob"
                }
                result.toPlainText() shouldBe "Player Bob says: Hello, Bob!"
            }

            it("should resolve language references with variables in the path") {
                val result = langService.t("references.variable_in_reference") {
                    "gameId" to "test_game"
                }
                result.toPlainText() shouldBe "Game: Test Game"
            }
        }

        context("multiple variables") {
            val langService = createTestLangService()

            it("should handle multiple variables in one string") {
                val result = langService.t("complex.multi_var") {
                    "player" to "Charlie"
                    "count" to 5
                    "item_type" to "swords"
                    "location" to "castle"
                }
                result.toPlainText() shouldBe "Player Charlie has 5 swords in castle"
            }

            it("should handle mixed references and variables") {
                val result = langService.t("complex.multi_ref") {
                    "gameType" to "another_game"
                }
                result.toPlainText() shouldBe "Status: Hello, World! - Another Game"
            }
        }

        context("error handling") {
            val langService = createTestLangService()

            it("should return fallback for missing keys") {
                val result = langService.t("non.existent.key")
                result.toPlainText() shouldBe "[non.existent.key]"
            }

            it("should return fallback for missing referenced keys") {
                val result = langService.t("missing.partial")
                result.toPlainText() shouldBe "This references [does.not.exist]"
            }

            it("should handle circular references gracefully") {
                val result = langService.t("cycles.cycle_a")
                result.toPlainText() shouldContain "[cycles.cycle_b]"
            }
        }

        context("edge cases") {
            val langService = createTestLangService()

            it("should handle empty placeholders") {
                val result = langService.t("edge_cases.empty_placeholder")
                result.toPlainText() shouldBe "This has {}"
            }

            it("should handle nested braces") {
                val result = langService.t("edge_cases.nested_braces")
                result.toPlainText() shouldBe "This has {{nested}} braces"
            }

            it("should handle special characters") {
                val result = langService.t("edge_cases.special_chars")
                result.toPlainText() shouldBe "Special chars: !@#$%^&*()"
            }

            it("should handle unicode characters") {
                val result = langService.t("edge_cases.unicode")
                result.toPlainText() shouldBe "Unicode: 你好 🌍"
            }
        }

        context("variable types") {
            val langService = createTestLangService()

            it("should handle null variables") {
                val result = langService.t("simple.with_variable") {
                    "name" to null
                }
                result.toPlainText() shouldBe "Hello, null!"
            }

            it("should handle boolean variables") {
                val result = langService.t("simple.with_variable") {
                    "name" to true
                }
                result.toPlainText() shouldBe "Hello, true!"
            }

            it("should handle list variables") {
                val result = langService.t("simple.with_variable") {
                    "name" to listOf("item1", "item2")
                }
                result.toPlainText() shouldBe "Hello, [item1, item2]!"
            }
        }

        context("VarsBuilder DSL") {
            val langService = createTestLangService()

            it("should support fluent variable building") {
                val result = langService.t("complex.multi_var") {
                    "player" to "Dave"
                    "count" to 10
                    "item_type" to "potions"
                    "location" to "dungeon"
                }
                result.toPlainText() shouldBe "Player Dave has 10 potions in dungeon"
            }

            it("should handle empty vars builder") {
                val result = langService.t("simple.greeting") {}
                result.toPlainText() shouldBe "Hello, World!"
            }
        }

        context("nested language references") {
            val langService = createTestLangService()

            it("should handle deeply nested references") {
                // Test a case where a reference contains another reference
                val result = langService.t("references.complex_reference") {
                    "name" to "nested"
                }
                // This should resolve both the outer reference and the inner one
                result.toPlainText() shouldBe "Player nested says: Hello, nested!"
            }
        }
    }
})