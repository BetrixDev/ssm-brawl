package dev.betrix.superSmashMobsBrawl.minigames.managers

import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.superSmashMobsBrawl.Manageable
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.events.BrawlDeathEvent
import dev.betrix.superSmashMobsBrawl.events.PlayerDeathAnalyticsEvent
import dev.betrix.superSmashMobsBrawl.events.PlayerRespawnAnalyticsEvent
import dev.betrix.superSmashMobsBrawl.extensions.getFarthestFromPlayers
import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame
import dev.betrix.superSmashMobsBrawl.minigames.IKitHandler
import dev.betrix.superSmashMobsBrawl.minigames.ITeleportationHandler
import dev.betrix.superSmashMobsBrawl.models.SpawnPoint
import dev.betrix.superSmashMobsBrawl.services.LangService
import gg.flyte.twilight.extension.feed
import gg.flyte.twilight.extension.heal
import gg.flyte.twilight.scheduler.delay
import java.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay as coDelay
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.GameMode
import org.bukkit.OfflinePlayer
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

interface IRespawnManager {
    fun initialize(minigame: BrawlMinigame)

    fun markRespawning(player: OfflinePlayer)

    fun clearRespawning(player: OfflinePlayer)

    fun isRespawning(player: OfflinePlayer): Boolean

    suspend fun handlePlayerDeath(player: Player)
}

class DefaultRespawnManager(private val minigame: BrawlMinigame) :
    Manageable(), IRespawnManager, KoinComponent {

    private val langService: LangService by inject()
    private val plugin: SuperSmashMobsBrawl by inject()

    private val respawning = mutableSetOf<java.util.UUID>()

    override fun initialize(minigame: BrawlMinigame) {
        // Listen for death events
        listeners.add(
            BrawlDeathEvent.listen {
                // Only handle deaths for players in this minigame and not already respawning
                if (
                    minigame.allPlayers().none {
                        it.isOnline && it.player?.uniqueId == player.uniqueId
                    } || isRespawning(player)
                )
                    return@listen

                // Fire analytics event with the real reason
                PlayerDeathAnalyticsEvent(player, reason, minigame).callEvent()

                plugin.launch { handlePlayerDeath(player) }
            }
        )
    }

    override fun markRespawning(player: OfflinePlayer) {
        respawning.add(player.uniqueId)
    }

    override fun clearRespawning(player: OfflinePlayer) {
        respawning.remove(player.uniqueId)
    }

    override fun isRespawning(player: OfflinePlayer): Boolean {
        return respawning.contains(player.uniqueId)
    }

    override suspend fun handlePlayerDeath(player: Player) {
        // Check if player is still in the minigame at the start
        if (!isPlayerInMinigame(player)) {
            return
        }

        // Unassign kit
        (minigame.kitHandler as? IKitHandler)?.removeKitFromPlayer(player)

        // Lightning effect and sound
        player.world.strikeLightningEffect(player.location)

        delay(1) { player.playSound(player.eyeLocation, Sound.ENTITY_PLAYER_HURT, 1f, 1f) }

        var respawnSuccessful = false

        if (minigame.minigameDef.respawnDelaySeconds != null) {
            // Teleport to spectator area
            val world = minigame.worldManager.getWorld()
            if (world != null) {
                (minigame.teleportationHandler as? ITeleportationHandler)
                    ?.teleportPlayerToSpectatorArea(player)
            }

            player.gameMode = GameMode.SPECTATOR
            player.allowFlight = true
            player.isFlying = true
            player.fallDistance = 0f

            // Mark player as respawning
            markRespawning(player)

            try {
                val respawnDelay = minigame.minigameDef.respawnDelaySeconds ?: 0

                // Fire analytics event
                PlayerRespawnAnalyticsEvent(player, minigame, respawnDelay).callEvent()

                repeat(respawnDelay) { iteration ->
                    // Check if player is still in the minigame before each countdown step
                    if (!isPlayerInMinigame(player)) {
                        return@handlePlayerDeath
                    }

                    val secondsLeft = respawnDelay - iteration

                    val title =
                        Title.title(
                            langService.t("messages.minigames.respawn.timeLeft") {
                                "secondsLeft" to secondsLeft
                            },
                            Component.empty(),
                            Title.Times.times(
                                Duration.ofMillis(250),
                                Duration.ofMillis(500),
                                Duration.ofMillis(250),
                            ),
                        )

                    player.showTitle(title)

                    coDelay(1.seconds)
                }

                // Final check before respawning
                if (!isPlayerInMinigame(player)) {
                    return@handlePlayerDeath
                }

                // If we reach here, respawn was successful
                respawnSuccessful = true
            } finally {
                // Only remove from respawning if respawn failed (player left during countdown)
                if (!respawnSuccessful) {
                    clearRespawning(player)
                }
            }
        }

        // Remove player from respawning state before respawning (successful case)
        if (respawnSuccessful || minigame.minigameDef.respawnDelaySeconds == null) {
            clearRespawning(player)
        }

        // Find spawn point
        val world = minigame.worldManager.getWorld() ?: return
        val activePlayers =
            minigame
                .allPlayers()
                .mapNotNull { if (it.isOnline) it.player else null }
                .filter { it != player && it.gameMode != GameMode.SPECTATOR }

        val spawnPoint =
            world.data.spawnPoints.getFarthestFromPlayers(activePlayers, world.world)
                ?: SpawnPoint(0.0, 100.0, 0.0)

        // Teleport and restore player
        (minigame.teleportationHandler as? ITeleportationHandler)?.teleportPlayerRandomSpawnPoint(
            player
        )
        player.feed()
        player.heal()
        player.gameMode = GameMode.SURVIVAL

        // Handle kit assignment
        (minigame.kitHandler as? IKitHandler)?.assignKitToPlayer(player)
    }

    private fun isPlayerInMinigame(player: Player): Boolean {
        return minigame.allPlayers().any {
            it.isOnline && it.player?.uniqueId == player.uniqueId
        } && !minigame.connectionManager.isDisconnected(player)
    }

    override fun teardown() {
        super.teardown()
        respawning.clear()
    }
}
