package dev.betrix.superSmashMobsBrawl.systems.scoreboards

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import dev.betrix.superSmashMobsBrawl.components.InMinigameComponent
import dev.betrix.superSmashMobsBrawl.components.PlayerComponent
import dev.betrix.superSmashMobsBrawl.components.ScoreboardComponent

class MinigameScoreboardSystem() : IteratingSystem(
    family { all(PlayerComponent, ScoreboardComponent, InMinigameComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val scoreboard = entity[ScoreboardComponent]
        val player = entity[PlayerComponent].player
    }
}