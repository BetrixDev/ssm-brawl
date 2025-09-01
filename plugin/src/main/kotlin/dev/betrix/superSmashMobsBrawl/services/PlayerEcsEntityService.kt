package dev.betrix.superSmashMobsBrawl.services

import org.bukkit.entity.Player
import com.github.quillraven.fleks.Entity as EcsEntity

object PlayerEcsEntityService {
    private val playerEntityMap = hashMapOf<Player, EcsEntity>()

    fun onCreateEntityForPlayer(player: Player, entity: EcsEntity) {
        playerEntityMap[player] = entity
    }

    fun onRemoveEntityForPlayer(player: Player) {
        playerEntityMap.remove(player)
    }

    fun getEntityForPlayer(player: Player): EcsEntity? {
        return playerEntityMap[player]
    }
}