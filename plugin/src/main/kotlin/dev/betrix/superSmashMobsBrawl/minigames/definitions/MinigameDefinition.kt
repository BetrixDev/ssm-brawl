package dev.betrix.superSmashMobsBrawl.minigames.definitions

import dev.betrix.superSmashMobsBrawl.minigames.MinigameMetadata
import dev.betrix.superSmashMobsBrawl.minigames.instances.MinigameInstance
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam

abstract class MinigameDefinition {
    abstract val name: String
    abstract val id: String
    abstract val metadata: MinigameMetadata

    abstract fun createInstance(teams: List<MinigameTeam>): MinigameInstance
}