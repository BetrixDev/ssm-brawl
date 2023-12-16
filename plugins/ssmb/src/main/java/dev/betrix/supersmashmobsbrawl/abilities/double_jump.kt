package dev.betrix.supersmashmobsbrawl.abilities

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import dev.betrix.supersmashmobsbrawl.SSMBPlayer
import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.betrix.supersmashmobsbrawl.enums.TaggedKeyBool
import dev.betrix.supersmashmobsbrawl.extensions.getMetadata
import dev.betrix.supersmashmobsbrawl.extensions.setMetadata
import dev.betrix.supersmashmobsbrawl.utils.isOnGround
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import org.bukkit.Sound

fun tryDoubleJump(player: SSMBPlayer) {
    val plugin = SuperSmashMobsBrawl.instance
    val bukkitPlayer = player.bukkitPlayer

    if (bukkitPlayer.getMetadata(TaggedKeyBool.PLAYER_CAN_DOUBLE_JUMP) == false) {
        return
    }

    bukkitPlayer.setMetadata {
        set(TaggedKeyBool.PLAYER_CAN_DOUBLE_JUMP, false)
    }

    bukkitPlayer.isFlying = true
    bukkitPlayer.allowFlight = false
    bukkitPlayer.fallDistance = 0F

    bukkitPlayer.playSound(bukkitPlayer.location, Sound.ENTITY_BLAZE_SHOOT, 1F, 1F)

    var vec = bukkitPlayer.location.direction
    vec.y = 0.6
    vec = vec.normalize()
    vec = vec.multiply(0.9)
    if (vec.y > 0.6) {
        vec.y = 0.6
    }
    if (bukkitPlayer.isOnGround) {
        vec.y += 0.1
    }
    bukkitPlayer.fallDistance = 0F
    bukkitPlayer.velocity = vec.multiply(1.5)

    plugin.launch {
        while (true) {
            if (isOnGround(bukkitPlayer)) {
                bukkitPlayer.setMetadata {
                    set(TaggedKeyBool.PLAYER_CAN_DOUBLE_JUMP, true)
                }
                bukkitPlayer.allowFlight = true
                this.cancel()
            }
            
            delay(1.ticks)
        }
    }
}