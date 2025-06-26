package net.ssmb.commands

import br.com.devsrsouza.kotlinbukkitapi.command.command
import net.kyori.adventure.text.Component
import net.ssmb.SSMB
import net.ssmb.blockwork.Blockwork
import net.ssmb.blockwork.annotations.Service
import net.ssmb.blockwork.interfaces.OnStart
import net.ssmb.blockwork.removeTag
import net.ssmb.components.worlds.HubWorldComponent

@Service
class HubCommand(private val plugin: SSMB) : OnStart {
    override fun onStart() {
        plugin.command("hub") {
            aliases = listOf("lobby")

            executorPlayer {
                val hubComponent = Blockwork.components.getAllWorldComponents<HubWorldComponent>().first()

                sender.removeTag("isInMinigame")
                
                hubComponent.teleportPlayer(sender)
                sender.sendMessage(Component.text("Teleported to the hub!"))
            }
        }
    }
}