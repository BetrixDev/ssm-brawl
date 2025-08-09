package dev.betrix.superSmashMobsBrawl.extensions

import dev.betrix.superSmashMobsBrawl.services.DebugService
import dev.betrix.superSmashMobsBrawl.services.KitService
import dev.betrix.superSmashMobsBrawl.services.LangService
import org.bukkit.entity.Player
import org.koin.core.context.GlobalContext

/**
 * Check if debug mode is enabled for this player.
 *
 * Usage:
 * ```kotlin
 * if (player.hasDebugEnabled()) {
 *     player.sendMessage("Some debug information")
 * }
 * ```
 */
fun Player.hasDebugEnabled(): Boolean {
    return DebugService.isDebugEnabled(this)
}

fun Player.sendDebugMessage(message: String) {
    if (!hasDebugEnabled()) {
        return
    }

    val lang: LangService = GlobalContext.get().get()

    sendMessage(lang.t("messages.debug") { "message" to message })
}

fun Player.hasPassive(passiveId: String): Boolean {
    val kitService: KitService = GlobalContext.get().get()

    val brawlKit = kitService.getBrawlKit(this) ?: return false

    return brawlKit.getPassive(passiveId) != null
}
