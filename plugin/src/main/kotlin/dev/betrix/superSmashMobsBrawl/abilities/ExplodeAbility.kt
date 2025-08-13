package dev.betrix.superSmashMobsBrawl.abilities

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.events.SmashDamageType
import dev.betrix.superSmashMobsBrawl.extensions.doKnockback
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.getNearbyEntities
import gg.flyte.twilight.scheduler.repeatingTask
import kotlin.math.min
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.util.Vector

class ExplodeAbility(player: Player) : BrawlAbility("explode", player) {

    private var isExplodeActive = false
    private val fuseTimeTicks = metadata.int("fuseTimeTicks") ?: 30
    private val explosionRadius = metadata.double("explosionRadius") ?: 8.0
    private val explosionKnockbackMultiplier =
        metadata.double("explosionKnockbackMultiplier") ?: 2.5

    override fun canActivate(sendMessage: Boolean): Boolean {
        if (isExplodeActive) {
            if (sendMessage) {
                player.sendMessage(lang.t("messages.abilities.explode.alreadyCharging"))
            }
            return false
        }

        return super.canActivate(sendMessage) && !isExplodeActive
    }

    override fun teardown() {
        isExplodeActive = false
        super.teardown()
    }

    override fun setup() {
        listeners.add(
            event<PlayerToggleSneakEvent> {
                if (this@ExplodeAbility.player != player || !isExplodeActive) {
                    return@event
                }

                setCooldown()
                isExplodeActive = false
                resetPlayerData()
            }
        )

        super.setup()
    }

    override fun activate() {
        val currentTimeAtActivation = System.currentTimeMillis()

        super.activate()

        isExplodeActive = true

        player.velocity = Vector()
        player.level = 0
        player.exp = 0f

        var iteration = 0
        runnables.add(
            repeatingTask(1) {
                if (iteration == fuseTimeTicks || !isExplodeActive) {
                    cancel()

                    return@repeatingTask
                }

                player.exp = min((iteration + 1) / fuseTimeTicks.toFloat(), 0.9999f)

                val volume = 0.5f + iteration / 20f

                player.world.playSound(player.location, Sound.ENTITY_CREEPER_PRIMED, volume, volume)

                iteration++
            }
        )

        jobs.add(
            plugin.launch {
                withContext(Dispatchers.IO) { delay(fuseTimeTicks.ticks) }

                if (!isExplodeActive) {
                    cancel()
                    return@launch
                }

                resetPlayerData()

                isExplodeActive = false

                player.world.playSound(player.location, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f)
                player.world.spawnParticle(Particle.EXPLOSION, player.location, 3)

                player
                    .getNearbyEntities(explosionRadius)
                    .filter { it is LivingEntity && it != player }
                    .forEach { entity ->
                        val distance = player.location.distance(entity.location)
                        val damage =
                            ((0.1 + 0.9 * ((explosionRadius - distance) / explosionRadius)) * 20) *
                                0.75

                        entity.doKnockback(
                            explosionKnockbackMultiplier,
                            damage,
                            (entity as LivingEntity).health,
                            player.location.toVector(),
                            null,
                        )

                        val damageEvent =
                            SmashDamageEvent(
                                entity,
                                Damager.DamagerLivingEntity(player),
                                damage,
                                explosionKnockbackMultiplier,
                                SmashDamageType.Explosion,
                            )

                        damageEvent.callEvent()
                    }

                player.setVelocity(1.8, 0.2, 1.4, true)

                setCooldown(currentTimeAtActivation)
            }
        )
    }

    private fun resetPlayerData() {
        player.level = 0
        player.exp = 0f
    }
}
