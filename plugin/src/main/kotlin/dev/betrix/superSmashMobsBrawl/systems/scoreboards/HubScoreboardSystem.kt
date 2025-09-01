package dev.betrix.superSmashMobsBrawl.systems.scoreboards

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import dev.betrix.superSmashMobsBrawl.components.InHubComponent
import dev.betrix.superSmashMobsBrawl.components.PlayerComponent
import dev.betrix.superSmashMobsBrawl.components.ScoreboardComponent
import net.kyori.adventure.text.Component

class HubScoreboardSystem : IteratingSystem(
    family { all(PlayerComponent, ScoreboardComponent, InHubComponent) }
) {
    override fun onTickEntity(entity: Entity) {
        val scoreboard = entity[ScoreboardComponent]
        val player = entity[PlayerComponent].player

    

        scoreboard.setTitle(Component.text("SSM BRAWL"))
    }
}