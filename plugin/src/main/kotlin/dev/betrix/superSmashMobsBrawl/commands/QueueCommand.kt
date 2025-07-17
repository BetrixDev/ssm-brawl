package dev.betrix.superSmashMobsBrawl.commands

import com.github.michaelbull.result.mapBoth
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.utils.ONLY_PLAYERS_EXEC_MESSAGE
import dev.betrix.superSmashMobsBrawl.utils.mm
import dev.rollczi.litecommands.annotations.argument.Arg
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Command(name = "queue")
class QueueCommand(private val plugin: SuperSmashMobsBrawl) {

    @Execute
    fun queue(@Context sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage(ONLY_PLAYERS_EXEC_MESSAGE)
            return
        }

        val queueEntry = plugin.queueService.getQueueEntry(sender)

        val playerMessage = when (queueEntry) {
            null -> "<gray>You are not currently in any queue.</gray>"
            else -> "<gold>You are currently in the queue for ${queueEntry.minigame.name}!</gold>"
        }

        sender.sendMessage(playerMessage)
    }

    @Execute
    fun queue(@Context sender: CommandSender, @Arg minigame: MinigameDefinition) {
        if (sender !is Player) {
            sender.sendMessage(ONLY_PLAYERS_EXEC_MESSAGE)
            return
        }

        val playerMessage = plugin.queueService.addPlayer(sender, minigame)
            .mapBoth(
                success = { mm("<gold>You have joined the queue for ${it.minigame.name}!</gold>") },
                failure = { mm("<red>You are currently in a queue for ${it.minigame.name}.<newline>Please leave that queue before joining a new one</red>") }
            )

        sender.sendMessage(playerMessage)
    }

    @Execute(name = "leave")
    fun queueLeave(@Context sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage(ONLY_PLAYERS_EXEC_MESSAGE)
            return
        }

        val playerMessage = plugin.queueService.removePlayer(sender)
            .mapBoth(
                success = { mm("<gold>You have been removed from the queue for ${it.minigame.name}</gold>")},
                failure = { mm("<red>You are not currently in a queue</red>")}
            )

        sender.sendMessage(playerMessage)
    }
}
