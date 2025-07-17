package dev.betrix.superSmashMobsBrawl.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.FamilyOnAdd
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import dev.betrix.superSmashMobsBrawl.components.InQueueComponent
import dev.betrix.superSmashMobsBrawl.components.MinecraftPlayerComponent
import dev.betrix.superSmashMobsBrawl.components.MinigamePreflightComponent
import dev.betrix.superSmashMobsBrawl.enums.Minigame
import dev.betrix.superSmashMobsBrawl.utils.mm
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound

class QueueSystem : IteratingSystem( family { all(InQueueComponent) }), FamilyOnAdd {

    override fun onTickEntity(entity: Entity) {}

    override fun onAddEntity(entity: Entity) {
        val minigameId = entity[InQueueComponent].minigameId

        val queuedEntities = world.family { all(InQueueComponent) }.filter { it[InQueueComponent].minigameId == minigameId }

        val minigame = Minigame.fromId(minigameId)

        if (minigame == null) {
            queuedEntities.forEach { entity ->
                entity[MinecraftPlayerComponent].player.sendMessage(mm("<gray>You have been removed from the game queue you were in<gray/>"))
                entity.configure {
                    it -= InQueueComponent
                }
            }

            return
        }

        val playersNeededToStartGame = minigame.playersPerTeam * minigame.totalTeams

        if (queuedEntities.size >= playersNeededToStartGame) {
            val players = queuedEntities.map { entity ->
                entity.configure {
                    it -= InQueueComponent
                }

                entity[MinecraftPlayerComponent].player
            }

            val audience = Audience.audience(players)

            audience.sendMessage(mm("<gold>Enough players found! Game starting soon</gold>"))
            audience.playSound(Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.MASTER, 1f, 1f))

            world.entity {
                it += MinigamePreflightComponent(minigameId, players)
            }
        }
    }
}
