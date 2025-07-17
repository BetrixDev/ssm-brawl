package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class BelowNameDisplayComponent(val text: net.kyori.adventure.text.Component) :
    Component<BelowNameDisplayComponent> {
    override fun type() = BelowNameDisplayComponent

    companion object : ComponentType<BelowNameDisplayComponent>()
}
