package net.ssmb.commands

import com.github.shynixn.mccoroutine.bukkit.launch
import dev.rollczi.litecommands.annotations.argument.Arg
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import net.kyori.adventure.text.Component
import net.ssmb.SSMB
import net.ssmb.dtos.queue.AddPlayerError
import net.ssmb.dtos.queue.AddPlayerResponse
import net.ssmb.dtos.queue.AddPlayerSuccess
import org.bukkit.entity.Player

@Command(name = "queue")
class QueueCommand {
    private val plugin = SSMB.instance

    @Execute(name = "leave")
    fun execute(@Context sender: Player) {
        plugin.launch {
            when (plugin.api.queueRemovePlayers(listOf(sender.uniqueId.toString()))) {
                200 -> sender.sendMessage(Component.text("left queue"))
                else -> sender.sendMessage(Component.text("unknown error"))
            }
        }
    }

    @Execute
    fun execute(@Context sender: Player, @Arg minigameId: String) {
        plugin.launch {
            val response = plugin.api.queueAddPlayer(sender, minigameId, false)

            if (response is AddPlayerResponse.Error) {
                when (response.value) {
                    AddPlayerError.ALREADY_IN_QUEUE -> sender.sendMessage(Component.text("already in queue"))
                    AddPlayerError.UNKNOWN -> sender.sendMessage(Component.text("unknown error"))
                }
            } else if (response is AddPlayerResponse.Success) {
//                val addedMessage = plugin.lang.getComponent("queue.added")

                sender.sendMessage(Component.text("added bitch"))

                if (response.value is AddPlayerSuccess.Added) {
                    //
                } else if (response.value is AddPlayerSuccess.StartGame) {
                    plugin.minigames.tryStartMinigame(
                        response.value.playerUuids,
                        response.value.minigameId
                    )
                }
            }
        }
    }
}
