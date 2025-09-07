package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity

data class InMinigameComponent(val minigameEntity: Entity, val startingSelectedKitId: String) :
    Component<InMinigameComponent> {
    override fun type() = InMinigameComponent

    companion object : ComponentType<InMinigameComponent>()
}
