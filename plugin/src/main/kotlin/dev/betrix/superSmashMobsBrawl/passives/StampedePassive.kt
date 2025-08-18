package dev.betrix.superSmashMobsBrawl.passives

import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import dev.betrix.superSmashMobsBrawl.events.SmashDamageType
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.TwilightRunnable
import gg.flyte.twilight.scheduler.repeatingTask
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class StampedePassive(player: Player) : BrawlPassive("stampede", player) {

    private val stackIncreaseTimeMs = metadata.long("stackIncreaseTimeMs") ?: 3000
    private val maxStacks = metadata.int("maxStacks") ?: 3
    private val stopSprintDamage = metadata.int("stopSprintDamage") ?: 3

    private var startTimeMs = 0L
    private var ticks = 0
    private var stacks = -1

    private var runnable: TwilightRunnable? = null

    override fun setup() {
        super.setup()

        runnable?.cancel()
        runnable =
            repeatingTask(1) {
                ticks = (ticks + 1) % 5

                if (ticks != 0) {
                    return@repeatingTask
                }

                if (stacks == -1) {
                    if (player.isSprinting && !player.location.block.isLiquid) {
                        startTimeMs = System.currentTimeMillis()
                        stacks = 0
                    }

                    return@repeatingTask
                }

                if (!player.isSprinting || player.location.block.isLiquid) {
                    removeStampede()
                    return@repeatingTask
                }

                if (stacks > 0) {
                    player.removePotionEffect(PotionEffectType.SPEED)
                    player.addPotionEffect(
                        PotionEffect(PotionEffectType.SPEED, 38, stacks - 1, false, false)
                    )
                }

                if (stacks < maxStacks) {
                    val elapsedMs = System.currentTimeMillis() - startTimeMs
                    val denominator =
                        if (stackIncreaseTimeMs > 0) stackIncreaseTimeMs.toFloat() else 1f
                    player.exp = kotlin.math.min(0.9999f, elapsedMs.toFloat() / denominator)
                } else {
                    player.exp = 0.9999f
                }

                if (System.currentTimeMillis() - startTimeMs < stackIncreaseTimeMs) {
                    return@repeatingTask
                }

                startTimeMs = System.currentTimeMillis()

                if (stacks < maxStacks) {
                    stacks++
                    player.world.playSound(
                        player.eyeLocation,
                        Sound.ENTITY_COW_HURT,
                        2f,
                        0.75f + 0.25f * stacks,
                    )
                }
            }
        runnable?.let { runnables.add(it) }

        listeners.add(
            event<SmashDamageEvent> {
                when (victim) {
                    player -> {
                        if (isCancelled || damage < stopSprintDamage) {
                            return@event
                        }

                        removeStampede()
                    }

                    else -> {
                        if (isCancelled || stacks <= 0) {
                            return@event
                        }

                        val isThisPlayerDamager =
                            when (damager) {
                                is Damager.DamagerLivingEntity -> damager.livingEntity == player
                                else -> false
                            }

                        if (!isThisPlayerDamager) {
                            return@event
                        }

                        if (damageType != null && damageType != SmashDamageType.MeleeAttack) {
                            return@event
                        }

                        damage += stacks
                        knockbackMultiplier *= 1 + 0.1 * stacks

                        player.world.playSound(
                            player.eyeLocation,
                            Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR,
                            1f,
                            2f,
                        )
                        player.world.playSound(player.eyeLocation, Sound.ENTITY_COW_HURT, 2f, 2f)
                        removeStampede()
                    }
                }
            }
        )
    }

    private fun removeStampede() {
        stacks = -1
        player.removePotionEffect(PotionEffectType.SPEED)
        player.exp = 0f
    }

    override fun teardown() {
        removeStampede()
        super.teardown()
    }
}
