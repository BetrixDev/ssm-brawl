package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/** Associates a player entity with a team inside a minigame. */
data class InTeamComponent(val teamId: String) : Component<InTeamComponent> {
    override fun type() = InTeamComponent

    companion object : ComponentType<InTeamComponent>()
}
