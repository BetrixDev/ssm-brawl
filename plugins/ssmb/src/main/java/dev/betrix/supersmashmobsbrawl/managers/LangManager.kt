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

    fun process(entry: LangEntry, viewer: Player, customPlaceholders: HashMap<String, String>? = null): Component {
        val langEntry = rawLangEntries["en"]?.get(entry.id)!!

        val parsedText = PlaceholderAPI.setPlaceholders(viewer, langEntry)

        if (customPlaceholders != null) {
            customPlaceholderPattern.findAll(parsedText).forEach {
                if (customPlaceholders.containsKey(it.value)) {
                    plugin.logger.info(it.value + "    " + customPlaceholders[it.value]!!)
                    parsedText.replace(it.value, customPlaceholders[it.value]!!)
                } else {
                    plugin.logger.warning("Attempted to find custom placeholder for ${it.value} in lang entry ${entry.id} but got null")
                }
            }
        }

        return MiniMessage.miniMessage().deserialize(parsedText)
    }

    fun getRaw(entry: LangEntry): String {
        return this.rawLangEntries["en"]?.get(entry.id)!!
    }
}