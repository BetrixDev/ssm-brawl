package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.extensions.playSound
import dev.betrix.superSmashMobsBrawl.extensions.sendDebugMessage
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
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

class IronHookAbility(player: Player) : BrawlAbility("iron_hook", player) {
    private val activeProjectiles = mutableListOf<BrawlProjectile>()

    private val projectileDamage = metadata.double("projectileDamage") ?: 4.0
    private val projectileSize = (metadata.double("projectileSize") ?: 0.6).coerceAtLeast(0.1)

    override fun activate() {
        super.activate()
        throwProjectile()
    }

    override fun teardown() {
        while (activeProjectiles.isNotEmpty()) {
            val projectile = activeProjectiles.removeAt(activeProjectiles.lastIndex)
            projectile.teardown()
        }
        super.teardown()
    }

    private fun throwProjectile() {
        player.playSound(Sound.ENTITY_IRON_GOLEM_ATTACK, volume = 1.5f, pitch = 0.8f)

        val ironHookProjectile =
            BrawlProjectile.potion(
                    player,
                    ItemStack.of(Material.TRIPWIRE_HOOK),
                    "abilities.iron_hook.name",
                )
                .setInitialVelocity { projectile ->
                    projectile.setVelocity(
                        player.eyeLocation.direction,
                        1.8,
                        false,
                        0.0,
                        0.2,
                        10.0,
                        false,
                    )
                }
                .projectileSize(projectileSize)
                .trailEffect(Particle.CRIT)
                .trailSoundEffect(Sound.ITEM_FLINTANDSTEEL_USE, volume = 1.4f, pitch = 0.8f)
                .onHitEntity { entity, projectile ->
                    handleIronHookHit(entity, projectile)
                    ProjectileAction.DESTROY
                }
                .onTeardown { activeProjectiles.remove(it) }
                .launch()

        activeProjectiles.add(ironHookProjectile)
    }

    private fun handleIronHookHit(entity: Entity?, projectile: BrawlProjectile) {
        player.sendDebugMessage(
            "[SB] Iron hook hit at ${projectile.projectileEntity?.x?.round(1)}, ${
                projectile.projectileEntity?.y?.round(
                    1
                )
            }, ${projectile.projectileEntity?.z?.round(1)} after ${projectile.projectileEntity?.ticksLived} ticks"
        )

        if (entity != null && entity is LivingEntity) {
            player.sendDebugMessage("[SB] ${entity.name} was hit")

            val damage = projectileDamage * projectile.velocityBeforeImpact.length()

            val damageEvent =
                BrawlDamageEvent(
                    entity,
                    Damager.DamagerLivingEntity(player),
                    damage,
                    0.0,
                    BrawlDamageType.Projectile,
                )

            damageEvent.callEvent()

            val pull = player.location.toVector().subtract(entity.location.toVector()).normalize()
            entity.setVelocity(pull, 2.0, false, 0.0, 0.8, 1.5, true)
        } else {
            player.sendDebugMessage("[SB] No entity was hit")
        }
    }
}
