package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.extensions.itemEffect
import kotlin.math.max
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player

class BoneExplosionAbility(player: Player) : BrawlAbility("bone_explosion", player) {

    private val explosionRadius = metadata.double("explosionRadius") ?: 7.0
    private val baseDamage = metadata.double("baseDamage") ?: 6.0
    private val explosionKnockbackMultiplier =
        metadata.double("explosionKnockbackMultiplier") ?: 2.5
    private val explosionBoneCount = metadata.int("explosionBoneCount") ?: 48
    private val explosionBoneVelocity = metadata.double("explosionBoneVelocity") ?: 0.8

    override fun activate() {
        player.location
            .clone()
            .add(0.0, 0.5, 0.5)
            .itemEffect(
                explosionBoneCount,
                explosionBoneVelocity,
                Sound.ENTITY_SKELETON_HURT,
                2f,
                1.2f,
                Material.BONE,
                40,
            )

        val validEntities =
            player.location.getNearbyPlayers(explosionRadius).filter { it != player }

        validEntities.forEach { entity ->
            val damage =
                max(
                    0.0,
                    baseDamage *
                        (1.0 - (entity.location.distance(player.location) / explosionRadius)),
                )

            val damageEvent =
                BrawlDamageEvent(
                    entity,
                    Damager.DamagerLivingEntity(player),
                    damage,
                    explosionKnockbackMultiplier,
                    BrawlDamageType.Explosion,
                )

            damageEvent.callEvent()
        }

        super.activate()
    }
}
