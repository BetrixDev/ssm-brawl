package dev.betrix.superSmashMobsBrawl.services

import com.github.michaelbull.result.*
import com.github.quillraven.fleks.World
import dev.betrix.superSmashMobsBrawl.components.InMinigameComponent
import dev.betrix.superSmashMobsBrawl.components.MinigameComponent
import dev.betrix.superSmashMobsBrawl.extensions.ecsEntity
import dev.betrix.superSmashMobsBrawl.models.brawlData.KitSwitchingMode
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * ECS-aware MinigameService.
 *
 * Exposes data lookups and a lightweight view for a player's current minigame
 * without owning minigame instances.
 */
class MinigameService : KoinComponent {
    private val dataService: DataService by inject()
    private val ecsWorld: World by inject()

    fun getMinigameData(id: String): MinigameDef? = dataService.getMinigame(id)

    fun getAllMinigameData(): List<MinigameDef> = dataService.getAllMinigames()

    fun findClosestMinigameById(id: String): MinigameDef? {
        if (id.isBlank()) return null

        getMinigameData(id)?.let { return it }

        getAllMinigameData().find { it.id.equals(id, ignoreCase = true) }?.let { return it }

        return getAllMinigameData().find { it.id.contains(id, ignoreCase = true) }
    }

    data class MinigameView(
        val minigameId: String,
        val kitSwitchingMode: KitSwitchingMode,
        val allowRejoinAfterLeave: Boolean,
        val isPassiveValid: (passiveId: String) -> Boolean,
    )

    fun isPlayerInMinigame(player: Player): Boolean = getMinigameForPlayer(player) != null

    fun getMinigameForPlayer(player: Player): MinigameView? {
        val entity = player.ecsEntity ?: return null
        val minigameEntity = with(ecsWorld) { entity.getOrNull(InMinigameComponent)?.minigameEntity } ?: return null
        val comp = with(ecsWorld) { minigameEntity[MinigameComponent] }
        val def = comp.minigame
        return MinigameView(
            minigameId = def.id,
            kitSwitchingMode = def.kitSwitchingMode,
            allowRejoinAfterLeave = def.allowRejoinAfterLeave,
            isPassiveValid = def::isPassiveValid,
        )
    }
}
