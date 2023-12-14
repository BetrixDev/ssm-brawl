package dev.betrix.supersmashmobsbrawl.abilities

import br.com.devsrsouza.kotlinbukkitapi.extensions.item
import dev.betrix.supersmashmobsbrawl.SSMBPlayer
import dev.betrix.supersmashmobsbrawl.enums.SSMBAbility
import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyNum
import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyStr
import dev.betrix.supersmashmobsbrawl.extensions.setMetadata
import dev.betrix.supersmashmobsbrawl.managers.api.payloads.ValueNumOrStr
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.entity.ThrownPotion
import kotlin.collections.set

fun tryUseSulphurBomb(player: SSMBPlayer) {
    val ability = SSMBAbility.SULPHUR_BOMB

    val lastTimeUsed = player.cooldowns[ability.id]
    val currentTime = System.currentTimeMillis()

    if (lastTimeUsed != null && currentTime < lastTimeUsed + ability.cooldown * 1000) {
        val timeLeft = ((lastTimeUsed + ability.cooldown * 1000) - currentTime).toDouble() / 1000.0

        player.bukkitPlayer.sendMessage(
            MiniMessage.miniMessage()
                .deserialize("<blue>Recharge></blue> <gray>You cannot use <green>Sulphur Bomb</green> for <green>$timeLeft seconds</green></gray>")
        )

        return
    }

    val abilityMetaData = player.selectedKitData?.abilities?.find {
        it.id === "sulphur_bomb"
    }?.meta ?: return

    val location = player.bukkitPlayer.eyeLocation
    val direction = location.direction

    val projectile = player.bukkitPlayer.world.spawn(location, ThrownPotion::class.java)
    projectile.velocity = direction.multiply(1.55)
    projectile.shooter = player.bukkitPlayer

    projectile.setMetadata {
        abilityMetaData.map.forEach {
            when (it.value) {
                is ValueNumOrStr.StringValue -> set(
                    TaggedKeyStr.fromId(it.key)!!,
                    (it.value as ValueNumOrStr.StringValue).value
                )

                is ValueNumOrStr.DoubleValue -> set(
                    TaggedKeyNum.fromId(it.key)!!,
                    (it.value as ValueNumOrStr.DoubleValue).value
                )
            }
        }
    }

    projectile.item = item(Material.COAL)

    player.cooldowns[ability.id] = currentTime
}