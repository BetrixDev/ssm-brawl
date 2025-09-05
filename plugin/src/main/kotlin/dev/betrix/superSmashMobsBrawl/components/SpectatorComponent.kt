package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.entity.Entity

data class SpectatorComponent(
    var spectatingPlayer: Entity? = null,
    val spectatorMode: SpectatorMode = SpectatorMode.FREE_ROAM,
) : Component<SpectatorComponent> {
    override fun type() = SpectatorComponent

    companion object : ComponentType<SpectatorComponent>()

    enum class SpectatorMode {
        FREE_ROAM,
        FOLLOW_PLAYER,
        FIXED_CAMERA,
    }
}
