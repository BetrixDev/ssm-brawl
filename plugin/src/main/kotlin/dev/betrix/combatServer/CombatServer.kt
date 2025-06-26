package dev.betrix.combatServer

import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import dev.betrix.combatServer.components.BelowNameDisplay
import dev.betrix.combatServer.components.MinecraftPlayer
import gg.flyte.twilight.event.event
import net.kyori.adventure.text.Component
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin

class CombatServer : JavaPlugin() {

    lateinit var world: World

    override fun onEnable() {
        world = configureWorld {
            injectables {
                add(this)
            }
        }

        event<PlayerJoinEvent> {
            world.entity {
                it += MinecraftPlayer(player)
                it += BelowNameDisplay(Component.text("Really cool person"))
            }
        }

        logger.info("Combat Server started!")
    }

    override fun onDisable() {
        logger.info("Combat Server shutting down")
    }
}
