package dev.betrix.superSmashMobsBrawl.commands

import com.github.michaelbull.result.onSuccess
import dev.betrix.superSmashMobsBrawl.services.LangService
import dev.betrix.superSmashMobsBrawl.services.QueueService
import dev.betrix.superSmashMobsBrawl.utils.ONLY_PLAYERS_EXEC_MESSAGE
import dev.betrix.superSmashMobsBrawl.utils.mm
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Command(name = "leave")
class LeaveCommand : KoinComponent {
    private val lang: LangService by inject()

    @Execute
    fun leave(@Context sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage(ONLY_PLAYERS_EXEC_MESSAGE)
            return
        }

        // First try to leave queue
        QueueService.removePlayer(sender).onSuccess { queueEntry ->
            sender.sendMessage(
                mm("<green>You have left the queue for TODO</green>")
                //                        mm("<green>You have left the queue for
                // ${queueEntry.minigame.name}</green>")
            )
            return
        }

        //        // If not in queue, try to leave minigame
        //        MinigameService.handlePlayerLeave(sender)
        //            .onSuccess {
        //                sender.sendMessage(mm("<green>You have left the minigame</green>"))
        //                return
        //            }
        //            .onFailure { err ->
        //                when (err) {
        //                    MinigameLeaveError.PlayerNotInMinigame -> {
        //                        sender.sendMessage(mm("<red>You are not in anything you can
        // leave</red>"))
        //                    }
        //                    MinigameLeaveError.HubNotReady -> {
        //                        sender.sendMessage(
        //                            mm(
        //                                "<red>Hub world is still loading, please try again in a
        // moment</red>"
        //                            )
        //                        )
        //                    }
        //                    MinigameLeaveError.Unknown -> {
        //                        sender.sendMessage(
        //                            mm(
        //                                "<red>An unknown error occurred which caused you to be
        // unable to leave this minigame</red>"
        //                            )
        //                        )
        //                    }
        //                    else -> {
        //                        sender.sendMessage(mm("<red>You cannot leave this
        // minigame</red>"))
        //                    }
        //                }
        //            }
    }
}
