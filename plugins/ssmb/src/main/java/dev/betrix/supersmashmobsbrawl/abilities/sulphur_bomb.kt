package dev.betrix.supersmashmobsbrawl.abilities

import br.com.devsrsouza.kotlinbukkitapi.extensions.item
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import dev.betrix.supersmashmobsbrawl.SSMBPlayer
import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyNum
import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyStr
import dev.betrix.supersmashmobsbrawl.extensions.getMetadata
import dev.betrix.supersmashmobsbrawl.extensions.isDouble
import dev.betrix.supersmashmobsbrawl.extensions.setMetadata
import dev.betrix.supersmashmobsbrawl.managers.api.payloads.StartGameResponse
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.entity.ThrownPotion
import org.bukkit.event.entity.PotionSplashEvent
import kotlin.collections.set

fun tryUseSulphurBomb(player: SSMBPlayer, abilityData: StartGameResponse.AbilitiesData) {
    val plugin = SuperSmashMobsBrawl.instance
    val lastTimeUsed = player.cooldowns[abilityData.id]
    val currentTime = System.currentTimeMillis()

    if (lastTimeUsed != null && currentTime < lastTimeUsed + abilityData.cooldown * 1000) {
        val timeLeft = ((lastTimeUsed + abilityData.cooldown * 1000) - currentTime).toDouble() / 1000.0

        player.bukkitPlayer.sendMessage(
            MiniMessage.miniMessage()
                .deserialize("<blue>Recharge></blue> <gray>You cannot use <green>Sulphur Bomb</green> for <green>$timeLeft seconds</green></gray>")
        )

        return
    }

    val location = player.bukkitPlayer.eyeLocation
    val direction = location.direction

    val projectile = player.bukkitPlayer.world.spawn(location, ThrownPotion::class.java)
    projectile.velocity = direction.multiply(1.55)
    projectile.shooter = player.bukkitPlayer

    projectile.setMetadata {
        abilityData.meta.forEach {
            if (it.value.isDouble()) {
                set(TaggedKeyNum.fromId(it.key)!!, it.value.toDouble())
            } else {
                set(TaggedKeyStr.fromId(it.key)!!, it.value)
            }
        }
    }

    projectile.item = item(Material.COAL)

    plugin.launch {
        val projectileHitboxSize = projectile.getMetadata(TaggedKeyNum.PROJECTILE_HITBOX_SIZE)!!
        val projectileDamage = projectile.getMetadata(TaggedKeyNum.PROJECTILE_DAMAGE)!!

        while (true) {
            val nearbyEntities =
                projectile.getNearbyEntities(projectileHitboxSize, projectileHitboxSize, projectileHitboxSize)

            nearbyEntities.forEach {
                if (it !is Player || it == player.bukkitPlayer) {
                    return@forEach
                }

                val splashEvent = PotionSplashEvent(projectile, it, null, null, mutableMapOf(it to 1.0))
                splashEvent.callEvent()
                this.cancel()
            }

            delay(1.ticks)
        }
    }

    player.cooldowns[abilityData.id] = currentTime
}