package dev.betrix.superSmashMobsBrawl.abilities.instances

import dev.betrix.superSmashMobsBrawl.abilities.definitions.AbilityDefinition
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.events.SmashDamageType
import dev.betrix.superSmashMobsBrawl.extensions.doKnockback
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.add
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
    private val projectileVelocityMultiplier = 1.55

    override fun setup() {
        listeners.add(
            event<PotionSplashEvent> {
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

                player.world.spawnParticle(
                    Particle.CAMPFIRE_COSY_SMOKE,
                    entity.location.add(0, 0.25, 0),
                    25,
                )
                player.world.playSound(entity.location, Sound.ENTITY_GENERIC_EXPLODE, 1F, 1F)
            }
        )
    }

    override fun activate() {
        setCooldown()
        throwProjectile()

        player.sendMessage("§7Sulphur Bomb thrown!")
    }

    private fun throwProjectile() {
        val location = player.eyeLocation
        val direction = location.direction

        val projectile = player.world.spawn(location, ThrownPotion::class.java)
        projectile.velocity = direction.multiply(projectileVelocityMultiplier)
        projectile.shooter = player
        projectile.item = ItemStack.of(Material.COAL)

        runnables.add(
            repeatingTask(2) {
                if (projectile.isDead || !projectile.isValid) {
                    cancel()
                    return@repeatingTask
                }

                val nearbyEntities =
                    projectile
                        .getNearbyEntities(projectileCollisionSize)
                        .filter { it != player }
                        .sortedBy { it.location.distance(projectile.location) }

                if (nearbyEntities.isEmpty()) {
                    return@repeatingTask
                }

                val closestEntity = nearbyEntities.first()

                val splashEvent = PotionSplashEvent(projectile, closestEntity, null, null, mapOf())
                splashEvent.callEvent()

                cancel()
            }
        )
    }
}
