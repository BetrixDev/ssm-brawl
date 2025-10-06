package dev.betrix.superSmashMobsBrawl

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import dev.betrix.superSmashMobsBrawl.commands.*
import dev.betrix.superSmashMobsBrawl.commands.resolvers.*
import dev.betrix.superSmashMobsBrawl.extensions.hasPassive
import dev.betrix.superSmashMobsBrawl.models.brawlData.KitDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.services.*
import dev.rollczi.litecommands.LiteCommands
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory
import gg.flyte.twilight.Twilight
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.feed
import gg.flyte.twilight.twilight
import java.util.logging.Logger
import org.bukkit.GameMode
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.PotionSplashEvent
import org.bukkit.event.inventory.InventoryInteractEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class SuperSmashMobsBrawl : SuspendingJavaPlugin(), KoinComponent {
    private val lang: LangService by inject()
    lateinit var liteCommands: LiteCommands<CommandSender>
    lateinit var twilight: Twilight
    lateinit var axiomLoggerHandler: AxiomLoggerHandler

    override suspend fun onEnableAsync() {
        twilight = twilight(this)

        axiomLoggerHandler = AxiomLoggerHandler(this)
        Logger.getLogger("").addHandler(axiomLoggerHandler)

        startKoin {
            modules(
                module {
                    single { this@SuperSmashMobsBrawl }
                    single<JavaPlugin> { this@SuperSmashMobsBrawl }
                    single { this@SuperSmashMobsBrawl.logger }
                    single { this@SuperSmashMobsBrawl.axiomLoggerHandler }
                    single { ApiService }
                    single { PlayerDocumentService }
                    single(createdAtStart = true) { DataService() }
                    single { MinigameService() }
                    single { KitService }
                    single(createdAtStart = true) { LangService(this@SuperSmashMobsBrawl) }
                    single { WorldService }
                    single { HubService }
                }
            )
        }

        StatisticsBroadcaster()

        HubService.initialize(this)
        HubProtectionService.registerEvents()
        DebugService.initialize(this)
        PlayerDocumentService.setup()

        server.motd(lang.t("motd"))

        liteCommands =
            LiteBukkitFactory.builder(this)
                .argument(MinigameDef::class.java, MinigameDefinitionArgument())
                .argument(KitDef::class.java, KitDefArgument())
                .commands(QueueCommand())
                .commands(KitCommand())
                .commands(LeaveCommand())
                .commands(DebugCommand())
                .build()

        event<PlayerDropItemEvent> {
            if (player.gameMode == GameMode.CREATIVE) {
                return@event
            }

            isCancelled = true
        }

        event<FoodLevelChangeEvent> {
            if (entity is Player) {
                val player = entity as Player
                if (!player.hasPassive("hunger")) {
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
        logger.info("SSMB shutting down")

        HubService.teardown()
        WorldService.teardown()
        DebugService.teardown()
        KitService.teardown()
        PlayerDocumentService.teardown()

        stopKoin()
    }
}
