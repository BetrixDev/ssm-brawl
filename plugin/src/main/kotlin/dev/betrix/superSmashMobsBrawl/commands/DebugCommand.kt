package dev.betrix.superSmashMobsBrawl.commands

import dev.betrix.superSmashMobsBrawl.services.DebugService
import dev.betrix.superSmashMobsBrawl.utils.ONLY_PLAYERS_EXEC_MESSAGE
import dev.betrix.superSmashMobsBrawl.utils.mm
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import dev.rollczi.litecommands.annotations.description.Description
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import dev.betrix.superSmashMobsBrawl.services.HubService
import dev.betrix.superSmashMobsBrawl.passives.definitions.DoubleJumpPassiveDefinition

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
        if (!sender.hasPermission("ssmb.debug")) {
            sender.sendMessage(mm("<red>You don't have permission to use this command!</red>"))
            return
        }

        val enabled = DebugService.toggleDebug(sender)
        if (enabled) {
            sender.sendMessage(mm("<green>Debug mode enabled!</green>"))
        } else {
            sender.sendMessage(mm("<red>Debug mode disabled!</red>"))
        }
    }

    @Execute
    @Description("Check flight status")
    fun flight(@Context sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage(mm("<red>Only players can use this command!</red>"))
            return
        }

        sender.sendMessage(mm("<yellow>Flight Status:</yellow>"))
        sender.sendMessage(mm("<gray>- allowFlight: ${sender.allowFlight}</gray>"))
        sender.sendMessage(mm("<gray>- isFlying: ${sender.isFlying}</gray>"))
        sender.sendMessage(mm("<gray>- flySpeed: ${sender.flySpeed}</gray>"))
        sender.sendMessage(mm("<gray>- gameMode: ${sender.gameMode}</gray>"))
        sender.sendMessage(mm("<gray>- isOnGround: ${sender.isOnGround}</gray>"))
        sender.sendMessage(mm("<gray>- world: ${sender.world.name}</gray>"))
        sender.sendMessage(mm("<gray>- isInHub: ${HubService.isPlayerInHub(sender)}</gray>"))
    }

    @Execute
    @Description("Manually enable flight")
    fun enableflight(@Context sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage(mm("<red>Only players can use this command!</red>"))
            return
        }

        sender.allowFlight = true
        sender.sendMessage(mm("<green>Flight enabled! allowFlight=${sender.allowFlight}</green>"))
    }

    @Execute
    @Description("Give double jump passive")
    fun doublejump(@Context sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage(mm("<red>Only players can use this command!</red>"))
            return
        }

        // Check permission
        if (!sender.hasPermission("ssmb.debug")) {
            sender.sendMessage(mm("<red>You don't have permission to use this command!</red>"))
            return
        }

        val doubleJumpPassive = DoubleJumpPassiveDefinition.createInstance(sender)
        doubleJumpPassive.setup()
        sender.sendMessage(mm("<green>Double jump passive given!</green>"))
    }
}
