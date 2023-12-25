package dev.betrix.supersmashmobsbrawl.commands

import com.github.shynixn.mccoroutine.bukkit.launch
import dev.betrix.supersmashmobsbrawl.SuperSmashMobsBrawl
import dev.betrix.supersmashmobsbrawl.views.QueueSelectionView
import dev.rollczi.litecommands.annotations.argument.Arg
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import org.bukkit.entity.Player
import java.util.*

@Command(name = "queue")
class QueueCommand {
    private val plugin = SuperSmashMobsBrawl.instance

    @Execute(name = "ranked")
    fun queueRanked(@Context sender: Player) {
        sender.sendMessage("You have queue for ranked")
    }

    @Execute(name = "leave")
    fun queueLeave(@Context sender: Player) {
        plugin.launch {
            plugin.queue.tryRemovePlayerFromQueue(sender)
        }
    }

    @Execute
    fun queue(@Context sender: Player, @Arg modeId: Optional<String>) {
        if (modeId.isEmpty) {
            QueueSelectionView(sender).showMenuToPlayer()
        }

        plugin.launch {
            plugin.queue.tryAddPlayerToQueue(sender, modeId.get(), false)
        }
    }
}