package net.ssmb.components.entities.passives

import br.com.devsrsouza.kotlinbukkitapi.extensions.event
import br.com.devsrsouza.kotlinbukkitapi.extensions.events
import net.ssmb.SSMB
import net.ssmb.blockwork.annotations.Component
import net.ssmb.blockwork.components.EntityComponent
import net.ssmb.blockwork.interfaces.OnDestroy
import net.ssmb.blockwork.interfaces.OnStart
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerToggleFlightEvent

@Component("passive_double_jump")
class DoubleJumpPassive(private val plugin: SSMB): EntityComponent<Player>(), OnStart, OnDestroy, Listener {

    private var canDoubleJump = true

    override fun onStart() {
        entity.allowFlight = true

        plugin.events {
            event<PlayerToggleFlightEvent> {
                if (player != entity || player.gameMode == GameMode.CREATIVE) {
                    return@event
                }

                isCancelled = true
                player.isFlying = false
                player.allowFlight = false
                player.fallDistance = 0f
            }
        }

        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        TODO("Not yet implemented")
    }
}