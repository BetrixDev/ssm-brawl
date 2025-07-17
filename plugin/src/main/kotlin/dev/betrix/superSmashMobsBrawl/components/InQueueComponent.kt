package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

class InQueueComponent(val minigameId: String) : Component<InQueueComponent> {
    override fun type() = InQueueComponent

    companion object : ComponentType<InQueueComponent>()
}
