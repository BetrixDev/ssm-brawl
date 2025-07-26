package dev.betrix.superSmashMobsBrawl.abilities.instances

import dev.betrix.superSmashMobsBrawl.abilities.definitions.AbilityDefinition
import dev.betrix.superSmashMobsBrawl.extensions.doKnockback
import dev.betrix.superSmashMobsBrawl.utils.isOnGround
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.entity.ThrownPotion
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PotionSplashEvent
import org.bukkit.inventory.ItemStack

class SulphurBombAbilityInstance(definition: AbilityDefinition, player: Player) :
    AbilityInstance(definition, player) {
    private val knockbackModifier = 2.5
    private val explosionRadius = 3.0
    private val projectileSpeed = 1.55
    private val projectileCollisionSize = 0.65

    override fun canActivate(): Boolean {
        return super.canActivate() && isOnGround(player) && !player.isInWater
    }

    override fun setup() {
        //        TODO("Not yet implemented")
    }

    override fun teardown() {
        TODO("Not yet implemented")
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
        projectile.velocity = direction.multiply(projectileSpeed)
        projectile.shooter = player
        projectile.item = ItemStack.of(Material.COAL)

        // Throw sound
        player.world.playSound(player.location, Sound.ENTITY_CREEPER_PRIMED, 0.5f, 1.2f)

        trackProjectile(projectile)
    }

    private fun trackProjectile(projectile: ThrownPotion) {
        repeatingTask(1) {
            val nearbyEntities =
                projectile.getNearbyEntities(
                    projectileCollisionSize,
                    projectileCollisionSize,
                    projectileCollisionSize,
                )

            nearbyEntities.forEach { entity ->
                if (entity !is Player || entity == player) {
                    return@forEach
                }

                val splashEvent =
                    PotionSplashEvent(projectile, entity, null, null, mutableMapOf(entity to 1.0))
                splashEvent.callEvent()
                this.cancel()
            }
        }
    }

    @EventHandler
    fun onPotionSplash(event: PotionSplashEvent) {
        if (event.entity.ownerUniqueId != player.uniqueId) return

        event.isCancelled = true

        val splashLocation = event.entity.location
        val damage = 3.0

        // Handle direct hit
        event.hitEntity?.let { hitEntity ->
            if (hitEntity is Player && hitEntity != player) {
                dealDamageAndKnockback(hitEntity, splashLocation, damage, knockbackModifier)
                player.sendMessage(
                    "§aDirectly hit ${hitEntity.name} for §6${damage.toInt()} §adamage!"
                )
            }
        }

        // Area damage
        handleAreaDamage(splashLocation, damage)

        // Visual effects
        createExplosionEffects(splashLocation)
    }

    private fun handleAreaDamage(location: org.bukkit.Location, damage: Double) {
        val nearbyPlayers =
            location.world
                ?.getNearbyEntities(location, explosionRadius, explosionRadius, explosionRadius)
                ?.filterIsInstance<Player>()
                ?.filter { it != player }

        nearbyPlayers?.forEach { target ->
            val distance = target.location.distance(location)
            if (distance <= explosionRadius) {
                val damageMultiplier = 1.0 - (distance / explosionRadius) * 0.5
                val finalDamage = damage * damageMultiplier.coerceAtLeast(0.3)
                val finalKnockback = knockbackModifier * damageMultiplier

                dealDamageAndKnockback(target, location, finalDamage, finalKnockback)
                player.sendMessage("§7Hit ${target.name} for §6${finalDamage.toInt()} §7damage!")
            }
        }
    }

    private fun dealDamageAndKnockback(
        target: Player,
        explosionLocation: org.bukkit.Location,
        damage: Double,
        knockback: Double,
    ) {
        target.doKnockback(knockback, damage, target.health, explosionLocation.toVector(), null)
        target.damage(damage, player)
    }

    private fun createExplosionEffects(location: org.bukkit.Location) {
        // Main explosion
        location.world?.spawnParticle(Particle.EXPLOSION, location, 1)
        location.world?.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.5f)

        // Additional effects
        location.world?.spawnParticle(Particle.SMOKE, location, 5)
        location.world?.spawnParticle(Particle.FLAME, location, 8)
    }
}
