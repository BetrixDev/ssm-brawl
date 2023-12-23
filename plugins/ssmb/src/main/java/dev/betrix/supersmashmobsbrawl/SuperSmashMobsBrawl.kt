package dev.betrix.supersmashmobsbrawl

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.onarandombox.MultiverseCore.MultiverseCore
import dev.betrix.supersmashmobsbrawl.commands.QueueCommand
import dev.betrix.supersmashmobsbrawl.commands.SchematicCommand
import dev.betrix.supersmashmobsbrawl.listeners.*
import dev.betrix.supersmashmobsbrawl.managers.*
import dev.betrix.supersmashmobsbrawl.maps.HubMap
import dev.betrix.supersmashmobsbrawl.maps.SSMBMap
import dev.rollczi.litecommands.LiteCommands
import dev.rollczi.litecommands.bukkit.LiteCommandsBukkit
import org.popcraft.chunky.api.ChunkyAPI

class SuperSmashMobsBrawl : SuspendingJavaPlugin() {
    lateinit var api: ApiManager
    lateinit var queue: QueueManager
    lateinit var games: GameManager
    lateinit var cache: HttpCacheManager
    lateinit var lang: LangManager
    lateinit var editorManager: SchematicEditorManager
    lateinit var mvc: MultiverseCore
    lateinit var chunky: ChunkyAPI
    private lateinit var commands: LiteCommands<*>

    lateinit var hub: HubMap

    companion object {
        lateinit var instance: SuperSmashMobsBrawl
    }

    override suspend fun onEnableAsync() {
        logger.info("Initializing Super Smash Mobs Brawl!")

        instance = this

        api = ApiManager()
        queue = QueueManager()
        games = GameManager()
        cache = HttpCacheManager()
        lang = LangManager()
        editorManager = SchematicEditorManager()
        mvc = server.pluginManager.getPlugin("Multiverse-Core") as MultiverseCore
        chunky = server.servicesManager.load(ChunkyAPI::class.java)!!

        SSMBPlaceholderExpansion().register()

        SSMBMap.clearCurrentWorlds()
        hub = HubMap("hub-1")
        hub.createWorld()

        server.pluginManager.registerEvents(PlayerTeleportListener(), this)
        server.pluginManager.registerSuspendingEvents(PlayerJoinListener(), this)
        server.pluginManager.registerEvents(EntityPickupItemListener(), this)
        server.pluginManager.registerEvents(PlayerPickItemListener(), this)
        server.pluginManager.registerEvents(PlayerDropItemListener(), this)
        server.pluginManager.registerEvents(InventoryClickListener(), this)
        server.pluginManager.registerEvents(EntityDamageByBlockListener(), this)
        server.pluginManager.registerEvents(PlayerQuitListener(), this)

        commands = LiteCommandsBukkit
            .builder("ssmb", this)
            .commands(
                QueueCommand(),
                SchematicCommand()
            )
            .build()

        lang.revalidateLangCache()

        logger.info("initialized")
    }

    // TODO: Create a much better shutdown sequence
    override suspend fun onDisableAsync() {
        hub.teleportAllToDefaultWorld()
        api.clearQueue()
        api.destroyClient()
    }
}