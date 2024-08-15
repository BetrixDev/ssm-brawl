package net.ssmb.components.worlds

import net.ssmb.blockwork.annotations.Component
import net.ssmb.blockwork.components.WorldComponent
import net.ssmb.blockwork.interfaces.OnStart
import net.ssmb.services.MinigamesService
import org.bukkit.World

@Component("minigameTesting")
class MinigameTestingWorldComponent: WorldComponent(), OnStart {
    companion object Meta {
        fun predicate(world: World, minigames: MinigamesService): Boolean {
            val gameId = world.getMetadata("gameId").first()?.asString()
            return gameId != null && minigames.getOngoingMinigameData(gameId) != null
        }
    }

    override fun onStart() {

    }
}