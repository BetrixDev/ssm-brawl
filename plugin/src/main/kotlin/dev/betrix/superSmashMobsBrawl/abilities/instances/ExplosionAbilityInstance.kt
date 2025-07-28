package dev.betrix.superSmashMobsBrawl.abilities.instances

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.abilities.definitions.AbilityDefinition
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.events.SmashDamageType
import dev.betrix.superSmashMobsBrawl.extensions.doKnockback
import dev.betrix.superSmashMobsBrawl.utils.mm
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.getNearbyEntities
import gg.flyte.twilight.scheduler.repeatingTask
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerToggleSneakEvent

class ExplosionAbilityInstance(definition: AbilityDefinition, player: Player) :
    AbilityInstance(definition, player) {

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
                if (this@ExplosionAbilityInstance.player != player || !isExplodeActive) {
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

        player.walkSpeed = 0.05f
        player.level = 0
        player.exp = 0f

        var iteration = 0
        runnables.add(
            repeatingTask(1) {
                if (iteration == fuseTimeTicks || !isExplodeActive) {
                    cancel()
                }

                player.exp = (iteration + 1) / fuseTimeTicks.toFloat()

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
                            (0.1 + 0.9 * ((explosionRadius - distance) / explosionRadius)) * 0.75

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
                                player,
                                damage,
                                explosionKnockbackMultiplier,
                                SmashDamageType.Explosion,
                            )

                        damageEvent.callEvent()
                    }

                setCooldown(currentTimeAtActivation)
            }
        )
    }

    private fun resetPlayerData() {
        player.walkSpeed = 0.2f
        player.level = 0
        player.exp = 0f
    }
}
