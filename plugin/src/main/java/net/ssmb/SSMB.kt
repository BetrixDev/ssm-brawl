package net.ssmb

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.setSuspendingExecutor
import net.ssmb.commands.QueueCommand
import net.ssmb.services.ApiService
import net.ssmb.services.LangService
import net.ssmb.services.MinigameService
import net.ssmb.services.WorldService

class SSMB : SuspendingJavaPlugin() {
    lateinit var api: ApiService
    lateinit var lang: LangService
    lateinit var minigames: MinigameService
    lateinit var worlds: WorldService

    companion object {
        lateinit var instance: SSMB
    }

    override suspend fun onEnableAsync() {
        instance = this

        api = ApiService()
        lang = LangService(this)
        minigames = MinigameService(this)
        worlds = WorldService(this)

        getCommand("queue")!!.setSuspendingExecutor(QueueCommand(this))
    }

    override suspend fun onDisableAsync() {
        // Plugin shutdown logic
    }
}
