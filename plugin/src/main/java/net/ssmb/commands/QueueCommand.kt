package net.ssmb.commands

import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
import net.kyori.adventure.text.Component
import net.ssmb.SSMB
import net.ssmb.dtos.queue.AddPlayerResponse
import net.ssmb.dtos.queue.AddPlayerSuccess
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class QueueCommand : SuspendingCommandExecutor {
    private val plugin = SSMB.instance

    override suspend fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) {
            return false
        }

        if (args.isNotEmpty()) {
            val minigameId = args[0]
            val shouldForce = args[1] == "-f"

            val response = plugin.api.queueAddPlayer(sender, minigameId, shouldForce)

            if (response is AddPlayerResponse.Error) {
                //
            } else if (response is AddPlayerResponse.Success) {
                sender.sendMessage(Component.text("Added to the queue for"))
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

        return false
    }
}
