package dev.betrix.superSmashMobsBrawl.commands

import dev.betrix.superSmashMobsBrawl.services.DebugService
import dev.betrix.superSmashMobsBrawl.utils.mm
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.description.Description
import dev.rollczi.litecommands.annotations.execute.Execute
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Command(name = "debug")
class DebugCommand {

    @Execute
    @Description("Toggle debug mode on/off")
    fun execute(@Context sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage(mm("<red>Only players can use this command!</red>"))
            return
        }

        // Check permission
//        if (!sender.hasPermission("ssmb.debug")) {
//            sender.sendMessage(mm("<red>You don't have permission to use this command!</red>"))
//            return
//        }

        val enabled = DebugService.toggleDebug(sender)
        if (enabled) {
            sender.sendMessage(mm("<green>Debug mode enabled!</green>"))
        } else {
            sender.sendMessage(mm("<red>Debug mode disabled!</red>"))
        }
    }
}
