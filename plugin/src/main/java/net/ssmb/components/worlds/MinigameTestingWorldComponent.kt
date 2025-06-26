package net.ssmb.components.worlds

import net.kyori.adventure.text.Component
import net.ssmb.SSMB
import net.ssmb.blockwork.CollectionService
import net.ssmb.blockwork.addTag
import net.ssmb.blockwork.components.WorldComponent
import net.ssmb.blockwork.getTags
import net.ssmb.blockwork.interfaces.OnStart
import net.ssmb.blockwork.removeTag
import net.ssmb.services.MinigamesService
import net.ssmb.services.OngoingMinigameDataRecord
import org.bukkit.World
import net.ssmb.blockwork.annotations.Component as BlockworkComponent

@BlockworkComponent("minigameTesting")
class MinigameTestingWorldComponent(private val plugin: SSMB, private val minigames: MinigamesService): WorldComponent(), OnStart {

    private lateinit var minigameData: OngoingMinigameDataRecord
    private val listeners = mutableListOf<() -> Unit>()

    companion object Meta {
        fun predicate(world: World, minigames: MinigamesService): Boolean {
            val gameId = world.getMetadata("gameId").first()?.asString()
            return gameId != null && minigames.getOngoingMinigameData(gameId) != null
        }
    }

    override fun onStart() {
        val gameId = world.getMetadata("gameId").first()?.asString()
        minigameData = minigames.getOngoingMinigameData(gameId!!)!!

        minigameData.players.forEach { player ->
            player.getTags().forEach { tag ->
               player.removeTag(tag)

                listeners.add(CollectionService.onEntityRemoved("isInMinigame") { entity ->
                    if (player != entity) {
                        return@onEntityRemoved
                    }

                    // do stuff here
                })
            }

            player.addTag("isInMinigame")

            player.teleport(world.spawnLocation)
            player.sendMessage(Component.text("You have been teleported to the minigame world"))
        }
    }
}