package dev.betrix.superSmashMobsBrawl.abilities.instances

import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.abilities.definitions.AbilityDefinition
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.events.SmashDamageType
import dev.betrix.superSmashMobsBrawl.extensions.doKnockback
import dev.betrix.superSmashMobsBrawl.utils.isOnGround
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.getNearbyEntities
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.ThrownPotion
import org.bukkit.event.entity.PotionSplashEvent
import org.bukkit.inventory.ItemStack

class SulphurBombAbilityInstance(definition: AbilityDefinition, player: Player) :
    AbilityInstance(definition, player) {

    private val projectileCollisionSize = 0.65
    private val projectileKnockbackModifier = 2.5
    private val projectileDamage = 6.5

    override fun canActivate(): Boolean {
        return super.canActivate() && isOnGround(player) && !player.isInWater
    }

    override fun setup() {
        listeners.add(
            event<PotionSplashEvent> {
                SuperSmashMobsBrawl.instance.logger.info("Potion splashed")
                if (entity.ownerUniqueId != player.uniqueId) {
                    return@event
                }

                isCancelled = true

                if (hitEntity != null && hitEntity is LivingEntity && hitEntity != player) {
                    val target = hitEntity as LivingEntity

                    val damageEvent =
                        SmashDamageEvent(
                            target,
                            player,
                            projectileDamage,
                            0.0,
                            SmashDamageType.Projectile,
                        )

                    damageEvent.callEvent()

                    target.doKnockback(
                        projectileKnockbackModifier,
                        projectileDamage,
                        target.health,
                        entity.location.toVector(),
                        null,
                    )
                }

                player.world.spawnParticle(Particle.SMOKE, entity.location, 1)
                player.world.playSound(entity.location, Sound.ENTITY_GENERIC_EXPLODE, 1F, 1.5F)
            }
        )
    }

    override fun activate(): Boolean {
        if (!canActivate()) {
            if (isOnCooldown()) {
                player.sendMessage("§cSulphur Bomb is on cooldown! (${getRemainingCooldown()}s)")
            }
            return false
        }

        setCooldown()
        throwProjectile()

        player.sendMessage("§7Sulphur Bomb thrown!")
        return true
    }

    private fun throwProjectile() {
        val location = player.eyeLocation
        val direction = location.direction

        val projectile = player.world.spawn(location, ThrownPotion::class.java)
        projectile.velocity = direction.multiply(1.55)
        projectile.shooter = player
        projectile.item = ItemStack.of(Material.COAL)

        repeatingTask(1) {
            val nearbyEntities =
                projectile.getNearbyEntities(projectileCollisionSize).sortedBy {
                    it.location.distance(projectile.location)
                }

            if (nearbyEntities.isEmpty()) {
                return@repeatingTask
            }

            val closestEntity = nearbyEntities.first()

            val splashEvent = PotionSplashEvent(projectile, closestEntity, null, null, mapOf())
            splashEvent.callEvent()
        }
    }
}
