package net.ssmb.passives

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.delay
import net.ssmb.SSMB
import net.ssmb.extensions.doKnockback
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class LightningShieldPassive(private val player: Player) : IPassive, Listener {
    private val plugin = SSMB.instance

    private var isActive = false

    override fun createPassive() {
        plugin.server.pluginManager.registerEvents(this, plugin)
        isActive = false
    }

    override fun destroyPassive() {
        HandlerList.unregisterAll(this)
        isActive = false
    }

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.isCancelled) return
        if (event.entity != player) return
        if (event.damager !is LivingEntity) return

        if (
            event.cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK ||
                event.cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK
        ) {
            if (isActive) {
                event.isCancelled = true

                val damager = event.damager as LivingEntity
                damager.doKnockback(
                    2.5,
                    4.0,
                    damager.health,
                    event.entity.location.toVector(),
                    null
                )
                damager.damage(4.0, event.entity)

                plugin.launch {
                    repeat(20) {
                        damager.playHurtAnimation(0f)
                        delay(1.ticks)
                    }
                }
            }
        } else if (
            event.cause == EntityDamageEvent.DamageCause.FIRE_TICK ||
                event.cause == EntityDamageEvent.DamageCause.STARVATION ||
                event.cause == EntityDamageEvent.DamageCause.POISON
        ) {
            return
        }

        activate()
    }

    private fun activate() {
        isActive = true

        player.removePotionEffect(PotionEffectType.SPEED)
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 80, 1, false, false))
        player.world.playSound(player.location, Sound.ENTITY_CREEPER_HURT, 3f, 1.25f)

        plugin.launch {
            delay(40.ticks)
            isActive = false
        }
    }
}
