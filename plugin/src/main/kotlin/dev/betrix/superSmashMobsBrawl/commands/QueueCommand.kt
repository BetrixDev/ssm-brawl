package dev.betrix.superSmashMobsBrawl.commands

import com.github.michaelbull.result.mapBoth
import dev.betrix.superSmashMobsBrawl.extensions.hasDebugEnabled
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.services.LangService
import dev.betrix.superSmashMobsBrawl.services.QueueService
import dev.betrix.superSmashMobsBrawl.utils.ONLY_PLAYERS_EXEC_MESSAGE
import dev.betrix.superSmashMobsBrawl.utils.mm
import dev.rollczi.litecommands.annotations.argument.Arg
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Command(name = "queue")
class QueueCommand() : KoinComponent {

    private val lang: LangService by inject()

    @Execute
    fun queue(@Context sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage(ONLY_PLAYERS_EXEC_MESSAGE)
            return
        }

        val queueEntry = QueueService.getQueueEntry(sender)

        val playerMessage =
            when (queueEntry) {
                null -> "<gray>You are not currently in any queue.</gray>"
                else -> "<gold>You are currently in the queue for TODO!</gold>"
            //                "<gold>You are currently in the queue for
            // ${queueEntry.minigame.name}!</gold>"
            }

        sender.sendMessage(playerMessage)

        // Example debug usage - show additional information if debug is enabled
        if (sender.hasDebugEnabled()) {
            sender.sendMessage(
                mm("<gray>[DEBUG] Queue status checked at ${System.currentTimeMillis()}</gray>")
            )
        }
    }

    @Execute
    fun queue(@Context sender: CommandSender, @Arg minigame: MinigameDef) {
        if (sender !is Player) {
            sender.sendMessage(ONLY_PLAYERS_EXEC_MESSAGE)
            return
        }

        val playerMessage =
            QueueService.addPlayer(sender, minigame)
                .mapBoth(
                    success = {
                        lang.t("messages.queue.join.success") {
                            "minigameId" to minigame.id
                        }
                    },
                    failure = {
                        mm(
                            "<red>You are currently in a queue for TODO.<newline>Please leave that queue before joining a new one</red>"
                            //                            "<red>You are currently in a queue for
                            // ${it.minigame.name}.<newline>Please leave that queue before joining a
                            // new one</red>"
                        )
                    },
                )

        sender.sendMessage(playerMessage)
    }

    @Execute(name = "leave")
    fun queueLeave(@Context sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage(ONLY_PLAYERS_EXEC_MESSAGE)
            return
        }

        val playerMessage =
            QueueService.removePlayer(sender)
                .mapBoth(
                    success = {
                        mm(
                            "<gold>You have been removed from the queue for TODO</gold>"
                            //                            "<gold>You have been removed from the
                            // queue for ${it.minigame.name}</gold>"
                        )
                    },
                    failure = { mm("<red>You are not currently in a queue</red>") },
                )

        sender.sendMessage(playerMessage)
    }
}
