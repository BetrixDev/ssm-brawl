package net.ssmb.commands

import com.github.shynixn.mccoroutine.bukkit.launch
import dev.rollczi.litecommands.argument.Arg
import dev.rollczi.litecommands.command.route.Route
import net.ssmb.SSMB
import net.ssmb.dtos.queue.AddPlayerResponse
import net.ssmb.dtos.queue.AddPlayerSuccess
import org.bukkit.entity.Player
import org.jetbrains.annotations.Async.Execute

@Route(name = "queue")
class QueueCommand {
    private val plugin = SSMB.instance

    @Execute
    fun execute(sender: Player, @Arg minigameId: String) {
        plugin.launch {
            val response = plugin.api.queueAddPlayer(sender, minigameId, false)

            if (response is AddPlayerResponse.Error) {
                val errorMessage = plugin.lang.getComponent("queue.error")

                sender.sendMessage(errorMessage)
            } else if (response is AddPlayerResponse.Success) {
                val addedMessage = plugin.lang.getComponent("queue.added")

                sender.sendMessage(addedMessage)

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
