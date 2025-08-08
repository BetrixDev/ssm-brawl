package dev.betrix.superSmashMobsBrawl.services

import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.regex.Matcher
import java.util.regex.Pattern
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LangService : KoinComponent {
    private val plugin: JavaPlugin by inject()

    private val dataFolder = plugin.dataFolder
    private val enLang: YamlConfiguration = loadLangWithDefaults("data/lang/en.yml")

    private val miniMessage: MiniMessage = MiniMessage.miniMessage()

    // Matches {content} with no nested braces (variables)
    private val variablePattern: Pattern = Pattern.compile("\\{([^{}]+)}")

    // Matches ${content} with no nested braces (lang references)
    private val langRefPattern: Pattern = Pattern.compile("\\$\\{([^{}]+)}")

    // Built once from the current language file; contains style placeholders for palette entries
    private val paletteResolver: TagResolver by lazy { buildPaletteResolver() }

    fun t(key: String, varsBuilder: VarsBuilder.() -> Unit = {}): Component {
        val vars = VarsBuilder().apply(varsBuilder).build()
        val resolved = resolveAndFormat(key, vars, visited = mutableSetOf())
        return miniMessage.deserialize(resolved, paletteResolver)
    }

    private fun resolveAndFormat(
        key: String,
        vars: Map<String, Any?>,
        visited: MutableSet<String>,
    ): String {
        if (!visited.add(key)) return "[$key]" // cycle fallback

        val raw =
            enLang.getString(key)
                ?: enLang.defaults?.getString(key)
                ?: run {
                    visited.remove(key)
                    return "[$key]"
                }

        // First interpolate simple variables so nested placeholders inside reference keys are resolved
        val withVarsFirst = interpolateVariables(raw, vars)

        // Then expand ${...} references (which may now include previously nested variables)
        val expandedRefs = expandLangReferences(withVarsFirst, vars, visited)

        // Finally, interpolate any remaining variables from referenced strings
        val withVarsFinal = interpolateVariables(expandedRefs, vars)

        visited.remove(key)
        return withVarsFinal
    }

    private fun expandLangReferences(
        text: String,
        vars: Map<String, Any?>,
        visited: MutableSet<String>,
    ): String {
        val matcher = langRefPattern.matcher(text)
        val sb = StringBuffer()

        while (matcher.find()) {
            val token = matcher.group(1).trim()

            // token like "minigames.test.name" (any nested vars should already be interpolated)
            val resolvedPath = token

            // Resolve that key recursively
            val replacement = resolveAndFormat(resolvedPath, vars, visited)

            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement))
        }

        matcher.appendTail(sb)
        return sb.toString()
    }

    private fun interpolateVariables(text: String, vars: Map<String, Any?>): String {
        val matcher = variablePattern.matcher(text)
        val sb = StringBuffer()

        while (matcher.find()) {
            val token = matcher.group(1).trim()

            // If this is actually a ${...} reference, leave untouched for the reference phase
            if (token.startsWith("$")) {
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

    private fun buildPaletteResolver(): TagResolver {
        val section: ConfigurationSection? = enLang.getConfigurationSection("palette")
            ?: enLang.defaults?.getConfigurationSection("palette")

        if (section == null) return TagResolver.empty()

        val builder = TagResolver.builder()
        for (name in section.getKeys(false)) {
            val value = section.getString(name)?.trim().orEmpty()
            val color = parseTextColor(value) ?: continue
            builder.resolver(Placeholder.styling(name, color))
        }
        return builder.build()
    }

    private fun parseTextColor(value: String): TextColor? {
        if (value.isEmpty()) return null
        return when {
            value.startsWith("#") -> TextColor.fromHexString(value)
            else -> TextColor.fromCSSHexString(value) // attempt parse common formats/names if supported
        }
    }

    class VarsBuilder {
        private val map = linkedMapOf<String, Any?>()

        infix fun String.to(value: Any?) {
            map[this] = value
        }

        fun build(): Map<String, Any?> = map.toMap()
    }

    private fun loadLangWithDefaults(relativePath: String): YamlConfiguration {
        val file = dataFolder.resolve(relativePath)

        // Load existing file (may be empty or partial)
        val config = YamlConfiguration.loadConfiguration(file)

        // If we have a bundled default, merge it as defaults and persist missing keys
        plugin.getResource(relativePath)?.use { inputStream ->
            val reader = InputStreamReader(inputStream, StandardCharsets.UTF_8)
            val defaults = YamlConfiguration.loadConfiguration(reader)
            config.setDefaults(defaults)
            config.options().copyDefaults(true)
            // Best-effort save to ensure missing keys are written for visibility/editing
            try {
                config.save(file)
            } catch (_: Throwable) {
                // ignore IO issues here; runtime reads will still use merged defaults
            }
        }

        return config
    }
}
