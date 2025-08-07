import java.util.regex.Matcher
import java.util.regex.Pattern
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.configuration.file.YamlConfiguration
import java.io.StringReader

/**
 * Standalone test version of LangService for testing core translation logic
 */
class LangService(
    private val langConfig: YamlConfiguration
) {
    private val miniMessage: MiniMessage = MiniMessage.miniMessage()

    // Matches {content} with no nested braces
    private val placeholderPattern: Pattern = Pattern.compile("\\{([^{}]+)}")

    fun t(key: String, varsBuilder: VarsBuilder.() -> Unit = {}): Component {
        val vars = VarsBuilder().apply(varsBuilder).build()
        val resolved = resolveAndFormat(key, vars, visited = mutableSetOf())
        return miniMessage.deserialize(resolved)
    }

    private fun resolveAndFormat(
        key: String,
        vars: Map<String, Any?>,
        visited: MutableSet<String>,
    ): String {
        if (!visited.add(key)) return "[$key]" // cycle fallback

        val raw =
            langConfig.getString(key)
                ?: run {
                    visited.remove(key)
                    return "[$key]"
                }

        // Expand lang references; allow variables inside the lang path
        val expandedRefs = expandLangReferences(raw, vars, visited)

        // Interpolate remaining variables in the final text
        val withVars = interpolateVariables(expandedRefs, vars)

        visited.remove(key)
        return withVars
    }

    private fun expandLangReferences(
        text: String,
        vars: Map<String, Any?>,
        visited: MutableSet<String>,
    ): String {
        val matcher = placeholderPattern.matcher(text)
        val sb = StringBuffer()

        while (matcher.find()) {
            val token = matcher.group(1).trim()

            if (token.startsWith("lang:")) {
                // token like "lang:minigames.{minigameId}.name"
                val rawPath = token.removePrefix("lang:").trim()

                // First, interpolate variables inside the lang path itself
                val resolvedPath = interpolateVariables(rawPath, vars)

                // Then resolve that key recursively
                val replacement = resolveAndFormat(resolvedPath, vars, visited)

                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement))
            } else {
                // Keep other placeholders (variables) for later interpolation
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group()))
            }
        }

        matcher.appendTail(sb)
        return sb.toString()
    }

    private fun interpolateVariables(text: String, vars: Map<String, Any?>): String {
        val matcher = placeholderPattern.matcher(text)
        val sb = StringBuffer()

        while (matcher.find()) {
            val token = matcher.group(1).trim()

            if (token.startsWith("lang:")) {
                // Should have been handled in expandLangReferences; leave as-is if any remain
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group()))
                continue
            }

            val value = vars[token]
            val replacement = value?.toString() ?: matcher.group() // leave placeholder if missing
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement))
        }

        matcher.appendTail(sb)
        return sb.toString()
    }

    class VarsBuilder {
        private val map = linkedMapOf<String, Any?>()

        infix fun String.to(value: Any?) {
            map[this] = value
        }

        fun build(): Map<String, Any?> = map.toMap()
    }
}

/**
 * Comprehensive test suite for LangService functionality
 */
