package dev.betrix.superSmashMobsBrawl.systems

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.FamilyOnAdd
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import dev.betrix.superSmashMobsBrawl.components.MinecraftPlayerComponent
import dev.betrix.superSmashMobsBrawl.components.abilities.SulphurBombAbilityComponent
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.getNearbyEntities
import gg.flyte.twilight.extension.name
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.entity.ThrownPotion
import org.bukkit.event.block.Action
import org.bukkit.event.entity.PotionSplashEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class SulphurBombSystem: IteratingSystem( family { all(SulphurBombAbilityComponent, MinecraftPlayerComponent) }), FamilyOnAdd {
    private val lastUsedMap = hashMapOf<Player, Long>()

    override fun onTickEntity(entity: Entity) {}

    override fun onAddEntity(entity: Entity) {
        val localPlayer = entity[MinecraftPlayerComponent].player

        val itemStack = ItemStack.of(Material.IRON_SHOVEL)
        itemStack.name("Sulphur Bomb")

        localPlayer.inventory.setItem(1, itemStack)

        event<PlayerInteractEvent> InteractEvent@ {
            if (player != localPlayer || action != Action.RIGHT_CLICK_AIR || action != Action.RIGHT_CLICK_BLOCK || item != itemStack) {
                return@InteractEvent
            }

            isCancelled = true

            if (!canUseAbility(player)) {
                return@InteractEvent
            }

            val location = player.eyeLocation
            val direction = location.direction

            val projectile = player.world.spawn(location, ThrownPotion::class.java)
            projectile.velocity = direction.multiply(1.55)
            projectile.shooter = player
            projectile.item = ItemStack.of(Material.COAL)

            val taskStartTime = System.currentTimeMillis()

            repeatingTask(1) {
                if (taskStartTime + 30000 > System.currentTimeMillis()) {
                    this.cancel()
                    return@repeatingTask
                }

                val nearbyEntities = projectile.getNearbyEntities(0.65)

                nearbyEntities.forEach {
                    if (it !is Player || it == player) {
                        return@forEach
                    }

                    val splashEvent = PotionSplashEvent(projectile, it, null, null, mutableMapOf(it to 1.0))
                    splashEvent.callEvent()
                    this.cancel()
                }
            }
        }
    }

    private fun canUseAbility(player: Player): Boolean {
        val lastUsedTime = lastUsedMap[player]

        if (lastUsedTime == null) {
            return true
        }

        val currentTime = System.currentTimeMillis()

        if (currentTime < lastUsedTime + 3000) {
            val timeLeft = ((lastUsedTime + 3000) - currentTime) / 1000.0

            player.sendMessage("You can use that ability again in ${timeLeft}s")

            return false
        }

        return true
    }
}