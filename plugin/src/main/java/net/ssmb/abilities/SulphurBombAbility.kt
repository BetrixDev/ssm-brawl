package net.ssmb.abilities

import br.com.devsrsouza.kotlinbukkitapi.extensions.item
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import net.ssmb.SSMB
import net.ssmb.dtos.minigame.MinigameStartSuccess
import net.ssmb.extensions.doKnockback
import net.ssmb.extensions.get
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.entity.ThrownPotion
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PotionSplashEvent

class SulphurBombAbility(
    private val player: Player,
    abilityEntry: MinigameStartSuccess.PlayerData.KitData.AbilityEntry
) : SsmbAbility(player, abilityEntry) {
    private val plugin = SSMB.instance

    override fun doAbility() {
        timeLastUsed = System.currentTimeMillis()

        val location = player.eyeLocation
        val direction = location.direction

        val projectile = player.world.spawn(location, ThrownPotion::class.java)
        projectile.velocity = direction.multiply(1.55)
        projectile.shooter = player
        projectile.item = item(Material.COAL)

        plugin.launch {
            val projectileSize = 0.65

            while (true) {
                val nearbyEntities =
                    projectile.getNearbyEntities(projectileSize, projectileSize, projectileSize)

                nearbyEntities.forEach {
                    if (it !is Player || it == player) {
                        return@forEach
                    }

                    val splashEvent =
                        PotionSplashEvent(projectile, it, null, null, mutableMapOf(it to 1.0))
                    splashEvent.callEvent()
                    this.cancel()
                }

                delay(1.ticks)
            }
        }
    }

    @EventHandler
    fun onPotionSplash(event: PotionSplashEvent) {
        if (event.entity.ownerUniqueId != player.uniqueId) return

        event.isCancelled = true

        val splashedItem = event.entity
        val splashedLocation = splashedItem.location

        val projKnockbackMultiplier = 2.5
        val projDamage = 6.5

        if (event.hitEntity != null && event.hitEntity is Player && event.hitEntity != player) {
            val target = event.hitEntity as Player
            target.doKnockback(
                projKnockbackMultiplier,
                projDamage,
                target.health,
                player.location.toVector(),
                null
            )
            target.damage(projDamage, splashedItem)
        }

        player.world.spawnParticle(Particle.EXPLOSION_LARGE, splashedLocation, 1)
        player.world.playSound(splashedLocation, Sound.ENTITY_GENERIC_EXPLODE, 1F, 1.5F)
    }
}
