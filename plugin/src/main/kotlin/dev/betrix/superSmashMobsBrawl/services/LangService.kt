package dev.betrix.superSmashMobsBrawl.services

import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LangService : KoinComponent {
    private val plugin: JavaPlugin by inject()

    private val dataFolder = plugin.dataFolder
    private val enLang: YamlConfiguration = loadLangWithDefaults("data/lang/en.yml")
    private val themeConfig: YamlConfiguration = loadLangWithDefaults("data/theme.yml")
    private val tokenToHex: Map<String, String> by lazy {
        val section = themeConfig.getConfigurationSection("tokens")
        val map = linkedMapOf<String, String>()
        if (section != null) {
            for (key in section.getKeys(false)) {
                val hex = section.getString(key) ?: continue
                map[key.lowercase(Locale.ROOT)] = hex
            }
        }
        map.toMap()
    }

    private val miniMessage: MiniMessage = MiniMessage.miniMessage()

    // Matches {content} with no nested braces
    private val placeholderPattern: Pattern = Pattern.compile("\\{([^{}]+)}")

    fun t(key: String, varsBuilder: VarsBuilder.() -> Unit = {}): Component {
        val vars = VarsBuilder().apply(varsBuilder).build()
        val resolved = resolveAndFormat(key, vars, visited = mutableSetOf())
        val preprocessed = preprocessThemeTokens(resolved)
        return miniMessage.deserialize(preprocessed)
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

        // First interpolate simple variables so nested placeholders inside lang: paths are resolved
        val withVarsFirst = interpolateVariables(raw, vars)

        // Then expand lang references (which may now include previously nested variables)
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
        val matcher = placeholderPattern.matcher(text)
        val sb = StringBuffer()

        while (matcher.find()) {
            val token = matcher.group(1).trim()

            if (token.startsWith("lang:")) {
                // token like "lang:minigames.test.name" (any nested vars should already be
                // interpolated)
                val resolvedPath = token.removePrefix("lang:").trim()

                // Resolve that key recursively
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
                // Leave lang references untouched in this phase
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group()))
                continue
            }

            val value = vars[token]
            val replacement =
                value?.let { miniMessage.escapeTags(it.toString()) }
                    ?: matcher.group() // leave placeholder if missing
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement))
        }

        matcher.appendTail(sb)
        return sb.toString()
    }

    /**
     * Replace token tags like <primary> and </primary> with concrete color tags using hex values
     * from data/theme.yml. Also expand token names in arguments for tags that accept color
     * parameters (e.g., <gradient:primary:ui-shadow>, <shadow:text>).
     */
    private fun preprocessThemeTokens(message: String): String {
        if (tokenToHex.isEmpty()) return message

        var result = message

        // Replace tokens inside color-accepting tag arguments (gradient, shadow)
        run {
            val pattern = Pattern.compile("<(gradient|shadow):([^>]+)>")
            val matcher = pattern.matcher(result)
            val sb = StringBuffer()
            while (matcher.find()) {
                val tagName = matcher.group(1)
                val args = matcher.group(2)
                val replacedArgs =
                    args.split(":").joinToString(":") { part ->
                        val key = part.trim().lowercase(Locale.ROOT)
                        tokenToHex[key] ?: part
                    }
                val replacement = "<" + tagName + ":" + replacedArgs + ">"
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement))
            }
            matcher.appendTail(sb)
            result = sb.toString()
        }

        // Replace simple token tags <token> and </token> with <hex> and </hex>
        run {
            val group = tokenToHex.keys.joinToString("|") { Pattern.quote(it) }
            if (group.isNotEmpty()) {
                val pattern = Pattern.compile("<(/?)($group)>")
                val matcher = pattern.matcher(result)
                val sb = StringBuffer()
                while (matcher.find()) {
                    val slash = matcher.group(1)
                    val name = matcher.group(2).lowercase(Locale.ROOT)
                    val hex = tokenToHex[name]
                    if (hex != null) {
                        val replacement = if (slash.isEmpty()) "<$hex>" else "</$hex>"
                        matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement))
                    } else {
                        matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group()))
                    }
                }
                matcher.appendTail(sb)
                result = sb.toString()
            }
        }

        return result
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
