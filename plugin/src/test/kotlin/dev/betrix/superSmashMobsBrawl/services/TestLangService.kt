package dev.betrix.superSmashMobsBrawl.services

import java.util.regex.Matcher
import java.util.regex.Pattern
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.configuration.file.YamlConfiguration

/**
 * Test version of LangService that doesn't depend on Koin or external dependencies
 * This allows us to test the core translation logic independently
 */
class TestLangService(
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