package dev.betrix.superSmashMobsBrawl.brawl.abilities

import dev.betrix.superSmashMobsBrawl.abilities.AbilityMetadata
import dev.betrix.superSmashMobsBrawl.brawl.BrawlAbility
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.events.SmashDamageType
import dev.betrix.superSmashMobsBrawl.extensions.itemEffect
import kotlin.math.max
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player

class BoneExplosionAbility(id: String, player: Player, metadata: AbilityMetadata) :
    BrawlAbility(id, player, metadata) {

    private val explosionRadius = 7.0
    private val baseDamage = 6.0

    override fun activate() {
        player.location
            .add(0.0, 0.5, 0.5)
            .itemEffect(48, 0.8, Sound.ENTITY_SKELETON_HURT, 2f, 1.2f, Material.BONE, 40)

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
                SmashDamageEvent(
                    entity,
                    Damager.LivingEntity(player),
                    damage,
                    2.5,
                    SmashDamageType.Explosion,
                )

            damageEvent.callEvent()
        }

        super.activate()
    }
}
