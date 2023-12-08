package dev.betrix.supersmashmobsbrawl.kits

import org.bukkit.entity.Creeper
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

class CreeperKit constructor(
    val player: Player,
) : BaseKit {
    private var playerModelEntity: Entity? = null

    private fun setPlayerModel() {
        val creeperEntity = player.world.spawn(player.location, Creeper::class.java)
        creeperEntity.setAI(false)

        playerModelEntity = creeperEntity
        player.addPassenger(creeperEntity)
    }

    private fun resetPlayerModel() {
        if (playerModelEntity != null) {
            player.removePassenger(playerModelEntity!!)
        }
    }

    override fun equipKit() {
        setPlayerModel()
    }

    override fun removeKit() {
        resetPlayerModel()
    }
}