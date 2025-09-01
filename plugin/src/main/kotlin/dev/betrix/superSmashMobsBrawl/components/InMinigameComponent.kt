package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import dev.betrix.superSmashMobsBrawl.minigames.BrawlMinigame

data class InMinigameComponent(
    val minigame: BrawlMinigame<*>
) : Component<InMinigameComponent> {
    override fun type() = InMinigameComponent

    companion object : ComponentType<InMinigameComponent>()
}