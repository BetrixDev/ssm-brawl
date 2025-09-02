package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

/** Marks an entity as waiting to respawn and tracks timing for countdown UI. */
data class RespawnComponent(val respawnAtMillis: Long?, var lastAnnouncedSecondsLeft: Int = -1) :
    Component<RespawnComponent> {
    override fun type() = RespawnComponent

    companion object : ComponentType<RespawnComponent>()
}
