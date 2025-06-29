package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

class InQueue(val queueId: String) : Component<InQueue> {
    override fun type() = InQueue

    companion object : ComponentType<InQueue>()
}
