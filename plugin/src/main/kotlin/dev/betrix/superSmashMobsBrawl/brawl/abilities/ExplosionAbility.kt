package dev.betrix.superSmashMobsBrawl.brawl.abilities

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.abilities.AbilityMetadata
import dev.betrix.superSmashMobsBrawl.brawl.BrawlAbility
import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.events.SmashDamageType
import dev.betrix.superSmashMobsBrawl.extensions.doKnockback
import dev.betrix.superSmashMobsBrawl.extensions.setVelocity
import dev.betrix.superSmashMobsBrawl.utils.mm
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.getNearbyEntities
import gg.flyte.twilight.scheduler.repeatingTask
import kotlin.math.min
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.util.Vector

class ExplosionAbility(id: String, player: Player, metadata: AbilityMetadata) :
    BrawlAbility(id, player, metadata) {

    private var isExplodeActive = false
    private val fuseTimeTicks = 30
    private val explosionRadius = 8.0
    private val explosionKnockbackMultiplier = 2.5

    override fun canActivate(): Boolean {
        if (isExplodeActive) {
            player.sendMessage(mm("<red>Already charging explosion!</red>"))
            return false
        }

        return super.canActivate() && !isExplodeActive
    }

    override fun teardown() {
        isExplodeActive = false
        super.teardown()
    }

    override fun setup() {
        listeners.add(
            event<PlayerToggleSneakEvent> {
                if (this@ExplosionAbility.player != player || !isExplodeActive) {
                    return@event
                }

                setCooldown()
                isExplodeActive = false
                resetPlayerData()
            }
        )
    }

    override fun activate() {
        val currentTimeAtActivation = System.currentTimeMillis()

        setCooldown()

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

                val volume = 0.5f + iteration / 20

                player.world.playSound(player.location, Sound.ENTITY_CREEPER_PRIMED, volume, volume)

                iteration++
            }
        )

        jobs.add(
            SuperSmashMobsBrawl.instance.launch {
                withContext(SuperSmashMobsBrawl.instance.asyncDispatcher) {
                    delay(fuseTimeTicks.ticks)
                }

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
                                Damager.LivingEntity(player),
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
