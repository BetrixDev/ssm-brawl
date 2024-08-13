package net.ssmb.commands

import br.com.devsrsouza.kotlinbukkitapi.command.arguments.string
import br.com.devsrsouza.kotlinbukkitapi.command.command
import com.destroystokyo.paper.utils.PaperPluginLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import net.ssmb.SSMB
import net.ssmb.blockwork.annotations.Service
import net.ssmb.blockwork.interfaces.OnStart
import net.ssmb.services.AlreadyInQueueException
import net.ssmb.services.MinigameNotFoundException
import net.ssmb.services.QueueService

@Service
class QueueCommand(private val plugin: SSMB, private val queue: QueueService) : OnStart {
    override fun onStart() {
        plugin.logger.info("Registering QueueCommand")

        plugin.command("queue") {
            aliases = listOf("q")
            permission = "ssmb.commands.queue"
            tabComplete { listOf("casual", "ranked", "leave") }

            executorPlayer {
                val minigame = string(0)

                try {
                    queue.addPlayerToQueue(sender, minigame)
                    sender.sendMessage(Component.text("You have been added to the queue for $minigame"))
                } catch (e: Exception) {
                    when (e) {
                        is AlreadyInQueueException -> sender.sendMessage(Component.text("You are already in a queue for ${e.minigame}, please leave that queue before entering a new one"))
                        is MinigameNotFoundException -> sender.sendMessage(Component.text("No minigame found with id ${e.minigameInput}"))
                        else -> e.printStackTrace()
                    }
                }

            }

            command("leave") {
                executorPlayer {
                    queue.removePlayerFromQueue(sender)
                    sender.sendMessage(Component.text("You have been removed from the queues!"))
                }
            }
        }
    }
}
