package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.entity.Player

data class PartyPromotePlayerComponent(val leader: Player, val promotee: Player) : Component<PartyPromotePlayerComponent> {
    override fun type() = PartyPromotePlayerComponent

    companion object : ComponentType<PartyPromotePlayerComponent>()
}
