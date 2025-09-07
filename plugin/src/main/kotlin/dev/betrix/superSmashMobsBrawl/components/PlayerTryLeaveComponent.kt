package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

enum class LeaveSpecifier {
    PARTY,
    QUEUE,
    DISBAND,
}

class PlayerTryLeaveComponent(val specifier: LeaveSpecifier?) : Component<PlayerTryLeaveComponent> {
    override fun type() = PlayerTryLeaveComponent

    companion object : ComponentType<PlayerTryLeaveComponent>()
}
