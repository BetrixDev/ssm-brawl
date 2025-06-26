package dev.betrix.combatServer.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class BelowNameDisplay(
    val text: net.kyori.adventure.text.Component
) : Component<BelowNameDisplay> {
    override fun type() = BelowNameDisplay

    companion object : ComponentType<BelowNameDisplay>()
}