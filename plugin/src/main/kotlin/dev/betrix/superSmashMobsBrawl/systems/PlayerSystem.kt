package dev.betrix.superSmashMobsBrawl.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import dev.betrix.superSmashMobsBrawl.components.PlayerComponent

class PlayerSystem : IteratingSystem(family { all(PlayerComponent) }) {
    override fun onTickEntity(entity: Entity) {}
}
