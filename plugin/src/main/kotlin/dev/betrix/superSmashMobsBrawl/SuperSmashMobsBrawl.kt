package dev.betrix.superSmashMobsBrawl

import dev.betrix.superSmashMobsBrawl.commands.KitCommand
import dev.betrix.superSmashMobsBrawl.commands.QueueCommand
import dev.betrix.superSmashMobsBrawl.commands.HubCommand
import dev.betrix.superSmashMobsBrawl.commands.HubTestCommand
import dev.betrix.superSmashMobsBrawl.commands.argumentResolvers.MinigameDefinitionArgument
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.services.HotbarService
import dev.betrix.superSmashMobsBrawl.services.HubService
import dev.betrix.superSmashMobsBrawl.services.HubProtectionService
import dev.rollczi.litecommands.LiteCommands
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory
import gg.flyte.twilight.Twilight
import gg.flyte.twilight.event.event
import gg.flyte.twilight.twilight
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.plugin.java.JavaPlugin

class SuperSmashMobsBrawl : JavaPlugin() {
    lateinit var liteCommands: LiteCommands<CommandSender>
    lateinit var twilight: Twilight

    companion object {
        lateinit var instance: SuperSmashMobsBrawl
    }

    override fun onEnable() {
        instance = this
        twilight = twilight(this)
        
        // Initialize services
        HotbarService.initialize(this)
        HubService.initialize(this)
        HubProtectionService.registerEvents()
        
        liteCommands =
            LiteBukkitFactory.builder(this)
                .argument(MinigameDefinition::class.java, MinigameDefinitionArgument())
                .commands(QueueCommand())
                .commands(KitCommand())
                .commands(HubCommand())
                .commands(HubTestCommand())
                .build()

        // Player join events are now handled by HubService

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
        HubService.cleanup()
        logger.info("SSMB shutting down")
    }
}
