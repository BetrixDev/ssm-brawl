package dev.betrix.superSmashMobsBrawl.commands

import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dev.betrix.superSmashMobsBrawl.services.LangService
import dev.betrix.superSmashMobsBrawl.services.MinigameLeaveError
import dev.betrix.superSmashMobsBrawl.services.MinigameService
import dev.betrix.superSmashMobsBrawl.services.QueueService
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
    private val minigameService: MinigameService by inject()

    @Execute
    fun leave(@Context sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage(lang.t("messages.commands.onlyPlayers"))
            return
        }

        // First try to leave queue
        QueueService.removePlayer(sender).onSuccess { queueEntry ->
            sender.sendMessage(
                lang.t("messages.queue.leave.success") { "minigameId" to queueEntry.minigame.id }
            )
            return
        }

        val currentMinigame = minigameService.getMinigameForPlayer(sender)
        minigameService
            .leaveMinigame(sender)
            .onSuccess {
                sender.sendMessage(
                    lang.t("messages.minigames.leave.success") { 
                        "minigameId" to (currentMinigame?.minigameDef?.id ?: "unknown")
                    }
                )
                return
            }
            .onFailure { err ->
                when (err) {
                    MinigameLeaveError.NotAllowedToLeave -> {
                        sender.sendMessage(lang.t("messages.minigames.leave.notAllowed"))
                    }

                    MinigameLeaveError.PlayerNotInMinigame -> {
                        sender.sendMessage(lang.t("messages.minigames.leave.notInGame"))
                    }

                    else -> {
                        sender.sendMessage(lang.t("messages.minigames.leave.unknown"))
                    }
                }
            }
    }
}
