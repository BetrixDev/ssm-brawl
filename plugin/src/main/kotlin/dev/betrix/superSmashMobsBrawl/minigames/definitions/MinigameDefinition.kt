package dev.betrix.superSmashMobsBrawl.minigames.definitions

import dev.betrix.superSmashMobsBrawl.abilities.definitions.AbilityDefinition
import dev.betrix.superSmashMobsBrawl.minigames.MinigameMetadata
import dev.betrix.superSmashMobsBrawl.minigames.instances.MinigameInstance
import dev.betrix.superSmashMobsBrawl.models.MinigameTeam
import dev.betrix.superSmashMobsBrawl.passives.definitions.PassiveDefinition

abstract class MinigameDefinition {
    abstract val name: String
    abstract val id: String
    abstract val metadata: MinigameMetadata

    /**
     * List of passive definitions that are blacklisted in this minigame. These passives will not be
     * enabled for any kit in this minigame.
     */
    open val blacklistedPassives: List<PassiveDefinition> = emptyList()

    /**
     * List of ability definitions that are blacklisted in this minigame. These abilities will not
     * be enabled for any kit in this minigame.
     */
    open val blacklistedAbilities: List<AbilityDefinition> = emptyList()

    abstract fun createInstance(teams: List<MinigameTeam>): MinigameInstance
}
