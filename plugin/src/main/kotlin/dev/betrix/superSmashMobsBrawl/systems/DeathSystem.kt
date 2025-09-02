package dev.betrix.superSmashMobsBrawl.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.components.DeadComponent
import dev.betrix.superSmashMobsBrawl.components.EliminatedComponent
import dev.betrix.superSmashMobsBrawl.components.InMinigameComponent
import dev.betrix.superSmashMobsBrawl.components.PlayerComponent
import dev.betrix.superSmashMobsBrawl.components.RespawnComponent
import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame
import dev.betrix.superSmashMobsBrawl.minigames.DeathDecision
import dev.betrix.superSmashMobsBrawl.services.LangService
import gg.flyte.twilight.extension.feed
import gg.flyte.twilight.extension.heal
import org.bukkit.GameMode
import org.bukkit.Sound

class DeathSystem(
    private val plugin: SuperSmashMobsBrawl = inject(),
    private val lang: LangService = inject(),
) :
    IteratingSystem(family { all(PlayerComponent, InMinigameComponent, DeadComponent) }) {

    override fun onTickEntity(entity: Entity) {
        val player = entity[PlayerComponent].player
        val minigame = entity[InMinigameComponent].minigame as BrawlMinigame<*>
        val death = entity[DeadComponent]

        // Visual/audio feedback
        player.world.strikeLightningEffect(player.location)
        gg.flyte.twilight.scheduler.delay(1) { player.playSound(player.eyeLocation, Sound.ENTITY_PLAYER_HURT, 1f, 1f) }

        // Ensure we remove active kit effects immediately
        minigame.unassignPlayerKit(player)

        // Move player to spectator while we decide what to do
        minigame.brawlWorld?.let { world ->
            player.teleport(world.data.spectatorSpawnPoint)
        }
        player.gameMode = GameMode.SPECTATOR
        player.allowFlight = true
        player.isFlying = true
        player.fallDistance = 0f

        // Let the minigame decide whether to respawn or eliminate
        val decision: DeathDecision = minigame.decideDeath(player, death.reason)

        when (decision) {
            is DeathDecision.Eliminate -> {
                // Mark eliminated and clean up minigame state the same way as a leave
                entity.configure { it += EliminatedComponent() }
                player.feed()
                player.heal()
                minigame.onPlayerLeave(player)
                // Remove the DeadComponent; processing complete
                entity.configure { it -= DeadComponent }
                // Notify minigame that processing finished
                minigame.onPostDeathProcessed(player, decision)
            }
            is DeathDecision.Respawn -> {
                // Start respawn countdown or respawn immediately
                val millis = decision.delaySeconds?.let { System.currentTimeMillis() + it * 1000L }
                entity.configure {
                    it += RespawnComponent(respawnAtMillis = millis)
                    it -= DeadComponent
                }
                // Do not call post hook yet; it will be called after respawn finishes
            }
        }
    }
}