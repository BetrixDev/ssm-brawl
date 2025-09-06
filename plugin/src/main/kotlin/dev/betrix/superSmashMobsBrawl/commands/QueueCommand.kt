package dev.betrix.superSmashMobsBrawl.commands

import com.github.michaelbull.result.mapBoth
import dev.betrix.superSmashMobsBrawl.SuperSmashMobsBrawl
import dev.betrix.superSmashMobsBrawl.components.LeaveSpecifier
import dev.betrix.superSmashMobsBrawl.components.PlayerGetQueueStatusComponent
import dev.betrix.superSmashMobsBrawl.components.PlayerTryLeaveComponent
import dev.betrix.superSmashMobsBrawl.components.PlayerTryQueueComponent
import dev.betrix.superSmashMobsBrawl.extensions.ecsEntity
import dev.betrix.superSmashMobsBrawl.models.brawlData.MinigameDef
import dev.rollczi.litecommands.annotations.argument.Arg
import dev.rollczi.litecommands.annotations.command.Command
import dev.rollczi.litecommands.annotations.context.Context
import dev.rollczi.litecommands.annotations.execute.Execute
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent

@Command(name = "queue")
class QueueCommand(private val plugin: SuperSmashMobsBrawl) : KoinComponent {

    @Execute
    fun queue(@Context sender: Player) {
        with(plugin.ecsWorld) {
            sender.ecsEntity?.configure {
                it += PlayerGetQueueStatusComponent
            }
        }
    }

    @Execute
    fun queue(@Context sender: Player, @Arg minigame: MinigameDef) {
        with(plugin.ecsWorld) {
            sender.ecsEntity?.configure {
                it += PlayerTryQueueComponent(minigame)
            }
        }
    }

    @Execute(name = "leave")
    fun queueLeave(@Context sender: Player) {
        with(plugin.ecsWorld) {
            sender.ecsEntity?.configure {
                it += PlayerTryLeaveComponent(LeaveSpecifier.QUEUE)
            }
        }
    }
}
