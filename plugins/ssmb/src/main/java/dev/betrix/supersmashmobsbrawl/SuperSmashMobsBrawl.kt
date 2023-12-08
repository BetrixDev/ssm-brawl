package dev.betrix.supersmashmobsbrawl

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.setSuspendingExecutor
import dev.betrix.supersmashmobsbrawl.commands.QueueCommand
import dev.betrix.supersmashmobsbrawl.listeners.PlayerJoinListener
import dev.betrix.supersmashmobsbrawl.listeners.TeleportListener
import dev.betrix.supersmashmobsbrawl.managers.ApiManager
import dev.betrix.supersmashmobsbrawl.managers.GameManager
import dev.betrix.supersmashmobsbrawl.managers.QueueManager
import dev.betrix.supersmashmobsbrawl.maps.BaseMap
import dev.betrix.supersmashmobsbrawl.maps.HubMap

class SuperSmashMobsBrawl : SuspendingJavaPlugin() {
    lateinit var api: ApiManager
    lateinit var queue: QueueManager
    lateinit var games: GameManager
    lateinit var hub: HubMap

    companion object {
        lateinit var instance: SuperSmashMobsBrawl
    }

    override suspend fun onEnableAsync() {
        logger.info("Initializing Super Smash Mobs Brawl!")

        instance = this

        // These are initialized here and not inline cuz
        api = ApiManager()
        queue = QueueManager()
        games = GameManager()

        BaseMap.clearCurrentWorlds()
        hub = HubMap("hub", "main_hub")

        server.pluginManager.registerEvents(TeleportListener(), this)
        server.pluginManager.registerEvents(PlayerJoinListener(), this)

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