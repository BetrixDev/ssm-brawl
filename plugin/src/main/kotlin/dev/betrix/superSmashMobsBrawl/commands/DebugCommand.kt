package dev.betrix.superSmashMobsBrawl.commands

import dev.betrix.superSmashMobsBrawl.services.DebugService
import dev.betrix.superSmashMobsBrawl.utils.ONLY_PLAYERS_EXEC_MESSAGE
import dev.betrix.superSmashMobsBrawl.utils.mm
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Command(name = "debug")
class DebugCommand {

    @Execute
    fun debug(@Context sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage(ONLY_PLAYERS_EXEC_MESSAGE)
            return
        }

        // Check permission
        if (!sender.hasPermission("ssmb.debug")) {
            sender.sendMessage(mm("<red>You don't have permission to use this command!</red>"))
            return
        }

        // Toggle debug mode
        val debugEnabled = DebugService.toggleDebug(sender)

        val message =
            if (debugEnabled) {
                mm("<green>Debug mode enabled! You will now see debug information.</green>")
            } else {
                mm(
                    "<yellow>Debug mode disabled. Debug information will no longer be shown.</yellow>"
                )
            }

        sender.sendMessage(message)
    }
}
