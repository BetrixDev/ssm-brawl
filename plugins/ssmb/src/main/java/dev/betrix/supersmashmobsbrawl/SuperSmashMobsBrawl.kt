package dev.betrix.supersmashmobsbrawl

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.github.shynixn.mccoroutine.bukkit.setSuspendingExecutor
import dev.betrix.supersmashmobsbrawl.commands.QueueCommand
import dev.betrix.supersmashmobsbrawl.listeners.*
import dev.betrix.supersmashmobsbrawl.managers.*
import dev.betrix.supersmashmobsbrawl.maps.BaseMap
import dev.betrix.supersmashmobsbrawl.maps.HubMap

class SuperSmashMobsBrawl : SuspendingJavaPlugin() {
    lateinit var api: ApiManager
    lateinit var queue: QueueManager
    lateinit var games: GameManager
    lateinit var cache: HttpCacheManager
    lateinit var lang: LangManager
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

        SSMBPlaceholderExpansion().register()

        BaseMap.clearCurrentWorlds()
        hub = HubMap("hub", "main_hub")

        server.pluginManager.registerEvents(PlayerTeleportListener(), this)
        server.pluginManager.registerSuspendingEvents(PlayerJoinListener(), this)
        server.pluginManager.registerEvents(EntityPickupItemListener(), this)
        server.pluginManager.registerEvents(PlayerPickItemListener(), this)
        server.pluginManager.registerEvents(PlayerDropItemListener(), this)
        server.pluginManager.registerEvents(InventoryClickListener(), this)
        server.pluginManager.registerEvents(EntityDamageByBlockListener(), this)

        getCommand("queue")?.setSuspendingExecutor(QueueCommand(this))

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