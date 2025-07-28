package dev.betrix.superSmashMobsBrawl

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import dev.betrix.superSmashMobsBrawl.commands.DebugCommand
import dev.betrix.superSmashMobsBrawl.commands.KitCommand
import dev.betrix.superSmashMobsBrawl.commands.LeaveCommand
import dev.betrix.superSmashMobsBrawl.commands.QueueCommand
import dev.betrix.superSmashMobsBrawl.commands.argumentResolvers.MinigameDefinitionArgument
import dev.betrix.superSmashMobsBrawl.di.appModule
import dev.betrix.superSmashMobsBrawl.lifecycle.ServiceRegistry
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.services.DebugService
import dev.betrix.superSmashMobsBrawl.services.HotbarService
import dev.betrix.superSmashMobsBrawl.services.HubProtectionService
import dev.betrix.superSmashMobsBrawl.services.HubService
import dev.betrix.superSmashMobsBrawl.services.KitService
import dev.betrix.superSmashMobsBrawl.services.WorldService
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
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class SuperSmashMobsBrawl : SuspendingJavaPlugin() {
    lateinit var liteCommands: LiteCommands<CommandSender>
    lateinit var twilight: Twilight

    companion object {
        lateinit var instance: SuperSmashMobsBrawl
    }

    override suspend fun onEnableAsync() {
        instance = this
        twilight = twilight(this)

        // Initialize Koin dependency injection
        startKoin {
            modules(appModule)
        }

        // Initialize services
        HotbarService.initialize(this)
        HubService.initialize(this)
        HubProtectionService.registerEvents()
        DebugService.initialize(this)

        // Register manageable services for automatic teardown
        ServiceRegistry.register(HubService)
        ServiceRegistry.register(DebugService)
        ServiceRegistry.register(KitService)
        ServiceRegistry.register(WorldService)

        liteCommands =
            LiteBukkitFactory.builder(this)
                .argument(MinigameDefinition::class.java, MinigameDefinitionArgument())
                .commands(QueueCommand())
                .commands(KitCommand())
                .commands(LeaveCommand())
                .commands(DebugCommand())
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

    override suspend fun onDisableAsync() {
        // Tear down all registered manageable services
        ServiceRegistry.teardownAllAsync()
        ServiceRegistry.teardownAll()
        
        // Stop Koin
        stopKoin()
        
        logger.info("SSMB shutting down")
    }
}
