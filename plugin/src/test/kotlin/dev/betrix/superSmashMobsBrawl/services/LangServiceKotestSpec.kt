package dev.betrix.superSmashMobsBrawl.services

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.configuration.file.YamlConfiguration
import java.io.StringReader

/**
 * Comprehensive Kotest specification for LangService functionality.
 * 
 * This test suite verifies all aspects of the LangService `t()` function including:
 * - Basic translation capabilities
 * - Variable substitution
 * - Language reference resolution
 * - Error handling for missing keys and circular references
 * - Edge cases and special characters
 * - Different variable types
 */
class LangServiceKotestSpec : FreeSpec({

    fun createTestConfig(): YamlConfiguration {
        val yamlContent = """
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
        
        val config = YamlConfiguration()
        config.load(StringReader(yamlContent))
        return config
    }

    fun toPlainText(component: net.kyori.adventure.text.Component): String =
        PlainTextComponentSerializer.plainText().serialize(component)

    "LangService basic functionality" - {
        val config = createTestConfig()
        val langService = LangService(config)

        "should translate simple messages without variables" {
            val result = langService.t("simple.greeting")
            toPlainText(result) shouldBe "Hello, World!"
        }

        "should handle single variable substitution" {
            val result = langService.t("simple.with_variable") {
                "name" to "Alice"
            }
            toPlainText(result) shouldBe "Hello, Alice!"
        }

        "should handle numeric variable substitution" {
            val result = langService.t("simple.with_number") {
                "count" to 42
            }
            toPlainText(result) shouldBe "You have 42 items"
        }

        "should leave placeholders when variables are missing" {
            val result = langService.t("simple.with_variable")
            toPlainText(result) shouldBe "Hello, {name}!"
        }
    }

    "LangService language references" - {
        val config = createTestConfig()
        val langService = LangService(config)

        "should resolve simple language references" {
            val result = langService.t("references.nested_reference")
            toPlainText(result) shouldBe "Welcome to Hello, World!"
        }

        "should resolve complex references with variables" {
            val result = langService.t("references.complex_reference") {
                "name" to "Bob"
            }
            toPlainText(result) shouldBe "Player Bob says: Hello, Bob!"
        }

        "should resolve references with variables in the path" {
            val result = langService.t("references.variable_in_reference") {
                "gameId" to "test_game"
            }
            toPlainText(result) shouldBe "Game: Test Game"
        }
    }

    "LangService multiple variables" - {
        val config = createTestConfig()
        val langService = LangService(config)

        "should handle multiple variables in one string" {
            val result = langService.t("complex.multi_var") {
                "player" to "Charlie"
                "count" to 5
                "item_type" to "swords"
                "location" to "castle"
            }
            toPlainText(result) shouldBe "Player Charlie has 5 swords in castle"
        }

        "should handle mixed references and variables" {
            val result = langService.t("complex.multi_ref") {
                "gameType" to "another_game"
            }
            toPlainText(result) shouldBe "Status: Hello, World! - Another Game"
        }
    }

    "LangService error handling" - {
        val config = createTestConfig()
        val langService = LangService(config)

        "should return fallback for missing keys" {
            val result = langService.t("non.existent.key")
            toPlainText(result) shouldBe "[non.existent.key]"
        }

        "should return fallback for missing referenced keys" {
            val result = langService.t("missing.partial")
            toPlainText(result) shouldBe "This references [does.not.exist]"
        }

        "should handle circular references gracefully" {
            val result = langService.t("cycles.cycle_a")
            toPlainText(result) shouldContain "[cycles.cycle_b]"
        }
    }

    "LangService edge cases" - {
        val config = createTestConfig()
        val langService = LangService(config)

        "should handle empty placeholders" {
            val result = langService.t("edge_cases.empty_placeholder")
            toPlainText(result) shouldBe "This has {}"
        }

        "should handle nested braces" {
            val result = langService.t("edge_cases.nested_braces")
            toPlainText(result) shouldBe "This has {{nested}} braces"
        }

        "should handle special characters" {
            val result = langService.t("edge_cases.special_chars")
            toPlainText(result) shouldBe "Special chars: !@#$%^&*()"
        }

        "should handle unicode characters" {
            val result = langService.t("edge_cases.unicode")
            toPlainText(result) shouldBe "Unicode: 你好 🌍"
        }
    }

    "LangService variable types" - {
        val config = createTestConfig()
        val langService = LangService(config)

        "should handle null variables" {
            val result = langService.t("simple.with_variable") {
                "name" to null
            }
            toPlainText(result) shouldBe "Hello, null!"
        }

        "should handle boolean variables" {
            val result = langService.t("simple.with_variable") {
                "name" to true
            }
            toPlainText(result) shouldBe "Hello, true!"
        }

        "should handle list variables" {
            val result = langService.t("simple.with_variable") {
                "name" to listOf("item1", "item2")
            }
            toPlainText(result) shouldBe "Hello, [item1, item2]!"
        }
    }

    "LangService VarsBuilder DSL" - {
        val config = createTestConfig()
        val langService = LangService(config)

        "should support fluent variable building" {
            val result = langService.t("complex.multi_var") {
                "player" to "Dave"
                "count" to 10
                "item_type" to "potions"
                "location" to "dungeon"
            }
            toPlainText(result) shouldBe "Player Dave has 10 potions in dungeon"
        }

        "should handle empty vars builder" {
            val result = langService.t("simple.greeting") {}
            toPlainText(result) shouldBe "Hello, World!"
        }
    }
})