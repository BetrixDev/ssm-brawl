package dev.betrix.superSmashMobsBrawl.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.FamilyOnAdd
import com.github.quillraven.fleks.FamilyOnRemove
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.components.MinecraftPlayerComponent
import dev.betrix.superSmashMobsBrawl.components.passives.DoubleJumpPassiveComponent
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import dev.betrix.superSmashMobsBrawl.utils.isOnGround
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.repeatingTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerToggleFlightEvent

class DoubleJumpSystem: IteratingSystem(family { all(
    DoubleJumpPassiveComponent, MinecraftPlayerComponent)}), FamilyOnAdd, FamilyOnRemove {
    private val canDoubleJumpMap = hashMapOf<Player, Boolean>()

    override fun onTickEntity(entity: Entity) {}

    override fun onAddEntity(entity: Entity) {
        val localPlayer = entity[MinecraftPlayerComponent].player

        localPlayer.allowFlight = true

        event<PlayerToggleFlightEvent> FlightEvent@ {
            if (player != localPlayer) {
                return@FlightEvent
            }

            isCancelled = true
            
            if (canDoubleJumpMap[localPlayer] == false) {
                return@FlightEvent
            }

            player.fallDistance = 0f
            player.playSound(player.location, Sound.ENTITY_BLAZE_SHOOT, 1F, 1F)
            player.setVelocity(player.location.direction, 0.9, true, 0.9, 0.0, 0.9, true)

            player.allowFlight = false
            canDoubleJumpMap[localPlayer] = false

            val job = repeatingTask(1) {
                player.sendMessage(if (isOnGround(localPlayer)) "You are on ground" else "You are not on ground")
                if (isOnGround(localPlayer)) {
                    canDoubleJumpMap[localPlayer] = true
                    localPlayer.allowFlight = true
                    this.cancel()
                }
            }

            event<PlayerDeathEvent> DeathEvent@ {
                if (player != this@FlightEvent.player) {
                    return@DeathEvent
                }

                job.cancel()
                canDoubleJumpMap[player] = true
            }
        }
    }

    override fun onRemoveEntity(entity: Entity) {
        val localPlayer = entity[MinecraftPlayerComponent].player
        localPlayer.allowFlight = false
        canDoubleJumpMap.remove(localPlayer)
    }
}