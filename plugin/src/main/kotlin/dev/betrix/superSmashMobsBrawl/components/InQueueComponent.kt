package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class InQueueComponent(
    val minigameId: String,
    val startTimestamp: Long = System.currentTimeMillis(),
    var priority: Int = 0,
) : Component<InQueueComponent> {
    override fun type() = InQueueComponent

    companion object : ComponentType<InQueueComponent>()
}

fun InQueueComponent.getWaitTime(): Long = System.currentTimeMillis() - startTimestamp

fun InQueueComponent.hasWaitedLongerThan(millis: Long): Boolean = getWaitTime() > millis

fun InQueueComponent.shouldPrioritize(): Boolean = priority > 0 || hasWaitedLongerThan(60000L)
