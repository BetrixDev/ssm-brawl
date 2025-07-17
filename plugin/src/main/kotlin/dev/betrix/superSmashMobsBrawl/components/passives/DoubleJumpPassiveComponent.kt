package dev.betrix.superSmashMobsBrawl.components.passives

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

class DoubleJumpPassiveComponent : Component<DoubleJumpPassiveComponent> {
    override fun type() = DoubleJumpPassiveComponent

    companion object : ComponentType<DoubleJumpPassiveComponent>()
}