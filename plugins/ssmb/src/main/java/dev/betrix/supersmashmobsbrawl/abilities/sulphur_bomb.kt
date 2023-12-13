package dev.betrix.supersmashmobsbrawl.abilities

import br.com.devsrsouza.kotlinbukkitapi.extensions.item
import dev.betrix.supersmashmobsbrawl.SSMBPlayer
import dev.betrix.supersmashmobsbrawl.enums.SSMBAbility
import dev.betrix.supersmashmobsbrawl.enums.TaggedKey
import dev.betrix.supersmashmobsbrawl.extensions.setDouble
import dev.betrix.supersmashmobsbrawl.extensions.setString
import dev.betrix.supersmashmobsbrawl.extensions.setValues
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
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

    val location = player.bukkitPlayer.eyeLocation
    val direction = location.direction

    val projectile = player.bukkitPlayer.world.spawn(location, ThrownPotion::class.java)
    projectile.velocity = direction.multiply(1.55)
    projectile.shooter = player.bukkitPlayer
    projectile.persistentDataContainer.setValues {
        setDouble(TaggedKey.PROJECTILE_DAMAGE, 6.5)
        setDouble(TaggedKey.PROJECTILE_DAMAGE_RANGE, 3.0)
        setString(TaggedKey.PROJECTILE_THROWER_UUID, player.bukkitPlayer.uniqueId.toString())
        setString(TaggedKey.PROJECTILE_EXPLOSION_PARTICLE, Particle.EXPLOSION_LARGE.toString())
        setString(TaggedKey.PROJECT_EXPLOSION_SOUND, Sound.ENTITY_GENERIC_EXPLODE.toString())
    }

    projectile.item = item(Material.COAL)

    player.cooldowns[ability.id] = currentTime
}