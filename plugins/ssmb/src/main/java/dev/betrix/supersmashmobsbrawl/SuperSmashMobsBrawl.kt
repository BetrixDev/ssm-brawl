package dev.betrix.supersmashmobsbrawl

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.setSuspendingExecutor
import dev.betrix.supersmashmobsbrawl.commands.QueueCommand
import dev.betrix.supersmashmobsbrawl.listeners.*
import dev.betrix.supersmashmobsbrawl.managers.ApiManager
import dev.betrix.supersmashmobsbrawl.managers.GameManager
import dev.betrix.supersmashmobsbrawl.managers.HttpCacheManager
import dev.betrix.supersmashmobsbrawl.managers.QueueManager
import dev.betrix.supersmashmobsbrawl.maps.BaseMap
import dev.betrix.supersmashmobsbrawl.maps.HubMap

class SuperSmashMobsBrawl : SuspendingJavaPlugin() {
    lateinit var api: ApiManager
    lateinit var queue: QueueManager
    lateinit var games: GameManager
    lateinit var cache: HttpCacheManager
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

        BaseMap.clearCurrentWorlds()
        hub = HubMap("hub", "main_hub")

        server.pluginManager.registerEvents(TeleportListener(), this)
        server.pluginManager.registerEvents(PlayerJoinListener(), this)
        server.pluginManager.registerEvents(PlayerInteractListener(), this)
        server.pluginManager.registerEvents(PotionSplashListener(), this)
        server.pluginManager.registerEvents(EntityPickupItemListener(), this)
        server.pluginManager.registerEvents(PlayerPickItemListener(), this)
        server.pluginManager.registerEvents(PlayerDropItemListener(), this)
        server.pluginManager.registerEvents(InventoryClickListener(), this)
        server.pluginManager.registerEvents(PlayerToggleFlightListener(), this)
        server.pluginManager.registerEvents(PlayerToggleSneakListener(), this)
        server.pluginManager.registerEvents(EntityDamageByBlockListener(), this)

        getCommand("queue")?.setSuspendingExecutor(QueueCommand(this))

        logger.info("initialized")
    }

    // TODO: Create a much better shutdown sequence
    override suspend fun onDisableAsync() {
        hub.teleportAllToDefaultWorld()
        api.clearQueue()
        api.destroyClient()
    }
}