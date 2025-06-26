package dev.betrix.superSmashMobsBrawl

import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import dev.betrix.superSmashMobsBrawl.components.BelowNameDisplay
import dev.betrix.superSmashMobsBrawl.components.MinecraftPlayer
import gg.flyte.twilight.event.event
import net.kyori.adventure.text.Component
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin

class SuperSmashMobsBrawl : JavaPlugin() {

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

        logger.info("SSMB started!")
    }

    override fun onDisable() {
        logger.info("SSMB shutting down")
    }
}
