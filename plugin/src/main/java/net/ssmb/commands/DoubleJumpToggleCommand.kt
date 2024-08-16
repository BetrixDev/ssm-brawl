package net.ssmb.commands

import br.com.devsrsouza.kotlinbukkitapi.command.command
import net.kyori.adventure.text.Component
import net.ssmb.SSMB
import net.ssmb.blockwork.addTag
import net.ssmb.blockwork.annotations.Service
import net.ssmb.blockwork.hasTag
import net.ssmb.blockwork.interfaces.OnStart
import net.ssmb.blockwork.removeTag

@Service
class DoubleJumpToggleCommand(private val plugin: SSMB) : OnStart {
    override fun onStart() {
        plugin.command("doublejump") {
            aliases = listOf("dj")

            executorPlayer {
                if (!sender.world.name.contains("hub")) {
                    sender.sendMessage(Component.text("You can only use this command in the hub world!"))
                    return@executorPlayer
                }

                val isDoubleJumpEnabled = sender.hasTag("passive_double_jump")

                if (isDoubleJumpEnabled) {
                    sender.removeTag("passive_double_jump")
                    sender.sendMessage(Component.text("Double jump disabled!"))
                } else {
                    sender.addTag("passive_double_jump")
                    sender.sendMessage(Component.text("Double jump enabled!"))
                }
            }
        }
    }
}