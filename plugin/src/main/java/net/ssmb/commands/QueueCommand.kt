package net.ssmb.commands

import br.com.devsrsouza.kotlinbukkitapi.command.arguments.string
import br.com.devsrsouza.kotlinbukkitapi.command.command
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.Component
import net.ssmb.SSMB
import net.ssmb.blockwork.annotations.Service
import net.ssmb.blockwork.interfaces.OnStart
import net.ssmb.services.QueueService

@Service
class QueueCommand(private val plugin: SSMB, private val queue: QueueService) : OnStart {
    override fun onStart() {
        plugin.logger.info("Registering QueueCommand")

        plugin.command("queue") {
            aliases = listOf("q")
            permission = "ssmb.commands.queue"
            tabComplete { listOf(*queue.defaultMinigameQueues, "leave") }

            executorPlayer {
                val minigame = string(0)

                withContext(Dispatchers.IO) {
                    sender.sendMessage(
                        Component.text("You have been added to the queue for $minigame")
                    )
                }
            }

            command("leave") {
                executor {
                    withContext(Dispatchers.IO) {
                        sender.sendMessage(Component.text("You have been removed from the queue."))
                    }
                }
            }
        }
    }
}
