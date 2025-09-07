package dev.betrix.superSmashMobsBrawl

import com.github.quillraven.fleks.World
import com.github.quillraven.fleks.configureWorld
import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import dev.betrix.superSmashMobsBrawl.commands.DebugCommand
import dev.betrix.superSmashMobsBrawl.commands.KitCommand
import dev.betrix.superSmashMobsBrawl.commands.LeaveCommand
import dev.betrix.superSmashMobsBrawl.commands.QueueCommand
import dev.betrix.superSmashMobsBrawl.commands.resolvers.KitDefArgument
import dev.betrix.superSmashMobsBrawl.commands.resolvers.LeaveSpecifierArgument
import dev.betrix.superSmashMobsBrawl.commands.resolvers.MinigameDefinitionArgument
import dev.betrix.superSmashMobsBrawl.components.LeaveSpecifier
import dev.betrix.superSmashMobsBrawl.components.PlayerComponent
import dev.betrix.superSmashMobsBrawl.components.ScoreboardComponent
import dev.betrix.superSmashMobsBrawl.extensions.ecsEntity
import dev.betrix.superSmashMobsBrawl.extensions.hasPassive
import dev.betrix.superSmashMobsBrawl.models.brawlData.KitDef
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.services.DataService
import dev.betrix.superSmashMobsBrawl.services.DebugService
import dev.betrix.superSmashMobsBrawl.services.HubProtectionService
import dev.betrix.superSmashMobsBrawl.services.HubService
import dev.betrix.superSmashMobsBrawl.services.KitService
import dev.betrix.superSmashMobsBrawl.services.LangService
import dev.betrix.superSmashMobsBrawl.services.MinigameService
import dev.betrix.superSmashMobsBrawl.services.PlayerEcsEntityService
import dev.betrix.superSmashMobsBrawl.services.WorldService
import dev.betrix.superSmashMobsBrawl.systems.MinigameWorldLoaderSystem
import dev.betrix.superSmashMobsBrawl.systems.PlayerLeaveSystem
import dev.betrix.superSmashMobsBrawl.systems.PlayerSystem
import dev.betrix.superSmashMobsBrawl.systems.QueueSystem
import dev.betrix.superSmashMobsBrawl.systems.scoreboards.HubScoreboardSystem
import dev.betrix.superSmashMobsBrawl.systems.scoreboards.MinigameScoreboardSystem
import dev.betrix.superSmashMobsBrawl.systems.scoreboards.QueueScoreboardSystem
import dev.betrix.superSmashMobsBrawl.systems.scoreboards.ScoreboardSystem
import dev.rollczi.litecommands.LiteCommands
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory
import gg.flyte.twilight.Twilight
import gg.flyte.twilight.event.event
import gg.flyte.twilight.extension.feed
import gg.flyte.twilight.scheduler.repeatingTask
import gg.flyte.twilight.scoreboard.TwilightScoreboard
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
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
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

    lateinit var ecsWorld: World

    override suspend fun onEnableAsync() {
        twilight = twilight(this)

        Logger.getLogger("").addHandler(AxiomLoggerHandler(this))

        ecsWorld = configureWorld {
            injectables {
                add(LangService(this@SuperSmashMobsBrawl))
                add(this@SuperSmashMobsBrawl)
                add<JavaPlugin>(this@SuperSmashMobsBrawl)
            }
            systems {
                add(HubScoreboardSystem())
                add(MinigameScoreboardSystem())
                add(ScoreboardSystem())
                add(PlayerSystem())
                add(QueueSystem())
                add(QueueScoreboardSystem())
                add(MinigameWorldLoaderSystem())
                add(PlayerLeaveSystem())
            }
        }

        logger.info("Total systems ${ecsWorld.systems.size}")

        var lastTick = System.currentTimeMillis()

        repeatingTask(1) {
            val currentTime = System.currentTimeMillis()
            ecsWorld.update((currentTime - lastTick) / 1000f)
            lastTick = currentTime
        }

        startKoin {
            modules(
                module {
                    single { this@SuperSmashMobsBrawl }
                    single<JavaPlugin> { this@SuperSmashMobsBrawl }
                    single { this@SuperSmashMobsBrawl.logger }
                    single(createdAtStart = true) { DataService() }
                    single { MinigameService() }
                    single { KitService }
                    single { ecsWorld }
                    single(createdAtStart = true) { LangService(this@SuperSmashMobsBrawl) }
                    single { WorldService }
                    single { HubService }
                }
            )
        }

        StatisticsBroadcaster()

        // Initialize services
        HubService.initialize(this)
        HubProtectionService.registerEvents()
        DebugService.initialize(this)

        server.motd(lang.t("motd"))

        liteCommands =
            LiteBukkitFactory.builder(this)
                .argument(MinigameDef::class.java, MinigameDefinitionArgument())
                .argument(KitDef::class.java, KitDefArgument())
                .argument(LeaveSpecifier::class.java, LeaveSpecifierArgument())
                .commands(QueueCommand(this))
                .commands(KitCommand())
                .commands(LeaveCommand(this))
                .commands(DebugCommand())
                .build()

        event<PlayerJoinEvent> {
            println("creating entity for ${player.name}")
            val entity =
                ecsWorld.entity {
                    it += PlayerComponent(player)
                    it += ScoreboardComponent(TwilightScoreboard(player))
                }
            PlayerEcsEntityService.onCreateEntityForPlayer(player, entity)
        }

        event<PlayerQuitEvent> {
            with(ecsWorld) { player.ecsEntity?.remove() }
            PlayerEcsEntityService.onRemoveEntityForPlayer(player)
        }

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
        HubService.teardown()
        WorldService.teardown()
        DebugService.teardown()
        stopKoin()
        logger.info("SSMB shutting down")
    }
}
