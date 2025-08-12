package dev.betrix.superSmashMobsBrawl.commands

import com.github.michaelbull.result.mapBoth
import dev.betrix.superSmashMobsBrawl.extensions.hasDebugEnabled
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.betrix.superSmashMobsBrawl.services.LangService
import dev.betrix.superSmashMobsBrawl.services.MinigameService
import dev.betrix.superSmashMobsBrawl.services.QueueService
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
class QueueCommand : KoinComponent {

    private val lang: LangService by inject()
    private val minigameService: MinigameService by inject()

    @Execute
    fun queue(@Context sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage(lang.t("messages.commands.onPlayers"))
            return
        }

        val queueEntry = QueueService.getQueueEntry(sender)

        val playerMessage =
            when (queueEntry) {
                null -> lang.t("messages.queue.status.notInQueue")
                else ->
                    lang.t("messages.queue.status.inQueue") {
                        "minigameId" to queueEntry.minigame.id
                    }
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
            sender.sendMessage(lang.t("messages.commands.onPlayers"))
            return
        }

        val playerMessage =
            QueueService.addPlayer(sender, minigame)
                .mapBoth(
                    success = {
                        lang.t("messages.queue.join.success") { "minigameId" to minigame.id }
                    },
                    failure = { lang.t("messages.queue.join.alreadyInQueue") },
                )

        sender.sendMessage(playerMessage)
    }

    @Execute(name = "leave")
    fun queueLeave(@Context sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage(lang.t("messages.commands.onPlayers"))
            return
        }

        QueueService.removePlayer(sender)
            .mapBoth(
                success = {
                    lang.t("messages.queue.leave.success") { "minigameId" to it.minigame.id }
                },
                failure = {
                    // If not in queue, fall back to leaving a running minigame
                    minigameService
                        .handlePlayerLeave(sender)
                        .mapBoth(
                            success = {
                                lang.t("messages.minigames.leave.success") {
                                    "minigameId" to it.minigameId
                                }
                            },
                            failure = { lang.t("messages.queue.leave.notInQueue") },
                        )
                },
            )
            .let { sender.sendMessage(it) }
    }
}
