package net.ssmb

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import dev.rollczi.litecommands.LiteCommands
import dev.rollczi.litecommands.bukkit.LiteCommandsBukkit
import net.ssmb.commands.QueueCommand
import net.ssmb.listeners.*
import net.ssmb.services.ApiService
import net.ssmb.services.LangService
import net.ssmb.services.MinigameService
import net.ssmb.services.WorldService
import org.bukkit.World
import org.bukkit.command.CommandSender

class SSMB : SuspendingJavaPlugin() {
    lateinit var api: ApiService
    lateinit var lang: LangService
    lateinit var minigames: MinigameService
    lateinit var worlds: WorldService
    lateinit var hub: World

    private lateinit var liteCommands: LiteCommands<CommandSender>

    companion object {
        lateinit var instance: SSMB
    }

    override suspend fun onEnableAsync() {
        instance = this

        api = ApiService()
        api.initialize()
        api.queueFlush()

        minigames = MinigameService()
        worlds = WorldService()

        lang = LangService()
        lang.initLangService()

        hub = worlds.createSsmbWorld("blue_forest", "hub_1")

        server.pluginManager.registerEvents(PlayerJoinListener(), this)
        server.pluginManager.registerEvents(InventoryOpenListener(), this)
        server.pluginManager.registerEvents(PlayerPickItemListener(), this)
        server.pluginManager.registerEvents(PlayerDropItemListener(), this)
        server.pluginManager.registerEvents(EntityDamageByBlockListener(), this)
        server.pluginManager.registerEvents(PlayerInteractListener(), this)

        liteCommands = LiteCommandsBukkit.builder("ssmb", this).commands(QueueCommand()).build()

        logger.info("STARTED SSMB")
    }

    override suspend fun onDisableAsync() {
        api.queueFlush()
        worlds.deleteAllLoadedWorlds()
    }
}
