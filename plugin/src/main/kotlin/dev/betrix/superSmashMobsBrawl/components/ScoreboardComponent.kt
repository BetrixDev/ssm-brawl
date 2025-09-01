package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component as EcsComponent
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import gg.flyte.twilight.scoreboard.TwilightScoreboard
import net.kyori.adventure.text.Component

data class ScoreboardComponent(
    val scoreboard: TwilightScoreboard
) : EcsComponent<ScoreboardComponent> {
    override fun type() = ScoreboardComponent

    companion object : ComponentType<ScoreboardComponent>()

    var title: Component? = null
    var lines: List<Component>? = null

    override fun World.onRemove(entity: Entity) {
        scoreboard.delete()
    }
}