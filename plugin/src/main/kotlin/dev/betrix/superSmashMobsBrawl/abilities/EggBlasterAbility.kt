package dev.betrix.superSmashMobsBrawl.abilities

import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.BrawlDamageType
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.projectiles.BrawlProjectile
import dev.betrix.superSmashMobsBrawl.projectiles.ProjectileAction
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.Sound
import org.bukkit.entity.Egg
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.Vector

class EggBlasterAbility(player: Player) : BrawlAbility("egg_blaster", player) {

    private val durationMs = metadata.long("durationMs") ?: 750L
    private val eggDamage = metadata.double("eggDamage") ?: 1.0

    private var eggTask: TwilightRunnable? = null
    private var startTime: Long = 0
    private val activeProjectiles = mutableListOf<BrawlProjectile>()

    override fun onPlayerInteract(event: PlayerInteractEvent) {
        if (eggTask != null) {
            return
        }

        super.onPlayerInteract(event)
    }

    override fun activate() {
        super.activate()

        startTime = System.currentTimeMillis()

        eggTask =
            repeatingTask(0) {
                if (!player.isBlocking) {
                    stopFiring()
                    return@repeatingTask
                }

                if (System.currentTimeMillis() - startTime >= durationMs) {
                    stopFiring()
                    return@repeatingTask
                }

                shootEgg()
            }
    }

    override fun teardown() {
        stopFiring()

        while (activeProjectiles.isNotEmpty()) {
            val projectile = activeProjectiles.removeAt(activeProjectiles.lastIndex)
            projectile.teardown()
        }

        super.teardown()
    }

    private fun shootEgg() {
        var offset = player.location.direction
        if (offset.y < 0) {
            offset.y = 0.0
        }

        val spawnLocation = player.location.add(0.0, 0.5, 0.0).add(offset)
        val eggVelocity = player.location.direction.add(Vector(0.0, 0.2, 0.0))

        val eggProjectile =
            BrawlProjectile.custom(player, "abilities.egg_blaster.name") {
                    player.world.spawn(spawnLocation, Egg::class.java).apply {
                        shooter = player
                        velocity = eggVelocity
                    }
                }
                .onHitEntity { entity, projectile ->
                    handleEggHit(entity)
                    ProjectileAction.DESTROY
                }
                .onTeardown { activeProjectiles.remove(it) }
                .launch()

        activeProjectiles.add(eggProjectile)

        player.world.playSound(player.location, Sound.ENTITY_CHICKEN_EGG, 0.5f, 1f)
    }

    private fun handleEggHit(entity: LivingEntity) {
        val damageEvent =
            BrawlDamageEvent(
                entity,
                Damager.DamagerLivingEntity(player),
                eggDamage,
                knockbackMultiplier = 0.0,
                damageType = BrawlDamageType.Projectile,
            )

        damageEvent.callEvent()

        // Set victim velocity to zero
        entity.velocity = Vector(0.0, 0.0, 0.0)

        // Bypass invulnerability frames
        entity.noDamageTicks = 0
    }

    private fun stopFiring() {
        eggTask?.cancel()
        eggTask = null
    }
}
