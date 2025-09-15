package dev.betrix.superSmashMobsBrawl.events

import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import kotlin.time.Duration

/**
 * Simple analytics events that don't affect gameplay but can be used for
 * statistics, achievements, and analytics tracking.
 */

class PlayerDeathAnalyticsEvent(
    val player: Player,
    val cause: DeathReason,
    val minigame: BrawlMinigame,
    val damager: Player? = null
) : Event() {
    companion object {
        private val HANDLER_LIST = HandlerList()
        @JvmStatic fun getHandlerList(): HandlerList = HANDLER_LIST
    }
    
    override fun getHandlers(): HandlerList = HANDLER_LIST
}

class PlayerDamageAnalyticsEvent(
    val victim: Player,
    val attacker: Player?,
    val damage: Double,
    val minigame: BrawlMinigame,
    val damageType: String? = null
) : Event() {
    companion object {
        private val HANDLER_LIST = HandlerList()
        @JvmStatic fun getHandlerList(): HandlerList = HANDLER_LIST
    }
    
    override fun getHandlers(): HandlerList = HANDLER_LIST
}

class MinigameEndAnalyticsEvent(
    val minigame: BrawlMinigame,
    val winners: List<Player>,
    val duration: Duration,
    val endReason: String = "natural"
) : Event() {
    companion object {
        private val HANDLER_LIST = HandlerList()
        @JvmStatic fun getHandlerList(): HandlerList = HANDLER_LIST
    }
    
    override fun getHandlers(): HandlerList = HANDLER_LIST
}

class PlayerKitSwitchAnalyticsEvent(
    val player: Player,
    val oldKitId: String?,
    val newKitId: String,
    val minigame: BrawlMinigame,
    val switchReason: String = "manual"
) : Event() {
    companion object {
        private val HANDLER_LIST = HandlerList()
        @JvmStatic fun getHandlerList(): HandlerList = HANDLER_LIST
    }
    
    override fun getHandlers(): HandlerList = HANDLER_LIST
}

class PlayerRespawnAnalyticsEvent(
    val player: Player,
    val minigame: BrawlMinigame,
    val respawnDelaySeconds: Int
) : Event() {
    companion object {
        private val HANDLER_LIST = HandlerList()
        @JvmStatic fun getHandlerList(): HandlerList = HANDLER_LIST
    }
    
    override fun getHandlers(): HandlerList = HANDLER_LIST
}

class PlayerJoinMinigameAnalyticsEvent(
    val player: Player,
    val minigame: BrawlMinigame
) : Event() {
    companion object {
        private val HANDLER_LIST = HandlerList()
        @JvmStatic fun getHandlerList(): HandlerList = HANDLER_LIST
    }
    
    override fun getHandlers(): HandlerList = HANDLER_LIST
}

class PlayerLeaveMinigameAnalyticsEvent(
    val player: Player,
    val minigame: BrawlMinigame,
    val reason: String = "manual"
) : Event() {
    companion object {
        private val HANDLER_LIST = HandlerList()
        @JvmStatic fun getHandlerList(): HandlerList = HANDLER_LIST
    }
    
    override fun getHandlers(): HandlerList = HANDLER_LIST
}
