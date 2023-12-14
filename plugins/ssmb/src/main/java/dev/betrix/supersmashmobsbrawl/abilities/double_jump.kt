package dev.betrix.supersmashmobsbrawl.abilities

import dev.betrix.supersmashmobsbrawl.SSMBPlayer
import org.bukkit.Sound

fun tryDoubleJump(player: SSMBPlayer) {
    val bukkitPlayer = player.bukkitPlayer

    bukkitPlayer.isFlying = true
    bukkitPlayer.allowFlight = false
    bukkitPlayer.fallDistance = 0F

    bukkitPlayer.playSound(bukkitPlayer.location, Sound.ENTITY_BLAZE_SHOOT, 1F, 1F)

    var vec = bukkitPlayer.location.direction
    vec.y = 0.9
    vec = vec.normalize()
    vec = vec.multiply(0.9)
    if (vec.y > 0.9) {
        vec.y = 0.9
    }
    if (bukkitPlayer.isOnGround) {
        vec.y += 0.2
    }
    bukkitPlayer.fallDistance = 0F
    bukkitPlayer.velocity = vec.multiply(1.5)
    bukkitPlayer.allowFlight = true
}