package net.ssmb.commands

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import kotlinx.coroutines.delay
import net.ssmb.SSMB
import net.ssmb.utils.t
import org.bukkit.Bukkit
import org.bukkit.entity.Player

@Command(name = "shutdown")
class ShutdownCommand {
    private val plugin = SSMB.instance

    @Execute
    fun execute(@Context sender: Player) {
        if (!sender.isOp) {
            sender.sendMessage(t("noPermissionForAction"))
            return
        }

        plugin.launch {
            sender.sendMessage(t("shutdown.attempting"))

            val apiShutdownResponse = plugin.api.serverBeginShutdown()

            if (apiShutdownResponse == 403) {
                sender.sendMessage("shutdown.alreadyInProgress")
                return@launch
            }

            if (apiShutdownResponse != 200) {
                sender.sendMessage(t("shutdown.unknownError"))
                return@launch
            }

            plugin.logger.info("Attempting to perform server shutdown")

            plugin.server.sendMessage(t("shutdown.broadcastShutdownStarted"))

            if (plugin.minigames.areMinigamesOngoing()) {
                sender.sendMessage(t("broadcast.waitingForMinigames"))
            }

            while (plugin.minigames.areMinigamesOngoing()) {
                plugin.logger.info("There are still minigames ongoing, pausing server shutdown")
                delay(40.ticks)
            }

            plugin.logger.info("All minigames have finished, proceeding with server shutdown")
            sender.sendMessage(t("broadcast.performingShutdown"))

            Bukkit.getServer().shutdown()
        }
    }
}
