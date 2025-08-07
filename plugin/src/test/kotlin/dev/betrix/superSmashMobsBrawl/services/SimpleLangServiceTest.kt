package dev.betrix.superSmashMobsBrawl.services

import org.bukkit.configuration.file.YamlConfiguration
import java.io.StringReader

/**
 * Simple test class to verify basic LangService functionality
 * This is a standalone class that can verify the LangService works correctly
 */
class SimpleLangServiceTest {
    
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            println("Running LangService tests...")
            
            val test = SimpleLangServiceTest()
            try {
                test.testBasicTranslation()
                test.testVariableSubstitution()
                test.testLanguageReferences()
                test.testMissingKeys()
                test.testCircularReferences()
                test.testMultipleVariables()
                test.testEdgeCases()
                
                println("✅ All tests passed!")
            } catch (e: Exception) {
                println("❌ Test failed: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    private fun createTestConfig(): YamlConfiguration {
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
    
    private fun testBasicTranslation() {
        println("Testing basic translation...")
        val config = createTestConfig()
        val langService = TestLangService(config)
        
        val result = langService.t("simple.greeting")
        val resultStr = result.toString()
        assert(resultStr.contains("Hello, World!")) { "Expected 'Hello, World!' but got '$resultStr'" }
        println("  ✓ Basic translation works")
    }
    
    private fun testVariableSubstitution() {
        println("Testing variable substitution...")
        val config = createTestConfig()
        val langService = TestLangService(config)
        
        val result = langService.t("simple.with_variable") {
            "name" to "Alice"
        }
        val resultStr = result.toString()
        assert(resultStr.contains("Alice")) { "Expected 'Alice' but got '$resultStr'" }
        println("  ✓ Variable substitution works")
        
        // Test numeric variables
        val numResult = langService.t("simple.with_number") {
            "count" to 42
        }
        val numResultStr = numResult.toString()
        assert(numResultStr.contains("42")) { "Expected '42' but got '$numResultStr'" }
        println("  ✓ Numeric variable substitution works")
        
        // Test missing variables
        val missingResult = langService.t("simple.with_variable")
        val missingResultStr = missingResult.toString()
        assert(missingResultStr.contains("{name}")) { "Expected '{name}' but got '$missingResultStr'" }
        println("  ✓ Missing variables leave placeholders")
    }
    
    private fun testLanguageReferences() {
        println("Testing language references...")
        val config = createTestConfig()
        val langService = TestLangService(config)
        
        val result = langService.t("references.nested_reference")
        val resultStr = result.toString()
        assert(resultStr.contains("Welcome to") && resultStr.contains("Hello, World!")) { 
            "Expected 'Welcome to Hello, World!' but got '$resultStr'" 
        }
        println("  ✓ Simple language references work")
        
        // Test complex references with variables
        val complexResult = langService.t("references.complex_reference") {
            "name" to "Bob"
        }
        val complexResultStr = complexResult.toString()
        assert(complexResultStr.contains("Player Bob says:") && complexResultStr.contains("Hello, Bob!")) { 
            "Expected complex reference with variables but got '$complexResultStr'" 
        }
        println("  ✓ Complex language references with variables work")
        
        // Test variable in reference path
        val varPathResult = langService.t("references.variable_in_reference") {
            "gameId" to "test_game"
        }
        val varPathResultStr = varPathResult.toString()
        assert(varPathResultStr.contains("Game:") && varPathResultStr.contains("Test Game")) { 
            "Expected 'Game: Test Game' but got '$varPathResultStr'" 
        }
        println("  ✓ Language references with variables in path work")
    }
    
    private fun testMissingKeys() {
        println("Testing missing keys...")
        val config = createTestConfig()
        val langService = TestLangService(config)
        
        val result = langService.t("non.existent.key")
        val resultStr = result.toString()
        assert(resultStr.contains("[non.existent.key]")) { 
            "Expected '[non.existent.key]' but got '$resultStr'" 
        }
        println("  ✓ Missing keys return fallback")
        
        val refResult = langService.t("missing.partial")
        val refResultStr = refResult.toString()
        assert(refResultStr.contains("This references") && refResultStr.contains("[does.not.exist]")) { 
            "Expected fallback for missing referenced key but got '$refResultStr'" 
        }
        println("  ✓ Missing referenced keys return fallback")
    }
    
    private fun testCircularReferences() {
        println("Testing circular references...")
        val config = createTestConfig()
        val langService = TestLangService(config)
        
        val result = langService.t("cycles.cycle_a")
        val resultStr = result.toString()
        assert(resultStr.contains("[cycles.cycle_b]")) { 
            "Expected circular reference fallback but got '$resultStr'" 
        }
        println("  ✓ Circular references handled gracefully")
    }
    
    private fun testMultipleVariables() {
        println("Testing multiple variables...")
        val config = createTestConfig()
        val langService = TestLangService(config)
        
        val result = langService.t("complex.multi_var") {
            "player" to "Charlie"
            "count" to 5
            "item_type" to "swords"
            "location" to "castle"
        }
        val resultStr = result.toString()
        assert(resultStr.contains("Player Charlie") && 
               resultStr.contains("5") && 
               resultStr.contains("swords") && 
               resultStr.contains("castle")) { 
            "Expected all variables substituted but got '$resultStr'" 
        }
        println("  ✓ Multiple variables work")
        
        val mixedResult = langService.t("complex.multi_ref") {
            "gameType" to "another_game"
        }
        val mixedResultStr = mixedResult.toString()
        assert(mixedResultStr.contains("Status:") && 
               mixedResultStr.contains("Hello, World!") && 
               mixedResultStr.contains("Another Game")) { 
            "Expected mixed references and variables but got '$mixedResultStr'" 
        }
        println("  ✓ Mixed references and variables work")
    }
    
    private fun testEdgeCases() {
        println("Testing edge cases...")
        val config = createTestConfig()
        val langService = TestLangService(config)
        
        // Test empty placeholders
        val emptyResult = langService.t("edge_cases.empty_placeholder")
        val emptyResultStr = emptyResult.toString()
        assert(emptyResultStr.contains("This has {}")) { 
            "Expected empty placeholder preserved but got '$emptyResultStr'" 
        }
        println("  ✓ Empty placeholders handled")
        
        // Test nested braces
        val nestedResult = langService.t("edge_cases.nested_braces")
        val nestedResultStr = nestedResult.toString()
        assert(nestedResultStr.contains("{{nested}}")) { 
            "Expected nested braces preserved but got '$nestedResultStr'" 
        }
        println("  ✓ Nested braces handled")
        
        // Test special characters
        val specialResult = langService.t("edge_cases.special_chars")
        val specialResultStr = specialResult.toString()
        assert(specialResultStr.contains("!@#$%^&*()")) { 
            "Expected special characters preserved but got '$specialResultStr'" 
        }
        println("  ✓ Special characters handled")
        
        // Test unicode
        val unicodeResult = langService.t("edge_cases.unicode")
        val unicodeResultStr = unicodeResult.toString()
        assert(unicodeResultStr.contains("你好 🌍")) { 
            "Expected unicode characters preserved but got '$unicodeResultStr'" 
        }
        println("  ✓ Unicode characters handled")
        
        // Test null variables
        val nullResult = langService.t("simple.with_variable") {
            "name" to null
        }
        val nullResultStr = nullResult.toString()
        assert(nullResultStr.contains("null")) { 
            "Expected null variable to become 'null' but got '$nullResultStr'" 
        }
        println("  ✓ Null variables handled")
        
        // Test boolean variables
        val boolResult = langService.t("simple.with_variable") {
            "name" to true
        }
        val boolResultStr = boolResult.toString()
        assert(boolResultStr.contains("true")) { 
            "Expected boolean variable to become 'true' but got '$boolResultStr'" 
        }
        println("  ✓ Boolean variables handled")
    }
}