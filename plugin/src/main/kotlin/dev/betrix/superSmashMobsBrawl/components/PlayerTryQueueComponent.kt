package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef

class PlayerTryQueueComponent(val minigame: MinigameDef) : Component<PlayerTryQueueComponent> {
    override fun type() = PlayerTryQueueComponent

    companion object : ComponentType<PlayerTryQueueComponent>()
}
