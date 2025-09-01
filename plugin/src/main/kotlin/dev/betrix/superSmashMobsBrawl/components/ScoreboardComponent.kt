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

    var virtualTitle: Component = Component.empty()
        protected set
    var virtualLines: List<Component> = emptyList()
        protected set

    var currentTitle: Component = Component.empty()
    var currentLines: List<Component> = emptyList()

    override fun World.onRemove(entity: Entity) {
        scoreboard.delete()
    }

    fun setTitle(component: Component) {
        virtualTitle = component
    }

    fun setLines(components: List<Component>) {
        virtualLines = components
    }
}