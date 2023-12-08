package dev.betrix.supersmashmobsbrawl.commands

import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.betrix.supersmashmobsbrawl.views.QueueSelectionView
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class QueueCommand constructor(private val plugin: SuperSmashMobsBrawl) : SuspendingCommandExecutor, TabCompleter {
    override suspend fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) {
            return false
        }

        if (args.isEmpty()) {
            QueueSelectionView.showMenuToPlayer(sender)
            return true
        }

        val modeId = args[0]
        var isRanked = false

        if (args.size >= 2) {
            isRanked = args[1].lowercase() == "ranked"
        }

        plugin.queue.tryAddPlayerToQueue(sender, modeId, isRanked)

//        val tryQueueResult = plugin.api.addPlayerToQueue(sender, modeId, isRanked)
//
//        if (tryQueueResult is JoinQueue.Success) {
//            sender.sendMessage(MiniMessage.miniMessage().deserialize("You have been added to the queue!"))
//
//            return true
//        } else if (tryQueueResult is JoinQueue.Error) {
//            when (tryQueueResult.value) {
//                JoinQueueError.ALREADY_IN_QUEUE -> {
//                    sender.sendMessage(
//                        MiniMessage.miniMessage()
//                            .deserialize("You are already in a queue! Please leave before attempting to join a new queue")
//                    )
//                }
//
//                else -> {
//                    sender.sendMessage(
//                        MiniMessage.miniMessage()
//                            .deserialize("There was an unknown error when adding you to the queue. Please try again")
//                    )
//                }
//            }
//
//            return true
//        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>?
    ): MutableList<String>? {
        TODO("Not yet implemented")
    }
}