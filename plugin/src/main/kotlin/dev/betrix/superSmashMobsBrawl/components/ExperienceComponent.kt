package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class ExperienceComponent(
    var pendingXp: Long = 0L,
    var sessionXp: Long = 0L,
    val xpSources: MutableList<XpGain> = mutableListOf(),
) : Component<ExperienceComponent> {
    override fun type() = ExperienceComponent

    companion object : ComponentType<ExperienceComponent>()
}

data class XpGain(
    val amount: Long,
    val source: String,
    val timestamp: Long = System.currentTimeMillis(),
)
