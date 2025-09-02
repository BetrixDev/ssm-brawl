package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef

data class QueueComponent(
    val minigame: MinigameDef,
    val partyId: String? = null,
    val queuedAt: Long = System.currentTimeMillis(),
) : Component<QueueComponent> {
    override fun type() = QueueComponent

    companion object : ComponentType<QueueComponent>()
}
