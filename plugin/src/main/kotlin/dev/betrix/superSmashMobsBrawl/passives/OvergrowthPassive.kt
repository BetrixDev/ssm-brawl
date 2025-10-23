package dev.betrix.superSmashMobsBrawl.passives

import dev.betrix.superSmashMobsBrawl.events.BrawlDamageEvent
import dev.betrix.superSmashMobsBrawl.events.Damager
import gg.flyte.twilight.event.event
import kotlin.math.ceil
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.koin.core.component.KoinComponent

class OvergrowthPassive(player: Player) : BrawlPassive("overgrowth", player), KoinComponent {

    private val absorptionRatio = metadata.double("absorptionRatio") ?: 0.25
    private val maxAbsorptionHearts = metadata.double("maxAbsorptionHearts") ?: 4.0
    private val absorptionDurationTicks = metadata.int("absorptionDurationTicks") ?: 200

    override fun setup() {
        val damageListener =
            event<BrawlDamageEvent> {
                val isThisPlayerDamager =
                    when (damager) {
                        is Damager.DamagerLivingEntity ->
                            damager.livingEntity == this@OvergrowthPassive.player
                        is Damager.System -> false
                        null -> false
                    }

                if (!isThisPlayerDamager) return@event

                grantAbsorption(damage)
            }
        listeners.add(damageListener)

        super.setup()
    }

    private fun grantAbsorption(damageDealt: Double) {
        val absorptionToGrant = damageDealt * absorptionRatio

        val currentAbsorption = getCurrentAbsorption()
        val newAbsorption = (currentAbsorption + absorptionToGrant).coerceAtMost(maxAbsorptionHearts)

        val amplifier = calculateAmplifier(newAbsorption)

        player.addPotionEffect(
            PotionEffect(
                PotionEffectType.ABSORPTION,
                absorptionDurationTicks,
                amplifier,
                false,
                true,
                true,
            )
        )
    }

    private fun getCurrentAbsorption(): Double {
        val absorptionEffect = player.getPotionEffect(PotionEffectType.ABSORPTION)
        if (absorptionEffect == null || !absorptionEffect.hasParticles()) {
            return 0.0
        }

        val amplifier = absorptionEffect.amplifier
        return (amplifier + 1) * 2.0
    }

    private fun calculateAmplifier(absorptionHearts: Double): Int {
        val heartsRounded = ceil(absorptionHearts).toInt()
        val amplifier = (heartsRounded / 2) - 1
        return amplifier.coerceAtLeast(0)
    }
}

