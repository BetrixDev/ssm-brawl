package dev.betrix.supersmashmobsbrawl.abilities

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import dev.betrix.supersmashmobsbrawl.SSMBPlayer
import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyBool
import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyNum
import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyStr
import dev.betrix.supersmashmobsbrawl.extensions.*
import dev.betrix.supersmashmobsbrawl.managers.api.payloads.StartGameResponse
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Particle
import org.bukkit.Sound

fun tryUseExplode(player: SSMBPlayer, abilityData: StartGameResponse.AbilitiesData) {
    val plugin = SuperSmashMobsBrawl.instance
    val lastTimeUsed = player.cooldowns[abilityData.id]
    val currentTime = System.currentTimeMillis()

    if (lastTimeUsed != null && currentTime < lastTimeUsed + abilityData.cooldown * 1000) {
        val timeLeft = ((lastTimeUsed + abilityData.cooldown * 1000) - currentTime).toDouble() / 1000.0

        player.bukkitPlayer.sendMessage(
            MiniMessage.miniMessage()
                .deserialize("<blue>Recharge></blue> <gray>You cannot use <green>${abilityData.displayName}</green> for <green>$timeLeft seconds</green></gray>")
        )

        return
    }

    val bukkitPlayer = player.bukkitPlayer

    bukkitPlayer.setMetadata {
        set(TaggedKeyBool.PLAYER_IS_EXPLODE_ACTIVE, true)
    }

    plugin.launch {
        bukkitPlayer.walkSpeed = 0.05F
        bukkitPlayer.level = 0
        bukkitPlayer.exp = 0F

        repeat(30) { index ->
            if (bukkitPlayer.getMetadata(TaggedKeyBool.PLAYER_IS_EXPLODE_ACTIVE)!!) {
                bukkitPlayer.exp = (index + 1) / 30F

                val volume = 0.5F + index / 20
                bukkitPlayer.world.playSound(bukkitPlayer.location, Sound.ENTITY_CREEPER_PRIMED, volume, volume)

                delay(1.ticks)
            } else {
                this.cancel()
            }
        }

        bukkitPlayer.walkSpeed = 0.2F
        bukkitPlayer.level = 0
        bukkitPlayer.exp = 0F

        if (bukkitPlayer.getMetadata(TaggedKeyBool.PLAYER_IS_EXPLODE_ACTIVE)!!) {
            val explosionSoundId = abilityData.meta[TaggedKeyStr.ABILITY_ACTIVATION_SOUND.id]!!
            val particleId = abilityData.meta[TaggedKeyStr.ABILITY_ACTIVATION_PARTICLE.id]!!
            val particleAmount = abilityData.meta[TaggedKeyNum.ABILITY_ACTIVATION_PARTICLE_AMOUNT.id]!!.toInt()
            val abilityMaxRange = abilityData.meta[TaggedKeyNum.ABILITY_DAMAGE_MAX_RANGE.id]!!.toDouble()
            val abilityBaseDamage = abilityData.meta[TaggedKeyNum.ABILITY_BASE_DAMAGE.id]!!.toDouble()

            bukkitPlayer.world.playSound(bukkitPlayer.location, Sound.valueOf(explosionSoundId.uppercase()), 1F, 1F)
            bukkitPlayer.world.spawnParticle(
                Particle.valueOf(particleId.uppercase()),
                bukkitPlayer.location,
                particleAmount
            )

            bukkitPlayer.setMetadata {
                set(TaggedKeyBool.PLAYER_IS_EXPLODE_ACTIVE, false)
            }

            bukkitPlayer.setVelocity(1.8, 0.2, 1.4, true)

            bukkitPlayer.getLivingEntitiesInRadius(abilityMaxRange).forEach {
                if (it == bukkitPlayer || it.getMetadata(TaggedKeyBool.PLAYER_CAN_BE_DAMAGED) == true) {
                    return@forEach
                }

                val distance = bukkitPlayer.location.distance(it.location)
                val damage = (0.1 + 0.9 * ((abilityMaxRange - distance) / abilityMaxRange)) * 0.75

                it.doKnockback(2.5, damage, it.health, bukkitPlayer.location.toVector(), null)
                it.damage(damage, bukkitPlayer)
            }

            player.cooldowns[abilityData.id] = currentTime
        }
    }
}