package dev.betrix.supersmashmobsbrawl

import dev.betrix.supersmashmobsbrawl.enums.LangEntry
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

class SSMBPlaceholderExpansion : PlaceholderExpansion() {
    private val plugin = SuperSmashMobsBrawl.instance

    override fun getIdentifier(): String {
        return "ssmb"
    }

    override fun getAuthor(): String {
        return "BetrixDev"
    }

    override fun getVersion(): String {
        return "0.0.1"
    }

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        if (params == "prefix") {
            return plugin.lang.getRaw(LangEntry.PREFIX)
        }

        return null
    }
}