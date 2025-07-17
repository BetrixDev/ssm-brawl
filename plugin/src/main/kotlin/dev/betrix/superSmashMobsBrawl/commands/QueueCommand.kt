package dev.betrix.superSmashMobsBrawl.commands

import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.components.InQueueComponent
import dev.betrix.superSmashMobsBrawl.components.MinecraftPlayerComponent
import dev.betrix.superSmashMobsBrawl.minigames.definitions.MinigameDefinition
import dev.betrix.superSmashMobsBrawl.registries.MinigameRegistry
import dev.betrix.superSmashMobsBrawl.utils.ONLY_PLAYERS_EXEC_MESSAGE
import dev.betrix.superSmashMobsBrawl.utils.mm
import dev.rollczi.litecommands.annotations.argument.Arg
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Command(name = "queue")
class QueueCommand(private val plugin: SuperSmashMobsBrawl) {

    @Execute
    fun queue(@Context sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage(ONLY_PLAYERS_EXEC_MESSAGE)
            return
        }

        with(plugin.world) {
            val playerEntity = plugin.playerFamily.find { e -> e[MinecraftPlayerComponent].player == sender }

            if (playerEntity == null) {
                sender.kick(Component.text("this is odd"))
                return
            }

            if (playerEntity has InQueueComponent) {
                val playerQueueId = playerEntity[InQueueComponent].minigameId
                val minigameDefinition = MinigameRegistry.getDefinition(playerQueueId)
                
                if (minigameDefinition != null) {
                    sender.sendMessage(
                        mm("<gold>You are currently in the queue for ${minigameDefinition.name}!</gold>")
                    )
                } else {
                    // Handle case where queue ID doesn't match any known queue
                    sender.sendMessage(
                        mm("<red>You are in an unknown queue: $playerQueueId</red>")
                    )
                }
            } else {
                sender.sendMessage(
                    mm("<gray>You are not currently in any queue.</gray>")
                )
            }
        }
    }

    @Execute
    fun queue(@Context sender: CommandSender, @Arg minigame: MinigameDefinition) {
        if (sender !is Player) {
            sender.sendMessage(ONLY_PLAYERS_EXEC_MESSAGE)
            return
        }

        with(plugin.world) {
            val playerEntity = plugin.playerFamily.find { e -> e[MinecraftPlayerComponent].player == sender }

            if (playerEntity == null) {
                sender.kick(Component.text("this is odd"))
                return
            }

            if (playerEntity has InQueueComponent) {
                val playerQueueId = playerEntity[InQueueComponent].minigameId
                sender.sendMessage(
                    mm(
                        "<red>You are currently in a queue for $playerQueueId.<newline>Please leave that queue before joining a new one</red>"
                    )
                )
                return
            }

            playerEntity.configure { it += InQueueComponent(minigame.id) }

            sender.sendMessage(
                mm("<gold>You have joined the queue for ${minigame.name}!</gold>")
            )
        }
    }

    @Execute(name = "leave")
    fun queueLeave(@Context sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage(ONLY_PLAYERS_EXEC_MESSAGE)
            return
        }

        with(plugin.world) {
            val playerEntity = plugin.playerFamily.find { e -> e[MinecraftPlayerComponent].player == sender }

            if (playerEntity == null) {
                sender.kick(Component.text("this is odd"))
                return
            }

            if (!(playerEntity has InQueueComponent)) {
                sender.sendMessage(mm("<red>You are not currently in a queue</red>"))
                return
            }

            val playerQueueId = playerEntity[InQueueComponent].minigameId

            playerEntity.configure { it -= InQueueComponent }

            sender.sendMessage(
                mm("<gold>You have been removed from the queue for $playerQueueId</gold>")
            )
        }
    }
}
