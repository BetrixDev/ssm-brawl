package dev.betrix.superSmashMobsBrawl.services

import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * ThemeService manages named color tokens for MiniMessage-based UI strings.
 *
 * It provides:
 * - A TagResolver that registers token tags like <primary>, <text>, etc.
 * - A lightweight pre-processor that expands token names inside tag arguments for tags that expect
 *   color parameters (e.g. <gradient:primary:muteddark>, <shadow:text>).
 *
 * Tokens are defined in data/theme.yml and can be overridden by server owners.
 */
class ThemeService : KoinComponent {
    private val plugin: JavaPlugin by inject()

    private val dataFolder = plugin.dataFolder

    private val themeConfig: YamlConfiguration = loadThemeWithDefaults("data/theme.yml")

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

    /** Returns a TagResolver that defines token tags like <primary> ... </primary>. */
    fun tokenTagResolver(): TagResolver {
        val builder = TagResolver.builder()
        for ((name, hex) in tokenToHex) {
            val color = TextColor.fromHexString(hex) ?: continue
            builder.tag(name) { _, _ -> Tag.styling { it.color(color) } }
        }
        return builder.build()
    }

    /**
     * Preprocesses a MiniMessage string to expand token names inside tag arguments for tags that
     * accept colors by name, such as gradient and shadow.
     */
    fun preprocessMessageColors(message: String): String {
        if (tokenToHex.isEmpty()) return message

        // Replace tokens within arguments of specific tags that accept color params
        val pattern: Pattern = Pattern.compile("<(gradient|shadow):([^>]+)>")
        val matcher: Matcher = pattern.matcher(message)
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
        return sb.toString()
    }

    private fun loadThemeWithDefaults(relativePath: String): YamlConfiguration {
        val file = dataFolder.resolve(relativePath)

        val config = YamlConfiguration.loadConfiguration(file)

        plugin.getResource(relativePath)?.use { inputStream ->
            val reader = InputStreamReader(inputStream, StandardCharsets.UTF_8)
            val defaults = YamlConfiguration.loadConfiguration(reader)
            config.setDefaults(defaults)
            config.options().copyDefaults(true)
            try {
                config.save(file)
            } catch (_: Throwable) {
                // ignore IO issues
            }
        }

        return config
    }
}
