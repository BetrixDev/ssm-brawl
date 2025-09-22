package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.extensions.doKnockback
import dev.betrix.superSmashMobsBrawl.extensions.playSound
import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import dev.betrix.superSmashMobsBrawl.projectiles.BrawlProjectile
import dev.betrix.superSmashMobsBrawl.projectiles.ProjectileAction
import gg.flyte.twilight.extension.round
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class SulphurBombAbility(player: Player) : BrawlAbility("sulphur_bomb", player) {

    private val projectileKnockbackModifier = metadata.double("projectileKnockbackModifier") ?: 2.5
    private val projectileDamage = metadata.double("projectileDamage") ?: 6.5
    private val projectileVelocityMultiplier =
        metadata.double("projectileVelocityMultiplier") ?: 1.55
    private val projectileSize = metadata.double("projectileSize") ?: 0.65
    private val projectileTrailMaxParticles = metadata.int("projectileTrailMaxParticles") ?: 8

    private val activeProjectiles = mutableListOf<BrawlProjectile>()

    override fun activate() {
        throwProjectile()
        super.activate()
    }

    override fun teardown() {
        activeProjectiles.forEach { it.teardown() }
        activeProjectiles.clear()
        super.teardown()
    }

    private fun throwProjectile() {
        player.playSound(Sound.ENTITY_FISHING_BOBBER_THROW, pitch = 0.75f)

        val sulphurBombProjectile =
            BrawlProjectile.potion(
                    player,
                    ItemStack.of(Material.COAL),
                    "abilities.sulphur_bomb.name",
                )
                .velocityMultiplier(projectileVelocityMultiplier)
                .projectileSize(projectileSize)
                .trailEffect(Particle.SMOKE, count = projectileTrailMaxParticles)
                .impactEffect(Particle.EXPLOSION, Sound.ENTITY_GENERIC_EXPLODE)
                .onHitEntity { entity, projectile ->
                    handleSulphurBombHit(entity, projectile)
                    ProjectileAction.DESTROY
                }
                .onHitBlock { _, projectile ->
                    handleSulphurBombHit(null, projectile)
                    ProjectileAction.DESTROY
                }
                .onTeardown { activeProjectiles.remove(it) }
                .launch()

        activeProjectiles.add(sulphurBombProjectile)
    }

    private fun handleSulphurBombHit(entity: Entity?, projectile: BrawlProjectile) {
        player.sendDebugMessage(
            "[SB] Sulphur bomb exploded at ${projectile.projectileEntity?.x?.round(1)}, ${
                projectile.projectileEntity?.y?.round(
                    1
                )
            }, ${projectile.projectileEntity?.z?.round(1)} after ${projectile.projectileEntity?.ticksLived} ticks"
        )

        if (entity != null && entity is LivingEntity) {
            player.sendDebugMessage("[SB] ${entity.name} was hit")

            val damageEvent =
                BrawlDamageEvent(
                    entity,
                    Damager.DamagerLivingEntity(player),
                    projectileDamage,
                    0.0,
                    BrawlDamageType.Projectile,
                )

            damageEvent.callEvent()

            entity.doKnockback(
                projectileKnockbackModifier,
                projectileDamage,
                entity.health,
                projectile.projectileEntity?.location?.toVector(),
                null,
            )
        } else {
            player.sendDebugMessage("[SB] No entity was hit")
        }
    }
}