fun main() {
    println("🧪 Running LangService tests...")
    
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

    var testsPassed = 0
    var testsTotal = 0
    
    fun runTest(name: String, test: () -> Unit) {
        testsTotal++
        try {
            test()
            println("  ✅ $name")
            testsPassed++
        } catch (e: Exception) {
            println("  ❌ $name: ${e.message}")
        }
    }

    val config = createTestConfig()
    val langService = LangService(config)

    println("\n📝 Basic Translation Tests")
    runTest("Simple greeting without variables") {
        val result = langService.t("simple.greeting")
        val resultStr = result.toString()
        assert(resultStr.contains("Hello, World!")) { "Expected 'Hello, World!' but got '$resultStr'" }
    }

    runTest("Variable substitution") {
        val result = langService.t("simple.with_variable") { "name" to "Alice" }
        assert(result.toString().contains("Alice")) { "Expected 'Alice' in result" }
    }

    runTest("Numeric variable substitution") {
        val result = langService.t("simple.with_number") { "count" to 42 }
        assert(result.toString().contains("42")) { "Expected '42' in result" }
    }

    runTest("Missing variables leave placeholders") {
        val result = langService.t("simple.with_variable")
        assert(result.toString().contains("{name}")) { "Expected '{name}' placeholder" }
    }

    println("\n🔗 Language Reference Tests")
    runTest("Simple language references") {
        val result = langService.t("references.nested_reference")
        val resultStr = result.toString()
        assert(resultStr.contains("Welcome to") && resultStr.contains("Hello, World!")) { 
            "Expected nested reference resolution" 
        }
    }

    runTest("Complex references with variables") {
        val result = langService.t("references.complex_reference") { "name" to "Bob" }
        val resultStr = result.toString()
        assert(resultStr.contains("Player Bob says:") && resultStr.contains("Hello, Bob!")) { 
            "Expected complex reference with variables" 
        }
    }

    runTest("Variable in reference path") {
        val result = langService.t("references.variable_in_reference") { "gameId" to "test_game" }
        val resultStr = result.toString()
        assert(resultStr.contains("Game:") && resultStr.contains("Test Game")) { 
            "Expected variable interpolation in reference path" 
        }
    }

    println("\n🔢 Multiple Variable Tests")
    runTest("Multiple variables in one string") {
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
            "Expected all variables substituted" 
        }
    }

    runTest("Mixed references and variables") {
        val result = langService.t("complex.multi_ref") { "gameType" to "another_game" }
        val resultStr = result.toString()
        assert(resultStr.contains("Status:") && 
               resultStr.contains("Hello, World!") && 
               resultStr.contains("Another Game")) { 
            "Expected mixed references and variables" 
        }
    }

    println("\n⚠️ Error Handling Tests")
    runTest("Missing keys return fallback") {
        val result = langService.t("non.existent.key")
        assert(result.toString().contains("[non.existent.key]")) { 
            "Expected fallback for missing key" 
        }
    }

    runTest("Missing referenced keys return fallback") {
        val result = langService.t("missing.partial")
        val resultStr = result.toString()
        assert(resultStr.contains("This references") && resultStr.contains("[does.not.exist]")) { 
            "Expected fallback for missing referenced key" 
        }
    }

    runTest("Circular references handled gracefully") {
        val result = langService.t("cycles.cycle_a")
        assert(result.toString().contains("[cycles.cycle_b]")) { 
            "Expected circular reference fallback" 
        }
    }

    println("\n🎯 Edge Case Tests")
    runTest("Empty placeholders") {
        val result = langService.t("edge_cases.empty_placeholder")
        assert(result.toString().contains("This has {}")) { 
            "Expected empty placeholder preserved" 
        }
    }

    runTest("Nested braces") {
        val result = langService.t("edge_cases.nested_braces")
        assert(result.toString().contains("{{nested}}")) { 
            "Expected nested braces preserved" 
        }
    }

    runTest("Special characters") {
        val result = langService.t("edge_cases.special_chars")
        assert(result.toString().contains("!@#$%^&*()")) { 
            "Expected special characters preserved" 
        }
    }

    runTest("Unicode characters") {
        val result = langService.t("edge_cases.unicode")
        assert(result.toString().contains("你好 🌍")) { 
            "Expected unicode characters preserved" 
        }
    }

    println("\n🔄 Variable Type Tests")
    runTest("Null variables") {
        val result = langService.t("simple.with_variable") { "name" to null }
        assert(result.toString().contains("null")) { 
            "Expected null variable to become 'null'" 
        }
    }

    runTest("Boolean variables") {
        val result = langService.t("simple.with_variable") { "name" to true }
        assert(result.toString().contains("true")) { 
            "Expected boolean variable to become 'true'" 
        }
    }

    runTest("List variables") {
        val result = langService.t("simple.with_variable") { "name" to listOf("item1", "item2") }
        val resultStr = result.toString()
        assert(resultStr.contains("item1") && resultStr.contains("item2")) { 
            "Expected list variables to be serialized" 
        }
    }

    println("\n📊 Test Summary")
    println("Tests passed: $testsPassed/$testsTotal")
    
    if (testsPassed == testsTotal) {
        println("🎉 All tests passed! LangService is working correctly.")
    } else {
        println("❌ Some tests failed. Please check the implementation.")
        kotlin.system.exitProcess(1)
    }
}