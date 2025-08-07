package dev.betrix.superSmashMobsBrawl.services

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.bukkit.configuration.file.YamlConfiguration
import java.io.StringReader

/**
 * Unit tests for LangService focusing on core translation logic
 * This test doesn't rely on Bukkit components or file system
 */
class LangServiceUnitTest : FunSpec({

    fun createTestYamlConfig(): YamlConfiguration {
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
        return yamlConfig
    }

    context("Basic translation functionality") {

        test("should translate simple messages") {
            val config = createTestYamlConfig()
            val langService = LangService(config)
            
            val result = langService.t("simple.greeting")
            // Test using the component's content since we can't easily convert to plain text in tests
            result.toString() shouldContain "Hello, World!"
        }

        test("should handle variable substitution") {
            val config = createTestYamlConfig()
            val langService = LangService(config)
            
            val result = langService.t("simple.with_variable") {
                "name" to "Alice"
            }
            result.toString() shouldContain "Alice"
        }

        test("should handle numeric variables") {
            val config = createTestYamlConfig()
            val langService = LangService(config)
            
            val result = langService.t("simple.with_number") {
                "count" to 42
            }
            result.toString() shouldContain "42"
        }

        test("should leave missing variables as placeholders") {
            val config = createTestYamlConfig()
            val langService = LangService(config)
            
            val result = langService.t("simple.with_variable")
            result.toString() shouldContain "{name}"
        }
    }

    context("Language references") {

        test("should resolve simple language references") {
            val config = createTestYamlConfig()
            val langService = LangService(config)
            
            val result = langService.t("references.nested_reference")
            result.toString() shouldContain "Welcome to"
            result.toString() shouldContain "Hello, World!"
        }

        test("should resolve complex references with variables") {
            val config = createTestYamlConfig()
            val langService = LangService(config)
            
            val result = langService.t("references.complex_reference") {
                "name" to "Bob"
            }
            result.toString() shouldContain "Player Bob says:"
            result.toString() shouldContain "Hello, Bob!"
        }

        test("should resolve references with variables in the path") {
            val config = createTestYamlConfig()
            val langService = LangService(config)
            
            val result = langService.t("references.variable_in_reference") {
                "gameId" to "test_game"
            }
            result.toString() shouldContain "Game:"
            result.toString() shouldContain "Test Game"
        }
    }

    context("Multiple variables") {

        test("should handle multiple variables in one string") {
            val config = createTestYamlConfig()
            val langService = LangService(config)
            
            val result = langService.t("complex.multi_var") {
                "player" to "Charlie"
                "count" to 5
                "item_type" to "swords"
                "location" to "castle"
            }
            val resultStr = result.toString()
            resultStr shouldContain "Player Charlie"
            resultStr shouldContain "5"
            resultStr shouldContain "swords"
            resultStr shouldContain "castle"
        }

        test("should handle mixed references and variables") {
            val config = createTestYamlConfig()
            val langService = LangService(config)
            
            val result = langService.t("complex.multi_ref") {
                "gameType" to "another_game"
            }
            val resultStr = result.toString()
            resultStr shouldContain "Status:"
            resultStr shouldContain "Hello, World!"
            resultStr shouldContain "Another Game"
        }
    }

    context("Error handling") {

        test("should return fallback for missing keys") {
            val config = createTestYamlConfig()
            val langService = LangService(config)
            
            val result = langService.t("non.existent.key")
            result.toString() shouldContain "[non.existent.key]"
        }

        test("should return fallback for missing referenced keys") {
            val config = createTestYamlConfig()
            val langService = LangService(config)
            
            val result = langService.t("missing.partial")
            val resultStr = result.toString()
            resultStr shouldContain "This references"
            resultStr shouldContain "[does.not.exist]"
        }

        test("should handle circular references gracefully") {
            val config = createTestYamlConfig()
            val langService = LangService(config)
            
            val result = langService.t("cycles.cycle_a")
            result.toString() shouldContain "[cycles.cycle_b]"
        }
    }

    context("Edge cases") {

        test("should handle empty placeholders") {
            val config = createTestYamlConfig()
            val langService = LangService(config)
            
            val result = langService.t("edge_cases.empty_placeholder")
            result.toString() shouldContain "This has {}"
        }

        test("should handle nested braces") {
            val config = createTestYamlConfig()
            val langService = LangService(config)
            
            val result = langService.t("edge_cases.nested_braces")
            result.toString() shouldContain "{{nested}}"
        }

        test("should handle special characters") {
            val config = createTestYamlConfig()
            val langService = LangService(config)
            
            val result = langService.t("edge_cases.special_chars")
            result.toString() shouldContain "!@#$%^&*()"
        }

        test("should handle unicode characters") {
            val config = createTestYamlConfig()
            val langService = LangService(config)
            
            val result = langService.t("edge_cases.unicode")
            result.toString() shouldContain "你好 🌍"
        }
    }

    context("Variable types") {

        test("should handle null variables") {
            val config = createTestYamlConfig()
            val langService = LangService(config)
            
            val result = langService.t("simple.with_variable") {
                "name" to null
            }
            result.toString() shouldContain "null"
        }

        test("should handle boolean variables") {
            val config = createTestYamlConfig()
            val langService = LangService(config)
            
            val result = langService.t("simple.with_variable") {
                "name" to true
            }
            result.toString() shouldContain "true"
        }

        test("should handle list variables") {
            val config = createTestYamlConfig()
            val langService = LangService(config)
            
            val result = langService.t("simple.with_variable") {
                "name" to listOf("item1", "item2")
            }
            result.toString() shouldContain "item1"
            result.toString() shouldContain "item2"
        }
    }

    context("VarsBuilder DSL") {

        test("should support fluent variable building") {
            val config = createTestYamlConfig()
            val langService = LangService(config)
            
            val result = langService.t("complex.multi_var") {
                "player" to "Dave"
                "count" to 10
                "item_type" to "potions"
                "location" to "dungeon"
            }
            val resultStr = result.toString()
            resultStr shouldContain "Player Dave"
            resultStr shouldContain "10"
            resultStr shouldContain "potions"
            resultStr shouldContain "dungeon"
        }

        test("should handle empty vars builder") {
            val config = createTestYamlConfig()
            val langService = LangService(config)
            
            val result = langService.t("simple.greeting") {}
            result.toString() shouldContain "Hello, World!"
        }
    }
})