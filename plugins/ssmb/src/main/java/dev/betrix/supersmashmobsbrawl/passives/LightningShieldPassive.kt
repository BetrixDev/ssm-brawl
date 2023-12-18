package dev.betrix.supersmashmobsbrawl.passives

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.betrix.supersmashmobsbrawl.disguises.CreeperDisguise
import dev.betrix.supersmashmobsbrawl.extensions.doKnockback
import kotlinx.coroutines.delay
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class LightningShieldPassive constructor(
    private val player: Player,
    private val disguise: CreeperDisguise
) : SSMBPassive() {
    private val plugin = SuperSmashMobsBrawl.instance

    private var isActive = false

    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    override fun destroyPassive() {
        HandlerList.unregisterAll(this)
    }

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.entity != player || event.isCancelled || event.damager !is LivingEntity) return

        if (
            event.cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK ||
            event.cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK
        ) {
            if (isActive) {
                event.isCancelled = true
                disguise.setPowered(false)

                val damager = event.damager as LivingEntity
                damager.doKnockback(2.5, 4.0, damager.health, event.entity.location.toVector(), null)
                damager.damage(4.0, event.entity)

                plugin.launch {
                    repeat(20) {
                        damager.playHurtAnimation(0F)
                        delay(1.ticks)
                    }
                }
            }
        } else if (event.cause == EntityDamageEvent.DamageCause.FIRE_TICK) {
            return
        } else if (event.cause == EntityDamageEvent.DamageCause.STARVATION) {
            return
        } else if (event.cause == EntityDamageEvent.DamageCause.POISON) {
            return
        }

        activate()
    }

    private fun activate() {
        player.removePotionEffect(PotionEffectType.SPEED)
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 80, 1, false, false))
        player.world.playSound(player.location, Sound.ENTITY_CREEPER_HURT, 3F, 1.25F)

        disguise.setPowered(true)
        isActive = true

        plugin.launch {
            delay(40.ticks)

            disguise.setPowered(false)
            isActive = false
        }
    }
}