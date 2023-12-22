package dev.betrix.supersmashmobsbrawl.managers

import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.betrix.supersmashmobsbrawl.enums.LangEntry
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player

class LangManager {
    private val plugin = SuperSmashMobsBrawl.instance
    private val customPlaceholderPattern = "<(.*?)>".toRegex()

    private var rawLangEntries = mapOf<String, Map<String, String>>()

    suspend fun revalidateLangCache() {
        plugin.logger.info("Attempting to revalidate the lang cache")
        rawLangEntries = plugin.api.fetchLang(true)
    }

    fun getComponent(entry: LangEntry, viewer: Player, customPlaceholders: HashMap<String, String>? = null): Component {
        val langEntry = rawLangEntries["en"]?.get(entry.id)!!

        var parsedText = PlaceholderAPI.setPlaceholders(viewer, langEntry)

        if (customPlaceholders != null) {
            customPlaceholderPattern.findAll(parsedText).forEach {
                if (customPlaceholders.containsKey(it.value.substring(1, it.value.length - 1))) {
                    parsedText = parsedText.replace(
                        it.value,
                        customPlaceholders[it.value.substring(1, it.value.length - 1)]!!
                    )
                }
            }
        }

        return MiniMessage.miniMessage().deserialize(parsedText)
    }

    fun getComponent(entry: LangEntry, customPlaceholders: HashMap<String, String>? = null): Component {
        var langEntry = rawLangEntries["en"]?.get(entry.id)!!

        if (customPlaceholders != null) {
            customPlaceholderPattern.findAll(langEntry).forEach {
                if (customPlaceholders.containsKey(it.value.substring(1, it.value.length - 1))) {
                    langEntry = langEntry.replace(
                        it.value,
                        customPlaceholders[it.value.substring(1, it.value.length - 1)]!!
                    )
                }
            }
        }

        return MiniMessage.miniMessage().deserialize(langEntry)
    }

    fun getRaw(entry: LangEntry): String {
        return this.rawLangEntries["en"]?.get(entry.id)!!
    }
}