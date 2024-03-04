package net.ssmb

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.setSuspendingExecutor
import net.ssmb.commands.QueueCommand
import net.ssmb.services.ApiService
import net.ssmb.services.LangService
import net.ssmb.services.MinigameService

class SSMB : SuspendingJavaPlugin() {
    lateinit var api: ApiService
    lateinit var lang: LangService
    lateinit var minigames: MinigameService

    override suspend fun onEnableAsync() {
        api = ApiService()
        lang = LangService(this)
        minigames = MinigameService(this)

        getCommand("queue")!!.setSuspendingExecutor(QueueCommand(this))
    }

    override suspend fun onDisableAsync() {
        // Plugin shutdown logic
    }
}
