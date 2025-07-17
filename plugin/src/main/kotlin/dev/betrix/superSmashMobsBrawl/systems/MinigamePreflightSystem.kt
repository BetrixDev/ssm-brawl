@file:OptIn(ExperimentalUuidApi::class)

package dev.betrix.superSmashMobsBrawl.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.FamilyOnAdd
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import dev.betrix.superSmashMobsBrawl.components.HasKitComponent
import dev.betrix.superSmashMobsBrawl.components.MinecraftPlayerComponent
import dev.betrix.superSmashMobsBrawl.components.MinigameComponent
import dev.betrix.superSmashMobsBrawl.components.MinigamePreflightComponent
import dev.betrix.superSmashMobsBrawl.components.abilities.SulphurBombAbilityComponent
import dev.betrix.superSmashMobsBrawl.components.passives.DoubleJumpPassiveComponent
import dev.betrix.superSmashMobsBrawl.enums.Minigame
import dev.betrix.superSmashMobsBrawl.utils.WorldUtils
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class MinigamePreflightSystem: IteratingSystem(
    family { all(MinigamePreflightComponent) }
), FamilyOnAdd {
    private val serverDir = Bukkit.getWorldContainer().toPath()

    override fun onAddEntity(entity: Entity) {
        val preflightData = entity[MinigamePreflightComponent]

        val minigameData = Minigame.fromId(preflightData.minigameId)

        if (minigameData == null) {
            doPreflightSafeExit(entity)
            return
        }

        val offlinePlayers = preflightData.players.filter { !it.isOnline }

        if (!offlinePlayers.isEmpty()) {
            entity[MinigamePreflightComponent].players = entity[MinigamePreflightComponent].players.filter { it.isOnline }

            doPreflightSafeExit(entity)
            return
        }

        val gameId = Uuid.random().toString()

        WorldUtils.copyAndLoadWorldAsync("campsite", gameId) { result ->
            result.fold(
                onSuccess = { world ->
                    onWorldCreated(world, entity, minigameData)
                },
                onFailure = { exception ->
                    exception.printStackTrace()
                    doPreflightSafeExit(entity)
                }
            )
        }
    }

    private fun onWorldCreated(bukkitWorld: World, entity: Entity, minigameData: Minigame) {
        val players = entity[MinigamePreflightComponent].players
        val playersFamily = world.family { all(MinecraftPlayerComponent) }

        val worldSpawnLocation = Location(bukkitWorld, 13.0, 106.0, 33.0)

        players.forEach { player ->
            val playerEcsEntity = playersFamily.find { it[MinecraftPlayerComponent].player == player }

            if (playerEcsEntity == null) {
                doPreflightSafeExit(entity, bukkitWorld)
                return
            }

            player.teleport(worldSpawnLocation)

            playerEcsEntity.configure {
                it += HasKitComponent()
                it += DoubleJumpPassiveComponent()
                it += SulphurBombAbilityComponent()
            }
        }

        entity.configure {
            it += MinigameComponent(minigameData, players, bukkitWorld)
        }

        removePreflightComponent(entity)
    }

    private fun doPreflightSafeExit(entity: Entity, world: World? = null) {
        removePreflightComponent(entity)

        TODO("Implement me!")
    }

    private fun removePreflightComponent(entity: Entity) {
        entity.configure {
            it -= MinigamePreflightComponent
        }
    }

    override fun onTickEntity(entity: Entity) {}
}