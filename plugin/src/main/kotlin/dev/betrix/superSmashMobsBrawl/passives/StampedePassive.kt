package dev.betrix.superSmashMobsBrawl.passives

import dev.betrix.superSmashMobsBrawl.events.Damager
import dev.betrix.superSmashMobsBrawl.events.SmashDamageEvent
import gg.flyte.twilight.event.event
import gg.flyte.twilight.scheduler.repeatingTask
import kotlin.math.min
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class StampedePassive(player: Player) : BrawlPassive("stampede", player) {

    private val maxLevel = metadata.int("maxLevel") ?: 4
    private val ticksPerLevel = metadata.int("ticksPerLevel") ?: 60 // 3s per level
    private val extraDamagePerLevel = metadata.double("extraDamagePerLevel") ?: 0.5
    private val extraKnockbackPerLevel = metadata.double("extraKnockbackPerLevel") ?: 0.1

    private var currentLevel = 0
    private var progressTicks = 0

    override fun setup() {
        // Progress task
        val task = repeatingTask(1) { onTick() }
        runnables.add(task)

        // Enhance melee hits with extra damage/knockback via extra SmashDamageEvent
        listeners.add(
            event<SmashDamageEvent> {
                val damagerPlayer =
                    (damager as? Damager.DamagerLivingEntity)?.livingEntity as? Player
                if (damagerPlayer != this@StampedePassive.player) return@event
                if (damageType != null) return@event // melee only
                if (currentLevel <= 0) return@event

                val bonusDamage = extraDamagePerLevel * currentLevel
                // Apply extra damage as a separate event so the minigame pipeline handles it
                SmashDamageEvent(victim, Damager.DamagerLivingEntity(player), bonusDamage)
                    .callEvent()

                // Apply an extra tiny nudge of knockback by issuing a zero-damage event is not
                // supported.
                // Knockback is already computed in the minigame per melee event, so we approximate
                // by an
                // additional micro-damage event to trigger another knockback instance when
                // bonusDamage > 0.
                // This is subtle and keeps within the framework constraints.
            }
        )

        super.setup()
    }

    private fun onTick() {
        if (!player.isOnline || player.gameMode != GameMode.SURVIVAL) return

        if (player.isSprinting) {
            if (currentLevel < maxLevel) {
                progressTicks++
                player.exp = min(0.9999f, progressTicks.toFloat() / ticksPerLevel.toFloat())
                if (progressTicks >= ticksPerLevel) {
                    currentLevel++
                    progressTicks = 0
                    applySpeedEffect()
                }
            } else {
                // At max level, keep XP bar full
                player.exp = 0.9999f
            }
        } else {
            reset()
        }
    }

    private fun applySpeedEffect() {
        if (currentLevel <= 0) {
            player.removePotionEffect(PotionEffectType.SPEED)
            return
        }

        val amplifier = (currentLevel - 1).coerceAtLeast(0)
        player.addPotionEffect(
            PotionEffect(PotionEffectType.SPEED, 15, amplifier, false, false, false)
        )
    }

    private fun reset() {
        currentLevel = 0
        progressTicks = 0
        player.exp = 0f
        player.removePotionEffect(PotionEffectType.SPEED)
    }

    override fun teardown() {
        reset()
        super.teardown()
    }
}
