package dev.betrix.superSmashMobsBrawl.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.components.InMinigameComponent
import dev.betrix.superSmashMobsBrawl.components.PlayerComponent
import dev.betrix.superSmashMobsBrawl.components.RespawnComponent
import dev.betrix.superSmashMobsBrawl.extensions.getFarthestFromPlayers
import dev.betrix.superSmashMobsBrawl.extensions.location
import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame
import dev.betrix.superSmashMobsBrawl.minigames.DeathDecision
import dev.betrix.superSmashMobsBrawl.services.LangService
import gg.flyte.twilight.extension.feed
import gg.flyte.twilight.extension.heal
import java.time.Duration
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.GameMode

class RespawnSystem(
    private val plugin: SuperSmashMobsBrawl = inject(),
    private val lang: LangService = inject(),
) : IteratingSystem(family { all(PlayerComponent, InMinigameComponent, RespawnComponent) }) {

    override fun onTickEntity(entity: Entity) {
        val player = entity[PlayerComponent].player
        val minigame = entity[InMinigameComponent].minigame as BrawlMinigame<*>
        val respawn = entity[RespawnComponent]

        val now = System.currentTimeMillis()
        val respawnAt = respawn.respawnAtMillis

        if (respawnAt == null || now >= respawnAt) {
            // Perform respawn immediately
            performRespawn(entity, minigame)
            // Notify minigame that respawn has completed
            minigame.onPostDeathProcessed(player, DeathDecision.Respawn(delaySeconds = null))
            return
        }

        val millisLeft = respawnAt - now
        val secondsLeft = ((millisLeft + 999) / 1000).toInt()
        if (secondsLeft != respawn.lastAnnouncedSecondsLeft) {
            respawn.lastAnnouncedSecondsLeft = secondsLeft
            // Show countdown title
            val title =
                Title.title(
                    lang.t("messages.minigames.respawn.timeLeft") { "secondsLeft" to secondsLeft },
                    Component.empty(),
                    Title.Times.times(
                        Duration.ofMillis(250),
                        Duration.ofMillis(500),
                        Duration.ofMillis(250),
                    ),
                )
            player.showTitle(title)
        }

        // If reached zero after showing the title, respawn next tick
        if (secondsLeft <= 0) {
            performRespawn(entity, minigame)
            minigame.onPostDeathProcessed(player, DeathDecision.Respawn(delaySeconds = 0))
        }
    }

    private fun performRespawn(entity: Entity, minigame: BrawlMinigame<*>) {
        val player = entity[PlayerComponent].player

        // Clear respawn component to prevent reprocessing
        entity.configure { it -= RespawnComponent }

        // Compute spawn point
        val spawnPoint =
            minigame.brawlWorld
                ?.data
                ?.spawnPoints
                ?.getFarthestFromPlayers(
                    minigame.getParticipants().filter {
                        it != player && it.isOnline && it.gameMode != GameMode.SPECTATOR
                    },
                    minigame.brawlWorld!!.world,
                )

        // Teleport and reset stats
        spawnPoint?.let { player.teleport(minigame.brawlWorld!!.world.location(it)) }
        player.feed()
        player.heal()
        player.gameMode = GameMode.SURVIVAL

        // Handle potential kit switching and re-assign
        minigame.handleKitSwitchOnRespawn(player)
        minigame.assignPlayerKit(player)
    }
}
