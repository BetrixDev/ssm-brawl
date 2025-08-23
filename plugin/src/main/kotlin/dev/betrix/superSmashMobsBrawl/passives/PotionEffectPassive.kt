package dev.betrix.superSmashMobsBrawl.passives

import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect

class PotionEffectPassive(player: Player) : BrawlPassive("potion_effect", player) {

    private val effectName = metadata.string("effect")
    private val effectLevel =
        metadata.int("level")
            ?: run {
                    plugin.logger.fine("metadata.level not set for $id passive, defaulting to 0")
                    0
                }
                .coerceAtLeast(0)

    private val isAmbient = metadata.boolean("isAmbient") ?: false
    private val showParticles = metadata.boolean("showParticles") ?: false
    private val showIcon = metadata.boolean("showIcon") ?: true

    private val resolvedPotionEffect =
        when (effectName) {
            null -> null
            else ->
                Registry.POTION_EFFECT_TYPE.get(NamespacedKey.minecraft(effectName.lowercase()))
                    ?: Registry.EFFECT.get(Key.key(effectName))
        }

    override fun setup() {
        if (resolvedPotionEffect == null) {
            plugin.logger.severe(
                "Unable to resolve potion effect $effectName for passive $id on kit ${kitData?.id ?: "unknown"}"
            )
            return
        }

        player.addPotionEffect(
            PotionEffect(
                resolvedPotionEffect,
                Int.MAX_VALUE,
                effectLevel,
                isAmbient,
                showParticles,
                showIcon,
            )
        )

        super.setup()
    }

    override fun teardown() {
        resolvedPotionEffect?.let { player.removePotionEffect(it) }
        super.teardown()
    }
}
