package net.ssmb

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.github.shynixn.mccoroutine.bukkit.setSuspendingExecutor
import net.ssmb.commands.QueueCommand
import net.ssmb.listeners.*
import net.ssmb.services.ApiService
import net.ssmb.services.LangService
import net.ssmb.services.MinigameService
import net.ssmb.services.WorldService
import org.bukkit.World

class SSMB : SuspendingJavaPlugin() {
    lateinit var api: ApiService
    lateinit var lang: LangService
    lateinit var minigames: MinigameService
    lateinit var worlds: WorldService
    lateinit var hub: World

    companion object {
        lateinit var instance: SSMB
    }

    override suspend fun onEnableAsync() {
        instance = this

        api = ApiService()
        api.initialize()

        minigames = MinigameService()
        worlds = WorldService()

        lang = LangService()
        lang.initLangService()

        hub = worlds.createSsmbWorld("blue_forest", "hub_1")

        getCommand("queue")!!.setSuspendingExecutor(QueueCommand())

        server.pluginManager.registerSuspendingEvents(PlayerJoinListener(), this)
        server.pluginManager.registerEvents(InventoryOpenListener(), this)
        server.pluginManager.registerEvents(PlayerPickItemListener(), this)
        server.pluginManager.registerEvents(PlayerDropItemListener(), this)
        server.pluginManager.registerEvents(EntityDamageByBlockListener(), this)
        server.pluginManager.registerEvents(PlayerInteractListener(), this)

        logger.info("STARTED SSMB")
    }

    override suspend fun onDisableAsync() {
        worlds.deleteAllLoadedWorlds()
    }
}
