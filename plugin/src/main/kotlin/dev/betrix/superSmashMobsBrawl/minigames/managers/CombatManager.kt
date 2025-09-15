package dev.betrix.superSmashMobsBrawl.minigames.managers

import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame
import org.bukkit.event.Listener

interface ICombatManager : Listener {
    fun initialize(minigame: BrawlMinigame)
}

class DefaultCombatManager : Manageable(), ICombatManager {
    override fun initialize(minigame: BrawlMinigame) {
        // Register damage listeners in future
    }
}

