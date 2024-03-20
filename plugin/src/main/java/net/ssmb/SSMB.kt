package net.ssmb

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.github.shynixn.mccoroutine.bukkit.setSuspendingExecutor
import net.ssmb.commands.QueueCommand
import net.ssmb.listeners.PlayerJoinListener
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
        minigames = MinigameService()
        worlds = WorldService()
        lang = LangService()
        lang.initLangService()

        hub = worlds.createSsmbWorld("blue_forest", "hub_1")

        getCommand("queue")!!.setSuspendingExecutor(QueueCommand(this))

        server.pluginManager.registerSuspendingEvents(PlayerJoinListener(), this)

        logger.info("STARTED SSMB")
    }

    override suspend fun onDisableAsync() {
        worlds.deleteAllLoadedWorlds()
    }
}
