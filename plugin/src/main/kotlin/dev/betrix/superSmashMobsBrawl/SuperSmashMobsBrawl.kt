package dev.betrix.superSmashMobsBrawl

import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import dev.betrix.superSmashMobsBrawl.commands.QueueCommand
import dev.betrix.superSmashMobsBrawl.commands.argumentResolvers.QueueArgument
import dev.betrix.superSmashMobsBrawl.components.BelowNameDisplay
import dev.betrix.superSmashMobsBrawl.components.MinecraftPlayer
import dev.betrix.superSmashMobsBrawl.enums.Queue
import dev.betrix.superSmashMobsBrawl.systems.QueueSystem
import dev.rollczi.litecommands.LiteCommands
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory
import gg.flyte.twilight.Twilight
import gg.flyte.twilight.event.event
import gg.flyte.twilight.twilight
import net.kyori.adventure.text.Component
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin

class SuperSmashMobsBrawl : JavaPlugin() {

    lateinit var liteCommands: LiteCommands<CommandSender>
    lateinit var twilight: Twilight
    lateinit var world: World
    lateinit var playerFamily: Family

    override fun onEnable() {
        twilight = twilight(this)
        world = configureWorld {
            injectables { add(this) }
            systems { add(QueueSystem()) }
        }
        playerFamily = world.family { all(MinecraftPlayer) }
        liteCommands =
            LiteBukkitFactory.builder(this)
                .argument(Queue::class.java, QueueArgument())
                .commands(QueueCommand(this))
                .build()

        event<PlayerJoinEvent> {
            logger.info("Player joined: ${player.name}")
            world.entity {
                it += MinecraftPlayer(player)
                it += BelowNameDisplay(Component.text("Really cool person"))
            }
        }

        event<PlayerDropItemEvent> {
            if (player.gameMode == GameMode.CREATIVE) {
                return@event
            }

            isCancelled = true
        }

        event<EntityPickupItemEvent> { isCancelled = true }

        event<InventoryMoveItemEvent> { isCancelled = true }

        logger.info("SSMB started!")
    }

    override fun onDisable() {
        logger.info("SSMB shutting down")
    }
}
