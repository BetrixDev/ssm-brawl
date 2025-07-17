package dev.betrix.superSmashMobsBrawl.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import gg.flyte.twilight.extension.feed
import gg.flyte.twilight.extension.heal
import org.bukkit.GameMode

class HasKitComponent : Component<HasKitComponent> {
    override fun type() = HasKitComponent

    companion object : ComponentType<HasKitComponent>()

    override fun World.onAdd(entity: Entity) {
        if (entity has MinecraftPlayerComponent) {
            val player = entity[MinecraftPlayerComponent].player

            player.inventory.clear()
            player.gameMode = GameMode.SURVIVAL
            player.heal()
            player.feed()
            player.exp = 0f
            player.level = 0
        }
    }
}