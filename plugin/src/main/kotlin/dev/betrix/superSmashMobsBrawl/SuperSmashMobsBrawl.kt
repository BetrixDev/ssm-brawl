package dev.betrix.superSmashMobsBrawl

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import dev.betrix.superSmashMobsBrawl.commands.DebugCommand
import dev.betrix.superSmashMobsBrawl.commands.KitCommand
import dev.betrix.superSmashMobsBrawl.commands.LeaveCommand
import dev.betrix.superSmashMobsBrawl.commands.QueueCommand
import dev.betrix.superSmashMobsBrawl.commands.argumentResolvers.MinigameDefinitionArgument
import dev.betrix.superSmashMobsBrawl.extensions.hasPassive
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.passives.definitions.HungerPassiveDefinition
import dev.betrix.superSmashMobsBrawl.services.DataService
import dev.betrix.superSmashMobsBrawl.services.DebugService
import dev.betrix.superSmashMobsBrawl.services.HotbarService
import dev.betrix.superSmashMobsBrawl.services.HubProtectionService
import dev.betrix.superSmashMobsBrawl.services.HubService
import dev.betrix.superSmashMobsBrawl.services.RegistryService
import dev.betrix.superSmashMobsBrawl.services.WorldService
import dev.betrix.superSmashMobsBrawl.utils.mm
import dev.rollczi.litecommands.LiteCommands
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory
import gg.flyte.twilight.Twilight
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.feed
import gg.flyte.twilight.twilight
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.PotionSplashEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import dev.betrix.superSmashMobsBrawl.di.appModule

class SuperSmashMobsBrawl : SuspendingJavaPlugin() {
    lateinit var liteCommands: LiteCommands<CommandSender>
    lateinit var twilight: Twilight

    companion object {
        lateinit var instance: SuperSmashMobsBrawl
    }

    override suspend fun onEnableAsync() {
        instance = this
        twilight = twilight(this)

        // Initialize Koin
        startKoin {
            modules(appModule)
        }

        // Initialize services
        DataService.readData()
        RegistryService.initializeRegistries()
        HotbarService.initialize(this)
        HubService.initialize(this)
        HubProtectionService.registerEvents()
        DebugService.initialize(this)

        server.motd(
            mm(
                "<gradient:#ff6b6b:#4ecdc4>⚔ <bold>SUPER SMASH MOBS BRAWL</bold> ⚔</gradient><newline><gradient:#ffd93d:#6bcf7f>\uD83C\uDFAE Choose Your Mob • Smash Enemies • Dominate! \uD83C\uDFC6</gradient>"
            )
        )

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

        event<FoodLevelChangeEvent> {
            if (entity is Player) {
                val player = entity as Player
                if (!player.hasPassive(HungerPassiveDefinition)) {
                    player.feed()
                    isCancelled = true
                }
            }
        }

        event<EntityPickupItemEvent> { isCancelled = true }

        event<InventoryMoveItemEvent> { isCancelled = true }

        event<PotionSplashEvent> {
            entity.remove()
            isCancelled = true
        }

        event<InventoryInteractEvent> { isCancelled = true }

        logger.info("SSMB started!")
    }

    override suspend fun onDisableAsync() {
        HubService.teardown()
        WorldService.teardown()
        DebugService.teardown()
        stopKoin()
        logger.info("SSMB shutting down")
    }
}
