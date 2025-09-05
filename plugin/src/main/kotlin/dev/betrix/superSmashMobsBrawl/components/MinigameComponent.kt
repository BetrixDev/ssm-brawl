package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.collection.MutableEntityBag
import com.github.quillraven.fleks.collection.mutableEntityBagOf
import dev.betrix.superSmashMobsBrawl.models.BrawlWorld
import dev.betrix.superSmashMobsBrawl.models.TeamData
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import java.util.UUID

enum class MinigameState {
    LOADING_WORLD, // World is being loaded
    WAITING, // Waiting for players
    STARTING, // Countdown before game starts
    ONGOING, // Game is active
    ENDING, // Game finished, showing results
    CLEANUP, // Cleaning up resources
    PAUSED, // Game is paused, cooldowns will stop
}

data class MinigameComponent(
    val minigameDef: MinigameDef,
    val instanceId: String = generateInstanceId(),
    var state: MinigameState = MinigameState.LOADING_WORLD,
    val playerEntities: MutableEntityBag,
    val spectatorEntities: MutableEntityBag = mutableEntityBagOf(),
    val teams: MutableMap<String, TeamData> = mutableMapOf(),
) : Component<MinigameComponent> {
    override fun type() = MinigameComponent

    companion object : ComponentType<MinigameComponent>()

    lateinit var loadedWorld: BrawlWorld
}

private fun generateInstanceId(): String {
    return "minigame_${UUID.randomUUID()}"
}
