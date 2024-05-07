package net.ssmb.commands

import br.com.devsrsouza.kotlinbukkitapi.extensions.item
import br.com.devsrsouza.kotlinbukkitapi.extensions.meta
import br.com.devsrsouza.kotlinbukkitapi.menu.dsl.menu
import br.com.devsrsouza.kotlinbukkitapi.menu.dsl.slot
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
import net.ssmb.utils.t
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.ItemMeta

@Command(name = "queue")
class QueueCommand {
    private val plugin = SSMB.instance

    @Execute
    fun execute(@Context sender: Player) {
        plugin.launch {
            val playableGames = plugin.api.minigameGetPlayableGames()

            val queueView =
                menu("Select a Minigame", 4, plugin) {
                    playableGames.forEachIndexed { idx, entry ->
                        slot(
                            idx,
                            item(Material.DIAMOND_BLOCK).meta<ItemMeta> {
                                displayName(Component.text(entry.displayName))
                                lore(mutableListOf(Component.text("${entry.playersInQueue} players in queue")))
                            }
                        ) {
                            onClick { sender.performCommand("queue ${entry.id}") }
                        }
                    }
                }

            sender.openInventory(queueView.inventory)
        }
    }

    @Execute(name = "leave")
    fun executeLeave(@Context sender: Player) {
        plugin.launch {
            when (plugin.api.queueRemovePlayers(listOf(sender.uniqueId.toString()))) {
                200 -> sender.sendMessage(t("queue.left"))
                else -> sender.sendMessage(t("unknownError"))
            }
        }
    }

    @Execute
    fun execute(@Context sender: Player, @Arg minigameId: String) {
        plugin.launch {
            val response = plugin.api.queueAddPlayer(sender, minigameId, false)

            if (response is AddPlayerResponse.Error) {
                when (response.value) {
                    AddPlayerError.ALREADY_IN_QUEUE ->
                        sender.sendMessage(Component.text("already in queue"))
                    AddPlayerError.UNKNOWN -> sender.sendMessage(Component.text("unknown error"))
                }
            } else if (response is AddPlayerResponse.Success) {
                sender.sendMessage(t("queue.added"))

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
